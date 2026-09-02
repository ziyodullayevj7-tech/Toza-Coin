package teamwork.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import teamwork.entity.WasteReportEntity;
import teamwork.enums.ReportStatus;

public interface WasteReportRepository extends JpaRepository<WasteReportEntity, String> {
    Page<WasteReportEntity> findAllByReporterId(String reporterId, Pageable pageable);

    Page<WasteReportEntity> findAllByReporterIdAndReportStatus(String reporterId, ReportStatus reportStatus, Pageable pageable);

    long countByReporterId(String reporterId);

    long countByReporterIdAndReportStatus(String reporterId, ReportStatus reportStatus);

}
