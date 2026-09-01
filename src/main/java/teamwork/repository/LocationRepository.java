package teamwork.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamwork.entity.LocationEntity;

public interface LocationRepository extends JpaRepository<LocationEntity, String> {
}
