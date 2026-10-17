package teamwork.view.user;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
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
import teamwork.dto.liveWasteMap.LiveWasteMapDto;
import teamwork.enums.SeverityLevelEnum;
import teamwork.enums.WasteTypeEnum;
import teamwork.service.MapService;
import teamwork.view.layout.user.UserLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Route(value = "user/map", layout = UserLayout.class)
@PageTitle("Jonli Xarita | TozaCoin")
@PermitAll
@CssImport("./themes/tozacoin/user-layout.css")
public class MapView extends VerticalLayout {

    private final MapService mapService;

    // Map container — Leaflet attaches to this div
    private final Div mapDiv = new Div();

    private final Span totalBadgeSpan   = new Span("0 ta muammo");

    private ComboBox<String> districtFilter;
    private ComboBox<String> categoryFilter;

    private List<LiveWasteMapDto> allPoints = new ArrayList<>();

    // -------------------------------------------------------------------------
    public MapView(MapService mapService) {
        this.mapService = mapService;

        setSizeFull();
        setSpacing(false);
        setPadding(false);
        addClassName("map-view-root");

        loadData();

        Div topBar      = buildTopBar();
        Div contentArea = new Div();
        contentArea.addClassName("map-content-area");
        contentArea.add(buildMapArea());

        add(topBar, contentArea);
        setFlexGrow(1, contentArea);
    }

    // =========================================================================
    // DATA
    // =========================================================================
    private void loadData() {
        try {
            allPoints = mapService.getLiveWasteForMap();
        } catch (Exception ex) {
            allPoints = new ArrayList<>();
        }
    }

    // =========================================================================
    // 1. TOP BAR
    // =========================================================================
    private Div buildTopBar() {
        Div bar = new Div();
        bar.addClassName("map-topbar");

        // --- left ---
        Div left = new Div();
        left.addClassName("map-topbar-left");

        Span globe = new Span("🗺️");
        globe.addClassName("map-topbar-globe");

        Div titleGroup = new Div();
        titleGroup.addClassName("map-topbar-title-group");

        Span title = new Span("Jonli Chiqindi Xaritasi");
        title.addClassName("map-topbar-title");

        Span liveBadge = new Span("● JONLI");
        liveBadge.addClassName("map-live-badge");

        titleGroup.add(title, liveBadge);
        left.add(globe, titleGroup);

        // --- right ---
        Div right = new Div();
        right.addClassName("map-topbar-right");

        Button refreshBtn = new Button("⟳ Yangilash");
        refreshBtn.addClassName("map-btn-refresh");
        refreshBtn.addClickListener(e -> {
            loadData();
            applyFilters();
        });

        Button reportBtn = new Button("＋ Muammo Qo'shish");
        reportBtn.addClassNames("map-btn-report");
        reportBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        reportBtn.addClickListener(e -> UI.getCurrent().navigate(ReportWasteView.class));

        right.add(refreshBtn, reportBtn);
        bar.add(left, right);
        return bar;
    }

    // =========================================================================
    // 2. MAP FILTERS
    // =========================================================================
    private Div buildFilterOverlay() {
        Div panel = new Div();
        panel.addClassName("map-filter-overlay");
        panel.add(buildPanelHeader(), buildFiltersSection());
        return panel;
    }

    private Div buildPanelHeader() {
        Div header = new Div();
        header.addClassName("map-panel-header");

        Span pin = new Span("📍");
        pin.addClassName("map-panel-pin");

        Div txt = new Div();
        txt.addClassName("map-panel-txt");

        Span t = new Span("Toshkent Xaritasi");
        t.addClassName("map-panel-title");

        Span s = new Span("Real vaqt rejimida");
        s.addClassName("map-panel-sub");

        txt.add(t, s);
        header.add(pin, txt);
        return header;
    }

