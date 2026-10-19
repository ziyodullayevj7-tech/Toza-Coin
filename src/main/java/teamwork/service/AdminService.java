package teamwork.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import teamwork.dto.coinTransaction.CoinTransactionDto;
import teamwork.dto.wasteReport.ConfirmWasteDto;
import teamwork.entity.ProfileEntity;
import teamwork.entity.WasteReportEntity;
import teamwork.enums.CoinType;
import teamwork.enums.ReportActionEnum;
import teamwork.enums.ReportStatus;
import teamwork.exceptions.AppBadRequestException;
import teamwork.repository.WasteReportRepository;

import java.util.Optional;

@Service
public class AdminService {
    @Autowired
    private WasteReportRepository wasteReportRepository;
    @Autowired
    private AuthService authService;
    @Autowired
    private CoinTransactionService coinTransactionService;
    @Transactional
    public boolean confirmReport(ConfirmWasteDto dto) {
        Optional<WasteReportEntity> optional = wasteReportRepository.findById((dto.getReportId()));
        if (optional.isPresent()) {
            WasteReportEntity wasteReport = optional.get();
            if (dto.getStatus().equals(ReportStatus.RESOLVED)) {
                // UPDATE REPORT STATUS TO RESOLVED
                wasteReport.setReportStatus(dto.getStatus());
                wasteReportRepository.save(wasteReport);

                // UPDATE USER BALANCE
                authService.updateUserBalance(dto.getReporterId(), wasteReport.getRewardCoins());

                // CREATE COIN TRANSACTION
                CoinTransactionDto coinTransaction =  new CoinTransactionDto();
                coinTransaction.setCoinType(CoinType.EARN);
                String actionTitle = "O'zi tozalash";
                String description = String.format("Ifloslik xabari (%s, %s, %s) - %s",
                        wasteReport.getCategory() != null ? wasteReport.getCategory().name() : "Chiqindi",
                        wasteReport.getLocation() != null ? wasteReport.getLocation().getDistrict() : "Manzil",
                        wasteReport.getLocation() != null ? wasteReport.getLocation().getRegion() : "Toshkent",
                        actionTitle);
                coinTransaction.setDescription(description);
                coinTransaction.setAmount(wasteReport.getRewardCoins());
                Optional<ProfileEntity> profile = authService.getProfileById(dto.getReporterId());
                if (profile.isEmpty()) throw new AppBadRequestException("Profile not found");
                boolean coinTransactionSuccess = coinTransactionService.save(coinTransaction, profile.get());
                if (!coinTransactionSuccess){
                    throw new AppBadRequestException("Something went wrong");
                }
                return true;
            } else if (dto.getStatus().equals(ReportStatus.REJECTED)) {
                wasteReport.setReportStatus(dto.getStatus());
                wasteReportRepository.save(wasteReport);
                return true;
            } else if (dto.getStatus().equals(ReportStatus.IN_PROGRESS)) {
                wasteReport.setReportStatus(dto.getStatus());
                wasteReportRepository.save(wasteReport);
                return true;
            }
        }
        throw new AppBadRequestException("Report not found");
    }
}
