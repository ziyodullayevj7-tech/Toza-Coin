package teamwork.view.user;

import teamwork.i18n.I18n;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import teamwork.dto.liveWasteMap.LiveWasteMapDto;
import teamwork.enums.WasteTypeEnum;
import teamwork.service.MapService;
import teamwork.service.WasteReportService;
import teamwork.service.AttachService;
import teamwork.service.MyReportsService;
import teamwork.enums.ReportStatus;
import teamwork.util.SecurityUtils;
import tools.jackson.databind.json.JsonMapper;
import teamwork.view.layout.user.UserLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Set;

@Route(value = "user/map", layout = UserLayout.class)
@RolesAllowed({"USER", "ADMIN"})
@CssImport("./themes/tozacoin/user-layout.css")
@CssImport("./themes/tozacoin/map-workspace.css")
@CssImport("./themes/tozacoin/community-cleanup.css")
public class MapView extends VerticalLayout implements com.vaadin.flow.router.HasDynamicTitle {

    private static final Logger log = LoggerFactory.getLogger(MapView.class);
    private final MapService mapService;
    private final WasteReportService reports;
    private final AttachService attachments;
    private final MyReportsService myWork;
    private CommunityCleanupDialog selectedDialog;
    private final Div activeTasks = new Div();
    private final Map<String, CommunityCleanupTask> tasks = new LinkedHashMap<>();
    private final Div feedback = new Div();
    private final Button resetFilters = I18n.localize(new Button("Filtrlarni tozalash", VaadinIcon.CLOSE_SMALL.create()));
    private boolean dataError;
    private boolean updatingFilters;

    // Map container — Leaflet attaches to this div
    private final Div mapDiv = I18n.localize(new Div());

    private final Span totalBadgeSpan = I18n.localize(new Span("0 ta muammo"));

    private ComboBox<String> districtFilter;
    private ComboBox<String> categoryFilter;

    private List<LiveWasteMapDto> allPoints = new ArrayList<>();

    public MapView(MapService mapService, WasteReportService reports, AttachService attachments, MyReportsService myWork) {
        this.mapService = mapService;
        this.reports = reports;
        this.attachments = attachments;
        this.myWork = myWork;
        setWidthFull();
        setSpacing(false);
        setPadding(false);
        addClassNames("map-view-root", "map-workspace");
        loadData();
        feedback.setId("map-load-feedback");
        feedback.addClassName("map-workspace-feedback");
        feedback.getElement().setAttribute("role", "status");
        Div contentArea = new Div(buildFiltersSection(), feedback, buildMapArea(), buildLegend());
        contentArea.addClassName("map-content-area");
        activeTasks.addClassName("map-active-cleanups");
        activeTasks.setId("map-active-cleanups");activeTasks.setVisible(false);
        add(buildTopBar(), activeTasks, contentArea);
        applyFilters();
    }

    private void loadData() {
        try {
            allPoints = mapService.getLiveWasteForMap();
            dataError = false;
        } catch (RuntimeException exception) {
            log.error("Could not load waste map data", exception);
            allPoints = new ArrayList<>();
            dataError = true;
        }
    }

    private Div buildTopBar() {
        Div symbol = new Div(VaadinIcon.MAP_MARKER.create());
        symbol.addClassName("map-heading-icon");
        symbol.getElement().setAttribute("aria-hidden", "true");
        H1 title = I18n.localize(new H1("Chiqindi xaritasi"));
        Paragraph subtitle = I18n.localize(new Paragraph("Tozalash kerak bo'lgan joylarni toping va hudud bo'yicha saralang."));
        Div titleGroup = new Div(title, subtitle);
        titleGroup.addClassName("map-topbar-title-group");
        Div left = new Div(symbol, titleGroup);
        left.addClassName("map-topbar-left");
        Button refresh = I18n.localize(new Button("Yangilash", VaadinIcon.REFRESH.create()));
        refresh.setId("map-refresh");
        refresh.addClassName("map-btn-refresh");
        refresh.setDisableOnClick(true);
        refresh.addClickListener(e -> {
            try {
                loadData();
                updateDistricts();
                applyFilters();
                restoreDraftsFromBrowser();
            } finally { refresh.setEnabled(true); }
        });
        Button report = I18n.localize(new Button("Report", VaadinIcon.CAMERA.create(),
                e -> UI.getCurrent().navigate(ReportWasteView.class)));
        report.addClassName("map-btn-report");
        report.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Div right = new Div(refresh, report);
        right.addClassName("map-topbar-right");
        Div bar = new Div(left, right);
        bar.addClassName("map-topbar");
        return bar;
    }