    private Div buildFiltersSection() {
        Div section = new Div();
        section.addClassName("map-filters-wrap");

        // District filter — built from live data so only real districts show
        districtFilter = new ComboBox<>("Tuman bo'yicha");
        districtFilter.addClassName("map-combo");
        List<String> districts = new ArrayList<>();
        districts.add("Barchasi");
        allPoints.stream()
                .filter(p -> p.getLocation() != null && p.getLocation().getDistrict() != null)
                .map(p -> p.getLocation().getDistrict())
                .distinct().sorted()
                .forEach(districts::add);
        districtFilter.setItems(districts);
        districtFilter.setValue("Barchasi");
        districtFilter.setClearButtonVisible(true);
        districtFilter.addValueChangeListener(e -> applyFilters());

        // Category filter
        categoryFilter = new ComboBox<>("Chiqindi turi");
        categoryFilter.addClassName("map-combo");
        categoryFilter.setItems(
                "Barchasi", "PLASTIC", "ILLEGAL_DUMP", "WATER", "AIR", "ELECTRONIC", "OTHER"
        );
        categoryFilter.setItemLabelGenerator(this::categoryLabel);
        categoryFilter.setValue("Barchasi");
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.addValueChangeListener(e -> applyFilters());

        section.add(districtFilter, categoryFilter);
        return section;
    }

    // =========================================================================
    // 3. MAP AREA (right)
    // =========================================================================
    private Div buildMapArea() {
        Div area = new Div();
        area.addClassName("map-right-area");

        // Leaflet target div. Sized by the .map-leaflet-div CSS (absolute, fills
        // the parent); a ResizeObserver on the JS side keeps Leaflet in sync.
        mapDiv.setId("leaflet-waste-map");
        mapDiv.addClassName("map-leaflet-div");

        // Total-count badge (top-right overlay)
        Div badge = new Div();
        badge.addClassName("map-count-overlay");
        totalBadgeSpan.setId("map-total-badge");
        totalBadgeSpan.addClassName("map-count-text");
        badge.add(new Span("📍 "), totalBadgeSpan);

        area.add(mapDiv, buildFilterOverlay(), badge);
        return area;
    }

    // =========================================================================
    // 4. FILTER LOGIC
    // =========================================================================
    private void applyFilters() {
        String district = districtFilter != null ? districtFilter.getValue() : null;
        String category = categoryFilter != null ? categoryFilter.getValue() : null;

        List<LiveWasteMapDto> filtered = allPoints.stream()
                .filter(p -> p.getLocation() != null)
                .filter(p -> district == null || "Barchasi".equals(district)
                        || district.equals(p.getLocation().getDistrict()))
                .filter(p -> category == null || "Barchasi".equals(category)
                        || category.equals(p.getWasteType() != null ? p.getWasteType().name() : ""))
                .collect(Collectors.toList());

        // Always remember the latest data in the browser, so if Leaflet is not
        // ready yet the init step will pick this up instead of losing the update.
        UI.getCurrent().getPage().executeJs(
                "window._wasteJson = $0; if (window.wasteMap) window.wasteMap.renderMarkers($0);",
                buildMarkersJson(filtered)
        );

    }

