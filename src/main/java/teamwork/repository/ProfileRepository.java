package teamwork.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamwork.entity.ProfileEntity;

public interface ProfileRepository extends JpaRepository<ProfileEntity, String> {
}
