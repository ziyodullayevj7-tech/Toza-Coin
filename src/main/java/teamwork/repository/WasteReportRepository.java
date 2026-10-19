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
import java.util.Optional;

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

    Optional<WasteReportEntity> findByReporterIdIs(String reporterId);

    @Query("SELECT wr FROM WasteReportEntity AS wr " +
            "WHERE wr.reportStatus = :reportStatus1 " +
            "OR wr.reportStatus = :reportStatus2")
    Page<WasteReportEntity> findAllUnresolved(@Param("reportStatus1") ReportStatus reportStatus1,
                                              @Param("reportStatus2") ReportStatus reportStatus2,
                                              Pageable pageable);
}
