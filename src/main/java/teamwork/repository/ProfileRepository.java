package teamwork.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import teamwork.entity.ProfileEntity;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<ProfileEntity, String> {
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN TRUE ELSE FALSE END " +
            "FROM ProfileEntity AS p " +
            "WHERE p.username = :username")
    boolean existsByUsername(@Param("username") String username);

    @Query("FROM ProfileEntity AS p " +
            "WHERE p.username =:username " +
            "AND p.visible IS TRUE")
    Optional<ProfileEntity> findByUsernameAndVisibleIsTrue(@Param("username") String username);

    Optional<ProfileEntity> findByUsername(String username);

    @Query("SELECT COALESCE(SUM(p.coinBalance), 0) FROM ProfileEntity p WHERE p.id = :userId")
    Optional<Integer> getBalanceById(String userId);
}
