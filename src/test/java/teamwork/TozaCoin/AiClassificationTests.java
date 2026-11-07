package teamwork.TozaCoin;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import teamwork.dto.ai.AiAnaliseResultDto;
import teamwork.dto.wasteReport.WasteReportDto;
import teamwork.entity.LocationEntity;
import teamwork.entity.WasteReportEntity;
import teamwork.enums.*;
import teamwork.repository.WasteReportRepository;
import teamwork.service.AiReviewPersistenceService;
import teamwork.service.AuthService;
import teamwork.service.CoinTransactionService;
import teamwork.service.LocationService;
import teamwork.service.WasteReportService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AiClassificationTests {
    private final WasteReportRepository reports = mock(WasteReportRepository.class);
    private final AuthService auth = mock(AuthService.class);
    private final CoinTransactionService coins = mock(CoinTransactionService.class);
    private final AiReviewPersistenceService service = new AiReviewPersistenceService(reports, auth, coins);

    @Test
    void creationOverridesClientClassificationWithReviewDefaults() {
        WasteReportService creation = new WasteReportService();
        LocationService locations = mock(LocationService.class);
        ReflectionTestUtils.setField(creation, "wasteReportRepository", reports);
        ReflectionTestUtils.setField(creation, "locationService", locations);
        ReflectionTestUtils.setField(creation, "authService", auth);
        ReflectionTestUtils.setField(creation, "eventPublisher", mock(ApplicationEventPublisher.class));
        when(locations.saveForWasteReport(any())).thenReturn(Optional.of(new LocationEntity()));
        when(reports.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        WasteReportDto input = new WasteReportDto();
        input.setBeforeImageUrl("before.jpg");
        input.setReportAction(ReportActionEnum.SEND_TO_OTHERS);
        input.setWasteType(WasteTypeEnum.PLASTIC);
        input.setSeverityLevel(SeverityLevelEnum.HIGH);

        assertTrue(creation.create(input));

        ArgumentCaptor<WasteReportEntity> saved = ArgumentCaptor.forClass(WasteReportEntity.class);
        verify(reports).save(saved.capture());
        assertEquals(WasteTypeEnum.OTHER, saved.getValue().getCategory());
        assertEquals(SeverityLevelEnum.UNDER_REVIEW, saved.getValue().getSeverity());
        assertEquals(AiReviewStatus.PENDING, saved.getValue().getAiReviewStatus());
    }

    @Test
    void completedReviewReplacesDefaultsWithImageClassification() {
        WasteReportEntity report = pendingReport();
        AiAnaliseResultDto result = analysis();
        result.setWasteType(WasteTypeEnum.ELECTRONIC);
        result.setSeverityLevel(SeverityLevelEnum.HIGH);

        service.saveSuccessfulReview("report", result);

        assertEquals(WasteTypeEnum.ELECTRONIC, report.getCategory());
        assertEquals(SeverityLevelEnum.HIGH, report.getSeverity());
        assertEquals(AiReviewStatus.COMPLETED, report.getAiReviewStatus());
        verify(reports).save(report);
        verifyNoInteractions(auth, coins);
    }

    @Test
    void failedReviewPreservesUnreviewedDefaults() {
        WasteReportEntity report = pendingReport();
        service.saveFailedReview("report");
        assertEquals(WasteTypeEnum.OTHER, report.getCategory());
        assertEquals(SeverityLevelEnum.UNDER_REVIEW, report.getSeverity());
        assertEquals(AiReviewStatus.FAILED, report.getAiReviewStatus());
        verifyNoInteractions(auth, coins);
    }

    @Test
    void incompleteClassificationCannotCompleteReview() {
        WasteReportEntity report = pendingReport();
        AiAnaliseResultDto result = analysis();
        result.setSeverityLevel(null);
        assertThrows(IllegalArgumentException.class, () -> service.saveSuccessfulReview("report", result));
        assertEquals(AiReviewStatus.PENDING, report.getAiReviewStatus());
        verify(reports, never()).save(any());
    }

    @Test
    void uncertainClassificationCanRemainUnderReviewAndDuplicateReviewIsIgnored() {
        WasteReportEntity report = pendingReport();
        service.saveSuccessfulReview("report", analysis());
        assertEquals(WasteTypeEnum.OTHER, report.getCategory());
        assertEquals(SeverityLevelEnum.UNDER_REVIEW, report.getSeverity());
        AiAnaliseResultDto duplicate = analysis();
        duplicate.setWasteType(WasteTypeEnum.PLASTIC);
        duplicate.setSeverityLevel(SeverityLevelEnum.LOW);
        service.saveSuccessfulReview("report", duplicate);
        assertEquals(WasteTypeEnum.OTHER, report.getCategory());
        verify(reports, times(1)).save(report);
    }

    private WasteReportEntity pendingReport() {
        WasteReportEntity report = new WasteReportEntity();
        report.setCategory(WasteTypeEnum.OTHER);
        report.setSeverity(SeverityLevelEnum.UNDER_REVIEW);
        report.setReportAction(ReportActionEnum.SEND_TO_OTHERS);
        report.setAiReviewStatus(AiReviewStatus.PENDING);
        when(reports.findByIdForUpdate("report")).thenReturn(Optional.of(report));
        return report;
    }

    private AiAnaliseResultDto analysis() {
        AiAnaliseResultDto result = new AiAnaliseResultDto();
        result.setWastePresent(false);
        result.setConfidenceScore(60);
        result.setWasteType(WasteTypeEnum.OTHER);
        result.setSeverityLevel(SeverityLevelEnum.UNDER_REVIEW);
        return result;
    }
}
