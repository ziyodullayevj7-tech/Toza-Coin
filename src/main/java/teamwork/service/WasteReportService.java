package teamwork.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import teamwork.dto.coinTransaction.CoinTransactionDto;
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

        CoinTransactionDto coinTransaction =  new CoinTransactionDto();
        coinTransaction.setCoinType(CoinType.EARN);
        // SET DESCRIPTION
        String actionTitle = (dto.getReportAction() == ReportActionEnum.CLEAN_MYSELF)
                ? "O'zi tozalash"
                : "Mas'ul tashkilotga yuborish";

        String description = String.format("Ifloslik xabari (%s, %s, %s) - %s",
                dto.getWasteType() != null ? dto.getWasteType().name() : "Chiqindi",
                dto.getLocation() != null ? dto.getLocation().getDistrict() : "Manzil",
                dto.getLocation() != null ? dto.getLocation().getRegion() : "Toshkent",
                actionTitle);

        coinTransaction.setDescription(description);
        coinTransaction.setAmount(rewardCoins);
        // SAVE COIN TRANSACTION
        boolean coinTransactionSuccess = coinTransactionService.save(coinTransaction, reporter);
        if (!coinTransactionSuccess){
            throw new AppBadRequestException("Something went wrong");
        }

        // UPDATE USER COIN BALANCE
        boolean updateUserBalance = authService.updateUserBalance(currentProfileId, rewardCoins);
        if (!updateUserBalance) {
            log.warn("Update user balance failed because user not found with this id");
            throw new AppBadRequestException("Something went wrong");
        }

        // IF LOCATION ID IS AVAILABLE
        WasteReportEntity wasteReport = new WasteReportEntity();
        wasteReport.setImageUrl(dto.getImageUrl());
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
        wasteReport.setReportStatus(dto.getReportStatus());
        wasteReport.setReporter(reporter);
        wasteReport.setCreatedDate(LocalDateTime.now());

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

        dto.setPhotoUrl(entity.getImageUrl());
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
}
