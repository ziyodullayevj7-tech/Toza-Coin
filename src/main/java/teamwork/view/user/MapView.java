package teamwork.view.user;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import teamwork.view.layout.user.UserLayout;

import java.util.List;

@Route(value = "user/map", layout = UserLayout.class)
@PageTitle("Interaktiv Xarita | TozaCoin")
@PermitAll
@CssImport("./themes/tozacoin/user-layout.css")
public class MapView extends VerticalLayout {

    // Ready for future Map Service & Repository Binding
    public record MapPointDto(
            String id,
            String title,
            String locationName,
            double lat,
            double lng,
            String category,
            String status, // ACTIVE, IN_PROGRESS, RESOLVED
            int rewardCoins,
            String reportedTime,
            String reporterName
    ) {}

    private final Div mapContainer = new Div();
    private final Div selectedPointCard = new Div();
    private final Span totalCountBadge = new Span("335 ta muammo");

    public MapView() {
        setWidthFull();
        setSpacing(false);
        setPadding(false);
        addClassName("map-view-wrapper");

        // 1. Top Interactive Control Bar
        add(buildControlBar());

        // 2. Main Full-Screen Map Area with Side Panel Overlay
        add(buildMainMapContainer());
    }

    /* ------------------------------------------------------------------------
     * 1. Top Control Bar (Filter tabs, district selector, action button)
     * ------------------------------------------------------------------------ */
    private Div buildControlBar() {
        Div bar = new Div();
        bar.addClassName("map-control-bar");

        // Left Filters Group
        Div leftFilters = new Div();
        leftFilters.addClassName("map-filters-left");

        // Status Tabs
        Div filterTabs = new Div();
        filterTabs.addClassName("map-filter-tabs");

        Button tabAll = createFilterTab("Barchasi (335)", true);
        Button tabActive = createFilterTab("🔴 Faol (89)", false);
        Button tabProgress = createFilterTab("🟡 Tozalanmoqda (34)", false);
        Button tabResolved = createFilterTab("🟢 Hal qilindi (212)", false);

        filterTabs.add(tabAll, tabActive, tabProgress, tabResolved);

        // District Combo
        ComboBox<String> districtCombo = new ComboBox<>();
        districtCombo.setPlaceholder("Tuman bo'yicha filter");
        districtCombo.setItems("Barcha tumanlar", "Yunusobod", "Mirzo Ulug'bek", "Chilonzor", "Sergeli", "Chirchiq");
        districtCombo.setValue("Barcha tumanlar");
        districtCombo.addClassName("map-combo-filter");

        // Category Combo
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.setPlaceholder("Chiqindi turi");
        categoryCombo.setItems("Barcha turlari", "Plastik", "Shisha", "Qog'oz", "Maishiy");
        categoryCombo.setValue("Barcha turlari");
        categoryCombo.addClassName("map-combo-filter");

        leftFilters.add(filterTabs, districtCombo, categoryCombo);

        // Right Action Group
        Div rightActions = new Div();
        rightActions.addClassName("map-actions-right");

        Button btnReportNew = new Button("+ Muammo xabar qilish");
        btnReportNew.addClassName("btn-report-map-action");
        btnReportNew.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnReportNew.addClickListener(e -> UI.getCurrent().navigate(ReportWasteView.class));

        rightActions.add(btnReportNew);

        bar.add(leftFilters, rightActions);
        return bar;
    }

    private Button createFilterTab(String label, boolean isActive) {
        Button btn = new Button(label);
        btn.addClassName("map-tab-btn");
        if (isActive) {
            btn.addClassName("active");
        }
        return btn;
    }