    private Div buildFiltersSection() {
        districtFilter = I18n.localize(new ComboBox<>("Tuman bo'yicha"));
        districtFilter.setId("map-district-filter");
        districtFilter.addClassName("map-combo");
        districtFilter.setItemLabelGenerator(value -> "Barchasi".equals(value) ? I18n.t(value) : value);
        updateDistricts();
        districtFilter.setValue("Barchasi");
        // The explicit All option is the reset state; a second clear icon is redundant.
        districtFilter.setClearButtonVisible(false);
        districtFilter.addValueChangeListener(e -> applyFilters());
        categoryFilter = I18n.localize(new ComboBox<>("Chiqindi turi"));
        categoryFilter.setId("map-category-filter");
        categoryFilter.addClassName("map-combo");
        categoryFilter.setItems("Barchasi", "PLASTIC", "ILLEGAL_DUMP", "WATER", "AIR", "ELECTRONIC", "OTHER");
        categoryFilter.setItemLabelGenerator(value -> I18n.t(categoryLabel(value)));
        categoryFilter.setValue("Barchasi");
        categoryFilter.setClearButtonVisible(false);
        categoryFilter.addValueChangeListener(e -> applyFilters());
        resetFilters.setId("map-reset-filters");
        resetFilters.addClassName("map-reset-filters");
        resetFilters.addClickListener(e -> {
            updatingFilters = true;
            districtFilter.setValue("Barchasi");
            categoryFilter.setValue("Barchasi");
            updatingFilters = false;
            applyFilters();
        });
        Div controls = new Div(districtFilter, categoryFilter, resetFilters);
        controls.addClassName("map-filters-toolbar");
        return controls;
    }

    private void updateDistricts() {
        String previous = districtFilter.getValue();
        List<String> districts = new ArrayList<>();
        districts.add("Barchasi");
        allPoints.stream().filter(this::hasValidLocation)
                .map(point -> point.getLocation().getDistrict())
                .filter(value -> value != null && !value.isBlank()).distinct().sorted().forEach(districts::add);
        updatingFilters = true;
        districtFilter.setItems(districts);
        districtFilter.setValue(districts.contains(previous) ? previous : "Barchasi");
        updatingFilters = false;
    }

    private Div buildMapArea() {
        Div area = new Div();
        area.addClassName("map-right-area");
        mapDiv.setId("leaflet-waste-map");
        mapDiv.addClassName("map-leaflet-div");
        I18n.attribute(mapDiv, "aria-label", "Chiqindi joylashuvlari xaritasi");
        Div badge = new Div(VaadinIcon.MAP_MARKER.create(), totalBadgeSpan);
        badge.addClassName("map-count-overlay");
        badge.getElement().setAttribute("role", "status");
        badge.getElement().setAttribute("aria-live", "polite");
        totalBadgeSpan.setId("map-total-badge");
        totalBadgeSpan.addClassName("map-count-text");
        area.add(mapDiv, badge);
        return area;
    }

    private Div buildLegend() {
        Div legend = new Div(I18n.localize(new Span("Muammo darajasi")));
        legend.addClassName("map-workspace-legend");
        String[] labels = {"Past", "O'rta", "Yuqori", "Ko'rib chiqilmoqda"};
        String[] classes = {"low", "medium", "high", "review"};
        for (int i = 0; i < labels.length; i++) {
            Span dot = new Span();
            dot.addClassNames("map-severity-dot", "is-" + classes[i]);
            dot.getElement().setAttribute("aria-hidden", "true");
            legend.add(new Div(dot, I18n.localize(new Span(labels[i]))));
        }
        return legend;
    }

