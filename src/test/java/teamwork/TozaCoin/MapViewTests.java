package teamwork.TozaCoin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import org.junit.jupiter.api.Test;
import teamwork.dto.liveWasteMap.LiveWasteMapDto;
import teamwork.dto.location.LocationDto;
import teamwork.enums.WasteTypeEnum;
import teamwork.service.MapService;
import teamwork.view.user.MapView;
import java.util.List;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MapViewTests {
    private final MapService service = mock(MapService.class);

    @Test
    void filtersUpdateVisibleCountAndResetRestoresAllLocations() {
        when(service.getLiveWasteForMap()).thenReturn(List.of(point("Chilonzor", WasteTypeEnum.PLASTIC),
                point("Yunusobod", WasteTypeEnum.WATER)));
        var view = new MapView(service, mock(teamwork.service.WasteReportService.class), mock(teamwork.service.AttachService.class), mock(teamwork.service.MyReportsService.class));
        assertEquals("2 ta muammo", byId(view, "map-total-badge").getElement().getText());
        assertFalse(((Button) byId(view, "map-reset-filters")).isEnabled());
        combo(view, "map-district-filter").setValue("Chilonzor");
        assertEquals("1 ta muammo", byId(view, "map-total-badge").getElement().getText());
        combo(view, "map-category-filter").setValue("WATER");
        assertEquals("0 ta muammo", byId(view, "map-total-badge").getElement().getText());
        assertTrue(byId(view, "map-load-feedback").isVisible());
        ((Button) byId(view, "map-reset-filters")).click();
        assertEquals("Barchasi", combo(view, "map-district-filter").getValue());
        assertEquals("2 ta muammo", byId(view, "map-total-badge").getElement().getText());
        assertFalse(byId(view, "map-load-feedback").isVisible());
        assertFalse(combo(view, "map-category-filter").isClearButtonVisible());
    }

    @Test
    void refreshRebuildsDistrictChoicesAndDropsUnavailableSelection() {
        when(service.getLiveWasteForMap()).thenReturn(List.of(point("Chilonzor", WasteTypeEnum.PLASTIC)))
                .thenReturn(List.of(point("Yunusobod", WasteTypeEnum.PLASTIC)));
        var view = new MapView(service, mock(teamwork.service.WasteReportService.class), mock(teamwork.service.AttachService.class), mock(teamwork.service.MyReportsService.class));
        combo(view, "map-district-filter").setValue("Chilonzor");
        ((Button) byId(view, "map-refresh")).click();
        assertEquals("Barchasi", combo(view, "map-district-filter").getValue());
        assertEquals(List.of("Barchasi", "Yunusobod"), combo(view, "map-district-filter").getListDataView().getItems().toList());
        assertTrue(((Button) byId(view, "map-refresh")).isEnabled());
    }

    @Test
    void dataFailureIsDistinctFromEmptyMapAndRefreshRecovers() {
        when(service.getLiveWasteForMap()).thenThrow(new IllegalStateException("Unavailable"))
                .thenReturn(List.of(point("Chilonzor", WasteTypeEnum.PLASTIC)));
        var view = new MapView(service, mock(teamwork.service.WasteReportService.class), mock(teamwork.service.AttachService.class), mock(teamwork.service.MyReportsService.class));
        assertEquals("alert", byId(view, "map-load-feedback").getElement().getAttribute("role"));
        assertNotEquals("0 ta muammo", byId(view, "map-total-badge").getElement().getText());
        ((Button) byId(view, "map-refresh")).click();
        assertFalse(byId(view, "map-load-feedback").isVisible());
        assertEquals("1 ta muammo", byId(view, "map-total-badge").getElement().getText());
    }

    @Test
    void invalidCoordinatesAreExcludedFromCountAndDistricts() {
        var invalid = point("Invalid", WasteTypeEnum.PLASTIC);
        invalid.getLocation().setLatitude(Double.NaN);
        when(service.getLiveWasteForMap()).thenReturn(List.of(invalid, point("Chilonzor", WasteTypeEnum.PLASTIC)));
        var view = new MapView(service, mock(teamwork.service.WasteReportService.class), mock(teamwork.service.AttachService.class), mock(teamwork.service.MyReportsService.class));
        assertEquals("1 ta muammo", byId(view, "map-total-badge").getElement().getText());
        assertFalse(combo(view, "map-district-filter").getListDataView().getItems().toList().contains("Invalid"));
    }

    @Test
    void reloadRestoresOnlyAnUnsubmittedTaskFromTheCurrentUsersWork() {
        var work=mock(teamwork.service.MyReportsService.class);
        when(service.getLiveWasteForMap()).thenReturn(List.of());
        var item=new teamwork.dto.myReport.MyReportResponseDto();item.setId("report");
        item.setReportStatus(teamwork.enums.ReportStatus.IN_PROGRESS);
        when(work.getMyWork(0,50,false)).thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(item)));
        var view=new MapView(service,mock(teamwork.service.WasteReportService.class),mock(teamwork.service.AttachService.class),work);
        var draft=new teamwork.view.user.CommunityCleanupTask("report","Plastik","Chilonzor","before.jpg",41.3,69.2,"reporter",50,System.currentTimeMillis()-60_000,null);
        try(var security=mockStatic(teamwork.util.SecurityUtils.class)) {
            security.when(teamwork.util.SecurityUtils::getCurrentUserId).thenReturn("cleaner");
            org.springframework.test.util.ReflectionTestUtils.invokeMethod(view,"restoreTasks",new tools.jackson.databind.json.JsonMapper().writeValueAsString(List.of(draft)));
            assertTrue(byId(view,"map-active-cleanups").isVisible());
            item.setReportStatus(teamwork.enums.ReportStatus.PENDING);
            org.springframework.test.util.ReflectionTestUtils.invokeMethod(view,"restoreTasks",new tools.jackson.databind.json.JsonMapper().writeValueAsString(List.of(draft)));
            assertFalse(byId(view,"map-active-cleanups").isVisible());
        }
    }

    @Test
    void markerPayloadIncludesReportIdAndSafelyEscapesDistrictText() {
        var point=point("District with a newline\n and quote\"",WasteTypeEnum.PLASTIC);point.setId("report");
        when(service.getLiveWasteForMap()).thenReturn(List.of(point));
        var view=new MapView(service,mock(teamwork.service.WasteReportService.class),mock(teamwork.service.AttachService.class),mock(teamwork.service.MyReportsService.class));
        String json=org.springframework.test.util.ReflectionTestUtils.invokeMethod(view,"buildMarkersJson",List.of(point));
        var parsed=new tools.jackson.databind.json.JsonMapper().readTree(json);
        assertEquals("report",parsed.get(0).get("id").asString());
        assertEquals(point.getLocation().getDistrict(),parsed.get(0).get("dist").asString());
    }

    private LiveWasteMapDto point(String district, WasteTypeEnum type) {
        var location = new LocationDto();
        location.setDistrict(district);
        location.setLatitude(41.3);
        location.setLongitude(69.2);
        var point = new LiveWasteMapDto();
        point.setLocation(location);
        point.setWasteType(type);
        return point;
    }
    @SuppressWarnings("unchecked")
    private ComboBox<String> combo(Component root, String id) { return (ComboBox<String>) byId(root, id); }
    private Component byId(Component root, String id) {
        return descendants(root).filter(c -> c.getId().orElse("").equals(id)).findFirst().orElseThrow();
    }
    private Stream<Component> descendants(Component root) {
        return root.getChildren().flatMap(c -> Stream.concat(Stream.of(c), descendants(c)));
    }
}