    /* ------------------------------------------------------------------------
     * 2. Main Full-Screen Map Container with Overlays
     * ------------------------------------------------------------------------ */
    private Div buildMainMapContainer() {
        mapContainer.addClassName("map-main-display-area");

        // Embed OpenStreetMap (Tashkent Center: Lat 41.2995, Lng 69.2401)
        IFrame mapIFrame = new IFrame("https://www.openstreetmap.org/export/embed.html?bbox=69.1000%2C41.2000%2C69.4000%2C41.4000&layer=mapnik");
        mapIFrame.addClassName("full-map-iframe");
        mapIFrame.getElement().setAttribute("title", "TozaCoin Full Interactive Map");

        // Top-Left Live Counters Overlay
        Div countersOverlay = new Div();
        countersOverlay.addClassName("full-map-counters-overlay");

        Div pillActive = createOverlayPill("red", "89 ta faol muammo");
        Div pillProgress = createOverlayPill("amber", "34 ta tozalanmoqda");
        Div pillResolved = createOverlayPill("green", "212 ta hal qilindi");

        countersOverlay.add(pillActive, pillProgress, pillResolved);

        // Right Side Interactive Issue Detail Panel
        Div detailPanel = buildSelectedPointPanel(null);

        mapContainer.add(mapIFrame, countersOverlay, detailPanel);
        return mapContainer;
    }

    private Div createOverlayPill(String themeColor, String label) {
        Div pill = new Div();
        pill.addClassName("full-map-pill");

        Span dot = new Span();
        dot.addClassNames("map-status-dot", themeColor);

        Span text = new Span(label);
        text.addClassName("map-pill-label");

        pill.add(dot, text);
        return pill;
    }

    /* ------------------------------------------------------------------------
     * 3. Selected Point Interactive Detail Panel (Service Ready)
     * ------------------------------------------------------------------------ */
    public Div buildSelectedPointPanel(MapPointDto point) {
        if (point == null) {
            point = new MapPointDto(
                    "p-101",
                    "Plastik chiqindi to'plami — Yunusobod",
                    "Yunusobod 4-mavze, Park yaqinida (500m uzoqlikda)",
                    41.3645,
                    69.2872,
                    "Plastik",
                    "ACTIVE",
                    15,
                    "Bugun, 14:23",
                    "Aziz Abdimuratov"
            );
        }

        selectedPointCard.addClassName("map-detail-panel");

        Div panelHeader = new Div();
        panelHeader.addClassName("panel-header-row");

        Span statusBadge = new Span("🔴 Ko'rib chiqilmoqda");
        statusBadge.addClassName("panel-status-pill");

        Span rewardCoins = new Span("🪙 +" + point.rewardCoins() + " tanga mukofot");
        rewardCoins.addClassName("panel-reward-badge");

        panelHeader.add(statusBadge, rewardCoins);

        H2 title = new H2(point.title());
        title.addClassName("panel-issue-title");

        Paragraph location = new Paragraph("📍 " + point.locationName());
        location.addClassName("panel-location-text");

        Div infoRow = new Div();
        infoRow.addClassName("panel-info-row");

        Span reporter = new Span("👤 Xabar beruvchi: " + point.reporterName());
        reporter.addClassName("panel-info-text");

        Span time = new Span("⏰ " + point.reportedTime());
        time.addClassName("panel-info-text");

        infoRow.add(reporter, time);

        // Action Buttons
        Div panelActions = new Div();
        panelActions.addClassName("panel-actions-row");

        Button btnVolunteer = new Button("🧹 Tozalashga ko'ngilli bo'lish");
        btnVolunteer.addClassName("btn-panel-primary");
        btnVolunteer.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnVolunteer.addClickListener(e -> UI.getCurrent().navigate(MyCleanupsView.class));

        Button btnRoute = new Button("📍 Marshrutni olish");
        btnRoute.addClassName("btn-panel-secondary");

        panelActions.add(btnVolunteer, btnRoute);

        selectedPointCard.removeAll();
        selectedPointCard.add(panelHeader, title, location, infoRow, panelActions);
        return selectedPointCard;
    }

    // Public method for loading live map markers from a Service
    public void loadMapData(List<MapPointDto> points) {
        if (points != null && !points.isEmpty()) {
            totalCountBadge.setText(points.size() + " ta muammo");
            buildSelectedPointPanel(points.get(0));
        }
    }
}
