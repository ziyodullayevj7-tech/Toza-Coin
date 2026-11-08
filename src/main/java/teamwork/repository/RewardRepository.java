package teamwork.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;
import teamwork.entity.RewardEntity;

import java.util.Optional;

public interface RewardRepository extends JpaRepository<RewardEntity, String> {
    Optional<RewardEntity> findByTitleAndProviderNameIgnoreCase(String title, String providerName);

    boolean existsByTitleIgnoreCaseAndProviderNameIgnoreCaseAndIdNot(
            String title, String providerName, String id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RewardEntity r WHERE r.id = :rewardId")
    Optional<RewardEntity> findByIdForUpdate(@Param("rewardId") String rewardId);

    @Query("SELECT r FROM RewardEntity AS r WHERE r.quantity >= :quantity AND r.isActive = :isActive ORDER BY r.createdDateTime DESC, r.id DESC")
    Page<RewardEntity> findAllByIsActiveAndQuantity(@Param("isActive") Boolean isActive, @Param("quantity") Integer quantity, Pageable pageable);
}