    private List<LiveWasteMapDto> filteredPoints() {
        String district = districtFilter == null ? null : districtFilter.getValue();
        String category = categoryFilter == null ? null : categoryFilter.getValue();
        return allPoints.stream().filter(this::hasValidLocation)
                .filter(point -> district == null || "Barchasi".equals(district) || district.equals(point.getLocation().getDistrict()))
                .filter(point -> category == null || "Barchasi".equals(category)
                        || point.getWasteType() != null && category.equals(point.getWasteType().name())).toList();
    }

    private boolean hasValidLocation(LiveWasteMapDto point) {
        if (point == null || point.getLocation() == null) return false;
        double lat = point.getLocation().getLatitude(), lng = point.getLocation().getLongitude();
        return Double.isFinite(lat) && Double.isFinite(lng) && lat >= -90 && lat <= 90
                && lng >= -180 && lng <= 180 && !(lat == 0 && lng == 0);
    }

    private void applyFilters() {
        if (updatingFilters) return;
        var points = filteredPoints();
        boolean filtered = districtFilter != null && !"Barchasi".equals(districtFilter.getValue())
                || categoryFilter != null && !"Barchasi".equals(categoryFilter.getValue());
        resetFilters.setEnabled(filtered);
        totalBadgeSpan.getElement().setAttribute("data-load-error", String.valueOf(dataError));
        I18n.text(totalBadgeSpan, dataError ? "Ma'lumotlar mavjud emas" : points.size() + " ta muammo");
        feedback.setVisible(dataError || points.isEmpty());
        feedback.setClassName("is-error", dataError);
        feedback.getElement().setAttribute("role", dataError ? "alert" : "status");
        I18n.text(feedback, dataError ? "Ma'lumotlarni yuklab bo'lmadi. Yangilash tugmasini bosing."
                : filtered ? "Tanlangan filtrlar bo'yicha joylar topilmadi." : "Hozircha xaritada tozalash joylari yo'q.");
        getUI().ifPresent(ui -> ui.getPage().executeJs(
                "window._wasteJson = $0; if (window.wasteMap) window.wasteMap.renderMarkers($0);", buildMarkersJson(points)));
    }

    @ClientCallable
    public void selectWaste(String reportId) {
        loadData();
        applyFilters();
        var selected = allPoints.stream().filter(this::hasValidLocation)
                .filter(point -> reportId != null && reportId.equals(point.getId())).findFirst();
        if (selected.isEmpty()) {
            I18n.notify("Bu joy hozir tozalash uchun mavjud emas. Xaritani yangilang.", 4000,
                    com.vaadin.flow.component.notification.Notification.Position.TOP_END);
            return;
        }
        var point = selected.get();var location = point.getLocation();
        String address = java.util.stream.Stream.of(location.getDistrict(), location.getStreetAddress())
                .filter(value -> value != null && !value.isBlank()).collect(java.util.stream.Collectors.joining(", "));
        var task = new CommunityCleanupTask(point.getId(), wasteLabel(point.getWasteType()), address,
                point.getImageUrl(), location.getLatitude(), location.getLongitude(), point.getReporterId(),
                point.getRewardCoins() == null ? 0 : point.getRewardCoins(), 0, null);
        openCleanup(task);
    }

    private void openCleanup(CommunityCleanupTask task) {
        if (selectedDialog != null && selectedDialog.isOpened()) selectedDialog.close();
        selectedDialog = new CommunityCleanupDialog(task, reports, attachments, this::taskChanged);
        selectedDialog.open();
    }

    private void taskChanged(CommunityCleanupTask task, Boolean completed) {
        if (Boolean.TRUE.equals(completed)) tasks.remove(task.reportId());
        else tasks.put(task.reportId(), task);
        persistTasks();renderActiveTasks();loadData();updateDistricts();applyFilters();
    }

