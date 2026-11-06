package teamwork.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import teamwork.entity.WasteReportEntity;
import teamwork.enums.ReportActionEnum;
import teamwork.enums.ReportStatus;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface WasteReportRepository extends JpaRepository<WasteReportEntity, String> {
    Page<WasteReportEntity> findAllByReporterId(String reporterId, Pageable pageable);

    Page<WasteReportEntity> findAllByReporterIdAndReportStatus(String reporterId, ReportStatus reportStatus, Pageable pageable);

    long countByReporterId(String reporterId);

    long countByReporterIdAndReportStatus(String reporterId, ReportStatus reportStatus);

    @Query("SELECT wr FROM WasteReportEntity wr JOIN FETCH wr.location " +
            "WHERE wr.reportAction = :reportAction AND wr.reportStatus = :reportStatus " +
            "AND wr.cleaner IS NULL " +
            "ORDER BY wr.createdDate DESC")
    List<WasteReportEntity> findAllLiveWaste(@Param("reportAction") ReportActionEnum reportAction,
                                             @Param("reportStatus") ReportStatus reportStatus,
                                             Pageable pageable);

    @Query(value = "SELECT wr FROM WasteReportEntity wr LEFT JOIN FETCH wr.location " +
            "WHERE (wr.reporterId = :userId OR wr.cleanerId = :userId) " +
            "AND (:cleaned IS NULL OR " +
            "(:cleaned = TRUE AND wr.afterImageUrl IS NOT NULL AND TRIM(wr.afterImageUrl) <> '') OR " +
            "(:cleaned = FALSE AND (wr.afterImageUrl IS NULL OR TRIM(wr.afterImageUrl) = '')))",
            countQuery = "SELECT COUNT(wr) FROM WasteReportEntity wr " +
                    "WHERE (wr.reporterId = :userId OR wr.cleanerId = :userId) " +
                    "AND (:cleaned IS NULL OR " +
                    "(:cleaned = TRUE AND wr.afterImageUrl IS NOT NULL AND TRIM(wr.afterImageUrl) <> '') OR " +
                    "(:cleaned = FALSE AND (wr.afterImageUrl IS NULL OR TRIM(wr.afterImageUrl) = '')))")
    Page<WasteReportEntity> findMyWork(@Param("userId") String userId,
                                       @Param("cleaned") Boolean cleaned, Pageable pageable);

    Optional<WasteReportEntity> findByReporterIdIs(String reporterId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT wr FROM WasteReportEntity wr WHERE wr.id = :reportId")
    Optional<WasteReportEntity> findByIdForUpdate(@Param("reportId") String reportId);

    @Query("SELECT wr FROM WasteReportEntity wr " +
            "WHERE wr.reportStatus = :pendingStatus " +
            "OR (wr.reportStatus = :inProgressStatus " +
            "AND wr.reportAction = :cleanMyself)")
    Page<WasteReportEntity> findAllUnresolved(@Param("pendingStatus") ReportStatus pendingStatus,
                                              @Param("inProgressStatus") ReportStatus inProgressStatus,
                                              @Param("cleanMyself") ReportActionEnum cleanMyself,
                                              Pageable pageable);
}