    // =========================================================================
    // 5. LEAFLET INIT  (runs after component is attached to DOM)
    // =========================================================================
    @Override
    protected void onAttach(AttachEvent event) {
        super.onAttach(event);

        String bootstrap = """
            (function (initialJson) {
                window._wasteJson = initialJson;

                // Wait until Leaflet's CSS is really applied (otherwise the map
                // gets created with wrong sizes / broken tiles).
                function loadCss() {
                    return new Promise(function (resolve, reject) {
                        function fallback(link) {
                            link.addEventListener('load', function () {
                                link.dataset.loadState = 'loaded';
                                resolve();
                            }, { once: true });
                            link.addEventListener('error', function () {
                                link.dataset.loadState = 'failed';
                                reject(new Error('Leaflet stylesheet could not be loaded from either CDN'));
                            }, { once: true });
                            link.href = 'https://cdn.jsdelivr.net/npm/leaflet@1.9.4/dist/leaflet.css';
                        }

                        var l = document.getElementById('leaflet-css-link');
                        if (l) {
                            if (l.sheet) return resolve();
                            if (l.dataset.loadState === 'failed') {
                                l.remove();
                                l = null;
                            }
                        }
                        if (l) {
                            l.addEventListener('load', resolve, { once: true });
                            l.addEventListener('error', function () { fallback(l); }, { once: true });
                            return;
                        }
                        l = document.createElement('link');
                        l.id   = 'leaflet-css-link';
                        l.rel  = 'stylesheet';
                        l.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css';
                        l.addEventListener('load', resolve, { once: true });
                        l.addEventListener('error', function () { fallback(l); }, { once: true });
                        document.head.appendChild(l);
                    });
                }

                function loadJs() {
                    return new Promise(function (resolve, reject) {
                        function fallback(script) {
                            script.addEventListener('load', function () {
                                script.dataset.loadState = 'loaded';
                                resolve();
                            }, { once: true });
                            script.addEventListener('error', function () {
                                script.dataset.loadState = 'failed';
                                reject(new Error('Leaflet script could not be loaded from either CDN'));
                            }, { once: true });
                            script.src = 'https://cdn.jsdelivr.net/npm/leaflet@1.9.4/dist/leaflet.js';
                        }

                        if (window.L) return resolve();
                        var s = document.getElementById('leaflet-js-tag');
                        if (s && s.dataset.loadState === 'failed') {
                            s.remove();
                            s = null;
                        }
                        if (!s) {
                            s = document.createElement('script');
                            s.id  = 'leaflet-js-tag';
                            s.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js';
                            s.addEventListener('load', function () {
                                s.dataset.loadState = 'loaded';
                                resolve();
                            }, { once: true });
                            s.addEventListener('error', function () { fallback(s); }, { once: true });
                            document.head.appendChild(s);
                            return;
                        }
                        if (s.dataset.loadState === 'loaded') return resolve();
                        s.addEventListener('load', resolve, { once: true });
                        s.addEventListener('error', function () { fallback(s); }, { once: true });
                    });
                }

                function esc(v) {
                    return String(v == null ? '' : v).replace(/[&<>"']/g, function (c) {
                        return {'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'}[c];
                    });
                }

                function init() {
                    var el = document.getElementById('leaflet-waste-map');
                    if (!el) return;

                    // Properly destroy any previous instance (navigating back to the page)
                    if (window.wasteMap && window.wasteMap.destroy) window.wasteMap.destroy();

                    var map = L.map(el, {
                        center:      [41.2995, 69.2401],
                        zoom:        12,
                        zoomControl: false
                    });

                    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                        attribution: '&copy; <a href="https://openstreetmap.org">OpenStreetMap</a>',
                        maxZoom: 19
                    }).addTo(map);

                    L.control.zoom({ position: 'bottomright' }).addTo(map);

                    var layer = L.layerGroup().addTo(map);

                    // Keep the map in sync with any layout change (sidebar collapse, window resize)
                    var ro = window.ResizeObserver
                        ? new ResizeObserver(function () { map.invalidateSize(); })
                        : null;
                    var resizeHandler = function () { map.invalidateSize(); };
                    if (ro) ro.observe(el);
                    else window.addEventListener('resize', resizeHandler);

                    function render(json) {
                        layer.clearLayers();
                        var pts = typeof json === 'string' ? JSON.parse(json) : json;
                        var markerLocations = [];
                        pts.forEach(function (p) {
                            if (p.lat == null || p.lng == null) return;
                            markerLocations.push([p.lat, p.lng]);

                            var color  = p.sev === 'HIGH'   ? '#ef4444'
                                       : p.sev === 'MEDIUM' ? '#f97316'
                                       : '#22c55e';
                            var radius = p.sev === 'HIGH'   ? 12
                                       : p.sev === 'MEDIUM' ? 10 : 8;

                            L.circleMarker([p.lat, p.lng], {
                                radius:      radius,
                                fillColor:   color,
                                color:       '#ffffff',
                                weight:      2.5,
                                opacity:     1,
                                fillOpacity: 0.88
                            }).addTo(layer).bindTooltip(
                                '<div class="wmt-wrap">' +
                                    '<span class="wmt-title">' + esc(p.label || 'Chiqindi') + '</span>' +
                                    '<span class="wmt-loc">📍 ' + esc(p.dist || "Noma'lum") + '</span>' +
                                    (p.coins ? '<span class="wmt-coins">🪙 +' + esc(p.coins) + ' tanga</span>' : '') +
                                '</div>',
                                { direction: 'top', offset: [0, -10], opacity: 1, className: 'wm-tooltip' }
                            );
                        });

                        var badge = document.getElementById('map-total-badge');
                        if (badge) badge.textContent = pts.length + ' ta muammo';
                        if (markerLocations.length) {
                            var bounds = L.latLngBounds(markerLocations);
                            if (!map.getBounds().contains(bounds)) {
                                map.fitBounds(bounds.pad(0.15), { maxZoom: 12 });
                            }
                        }
                    }

                    window.wasteMap = {
                        renderMarkers: render,
                        destroy: function () {
                            if (ro) ro.disconnect();
                            else window.removeEventListener('resize', resizeHandler);
                            try { map.remove(); } catch (e) {}
                            window.wasteMap = null;
                        }
                    };

                    render(window._wasteJson);
                    requestAnimationFrame(function () { map.invalidateSize(true); });
                }

                Promise.all([loadCss(), loadJs()]).then(init).catch(function (e) {
                    console.error('TozaCoin map: failed to load Leaflet', e);
                });
            })($0);
        """;

        event.getUI().getPage().executeJs(bootstrap, buildMarkersJson(allPoints));
    }

