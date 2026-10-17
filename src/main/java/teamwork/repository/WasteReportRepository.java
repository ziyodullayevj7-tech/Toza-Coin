package teamwork.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import teamwork.entity.WasteReportEntity;
import teamwork.enums.ReportActionEnum;
import teamwork.enums.ReportStatus;

import java.util.List;

public interface WasteReportRepository extends JpaRepository<WasteReportEntity, String> {
    Page<WasteReportEntity> findAllByReporterId(String reporterId, Pageable pageable);

    Page<WasteReportEntity> findAllByReporterIdAndReportStatus(String reporterId, ReportStatus reportStatus, Pageable pageable);

    long countByReporterId(String reporterId);

    long countByReporterIdAndReportStatus(String reporterId, ReportStatus reportStatus);

    @Query("SELECT wr FROM WasteReportEntity wr JOIN FETCH wr.location " +
            "WHERE wr.reportAction = :reportAction OR wr.reportStatus = :reportStatus " +
            "ORDER BY wr.createdDate DESC")
    List<WasteReportEntity> findAllLiveWaste(@Param("reportAction") ReportActionEnum reportAction,
                                             @Param("reportStatus") ReportStatus reportStatus,
                                             Pageable pageable);
}