    private void renderActiveTasks() {
        activeTasks.removeAll();activeTasks.setVisible(!tasks.isEmpty());
        if(tasks.isEmpty())return;
        activeTasks.add(I18n.localize(new H2("Davom etayotgan tozalashlar")),
                I18n.localize(new Paragraph("Boshlangan vazifalar shu brauzerda tiklanadi.")));
        for(var task:tasks.values()) {
            Button resume=I18n.localize(new Button("Davom etish",VaadinIcon.PLAY.create(),
                    e->openCleanup(task)));
            Div card=new Div(VaadinIcon.RECYCLE.create(),new Div(I18n.localize(new Span(task.title())),new Span(task.address())),resume);
            card.addClassName("map-active-cleanup-card");activeTasks.add(card);
        }
    }

    private String draftKey() { return "toza-cleanups-" + SecurityUtils.getCurrentUserId(); }

    private void persistTasks() {
        getUI().ifPresent(ui->getElement().executeJs(
                "try{localStorage.setItem($0,$1);}catch(e){console.warn('Cleanup draft could not be saved');}",
                draftKey(),new JsonMapper().writeValueAsString(tasks.values())));
    }

    private void restoreDraftsFromBrowser() {
        getUI().ifPresent(ui->getElement().executeJs(
                "try{return localStorage.getItem($0)||'[]';}catch(e){return '[]';}",draftKey())
                .then(String.class,this::restoreTasks));
    }

