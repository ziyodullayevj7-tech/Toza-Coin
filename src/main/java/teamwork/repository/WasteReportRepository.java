package teamwork.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamwork.entity.WasteReportEntity;

public interface WasteReportRepository extends JpaRepository<WasteReportEntity, String> {
}
