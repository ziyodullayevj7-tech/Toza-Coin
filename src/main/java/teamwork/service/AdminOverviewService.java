package teamwork.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import teamwork.enums.AiReviewStatus;
import teamwork.enums.ReportStatus;
import teamwork.repository.ProfileRepository;
import teamwork.repository.WasteReportRepository;

@Service
public class AdminOverviewService {
    private final ProfileRepository profiles;
    private final WasteReportRepository reports;

    public AdminOverviewService(ProfileRepository profiles, WasteReportRepository reports) {
        this.profiles = profiles;
        this.reports = reports;
    }

    @Transactional(readOnly = true)
    public Summary load() {
        return new Summary(profiles.count(), reports.countByReportStatus(ReportStatus.PENDING),
                reports.countByReportStatus(ReportStatus.IN_PROGRESS),
                reports.countByReportStatus(ReportStatus.RESOLVED),
                reports.countByReportStatus(ReportStatus.REJECTED),
                reports.countByAiReviewStatus(AiReviewStatus.FAILED));
    }

    public record Summary(long accounts, long pending, long inProgress, long resolved,
                          long rejected, long failedReviews) {
        public long totalReports() { return pending + inProgress + resolved + rejected; }
    }
}
