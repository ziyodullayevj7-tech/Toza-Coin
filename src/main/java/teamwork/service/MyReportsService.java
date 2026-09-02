package teamwork.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import teamwork.dto.myReport.MyReportResponseDto;
import teamwork.dto.wasteReport.WasteReportForMyReportDto;
import teamwork.entity.WasteReportEntity;
import teamwork.repository.WasteReportRepository;
import teamwork.util.SecurityUtils;

import teamwork.enums.ReportStatus;

import java.util.ArrayList;
import java.util.List;

@Service
public class MyReportsService {
    @Autowired
    private WasteReportService wasteReportService;
    @Autowired
    private WasteReportRepository wasteReportRepository;

    @Transactional
    public MyReportResponseDto getMyReportById() {
        String userId = SecurityUtils.getCurrentUserId();

        MyReportResponseDto dto = new MyReportResponseDto();

        WasteReportForMyReportDto wasteReportForMyReportDto = wasteReportService.getInfoForMyReport(userId);


        dto.setPhotoUrl(wasteReportForMyReportDto.getPhotoUrl());
        dto.setReportStatus(wasteReportForMyReportDto.getReportStatus());
        dto.setSeverityLevel(wasteReportForMyReportDto.getSeverityLevel());
        dto.setWasteType(wasteReportForMyReportDto.getWasteType());
        dto.setDescription(wasteReportForMyReportDto.getDescription());
        dto.setRegion(wasteReportForMyReportDto.getRegion());
        dto.setDistrict(wasteReportForMyReportDto.getDistrict());
        dto.setStreetAddress(wasteReportForMyReportDto.getStreetAddress());
        dto.setDate(wasteReportForMyReportDto.getDate());

        return dto;
    }

    @Transactional
    public Page<MyReportResponseDto> getMyRprtsPagination(int page, int size, ReportStatus status) {
        String userId = SecurityUtils.getCurrentUserId();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        
        Page<WasteReportEntity> wasteReportEntities;
        if (status == null) {
            wasteReportEntities = wasteReportRepository.findAllByReporterId(userId, pageable);
        } else {
            wasteReportEntities = wasteReportRepository.findAllByReporterIdAndReportStatus(userId, status, pageable);
        }

        List<WasteReportEntity> entityList = wasteReportEntities.getContent();
        long totalElements = wasteReportEntities.getTotalElements();

        List<MyReportResponseDto> dtoList = new ArrayList<>();
        entityList.forEach(entity -> dtoList.add(toDto(entity)));

        return new PageImpl<>(dtoList, pageable, totalElements);
    }

    public long getCountByStatus(ReportStatus status) {
        String userId = SecurityUtils.getCurrentUserId();
        if (status == null) {
            return wasteReportRepository.countByReporterId(userId);
        }
        return wasteReportRepository.countByReporterIdAndReportStatus(userId, status);
    }

    public Integer getTotalCoinsEarned() {
        String userId = SecurityUtils.getCurrentUserId();
        return wasteReportService.balanceByUserId(userId);
    }

    public MyReportResponseDto toDto(WasteReportEntity entity) {
        MyReportResponseDto dto = new MyReportResponseDto();
        dto.setId(entity.getId());
        dto.setPhotoUrl(entity.getImageUrl());
        dto.setReportStatus(entity.getReportStatus());
        dto.setSeverityLevel(entity.getSeverity());
        dto.setWasteType(entity.getCategory());
        dto.setDescription(entity.getDescription());
        dto.setRewardCoins(entity.getRewardCoins() != null ? entity.getRewardCoins() : 0);

        if (entity.getLocation() != null) {
            dto.setRegion(entity.getLocation().getRegion());
            dto.setDistrict(entity.getLocation().getDistrict());
            dto.setStreetAddress(entity.getLocation().getStreetAddress());
        }

        if (entity.getCapturedDate() != null) {
            dto.setDate(entity.getCapturedDate().toLocalDate());
        } else if (entity.getCreatedDate() != null) {
            dto.setDate(entity.getCreatedDate().toLocalDate());
        }

        return dto;
    }
}
