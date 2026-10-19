package teamwork.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import teamwork.dto.leaderBoard.LeaderBoardDto;
import teamwork.entity.ProfileEntity;
import teamwork.repository.ProfileRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class LeaderBoardService {
    @Autowired
    private ProfileRepository profileRepository;

    @Transactional
    public Page<LeaderBoardDto> getLeaderBoard(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<ProfileEntity> profiles = profileRepository.findAllOrderByCount(pageable);
        List<ProfileEntity> entities = profiles.getContent();
        long totalElements = profiles.getTotalElements();

        List<LeaderBoardDto> dtos = new ArrayList<>();
        entities.forEach(entity -> dtos.add(toDto(entity)));

        return new PageImpl<>(dtos, pageable, totalElements);
    }

    public LeaderBoardDto toDto(ProfileEntity entity) {
        LeaderBoardDto dto = new LeaderBoardDto();
        dto.setProfileId(entity.getId());
        dto.setFullName(entity.getName() + " " + entity.getSurname());
        dto.setDistrict(entity.getDistrict());
        dto.setReportCount(entity.getReportCount());
        dto.setStreakDays(entity.getStreakDays());
        dto.setTotalEarnedCoins(entity.getTotalEarnedCoins());
        return dto;
    }
}