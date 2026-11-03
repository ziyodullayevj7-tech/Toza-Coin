package teamwork.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import teamwork.dto.leaderBoard.LeaderBoardDto;
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


    @Transactional
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

        if (dto.getStatus() == ReportStatus.IN_PROGRESS) {
            if (report.getReportStatus() != ReportStatus.PENDING) {
                throw new AppBadRequestException("Only pending reports can enter review");
            }
            report.setReportStatus(ReportStatus.IN_PROGRESS);
            wasteReportRepository.save(report);
            return true;
        }

        if (dto.getStatus() != ReportStatus.RESOLVED) {
            throw new AppBadRequestException("Unsupported report status");
        }

        report.setReportStatus(ReportStatus.RESOLVED);
        wasteReportRepository.save(report);
        return true;
    }

    public Page<LeaderBoardDto> getUsers(int page, int size) {
        return authService.getUsersByRanking(page, size);
    }

    public Boolean blockUser(String userId) {
        return authService.updateProfileStatus(userId, ProfileStatus.BLOCKED);
    }
}
