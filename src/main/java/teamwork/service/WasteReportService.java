package teamwork.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import teamwork.dto.wasteReport.CleanReportedWasteDto;
import teamwork.dto.wasteReport.ConfirmWasteDto;
import teamwork.dto.wasteReport.WasteReportDto;
import teamwork.dto.wasteReport.WasteReportForMyReportDto;
import teamwork.entity.LocationEntity;
import teamwork.entity.ProfileEntity;
import teamwork.entity.WasteReportEntity;
import teamwork.enums.AiReviewStatus;
import teamwork.enums.WasteTypeEnum;
import teamwork.enums.SeverityLevelEnum;
import teamwork.enums.ReportActionEnum;
import teamwork.enums.ReportStatus;
import teamwork.event.WasteReportCreatedEvent;
import teamwork.exceptions.AppBadRequestException;
import teamwork.repository.WasteReportRepository;
import teamwork.util.SecurityUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class WasteReportService {
    @Autowired
    private WasteReportRepository wasteReportRepository;
    @Autowired
    private LocationService locationService;
    @Autowired
    private AuthService authService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional
    public boolean create(WasteReportDto dto) {
        if (dto.getReportAction() == null || dto.getBeforeImageUrl() == null
                || dto.getBeforeImageUrl().isBlank()) {
            throw new AppBadRequestException("A report action and before photo are required");
        }
        if (dto.getReportAction() == ReportActionEnum.CLEAN_MYSELF
                && (dto.getAfterImageUrl() == null || dto.getAfterImageUrl().isBlank()
                || dto.getCleanPeriod() == null || !Double.isFinite(dto.getCleanPeriod())
                || dto.getCleanPeriod() <= 0)) {
            throw new AppBadRequestException("A cleanup report requires an after photo and a valid cleaning duration");
        }

        // CURRENT USER ID
        String currentProfileId = SecurityUtils.getCurrentUserId();

        ProfileEntity reporter = authService.getCurrentProfile(currentProfileId);
        int cleanerRewardCoins = dto.getReportAction() == ReportActionEnum.CLEAN_MYSELF ? 50 : 0;
        int reporterRewardCoins = dto.getReportAction() == ReportActionEnum.SEND_TO_OTHERS ? 15 : 0;


        // SET LOCATION FIRST BECAUSE WE NEED THE ID OF LOCATION TO SET IT FOR WASTE REPORT ENTITY

        // IF LOCATION ID IS EMPTY
        Optional<LocationEntity> location = locationService.saveForWasteReport(dto.getLocation());
        if (location.isEmpty()){
            log.error("Location id came empty to WasteReportService");
            throw new AppBadRequestException("Something went wrong");
        }

        // IF LOCATION ID IS AVAILABLE
        WasteReportEntity wasteReport = new WasteReportEntity();
        LocalDateTime capturedDate = (dto.getCapturedDate() != null && !dto.getCapturedDate().isBlank())
                ? LocalDateTime.parse(dto.getCapturedDate())
                : LocalDateTime.now();
        wasteReport.setCapturedDate(capturedDate);
        wasteReport.setLocation(location.get());
        wasteReport.setCategory(WasteTypeEnum.OTHER);
        wasteReport.setSeverity(SeverityLevelEnum.UNDER_REVIEW);
        wasteReport.setDescription(dto.getDescription());
        wasteReport.setReportAction(dto.getReportAction());
        wasteReport.setAiReviewStatus(AiReviewStatus.PENDING);
        wasteReport.setReporterRewardCoins(reporterRewardCoins);
        wasteReport.setCleanerRewardCoins(cleanerRewardCoins);
        wasteReport.setCreatedDate(LocalDateTime.now());
        wasteReport.setReporter(reporter);
        if (dto.getReportAction() == ReportActionEnum.SEND_TO_OTHERS) {
            wasteReport.setReportStatus(ReportStatus.PENDING);
            wasteReport.setBeforeImageUrl(dto.getBeforeImageUrl());
        } else {
            wasteReport.setReportStatus(ReportStatus.PENDING);
            wasteReport.setBeforeImageUrl(dto.getBeforeImageUrl());
            wasteReport.setAfterImageUrl(dto.getAfterImageUrl());
            wasteReport.setCleanPeriod(dto.getCleanPeriod());
            wasteReport.setCleaner(reporter);
        }

        WasteReportEntity savedReport = wasteReportRepository.save(wasteReport);
        eventPublisher.publishEvent(new WasteReportCreatedEvent(savedReport.getId()));
        return true;
    }

    @Transactional
    public boolean claimReportForCleaning(String reportId) {
        if (reportId == null || reportId.isBlank()) {
            throw new AppBadRequestException("Waste report ID is required");
        }

        String currentProfileId = SecurityUtils.getCurrentUserId();
        Optional<WasteReportEntity> optional = wasteReportRepository.findByIdForUpdate(reportId);
        if (optional.isEmpty()) throw new AppBadRequestException("Waste Report not found");

        WasteReportEntity report = optional.get();
        if (report.getReportAction() != ReportActionEnum.SEND_TO_OTHERS
                || report.getReportStatus() != ReportStatus.IN_PROGRESS
                || report.getCleaner() != null) {
            throw new AppBadRequestException("This report is not available for cleaning");
        }
        if (currentProfileId.equals(report.getReporterId())) {
            throw new AppBadRequestException("You cannot clean your own reported waste");
        }

        report.setCleaner(authService.getCurrentProfile(currentProfileId));
        wasteReportRepository.save(report);
        return true;
    }

    @Transactional
    public boolean updateReportCleaner(CleanReportedWasteDto dto) {
        if (dto == null || dto.getWasteReportId() == null || dto.getWasteReportId().isBlank()
                || dto.getCleanedImageUrl() == null || dto.getCleanedImageUrl().isBlank()
                || dto.getCleanPeriod() == null || !Double.isFinite(dto.getCleanPeriod())
                || dto.getCleanPeriod() <= 0) {
            throw new AppBadRequestException("A cleaned photo and valid cleaning duration are required");
        }

        String currentProfileId = SecurityUtils.getCurrentUserId();
        ProfileEntity cleaner = authService.getCurrentProfile(currentProfileId);

        Optional<WasteReportEntity> optional = wasteReportRepository.findByIdForUpdate(dto.getWasteReportId());
        if (optional.isEmpty()) throw new AppBadRequestException("Waste Report not found");

        WasteReportEntity wasteReport = optional.get();
        if (wasteReport.getReportAction() != ReportActionEnum.SEND_TO_OTHERS
                || wasteReport.getReportStatus() != ReportStatus.IN_PROGRESS
                || (wasteReport.getCleaner() != null
                && !currentProfileId.equals(wasteReport.getCleaner().getId()))) {
            throw new AppBadRequestException("This report is not assigned to you for cleaning");
        }
        if (currentProfileId.equals(wasteReport.getReporterId())) {
            throw new AppBadRequestException("You cannot clean your own reported waste");
        }

        if (wasteReport.getCleaner() == null) {
            wasteReport.setCleaner(cleaner);
        }
        wasteReport.setCleanerRewardCoins(50);
        wasteReport.setAfterImageUrl(dto.getCleanedImageUrl());
        wasteReport.setCleanPeriod(dto.getCleanPeriod());
        wasteReport.setReportStatus(ReportStatus.PENDING);
        wasteReportRepository.save(wasteReport);
        return true;
    }

    public WasteReportForMyReportDto getInfoForMyReport(String userId) {
        WasteReportForMyReportDto dto = new WasteReportForMyReportDto();
        Optional<WasteReportEntity> optional = wasteReportRepository.findById(userId);
        if (optional.isEmpty()){
            throw new AppBadRequestException("Waste Report not found");
        }
        WasteReportEntity entity = optional.get();

        dto.setPhotoUrl(entity.getAfterImageUrl() != null
                ? entity.getAfterImageUrl() : entity.getBeforeImageUrl());
        dto.setReportStatus(entity.getReportStatus());
        dto.setSeverityLevel(entity.getSeverity());
        dto.setWasteType(entity.getCategory());
        dto.setDescription(entity.getDescription());
        dto.setReporterRewardCoins(entity.getReporterRewardCoins());
        dto.setCleanerRewardCoins(entity.getCleanerRewardCoins());
        dto.setAiReviewStatus(entity.getAiReviewStatus());
        dto.setAiRewardGranted(entity.getAiRewardGranted());
        dto.setRegion(entity.getLocation().getRegion());
        dto.setDistrict(entity.getLocation().getDistrict());
        dto.setStreetAddress(entity.getLocation().getStreetAddress());

        LocalDate date = entity.getCapturedDate().toLocalDate();
        dto.setDate(date);

        return dto;
    }

    public long countByReporterId(String userId) {
        return wasteReportRepository.countByReporterId(userId);
    }

    public long countByReporterIdAndReportStatus(String userId, ReportStatus status) {
        return wasteReportRepository.countByReporterIdAndReportStatus(userId, status);
    }

    public Integer balanceByUserId(String userId) {
        return authService.getBalanceByUserId(userId);
    }

    @Transactional
    public Page<ConfirmWasteDto> getUnresolvedReports(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").ascending());
        Page<WasteReportEntity> wasteReportEntities = wasteReportRepository.findAllUnresolved(
                ReportStatus.PENDING, ReportStatus.IN_PROGRESS, ReportActionEnum.CLEAN_MYSELF, pageable);

        List<WasteReportEntity> entityList = wasteReportEntities.getContent();
        long totalElements = wasteReportEntities.getTotalElements();

        List<ConfirmWasteDto> dtoList = new ArrayList<>();

        entityList.forEach(entity -> dtoList.add(toConfirmWasteDto(entity)));
        return new PageImpl<>(dtoList, pageable, totalElements);
    }

    public ConfirmWasteDto toConfirmWasteDto(WasteReportEntity entity){
        ConfirmWasteDto dto = new ConfirmWasteDto();
        dto.setReportId(entity.getId());
        dto.setReporterId(entity.getReporter().getId());
        dto.setCleanerId(entity.getCleaner() == null ? null : entity.getCleaner().getId());
        dto.setReporterFullName(entity.getReporter().getName() + " " + entity.getReporter().getSurname());
        dto.setDistrict(entity.getLocation().getDistrict());
        dto.setBeforeImageUrl(entity.getBeforeImageUrl());
        dto.setAfterImageUrl(entity.getAfterImageUrl());
        dto.setCleanPeriod(entity.getCleanPeriod());
        dto.setStatus(entity.getReportStatus());
        dto.setReportAction(entity.getReportAction());
        dto.setReportedDateTime(entity.getCreatedDate());
        dto.setConfidenceScore(entity.getConfidenceScore());
        dto.setReasoning(entity.getAiReasoning());
        dto.setAiReviewStatus(entity.getAiReviewStatus());
        dto.setWastePresent(entity.getAiWastePresent());
        dto.setSameLocation(entity.getAiSameLocation());
        dto.setWasteCleared(entity.getAiWasteCleared());
        dto.setDetectedAnchors(entity.getAiDetectedAnchors());
        return dto;
    }
}
