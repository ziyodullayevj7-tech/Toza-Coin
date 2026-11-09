package teamwork.TozaCoin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import teamwork.dto.coinTransaction.CoinTransactionDto;
import teamwork.dto.wasteReport.ConfirmWasteDto;
import teamwork.entity.ProfileEntity;
import teamwork.entity.WasteReportEntity;
import teamwork.enums.*;
import teamwork.exceptions.AppBadRequestException;
import teamwork.repository.WasteReportRepository;
import teamwork.service.*;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminRewardTests {
    private final WasteReportRepository reports = mock(WasteReportRepository.class);
    private final AuthService auth = mock(AuthService.class);
    private final CoinTransactionService coins = mock(CoinTransactionService.class);
    private final AdminService service = new AdminService();
    private final WasteReportEntity report = new WasteReportEntity();

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(service, "wasteReportRepository", reports);
        ReflectionTestUtils.setField(service, "authService", auth);
        ReflectionTestUtils.setField(service, "coinTransactionService", coins);
        ProfileEntity reporter = new ProfileEntity();
        reporter.setId("reporter");
        report.setId("report");
        report.setReporter(reporter);
        report.setReportAction(ReportActionEnum.SEND_TO_OTHERS);
        report.setReportStatus(ReportStatus.PENDING);
        when(reports.findByIdForUpdate("report")).thenReturn(Optional.of(report));
        when(auth.creditAiReviewReward(anyString(), anyInt())).thenReturn(true);
        when(coins.save(any(), any())).thenReturn(true);
    }

    @Test
    void approvalCreditsReporterAndRecordsEarnTransaction() {
        assertTrue(service.confirmReport(update(ReportStatus.RESOLVED)));
        verify(auth).creditAiReviewReward("reporter", 15);
        ArgumentCaptor<CoinTransactionDto> transaction = ArgumentCaptor.forClass(CoinTransactionDto.class);
        verify(coins).save(transaction.capture(), same(report.getReporter()));
        assertEquals(15, transaction.getValue().getAmount());
        assertEquals(CoinType.EARN, transaction.getValue().getCoinType());
        assertTrue(transaction.getValue().getDescription().contains("report"));
        assertTrue(report.getAiRewardGranted());
        assertEquals(ReportStatus.IN_PROGRESS, report.getReportStatus());
        assertEquals(50, report.getCleanerRewardCoins());
    }

    @Test
    void selfCleanupCreditsFiftyOnce() {
        report.setReportAction(ReportActionEnum.CLEAN_MYSELF);
        report.setAfterImageUrl("after.jpg");
        service.confirmReport(update(ReportStatus.RESOLVED));
        verify(auth).creditAiReviewReward("reporter", 50);
        assertThrows(AppBadRequestException.class, () -> service.confirmReport(update(ReportStatus.RESOLVED)));
        verify(coins, times(1)).save(any(), any());
    }

    @Test
    void alreadyPaidAiRewardIsNotRepeated() {
        report.setAiRewardGranted(true);
        service.confirmReport(update(ReportStatus.RESOLVED));
        verifyNoInteractions(auth, coins);
    }

    @Test
    void communityCleanerGetsSeparateReward() {
        report.setAiRewardGranted(true);
        ProfileEntity cleaner = new ProfileEntity();
        cleaner.setId("cleaner");
        report.setCleaner(cleaner);
        report.setAfterImageUrl("after.jpg");
        service.confirmReport(update(ReportStatus.RESOLVED));
        verify(auth).creditAiReviewReward("cleaner", 50);
        verify(auth, never()).creditAiReviewReward(eq("reporter"), anyInt());
        verify(coins).save(any(), same(cleaner));
        assertEquals(50, report.getCleanerRewardCoins());
    }

    @Test
    void rejectionAndReviewDoNotPayRewards() {
        report.setReportAction(ReportActionEnum.CLEAN_MYSELF);
        service.confirmReport(update(ReportStatus.IN_PROGRESS));
        service.confirmReport(update(ReportStatus.REJECTED));
        verifyNoInteractions(auth, coins);
    }

    @Test
    void balanceFailurePreventsResolutionAndLedgerWrite() {
        when(auth.creditAiReviewReward(anyString(), anyInt())).thenReturn(false);
        assertThrows(AppBadRequestException.class, () -> service.confirmReport(update(ReportStatus.RESOLVED)));
        assertEquals(ReportStatus.PENDING, report.getReportStatus());
        verifyNoInteractions(coins);
        verify(reports, never()).save(any());
    }

    @Test
    void ledgerFailurePreventsResolution() {
        when(coins.save(any(), any())).thenReturn(false);
        assertThrows(AppBadRequestException.class, () -> service.confirmReport(update(ReportStatus.RESOLVED)));
        assertEquals(ReportStatus.PENDING, report.getReportStatus());
        verify(reports, never()).save(any());
    }

    @Test
    void reportOnlyApprovalPublishesOnceAndDoesNotResolveTheWaste() {
        service.confirmReport(update(ReportStatus.IN_PROGRESS));
        assertEquals(ReportStatus.IN_PROGRESS, report.getReportStatus());
        assertNull(report.getCleaner());
        assertEquals(50, report.getCleanerRewardCoins());
        assertThrows(AppBadRequestException.class, () -> service.confirmReport(update(ReportStatus.IN_PROGRESS)));
        verify(auth, times(1)).creditAiReviewReward("reporter", 15);
        verify(coins, times(1)).save(any(), any());
    }

    private ConfirmWasteDto update(ReportStatus status) {
        ConfirmWasteDto dto = new ConfirmWasteDto();
        dto.setReportId("report");
        dto.setStatus(status);
        return dto;
    }
}
