package teamwork.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import teamwork.dto.ai.AiAnaliseResultDto;
import teamwork.entity.WasteReportEntity;
import teamwork.enums.ReportActionEnum;
import teamwork.exceptions.AppBadRequestException;
import teamwork.repository.WasteReportRepository;
import tools.jackson.databind.ObjectMapper;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class AiCleanupAnaliseService {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    private final WasteReportRepository wasteReportRepository;
    private final AttachService attachService;

    @Value("${gemini.api.key:}")
    private String apiKey;

    public AiCleanupAnaliseService(ObjectMapper objectMapper,
                                   WasteReportRepository wasteReportRepository,
                                   AttachService attachService) {
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
        this.objectMapper = objectMapper;
        this.wasteReportRepository = wasteReportRepository;
        this.attachService = attachService;
    }

    public void analyzeReport(String reportId) {
        WasteReportEntity report = wasteReportRepository.findById(reportId)
                .orElseThrow(() ->
                        new AppBadRequestException("Waste report not found"));

        if (report.getReportAction() != ReportActionEnum.CLEAN_MYSELF) {
            throw new AppBadRequestException(
                    "Only self-cleanup reports can be analyzed");
        }

        AttachService.ImageData before =
                attachService.readImageByStoredPath(report.getBeforeImageUrl());
        AttachService.ImageData after =
                attachService.readImageByStoredPath(report.getAfterImageUrl());

        AiAnaliseResultDto result = aiAnalise(
                before.bytes(), before.mimeType(),
                after.bytes(), after.mimeType()
        );

        report.setConfidenceScore(result.getConfidenceScore());
        report.setAiReasoning(result.getReasoning());
        wasteReportRepository.save(report);
    }

    public AiAnaliseResultDto aiAnalise(byte[] beforeBytes, String before,
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
            result.validate();
            return result;
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to analyze cleanup result" + e.getMessage(), e);
        }
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
