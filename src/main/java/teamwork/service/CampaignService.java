package teamwork.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import teamwork.repository.CampaignRepository;

@Service
public class CampaignService {
    @Autowired
    private CampaignRepository campaignRepository;
}
