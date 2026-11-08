package teamwork.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import teamwork.entity.ProfileRewardEntity;

public interface ProfileRewardRepository extends JpaRepository<ProfileRewardEntity, String> {
    @Query("SELECT pr FROM ProfileRewardEntity AS pr WHERE pr.profileId =:profileId ORDER BY pr.createdDateTime DESC ")
    Page<ProfileRewardEntity> findAllByUserId(@Param("profileId") String currentUserId, Pageable pageable);
}
