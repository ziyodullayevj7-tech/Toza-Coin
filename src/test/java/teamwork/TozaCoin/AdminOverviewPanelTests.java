package teamwork.TozaCoin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.progressbar.ProgressBar;
import org.junit.jupiter.api.Test;
import teamwork.service.AdminOverviewService;
import teamwork.view.layout.admin.AdminOverviewPanel;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminOverviewPanelTests {
    private final AdminOverviewService service = mock(AdminOverviewService.class);

    @Test
    void emptyPlatformShowsDeterminateZeroProgress() {
        when(service.load()).thenReturn(new AdminOverviewService.Summary(0, 0, 0, 0, 0, 0));
        var panel = new AdminOverviewPanel(service);
        var bars = descendants(panel).filter(ProgressBar.class::isInstance)
                .map(ProgressBar.class::cast).toList();
        assertEquals(4, bars.size());
        assertTrue(bars.stream().allMatch(bar -> bar.getValue() == 0 && !bar.isIndeterminate()));
    }

    @Test
    void reportDistributionUsesProportionsOfAllReportStatuses() {
        when(service.load()).thenReturn(new AdminOverviewService.Summary(12, 2, 3, 4, 1, 0));
        var panel = new AdminOverviewPanel(service);
        assertEquals(java.util.List.of(.2, .3, .4, .1), descendants(panel)
                .filter(ProgressBar.class::isInstance).map(ProgressBar.class::cast)
                .map(ProgressBar::getValue).toList());
    }

    @Test
    void unavailableDataDoesNotPretendToBeZeroAndCanBeRetried() {
        when(service.load()).thenThrow(new IllegalStateException("Unavailable"))
                .thenReturn(new AdminOverviewService.Summary(1, 1, 0, 0, 0, 0));
        var panel = new AdminOverviewPanel(service);
        assertTrue(descendants(panel).noneMatch(ProgressBar.class::isInstance));
        assertTrue(descendants(panel).anyMatch(component ->
                "alert".equals(component.getElement().getAttribute("role"))));
        Button retry = descendants(panel).filter(Button.class::isInstance).map(Button.class::cast)
                .findFirst().orElseThrow();
        retry.click();
        assertEquals(4, descendants(panel).filter(ProgressBar.class::isInstance).count());
        assertTrue(descendants(panel).noneMatch(component ->
                "alert".equals(component.getElement().getAttribute("role"))));
    }

    private Stream<Component> descendants(Component parent) {
        return parent.getChildren().flatMap(child -> Stream.concat(Stream.of(child), descendants(child)));
    }
}
