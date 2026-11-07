package teamwork.TozaCoin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import teamwork.dto.leaderBoard.LeaderBoardDto;
import teamwork.service.AdminService;
import teamwork.view.admin.AdminStatisticsView;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminStatisticsViewTests {
    private final AdminService service = mock(AdminService.class);

    @Test
    void paginationUsesBackendRankingAndKeepsOverallLeaders() {
        var first = page(0, 21, 10);
        var second = page(1, 21, 10);
        when(service.getUsers(0, 10)).thenReturn(first);
        when(service.getUsers(1, 10)).thenReturn(second);
        var view = new AdminStatisticsView(service);

        button(view, "statistics-next-page").click();

        assertEquals(second.getContent(), grid(view).getListDataView().getItems().toList());
        Component leaders = byId(view, "statistics-leaders");
        assertEquals(3, leaders.getChildren().count());
        assertTrue(descendants(leaders).filter(Span.class::isInstance).map(Span.class::cast)
                .anyMatch(span -> "User 1".equals(span.getText())));
        assertTrue(button(view, "statistics-previous-page").isEnabled());
        verify(service).getUsers(1, 10);
    }

    @Test
    void emptyDataHasNoLeadersOrEnabledPageActions() {
        when(service.getUsers(0, 10)).thenReturn(Page.empty(PageRequest.of(0, 10)));
        var view = new AdminStatisticsView(service);
        assertFalse(grid(view).isVisible());
        assertFalse(byId(view, "statistics-leaders").isVisible());
        assertFalse(button(view, "statistics-previous-page").isEnabled());
        assertFalse(button(view, "statistics-next-page").isEnabled());
    }

    @Test
    void loadFailureHidesStaleDataAndRefreshRecovers() {
        var first = page(0, 1, 1);
        when(service.getUsers(0, 10)).thenReturn(first)
                .thenThrow(new IllegalStateException("Unavailable")).thenReturn(first);
        var view = new AdminStatisticsView(service);

        button(view, "statistics-refresh").click();

        assertEquals(0, grid(view).getListDataView().getItems().count());
        assertFalse(grid(view).getParent().orElseThrow().isVisible());
        assertTrue(descendants(view).anyMatch(component ->
                "alert".equals(component.getElement().getAttribute("role")) && component.isVisible()));

        button(view, "statistics-refresh").click();

        assertTrue(grid(view).getParent().orElseThrow().isVisible());
        assertEquals(first.getContent(), grid(view).getListDataView().getItems().toList());
    }

    @Test
    void missingOptionalProfileFieldsDoNotBreakStatistics() {
        var user = new LeaderBoardDto();
        when(service.getUsers(0, 10)).thenReturn(new PageImpl<>(List.of(user)));
        var view = assertDoesNotThrow(() -> new AdminStatisticsView(service));
        assertEquals(List.of(user), grid(view).getListDataView().getItems().toList());
        assertEquals(1, byId(view, "statistics-leaders").getChildren().count());
    }

    private Page<LeaderBoardDto> page(int page, int total, int size) {
        var users = IntStream.range(0, size).mapToObj(index -> {
            var user = new LeaderBoardDto();
            int rank = page * 10 + index + 1;
            user.setProfileId("user-" + rank);
            user.setFullName("User " + rank);
            user.setTotalEarnedCoins(1000 - rank);
            return user;
        }).toList();
        return new PageImpl<>(users, PageRequest.of(page, 10), total);
    }

    @SuppressWarnings("unchecked")
    private Grid<LeaderBoardDto> grid(Component view) {
        return (Grid<LeaderBoardDto>) byId(view, "statistics-users-grid");
    }

    private Button button(Component view, String id) { return (Button) byId(view, id); }

    private Component byId(Component view, String id) {
        return descendants(view).filter(component -> id.equals(component.getId().orElse(null)))
                .findFirst().orElseThrow();
    }

    private Stream<Component> descendants(Component parent) {
        return parent.getChildren().flatMap(child -> Stream.concat(Stream.of(child), descendants(child)));
    }
}