    private void restoreTasks(String json) {
        if(json==null || json.length()>100_000)return;
        try {
            var drafts=new JsonMapper().readValue(json,CommunityCleanupTask[].class);
            if(drafts.length==0)return;
            Set<String> eligible=new HashSet<>();
            for(int page=0;page<20;page++) {
                var work=myWork.getMyWork(page,50,false);
                work.stream().filter(item->item.getReportStatus()==ReportStatus.IN_PROGRESS && !item.isCleaned())
                        .forEach(item->eligible.add(item.getId()));
                if(!work.hasNext())break;
            }
            tasks.clear();
            for(var task:java.util.Arrays.stream(drafts).limit(20).toList()) {
                if(task.reportId()==null || !eligible.contains(task.reportId())
                        || task.startedAt()<=0 || task.startedAt()>System.currentTimeMillis()+60_000
                        || !Double.isFinite(task.latitude()) || !Double.isFinite(task.longitude())
                        || Math.abs(task.latitude())>90 || Math.abs(task.longitude())>180
                        || SecurityUtils.getCurrentUserId().equals(task.reporterId()))continue;
                tasks.put(task.reportId(),task);
            }
            renderActiveTasks();persistTasks();
        } catch(RuntimeException exception){log.warn("Could not restore cleanup drafts",exception);}
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
                                l.href = '/vendor/leaflet/leaflet.css';
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
                                    s.src = '/vendor/leaflet/leaflet.js';
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

                            L.control.zoom({ position: 'bottomright',
                                zoomInTitle: window.tozaTranslate('Yaqinlashtirish'),
                                zoomOutTitle: window.tozaTranslate('Uzoqlashtirish') }).addTo(map);

                            var layer = L.layerGroup().addTo(map);

                            // Keep the map in sync with any layout change (sidebar collapse, window resize)
                            var ro = window.ResizeObserver
                                ? new ResizeObserver(function () { map.invalidateSize(); })
                                : null;
                            var resizeHandler = function () { map.invalidateSize(); };
                            if (ro) ro.observe(el);
                            else window.addEventListener('resize', resizeHandler);

                            function render(json, preserveView) {
                                layer.clearLayers();
                                var pts = typeof json === 'string' ? JSON.parse(json) : json;
                                var markerLocations = [];
                                pts.forEach(function (p) {
                                    if (p.lat == null || p.lng == null) return;
                                    markerLocations.push([p.lat, p.lng]);

                                    var color  = p.sev === 'HIGH'   ? '#b45c4a'
                                               : p.sev === 'UNDER_REVIEW' ? '#819080'
                                               : p.sev === 'MEDIUM' ? '#bd8c36'
                                               : '#628b4d';
                                    var radius = p.sev === 'HIGH'   ? 12
                                               : p.sev === 'MEDIUM' ? 10 : 8;

                                    var marker = L.circleMarker([p.lat, p.lng], {
                                        radius:      radius,
                                        fillColor:   color,
                                        color:       '#ffffff',
                                        weight:      2.5,
                                        opacity:     1,
                                        fillOpacity: 0.88
                                    }).addTo(layer).bindTooltip(
                                        '<div class="wmt-wrap">' +
                                            '<span class="wmt-title">' + esc(window.tozaTranslate(p.label || 'Chiqindi')) + '</span>' +
                                            '<span class="wmt-loc">' + esc(p.dist || "Noma'lum") + '</span>' +
                                            (p.coins ? '<span class="wmt-coins">+' + esc(p.coins) + ' ' + window.tozaTranslate('tanga') + '</span>' : '') +
                                        '</div>',
                                        { direction: 'top', offset: [0, -10], opacity: 1, className: 'wm-tooltip' }
                                    );
                                    marker.on('click', function () { $1.$server.selectWaste(p.id); });
                                    var markerElement = marker.getElement();
                                    if(markerElement){
                                        markerElement.setAttribute('tabindex','0');markerElement.setAttribute('role','button');
                                        markerElement.setAttribute('aria-label',window.tozaTranslate(p.label || 'Chiqindi')+' '+(p.dist || ''));
                                        markerElement.addEventListener('keydown',function(event){
                                            if(event.key==='Enter'||event.key===' '){event.preventDefault();$1.$server.selectWaste(p.id);}
                                        });
                                    }

                                });

                                var badge = document.getElementById('map-total-badge');
                                if (badge && badge.dataset.loadError !== 'true') badge.textContent = window.tozaTranslate('{0} ta muammo').replace('{0}', pts.length);
                                if (markerLocations.length && !preserveView) {
                                    var bounds = L.latLngBounds(markerLocations);
                                    if (!map.getBounds().contains(bounds)) {
                                        map.fitBounds(bounds.pad(0.15), { maxZoom: 12 });
                                    }
                                }
                            }

                            var localeHandler = function () { render(window._wasteJson, true); };
                            window.addEventListener('toza-language-changed', localeHandler);
                            window.wasteMap = {
                                renderMarkers: render,
                                destroy: function () {
                                    window.removeEventListener('toza-language-changed', localeHandler);
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
                            var feedback = document.getElementById('map-load-feedback');
                            if (feedback) {
                                feedback.removeAttribute('hidden');
                                feedback.classList.add('is-error');
                                feedback.setAttribute('role', 'alert');
                                feedback.textContent = window.tozaTranslate("Xaritani yuklab bo'lmadi. Sahifani qayta yuklang.");
                            }
                        });
                    })($0);
                """;

        event.getUI().getPage().executeJs(bootstrap, buildMarkersJson(filteredPoints()), getElement());
        restoreDraftsFromBrowser();
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
    private String buildMarkersJson(List<LiveWasteMapDto> points) {
        return new JsonMapper().writeValueAsString(points.stream().filter(this::hasValidLocation).map(point -> Map.of(
                "id", point.getId() == null ? "" : point.getId(),
                "lat", point.getLocation().getLatitude(), "lng", point.getLocation().getLongitude(),
                "sev", point.getSeverityLevel() == null ? "UNDER_REVIEW" : point.getSeverityLevel().name(),
                "label", wasteLabel(point.getWasteType()),
                "dist", point.getLocation().getDistrict() == null ? "" : point.getLocation().getDistrict(),
                "coins", point.getRewardCoins() == null ? 0 : point.getRewardCoins())).toList());
    }

    private String wasteLabel(WasteTypeEnum t) {
        if (t == null) return "Boshqa";
        return switch (t) {
            case PLASTIC -> "Plastik";
            case ILLEGAL_DUMP -> "Noqonuniy tashlama";
            case WATER -> "Suv ifloslanishi";
            case AIR -> "Havo ifloslanishi";
            case ELECTRONIC -> "Elektron chiqindi";
            case OTHER -> "Boshqa";
        };
    }


    private String categoryLabel(String raw) {
        try { return wasteLabel(WasteTypeEnum.valueOf(raw)); }
        catch (IllegalArgumentException exception) { return raw; }
    }

    @Override
    public String getPageTitle() {
        return I18n.t("Jonli Xarita | TozaCoin");
    }
}
