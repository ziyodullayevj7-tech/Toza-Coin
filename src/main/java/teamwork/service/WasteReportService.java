package teamwork.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import teamwork.dto.coinTransaction.CoinTransactionDto;
import teamwork.dto.wasteReport.ConfirmWasteDto;
import teamwork.dto.wasteReport.WasteReportDto;
import teamwork.dto.wasteReport.WasteReportForMyReportDto;
import teamwork.entity.LocationEntity;
import teamwork.entity.ProfileEntity;
import teamwork.entity.WasteReportEntity;
import teamwork.enums.CoinType;
import teamwork.enums.ReportActionEnum;
import teamwork.enums.ReportStatus;
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
    private CoinTransactionService coinTransactionService;
    @Autowired
    private AuthService authService;

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
        // DOUBLE CHECK THE REWARD AMOUNT BASED ON THE ACTION
        int rewardCoins = (dto.getReportAction() == ReportActionEnum.CLEAN_MYSELF) ? 50 : 15;

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
        wasteReport.setCategory(dto.getWasteType());
        wasteReport.setSeverity(dto.getSeverityLevel());
        wasteReport.setDescription(dto.getDescription());
        wasteReport.setReportAction(dto.getReportAction());
        wasteReport.setRewardCoins(rewardCoins);
        wasteReport.setReporter(reporter);
        wasteReport.setCreatedDate(LocalDateTime.now());
        if (dto.getReportAction() == ReportActionEnum.SEND_TO_OTHERS){
            CoinTransactionDto coinTransaction = new CoinTransactionDto();
            coinTransaction.setCoinType(CoinType.EARN);
            String actionTitle = "Mas'ul tashkilotga yuborish";
            String description = String.format("Ifloslik xabari (%s, %s, %s) - %s",
                    wasteReport.getCategory() != null ? wasteReport.getCategory().name() : "Chiqindi",
                    wasteReport.getLocation() != null ? wasteReport.getLocation().getDistrict() : "Manzil",
                    wasteReport.getLocation() != null ? wasteReport.getLocation().getRegion() : "Toshkent",
                    actionTitle);
            coinTransaction.setDescription(description);
            coinTransaction.setAmount(rewardCoins);
            boolean coinTransactionSuccess = coinTransactionService.save(coinTransaction, reporter);
            if (!coinTransactionSuccess) throw new AppBadRequestException("Something went wrong");
            authService.updateUserBalance(currentProfileId, rewardCoins);
            wasteReport.setReportStatus(ReportStatus.RESOLVED);
            wasteReport.setBeforeImageUrl(dto.getBeforeImageUrl());
            wasteReport.setCleanPeriod(0.0);
        }else {
            wasteReport.setReportStatus(ReportStatus.PENDING);
            wasteReport.setBeforeImageUrl(dto.getBeforeImageUrl());
            wasteReport.setAfterImageUrl(dto.getAfterImageUrl());
            wasteReport.setCleanPeriod(dto.getCleanPeriod());
        }
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
        dto.setCoins(entity.getRewardCoins());
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

    public Page<ConfirmWasteDto> getUnresolvedReports(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").ascending());
        Page<WasteReportEntity> wasteReportEntities = wasteReportRepository.findAllUnresolved(ReportStatus.PENDING,
                ReportStatus.IN_PROGRESS,
                pageable);

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
        dto.setReporterFullName(entity.getReporter().getName() + " " + entity.getReporter().getSurname());
        dto.setDistrict(entity.getLocation().getDistrict());
        dto.setBeforeImageUrl(entity.getBeforeImageUrl());
        dto.setAfterImageUrl(entity.getAfterImageUrl());
        dto.setCleanPeriod(entity.getCleanPeriod());
        dto.setStatus(entity.getReportStatus());
        dto.setReportedDateTime(entity.getCreatedDate());
        return dto;
    }
}
