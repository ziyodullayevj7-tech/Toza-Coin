package teamwork.view.layout;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.ArrayList;
import java.util.List;

@Route(value = "map-preview", layout = MainLayout.class)
@AnonymousAllowed
@CssImport("./themes/tozacoin/map-preview.css")
public class MapPreviewSection extends Div {

    private final Span activePillText = new Span("89 ta faol muammo");
    private final Span progressPillText = new Span("34 ta tozalanmoqda");
    private final Span resolvedPillText = new Span("212 ta hal qilindi");

    private final Button btnOpenFullMap = new Button("To'liq xaritani ochish →");
    private final List<TrashMarkerItem> trashMarkers = new ArrayList<>();

    public MapPreviewSection() {
        addClassName("map-preview-wrapper");

        Div container = new Div();
        container.addClassName("map-preview-container");

        // Header Block
        Div headerBlock = new Div();
        headerBlock.addClassName("map-preview-header");

        Span badge = new Span("XARITA");
        badge.addClassName("map-preview-badge");

        H2 title = new H2("Yaqinlashtagi ifloslik nuqtalari");
        title.addClassName("map-preview-title");

        Paragraph subtitle = new Paragraph("Real vaqtda yangilanadigan xaritada shahar bo'ylag barcha xabar qilingan ifloslik joylari ko'rsatiladi.");
        subtitle.addClassName("map-preview-subtitle");

        headerBlock.add(badge, title, subtitle);

        // Interactive Map Frame Container
        Div mapCard = new Div();
        mapCard.addClassName("map-frame-card");

        // OpenStreetMap Interactive Embed (Tashkent Center: lat 41.2995, lon 69.2401)
        IFrame mapIFrame = new IFrame("https://www.openstreetmap.org/export/embed.html?bbox=69.1000%2C41.2000%2C69.4000%2C41.4000&layer=mapnik");
        mapIFrame.addClassName("map-iframe");
        mapIFrame.getElement().setAttribute("title", "TozaCoin Trash Map Preview");
        mapIFrame.getElement().setAttribute("loading", "lazy");

        // Top-Left Legend Overlay
        Div legendOverlay = new Div();
        legendOverlay.addClassName("map-legend-overlay");

        Div pillActive = createLegendPill("red", activePillText);
        Div pillProgress = createLegendPill("yellow", progressPillText);
        Div pillResolved = createLegendPill("green", resolvedPillText);

        legendOverlay.add(pillActive, pillProgress, pillResolved);

        // Bottom-Right Action Overlay
        Div actionOverlay = new Div();
        actionOverlay.addClassName("map-action-overlay");

        btnOpenFullMap.addClassName("btn-open-full-map");
        btnOpenFullMap.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        actionOverlay.add(btnOpenFullMap);

        mapCard.add(mapIFrame, legendOverlay, actionOverlay);

        container.add(headerBlock, mapCard);
        add(container);
    }

    // Public method for updating problem counter legend pills dynamically from a Service
    public void setProblemCounts(int active, int inProgress, int resolved) {
        activePillText.setText(active + " ta faol muammo");
        progressPillText.setText(inProgress + " ta tozalanmoqda");
        resolvedPillText.setText(resolved + " ta hal qilindi");
    }

    // Public method for setting live real-time trash markers in the future
    public void setTrashMarkers(List<TrashMarkerItem> markers) {
        this.trashMarkers.clear();
        if (markers != null) {
            this.trashMarkers.addAll(markers);
        }
    }

    public List<TrashMarkerItem> getTrashMarkers() {
        return trashMarkers;
    }

    // ClickListener support for "To'liq xaritani ochish" button
    public void addOpenFullMapClickListener(ComponentEventListener<ClickEvent<Button>> listener) {
        btnOpenFullMap.addClickListener(listener);
    }

    public Button getOpenFullMapButton() {
        return btnOpenFullMap;
    }

    private Div createLegendPill(String dotColorClass, Span textSpan) {
        Div pill = new Div();
        pill.addClassName("map-legend-pill");

        Span dot = new Span();
        dot.addClassName("status-dot");
        dot.addClassName(dotColorClass);

        pill.add(dot, textSpan);
        return pill;
    }

    // DTO Model for Real-Time Trash Markers
    public static class TrashMarkerItem {
        private String id;
        private double latitude;
        private double longitude;
        private String title;
        private String status; // ACTIVE, IN_PROGRESS, RESOLVED
        private int rewardCoins;

        public TrashMarkerItem() {}

        public TrashMarkerItem(String id, double latitude, double longitude, String title, String status, int rewardCoins) {
            this.id = id;
            this.latitude = latitude;
            this.longitude = longitude;
            this.title = title;
            this.status = status;
            this.rewardCoins = rewardCoins;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }

        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public int getRewardCoins() { return rewardCoins; }
        public void setRewardCoins(int rewardCoins) { this.rewardCoins = rewardCoins; }
    }
}
