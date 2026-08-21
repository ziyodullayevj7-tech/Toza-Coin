package teamwork.repository;

import org.hibernate.internal.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import teamwork.entity.ProfileEntity;

public interface ProfileRepository extends JpaRepository<ProfileEntity, String> {
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN TRUE ELSE FALSE END " +
            "FROM ProfileEntity AS p " +
            "WHERE p.username = :username")
    boolean existsByUsername(@Param("username") String username);
}
