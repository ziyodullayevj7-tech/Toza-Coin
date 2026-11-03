package teamwork.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestClient;
import teamwork.dto.ai.AiAnaliseResultDto;
import teamwork.entity.WasteReportEntity;
import teamwork.enums.AiReviewStatus;
import teamwork.enums.ReportActionEnum;
import teamwork.event.WasteReportCreatedEvent;
import teamwork.repository.WasteReportRepository;
import tools.jackson.databind.ObjectMapper;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AiCleanupAnaliseService {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    private final WasteReportRepository wasteReportRepository;
    private final AttachService attachService;
    private final AiReviewPersistenceService aiReviewPersistenceService;

    @Value("${gemini.api.key:}")
    private String apiKey;

    public AiCleanupAnaliseService(ObjectMapper objectMapper,
                                   WasteReportRepository wasteReportRepository,
                                   AttachService attachService,
                                   AiReviewPersistenceService aiReviewPersistenceService) {
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
        this.objectMapper = objectMapper;
        this.wasteReportRepository = wasteReportRepository;
        this.attachService = attachService;
        this.aiReviewPersistenceService = aiReviewPersistenceService;
    }

    @Async("aiReviewExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void analyzeReport(WasteReportCreatedEvent event) {
        String reportId = event.reportId();
        try {
            WasteReportEntity report = wasteReportRepository.findById(reportId)
                    .orElseThrow(() -> new IllegalStateException("Waste report not found"));
            if (report.getAiReviewStatus() == AiReviewStatus.COMPLETED) {
                return;
            }

            AttachService.ImageData before =
                    attachService.readImageByStoredPath(report.getBeforeImageUrl());
            AiAnaliseResultDto result;
            if (report.getReportAction() == ReportActionEnum.CLEAN_MYSELF) {
                AttachService.ImageData after =
                        attachService.readImageByStoredPath(report.getAfterImageUrl());
                result = aiAnaliseCleanup(before.bytes(), before.mimeType(),
                        after.bytes(), after.mimeType());
            } else if (report.getReportAction() == ReportActionEnum.SEND_TO_OTHERS) {
                result = aiAnaliseReport(before.bytes(), before.mimeType());
            } else {
                throw new IllegalStateException("Unsupported report action for AI review");
            }
            aiReviewPersistenceService.saveSuccessfulReview(reportId, result);
        } catch (RuntimeException e) {
            log.error("AI review failed for waste report {}", reportId, e);
            try {
                aiReviewPersistenceService.saveFailedReview(reportId);
            } catch (RuntimeException persistenceException) {
                log.error("Could not save AI review failure for waste report {}", reportId, persistenceException);
            }
        }
    }

    private AiAnaliseResultDto aiAnaliseCleanup(byte[] beforeBytes, String before,
                                                byte[] afterBytes, String after) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("Gemini API key is not configured");
        }
        try {
            String prompt = """
                You are an environmental verification auditor for the TozaCoin cleanup system.
                Compare Image 1 (before cleanup) and Image 2 (claimed after cleanup).

                Evaluate:
                1. isSameLocation: Are both images showing the exact same physical spot? Check static anchors (trees, wall marks, pavement cracks, buildings).
                2. wasteCleared: Was real waste present in Image 1, and has it been genuinely cleaned in Image 2?
                3. confidenceScore: An integer between 0 and 100 representing certainty of a legitimate cleanup.
                4. detectedAnchors: List of common static visual anchors found in both frames.
                5. reasoning: Concise summary explaining the verdict.

                Return JSON matching this schema:
                {
                  "isSameLocation": true|false,
                  "wasteCleared": true|false,
                  "confidenceScore": 0-100,
                  "detectedAnchors": ["anchor1", "anchor2"],
                  "reasoning": "..."
                }
                """;

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt),
                                    Map.of("inline_data", Map.of(
                                            "mime_type", before,
                                            "data", Base64.getEncoder().encodeToString(beforeBytes)
                                    )),
                                    Map.of("inline_data", Map.of(
                                            "mime_type", after,
                                            "data", Base64.getEncoder().encodeToString(afterBytes)
                                    ))
                            ))
                    ),
                    "generationConfig", Map.of(
                            "responseMimeType", "application/json",
                            "temperature", 0.1
                    )
            );

            Map<?, ?> rawResponse = restClient.post()
                    .uri("/v1beta/models/gemini-3.8-flash:generateContent")
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            String contentJson = extractGeneratedText(rawResponse);

            AiAnaliseResultDto result = objectMapper
                    .readValue(contentJson, AiAnaliseResultDto.class);
            result.validateCleanup();
            return result;
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to analyze cleanup result" + e.getMessage(), e);
        }
    }

    private AiAnaliseResultDto aiAnaliseReport(byte[] imageBytes, String mimeType) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("Gemini API key is not configured");
        }
        try {
            String prompt = """
                You are an environmental report verification auditor for the TozaCoin system.
                Review the single submitted photo and determine whether it clearly shows real,
                visible waste or an environmental cleanliness problem that should be reported.
                Do not infer a cleanup; this is a report-only review.

                Return JSON matching this schema:
                {
                  "wastePresent": true|false,
                  "confidenceScore": 0-100,
                  "reasoning": "concise explanation"
                }
                """;
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt),
                                    Map.of("inline_data", Map.of(
                                            "mime_type", mimeType,
                                            "data", Base64.getEncoder().encodeToString(imageBytes)
                                    ))
                            ))
                    ),
                    "generationConfig", Map.of(
                            "responseMimeType", "application/json",
                            "temperature", 0.1
                    )
            );
            Map<?, ?> rawResponse = requestGemini(requestBody);
            AiAnaliseResultDto result = objectMapper.readValue(
                    extractGeneratedText(rawResponse), AiAnaliseResultDto.class);
            result.validateReport();
            return result;
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to analyze waste report: " + e.getMessage(), e);
        }
    }

    private Map<?, ?> requestGemini(Map<String, Object> requestBody) {
        return restClient.post()
                .uri("/v1beta/models/gemini-3.8-flash:generateContent")
                .header("x-goog-api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(Map.class);
    }

    private String extractGeneratedText(Map<?, ?> response) {
        Object candidatesValue = response.get("candidates");
        if (!(candidatesValue instanceof List<?> candidates) || candidates.isEmpty()
                || !(candidates.get(0) instanceof Map<?, ?> candidate)
                || !(candidate.get("content") instanceof Map<?, ?> content)
                || !(content.get("parts") instanceof List<?> parts)) {
            throw new IllegalStateException("Gemini response did not contain generated content");
        }

        for (Object partValue : parts) {
            if (partValue instanceof Map<?, ?> part
                    && part.get("text") instanceof String text
                    && !text.isBlank()) {
                return text;
            }
        }
        throw new IllegalStateException("Gemini response did not contain generated text");
    }
}
