package teamwork.TozaCoin;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import org.junit.jupiter.api.*;
import org.springframework.data.domain.Page;
import org.springframework.test.util.ReflectionTestUtils;
import teamwork.dto.wasteReport.ConfirmWasteDto;
import teamwork.enums.*;
import teamwork.service.*;
import teamwork.view.admin.AdminModerationView;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModerationPublicationViewTests {
    private UI ui;
    private final WasteReportService reports = mock(WasteReportService.class);
    private final AdminService admin = mock(AdminService.class);

    @BeforeEach
    void setup() {
        ui = new UI();UI.setCurrent(ui);ui.setLocale(java.util.Locale.forLanguageTag("uz"));
        when(reports.getUnresolvedReports(0, 10)).thenReturn(Page.empty());
    }
    @AfterEach void cleanup() { UI.setCurrent(null); }

    @Test
    void reportOnlyHasPublishActionAndConfirmationCallsBackendWithMapStatus() {
        var dto = new ConfirmWasteDto();dto.setReportId("report");dto.setReportAction(ReportActionEnum.SEND_TO_OTHERS);
        dto.setStatus(ReportStatus.PENDING);dto.setBeforeImageUrl("before.jpg");
        var view = new AdminModerationView(reports, admin);
        Dialog details = ReflectionTestUtils.invokeMethod(view, "openReportDetails", dto);
        var footer = details.getFooter().getElement().getChildren().flatMap(e -> e.getComponent().stream()).toList();
        assertTrue(footer.stream().filter(Button.class::isInstance).map(Button.class::cast)
                .anyMatch(button -> "Xaritaga chiqarish".equals(button.getText())));
        assertFalse(footer.stream().filter(Button.class::isInstance).map(Button.class::cast)
                .anyMatch(button -> "Ko'rib chiqishni boshlash".equals(button.getText())));
        assertEquals(1, descendants(details).filter(com.vaadin.flow.component.html.Image.class::isInstance).count());
        ConfirmDialog confirmation = ReflectionTestUtils.invokeMethod(view, "requestStatusChange", dto, ReportStatus.IN_PROGRESS, details);
        when(admin.confirmReport(any())).thenReturn(true);
        ComponentUtil.fireEvent(confirmation, new ConfirmDialog.ConfirmEvent(confirmation, true));
        verify(admin).confirmReport(argThat(update -> "report".equals(update.getReportId()) && update.getStatus() == ReportStatus.IN_PROGRESS));
        assertFalse(details.isOpened());
    }

    @Test
    void failedAiReportCanBeFilteredAndPublishedManually() {
        var dto=new ConfirmWasteDto();dto.setReportId("manual");dto.setReportAction(ReportActionEnum.SEND_TO_OTHERS);
        dto.setStatus(ReportStatus.PENDING);dto.setAiReviewStatus(AiReviewStatus.FAILED);
        when(reports.getUnresolvedReports(0,10)).thenReturn(new org.springframework.data.domain.PageImpl<>(java.util.List.of(dto)));
        var view=new AdminModerationView(reports,admin);
        @SuppressWarnings("unchecked")
        var filter=(com.vaadin.flow.component.combobox.ComboBox<String>)descendants(view)
                .filter(c->"moderation-task-filter".equals(c.getId().orElse(""))).findFirst().orElseThrow();
        filter.setValue("AI qo'lda tekshirish");
        Dialog details=ReflectionTestUtils.invokeMethod(view,"openReportDetails",dto);
        assertTrue(details.getFooter().getElement().getChildren().flatMap(e->e.getComponent().stream())
                .filter(Button.class::isInstance).map(Button.class::cast).anyMatch(b->"Xaritaga chiqarish".equals(b.getText()) && b.isEnabled()));
        when(admin.confirmReport(any())).thenReturn(true);
        ConfirmDialog confirmation=ReflectionTestUtils.invokeMethod(view,"requestStatusChange",dto,ReportStatus.IN_PROGRESS,details);
        ComponentUtil.fireEvent(confirmation,new ConfirmDialog.ConfirmEvent(confirmation,true));
        verify(admin).confirmReport(argThat(update->update.getStatus()==ReportStatus.IN_PROGRESS));
    }

    private Stream<Component> descendants(Component component) {
        return component.getChildren().flatMap(c -> Stream.concat(Stream.of(c), descendants(c)));
    }
}
