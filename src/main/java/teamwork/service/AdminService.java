package teamwork.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import teamwork.dto.leaderBoard.LeaderBoardDto;
import teamwork.dto.coinTransaction.CoinTransactionDto;
import teamwork.entity.ProfileEntity;
import teamwork.enums.CoinType;
import teamwork.enums.ReportActionEnum;
import teamwork.dto.wasteReport.ConfirmWasteDto;
import teamwork.entity.WasteReportEntity;
import teamwork.enums.ProfileStatus;
import teamwork.enums.ReportStatus;
import teamwork.exceptions.AppBadRequestException;
import teamwork.repository.WasteReportRepository;

@Service
public class AdminService {
    @Autowired
    private WasteReportRepository wasteReportRepository;
    @Autowired
    private AuthService authService;
    @Autowired
    private CoinTransactionService coinTransactionService;


    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public boolean confirmReport(ConfirmWasteDto dto) {
        if (dto == null || dto.getReportId() == null || dto.getReportId().isBlank()
                || dto.getStatus() == null) {
            throw new AppBadRequestException("Report ID and status are required");
        }

        WasteReportEntity report = wasteReportRepository.findByIdForUpdate(dto.getReportId())
                .orElseThrow(() -> new AppBadRequestException("Report not found"));
        if (report.getReportStatus() == ReportStatus.RESOLVED
                || report.getReportStatus() == ReportStatus.REJECTED) {
            throw new AppBadRequestException("This report has already been reviewed");
        }

        if (dto.getStatus() == ReportStatus.REJECTED) {
            report.setReportStatus(ReportStatus.REJECTED);
            wasteReportRepository.save(report);
            return true;
        }

        boolean reportOnly = report.getReportAction() == ReportActionEnum.SEND_TO_OTHERS
                && report.getCleaner() == null
                && (report.getAfterImageUrl() == null || report.getAfterImageUrl().isBlank());
        if (reportOnly && (dto.getStatus() == ReportStatus.IN_PROGRESS || dto.getStatus() == ReportStatus.RESOLVED)) {
            if (report.getReportStatus() != ReportStatus.PENDING) {
                throw new AppBadRequestException("This report has already been published for cleanup");
            }
            grantInitialReward(report);
            report.setCleanerRewardCoins(50);
            report.setReportStatus(ReportStatus.IN_PROGRESS);
            wasteReportRepository.save(report);
            return true;
        }

        if (dto.getStatus() == ReportStatus.IN_PROGRESS) {
            if (report.getReportStatus() != ReportStatus.PENDING
                    || report.getReportAction() != ReportActionEnum.CLEAN_MYSELF) {
                throw new AppBadRequestException("Only pending self-cleanup reports can enter review");
            }
            report.setReportStatus(ReportStatus.IN_PROGRESS);
            wasteReportRepository.save(report);
            return true;
        }

        if (dto.getStatus() != ReportStatus.RESOLVED) {
            throw new AppBadRequestException("Unsupported report status");
        }

        if (report.getAfterImageUrl() == null || report.getAfterImageUrl().isBlank()
                || report.getReportAction() == ReportActionEnum.SEND_TO_OTHERS && report.getCleaner() == null) {
            throw new AppBadRequestException("Cleanup evidence and a cleaner are required before completing this report");
        }
        grantInitialReward(report);
        // A community cleaner is a separate beneficiary from the original reporter.
        if (report.getReportAction() == ReportActionEnum.SEND_TO_OTHERS
                && report.getCleaner() != null) {
            if (report.getAfterImageUrl() == null || report.getAfterImageUrl().isBlank()) {
                throw new AppBadRequestException("A cleanup photo is required before rewarding the cleaner");
            }
            creditReward(report.getCleaner(), 50, "Approved cleanup reward: " + report.getId());
            report.setCleanerRewardCoins(50);
        }
        report.setReportStatus(ReportStatus.RESOLVED);
        wasteReportRepository.save(report);
        return true;
    }

    private void grantInitialReward(WasteReportEntity report) {
        // The report row is locked; AI and admin approval share this payment marker.
        if (!Boolean.TRUE.equals(report.getAiRewardGranted())) {
            int amount = report.getReportAction() == ReportActionEnum.CLEAN_MYSELF ? 50 : 15;
            creditReward(report.getReporter(), amount, "Approved report reward: " + report.getId());
            report.setAiRewardGranted(true);
        }
    }

    private void creditReward(ProfileEntity profile, int amount, String description) {
        if (profile == null || !authService.creditAiReviewReward(profile.getId(), amount)) {
            throw new AppBadRequestException("Could not credit the report reward");
        }
        CoinTransactionDto transaction = new CoinTransactionDto();
        transaction.setCoinType(CoinType.EARN);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        if (!coinTransactionService.save(transaction, profile)) {
            throw new AppBadRequestException("Could not save the reward transaction");
        }
    }

    public Page<LeaderBoardDto> getUsers(int page, int size) {
        return authService.getUsersByRanking(page, size);
    }

    public Boolean blockUser(String userId) {
        return authService.updateProfileStatus(userId, ProfileStatus.BLOCKED);
    }
}