    @Override
    protected void onDetach(DetachEvent event) {
        event.getUI().getPage().executeJs(
                "if (window.wasteMap && window.wasteMap.destroy) window.wasteMap.destroy();");
        super.onDetach(event);
    }

    // =========================================================================
    // 6. HELPERS
    // =========================================================================
    private String buildMarkersJson(List<LiveWasteMapDto> pts) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (LiveWasteMapDto p : pts) {
            if (p.getLocation() == null) continue;
            double lat = p.getLocation().getLatitude();
            double lng = p.getLocation().getLongitude();
            if (!Double.isFinite(lat) || !Double.isFinite(lng)
                    || lat < -90 || lat > 90 || lng < -180 || lng > 180
                    || lat == 0 && lng == 0) continue;

            if (!first) sb.append(",");
            first = false;

            String sev   = p.getSeverityLevel() != null ? p.getSeverityLevel().name() : "LOW";
            String label = jsonEsc(wasteLabel(p.getWasteType()));
            String dist  = jsonEsc(p.getLocation().getDistrict());
            int coins    = p.getRewardCoins() != null ? p.getRewardCoins() : 0;

            sb.append(String.format(Locale.US,
                    "{\"lat\":%f,\"lng\":%f,\"sev\":\"%s\",\"label\":\"%s\",\"dist\":\"%s\",\"coins\":%d}",
                    lat, lng, sev, label, dist, coins));
        }
        sb.append("]");
        return sb.toString();
    }

    /** Valid JSON string escaping (the old code produced the illegal escape \' ). */
    private static String jsonEsc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String wasteLabel(WasteTypeEnum t) {
        if (t == null) return "Boshqa";
        return switch (t) {
            case PLASTIC      -> "Plastik";
            case ILLEGAL_DUMP -> "Noqonuniy tashlama";
            case WATER        -> "Suv ifloslanishi";
            case AIR          -> "Havo ifloslanishi";
            case ELECTRONIC   -> "Elektron chiqindi";
            case OTHER        -> "Boshqa";
        };
    }



    private String categoryLabel(String raw) {
        return switch (raw) {
            case "PLASTIC"      -> "🛢️ Plastik";
            case "ILLEGAL_DUMP" -> "🗑️ Noqonuniy tashlama";
            case "WATER"        -> "💧 Suv ifloslanishi";
            case "AIR"          -> "💨 Havo ifloslanishi";
            case "ELECTRONIC"   -> "💻 Elektron chiqindi";
            case "OTHER"        -> "♻️ Boshqa";
            default             -> raw;
        };
    }
}