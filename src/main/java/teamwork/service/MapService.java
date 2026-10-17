package teamwork.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import teamwork.dto.liveWasteMap.LiveWasteMapDto;
import teamwork.dto.location.LocationDto;
import teamwork.entity.WasteReportEntity;
import teamwork.enums.ReportActionEnum;
import teamwork.enums.ReportStatus;
import teamwork.repository.LocationRepository;
import teamwork.repository.WasteReportRepository;

import java.util.List;

@Slf4j
@Service
public class MapService {
    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private WasteReportRepository wasteReportRepository;

    /**
     * Fetches up to 100 most recent waste reports that are either REJECTED or SEND_TO_OTHERS.
     * Single SQL query — no COUNT, no N+1 (location is JOIN FETCHed).
     */
    @Transactional
    public List<LiveWasteMapDto> getLiveWasteForMap() {
        Pageable limit = PageRequest.of(0, 100, Sort.by("createdDate").descending());

        return wasteReportRepository
                .findAllLiveWaste(ReportActionEnum.SEND_TO_OTHERS, ReportStatus.REJECTED, limit)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public LiveWasteMapDto toDto(WasteReportEntity entity) {
        LiveWasteMapDto dto = new LiveWasteMapDto();
        dto.setId(entity.getId());
        dto.setImageUrl(entity.getImageUrl());
        dto.setWasteType(entity.getCategory());
        dto.setSeverityLevel(entity.getSeverity());
        dto.setRewardCoins(entity.getRewardCoins());
        dto.setReporterId(entity.getReporterId());

        LocationDto location = new LocationDto();
        location.setLatitude(entity.getLocation().getLatitude());
        location.setLongitude(entity.getLocation().getLongitude());
        location.setRegion(entity.getLocation().getRegion());
        location.setDistrict(entity.getLocation().getDistrict());
        location.setStreetAddress(entity.getLocation().getStreetAddress());

        dto.setLocation(location);
        return dto;
    }
}
