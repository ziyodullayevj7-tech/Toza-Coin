package teamwork.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamwork.entity.CampaignEntity;

public interface CampaignRepository extends JpaRepository<CampaignEntity, String> {
}
