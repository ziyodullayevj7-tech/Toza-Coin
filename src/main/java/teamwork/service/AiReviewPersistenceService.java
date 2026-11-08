package teamwork.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import teamwork.dto.ai.AiAnaliseResultDto;
import teamwork.dto.coinTransaction.CoinTransactionDto;
import teamwork.entity.WasteReportEntity;
import teamwork.enums.AiReviewStatus;
import teamwork.enums.CoinType;
import teamwork.enums.ReportActionEnum;
import teamwork.enums.ReportStatus;
import teamwork.exceptions.AppBadRequestException;
import teamwork.repository.WasteReportRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AiReviewPersistenceService {
    private final WasteReportRepository wasteReportRepository;
    private final AuthService authService;
    private final CoinTransactionService coinTransactionService;

    public AiReviewPersistenceService(WasteReportRepository wasteReportRepository,
                                     AuthService authService,
                                     CoinTransactionService coinTransactionService) {
        this.wasteReportRepository = wasteReportRepository;
        this.authService = authService;
        this.coinTransactionService = coinTransactionService;
    }

    @Transactional
    public void saveSuccessfulReview(String reportId, AiAnaliseResultDto result) {
        WasteReportEntity report = findReportForUpdate(reportId);
        if (report.getAiReviewStatus() == AiReviewStatus.COMPLETED) {
            return;
        }

        if (report.getReportAction() == ReportActionEnum.CLEAN_MYSELF) {
            result.validateCleanup();
        } else {
            result.validateReport();
        }
        report.setCategory(result.getWasteType());
        report.setSeverity(result.getSeverityLevel());
        report.setAiReviewStatus(AiReviewStatus.COMPLETED);
        report.setConfidenceScore(result.getConfidenceScore());
        report.setAiReasoning(result.getReasoning());
        report.setAiWastePresent(result.getWastePresent());
        report.setAiSameLocation(result.getSameLocation());
        report.setAiWasteCleared(result.getWasteCleared());
        report.setAiDetectedAnchors(result.getDetectedAnchors() == null
                ? null
                : result.getDetectedAnchors().stream().collect(Collectors.joining(", ")));
        report.setAiReviewedAt(LocalDateTime.now());

        int reward = 0;
        if (report.getReportAction() == ReportActionEnum.SEND_TO_OTHERS
                && result.isEligibleForReportReward()) {
            reward = 15;
        } else if (report.getReportAction() == ReportActionEnum.CLEAN_MYSELF
                && result.isEligibleForCleanupReward()) {
            reward = 50;
        }
        // Manual approval may have rewarded the report while AI was running.
        if (Boolean.TRUE.equals(report.getAiRewardGranted())
                || report.getReportStatus() == ReportStatus.REJECTED
                || report.getReportStatus() == ReportStatus.RESOLVED) {
            reward = 0;
        }
        if (reward > 0) {
            report.setAiRewardGranted(true);
        }

        if (reward > 0) {
            String profileId = report.getReporter().getId();
            if (!authService.creditAiReviewReward(profileId, reward)) {
                throw new AppBadRequestException("Could not credit the AI review reward");
            }

            CoinTransactionDto transaction = new CoinTransactionDto();
            transaction.setCoinType(CoinType.EARN);
            transaction.setAmount(reward);
            transaction.setDescription(report.getReportAction() == ReportActionEnum.SEND_TO_OTHERS
                    ? "AI tekshiruvidan o'tgan chiqindi xabari uchun mukofot"
                    : "AI tekshiruvidan o'tgan tozalash hisoboti uchun mukofot");
            if (!coinTransactionService.save(transaction, report.getReporter())) {
                throw new AppBadRequestException("Could not save the AI review reward transaction");
            }
        }

        wasteReportRepository.save(report);
    }

    @Transactional
    public void saveFailedReview(String reportId) {
        Optional<WasteReportEntity> optional = wasteReportRepository.findByIdForUpdate(reportId);
        if (optional.isEmpty()) {
            throw new AppBadRequestException("Waste report not found while recording AI review failure");
        }

        WasteReportEntity report = optional.get();
        if (report.getAiReviewStatus() != AiReviewStatus.COMPLETED) {
            report.setAiReviewStatus(AiReviewStatus.FAILED);
            report.setAiReasoning("AI tekshiruvi yakunlanmadi. Keyinroq qayta urinib ko'ring.");
            wasteReportRepository.save(report);
        }
    }

    private WasteReportEntity findReportForUpdate(String reportId) {
        return wasteReportRepository.findByIdForUpdate(reportId)
                .orElseThrow(() -> new AppBadRequestException("Waste report not found"));
    }
}
