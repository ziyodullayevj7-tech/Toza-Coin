package teamwork.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamwork.entity.ProfileRoleEntity;

public interface ProfileRoleRepository extends JpaRepository<ProfileRoleEntity, String> {
}
