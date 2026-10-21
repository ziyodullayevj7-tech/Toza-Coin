package teamwork.view.admin;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import teamwork.dto.wasteReport.ConfirmWasteDto;
import teamwork.enums.ReportStatus;
import teamwork.service.AdminService;
import teamwork.service.WasteReportService;
import teamwork.view.layout.admin.AdminLayout;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Route(value = "admin/moderation", layout = AdminLayout.class)
@PageTitle("Moderatsiya | TozaCoin")
@RolesAllowed("ADMIN")
@CssImport("./themes/tozacoin/user-layout.css")
public class AdminModerationView extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(AdminModerationView.class);
    private static final int PAGE_SIZE = 10;
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final WasteReportService wasteReportService;
    private final AdminService adminService;
    private final List<ConfirmWasteDto> reports = new ArrayList<>();
    private final Grid<ConfirmWasteDto> reportsGrid = new Grid<>();
    private final Span queueCount = new Span("0");
    private final Span pageSummary = new Span();
    private final Span emptyMessage = new Span("Hozircha ko'rib chiqilishi kerak bo'lgan xabar yo'q.");
    private final Button previousButton = new Button("← Oldingi");
    private final Button nextButton = new Button("Keyingi →");
    private final Div statusFilters = new Div();
    private final Div errorMessage = new Div();
    private final TextField searchField = new TextField();

    private int currentPage;
    private int pageCount;
    private long totalReports;
    private ReportStatus selectedStatus;

    @Autowired
    public AdminModerationView(WasteReportService wasteReportService, AdminService adminService) {
        this.wasteReportService = wasteReportService;
        this.adminService = adminService;

        setWidthFull();
        setPadding(false);
        setSpacing(false);
        addClassName("admin-moderation-page");

        add(buildPageHeader(), buildToolbar(), buildReportsPanel(), buildPagination());
        configureSearch();
        loadReports();
    }

    private Div buildPageHeader() {
        Div header = new Div();
        header.addClassName("moderation-page-header");

        Div titleBlock = new Div();
        H2 title = new H2("Moderatsiya navbati");
        title.addClassName("moderation-page-title");
        Paragraph subtitle = new Paragraph("Kiruvchi xabarlar va tozalash tasdiqlarini ko'rib chiqing");
        subtitle.addClassName("moderation-page-subtitle");
        titleBlock.add(title, subtitle);

        Div countCard = new Div();
        countCard.addClassName("moderation-queue-badge");
        countCard.add(new Span("⏳"), queueCount, new Span("ta navbatda"));
        header.add(titleBlock, countCard);
        return header;
    }

    private Div buildToolbar() {
        Div toolbar = new Div();
        toolbar.addClassName("moderation-toolbar");

        statusFilters.addClassName("moderation-status-filters");
        updateStatusFilters();

        searchField.setPlaceholder("Joriy sahifadagi foydalanuvchi, tuman yoki ID...");
        searchField.setClearButtonVisible(true);
        searchField.setPrefixComponent(new Span("⌕"));
        searchField.addClassName("moderation-search");

        Button refreshButton = new Button("Yangilash", event -> loadReports());
        refreshButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        refreshButton.addClassName("moderation-refresh-button");

        toolbar.add(statusFilters, searchField, refreshButton);
        return toolbar;
    }

    private Div buildReportsPanel() {
        Div panel = new Div();
        panel.addClassName("moderation-grid-panel");

        errorMessage.addClassName("moderation-error");
        errorMessage.setVisible(false);

        configureGrid();
        emptyMessage.addClassName("moderation-empty-state");
        panel.add(errorMessage, reportsGrid, emptyMessage);
        return panel;
    }

    private HorizontalLayout buildPagination() {
        HorizontalLayout pagination = new HorizontalLayout();
        pagination.addClassName("moderation-pagination");
        pagination.setWidthFull();
        pagination.setAlignItems(Alignment.CENTER);
        pagination.setJustifyContentMode(JustifyContentMode.BETWEEN);

        previousButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        previousButton.addClickListener(event -> {
            if (currentPage > 0) {
                currentPage--;
                loadReports();
            }
        });
        nextButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        nextButton.addClickListener(event -> {
            if (currentPage + 1 < pageCount) {
                currentPage++;
                loadReports();
            }
        });

        pageSummary.addClassName("moderation-page-summary");
        pagination.add(previousButton, pageSummary, nextButton);
        return pagination;
    }

    private void configureGrid() {
        reportsGrid.addClassName("moderation-grid");
        reportsGrid.setWidthFull();
        reportsGrid.setHeight("min(620px, 65vh)");
        reportsGrid.setAllRowsVisible(false);
        reportsGrid.removeAllColumns();

        reportsGrid.addComponentColumn(this::buildPhotoPreview)
                .setHeader("Suratlar")
                .setWidth("150px")
                .setFlexGrow(0);
        reportsGrid.addComponentColumn(this::buildReporterCell)
                .setHeader("Foydalanuvchi / tuman")
                .setAutoWidth(true)
                .setFlexGrow(1);
        reportsGrid.addColumn(report -> formatDate(report))
                .setHeader("Sana")
                .setAutoWidth(true)
                .setFlexGrow(0);
        reportsGrid.addComponentColumn(this::buildConfidenceBadge)
                .setHeader("AI tahlili")
                .setWidth("130px")
                .setFlexGrow(0);
        reportsGrid.addComponentColumn(this::buildStatusBadge)
                .setHeader("Holat")
                .setWidth("155px")
                .setFlexGrow(0);
        reportsGrid.addComponentColumn(report -> {
                    Button viewButton = new Button("Ko'rish", event -> openReportDetails(report));
                    viewButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
                    return viewButton;
                })
                .setHeader("Amallar")
                .setWidth("115px")
                .setFlexGrow(0);
    }

    private void configureSearch() {
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(event -> updateGridItems());
    }

    private void updateStatusFilters() {
        statusFilters.removeAll();
        statusFilters.add(
                createStatusFilter("Barchasi", null),
                createStatusFilter("Kutilmoqda", ReportStatus.PENDING),
                createStatusFilter("Ko'rib chiqilmoqda", ReportStatus.IN_PROGRESS)
        );
    }

    private Button createStatusFilter(String label, ReportStatus status) {
        Button button = new Button(label);
        button.addClassName("moderation-filter-button");
        if (selectedStatus == status) {
            button.addClassName("active");
        }
        button.addClickListener(event -> {
            selectedStatus = status;
            updateStatusFilters();
            updateGridItems();
        });
        return button;
    }

    private HorizontalLayout buildPhotoPreview(ConfirmWasteDto report) {
        HorizontalLayout photos = new HorizontalLayout();
        photos.addClassName("moderation-photo-pair");
        photos.setSpacing(false);
        photos.add(createThumbnail(report.getBeforeImageUrl(), "Tozalashdan oldin"));
        photos.add(createThumbnail(report.getAfterImageUrl(), "Tozalashdan keyin"));
        return photos;
    }

    private Div createThumbnail(String imageUrl, String altText) {
        Div thumbnail = new Div();
        thumbnail.addClassName("moderation-thumbnail");
        if (imageUrl == null || imageUrl.isBlank()) {
            thumbnail.addClassName("moderation-thumbnail-missing");
            thumbnail.setText("—");
            thumbnail.setTitle(altText + " mavjud emas");
            return thumbnail;
        }
        Image image = new Image(imageUrl, altText);
        image.addClassName("moderation-thumbnail-image");
        thumbnail.add(image);
        return thumbnail;
    }

    private VerticalLayout buildReporterCell(ConfirmWasteDto report) {
        VerticalLayout cell = new VerticalLayout();
        cell.addClassName("moderation-reporter-cell");
        cell.setPadding(false);
        cell.setSpacing(false);

        Span name = new Span(nonBlank(report.getReporterFullName(), "Noma'lum foydalanuvchi"));
        name.addClassName("moderation-reporter-name");
        Span district = new Span(nonBlank(report.getDistrict(), "Manzil ko'rsatilmagan"));
        district.addClassName("moderation-district");
        Span id = new Span("ID: " + nonBlank(report.getReportId(), "—"));
        id.addClassName("moderation-report-id");
        cell.add(name, district, id);
        return cell;
    }

    private Span buildConfidenceBadge(ConfirmWasteDto report) {
        if (report.getConfidenceScore() == null) {
            Span badge = new Span("Tahlil yo'q");
            badge.addClassName("moderation-ai-badge");
            badge.addClassName("unavailable");
            return badge;
        }
        Span badge = new Span(report.getConfidenceScore() + "% ishonch");
        badge.addClassName("moderation-ai-badge");
        badge.addClassName(report.getConfidenceScore() >= 75 ? "high" : "low");
        return badge;
    }

    private Span buildStatusBadge(ConfirmWasteDto report) {
        Span badge = new Span(statusLabel(report.getStatus()));
        badge.addClassName("moderation-report-status");
        badge.addClassName(report.getStatus() == ReportStatus.IN_PROGRESS ? "in-progress" : "pending");
        return badge;
    }

    private String formatDate(ConfirmWasteDto report) {
        return report.getReportedDateTime() == null
                ? "Sana noma'lum"
                : DATE_FORMAT.format(report.getReportedDateTime());
    }

    private void loadReports() {
        try {
            Page<ConfirmWasteDto> page = wasteReportService.getUnresolvedReports(currentPage, PAGE_SIZE);
            reports.clear();
            reports.addAll(page.getContent());
            totalReports = page.getTotalElements();
            pageCount = page.getTotalPages();
            errorMessage.setVisible(false);
            errorMessage.setText("");
            queueCount.setText(String.valueOf(totalReports));
            updateGridItems();
            updatePagination();
        } catch (RuntimeException e) {
            log.error("Could not load moderation queue", e);
            errorMessage.setText("Xabarlarni yuklab bo'lmadi. Iltimos, qayta urinib ko'ring.");
            errorMessage.setVisible(true);
            reports.clear();
            updateGridItems();
            updatePagination();
            Notification.show("Moderatsiya navbatini yuklashda xatolik yuz berdi.",
                    4500, Position.TOP_END);
        }
    }

    private void updateGridItems() {
        String query = searchField.getValue() == null
                ? ""
                : searchField.getValue().trim().toLowerCase(Locale.ROOT);
        List<ConfirmWasteDto> filteredReports = reports.stream()
                .filter(report -> selectedStatus == null || report.getStatus() == selectedStatus)
                .filter(report -> query.isEmpty()
                        || contains(report.getReporterFullName(), query)
                        || contains(report.getDistrict(), query)
                        || contains(report.getReportId(), query))
                .toList();
        reportsGrid.setItems(filteredReports);
        emptyMessage.setVisible(filteredReports.isEmpty() && !errorMessage.isVisible());
        reportsGrid.setVisible(!filteredReports.isEmpty());
    }

    private void updatePagination() {
        int shownPage = pageCount == 0 ? 0 : currentPage + 1;
        pageSummary.setText(String.format(
                Locale.ROOT, "Sahifa %d / %d · %d ta xabar", shownPage, pageCount, totalReports));
        previousButton.setEnabled(currentPage > 0);
        nextButton.setEnabled(currentPage + 1 < pageCount);
    }

    private void openReportDetails(ConfirmWasteDto report) {
        Dialog dialog = new Dialog();
        dialog.addClassName("moderation-detail-dialog");
        dialog.setHeaderTitle("Xabar tafsilotlari");
        dialog.setWidth("min(940px, 96vw)");

        VerticalLayout content = new VerticalLayout();
        content.addClassName("moderation-detail-content");
        content.setPadding(false);
        content.setSpacing(true);

        Div meta = new Div();
        meta.addClassName("moderation-detail-meta");
        meta.add(detailValue("Foydalanuvchi",
                        nonBlank(report.getReporterFullName(), "Noma'lum foydalanuvchi")),
                detailValue("Tuman", nonBlank(report.getDistrict(), "Ko'rsatilmagan")),
                detailValue("Yuborilgan vaqt", formatDate(report)),
                detailValue("Tozalash vaqti", report.getCleanPeriod() == null
                        ? "Ko'rsatilmagan"
                        : report.getCleanPeriod() + " daqiqa"));

        Div photoSection = new Div();
        photoSection.addClassName("moderation-detail-photos");
        photoSection.add(buildDetailImage(report.getBeforeImageUrl(), "Tozalashdan oldin"),
                buildDetailImage(report.getAfterImageUrl(), "Tozalashdan keyin"));

        Div aiCard = new Div();
        aiCard.addClassName("moderation-ai-review");
        Span aiTitle = new Span("AI tahlili (qaror uchun yordamchi)");
        aiTitle.addClassName("moderation-ai-title");
        Span score = new Span(report.getConfidenceScore() == null
                ? "Ishonch darajasi mavjud emas"
                : "Ishonch darajasi: " + report.getConfidenceScore() + "%");
        score.addClassName("moderation-ai-score");
        Paragraph reasoning = new Paragraph(nonBlank(report.getReasoning(),
                "AI tahlili uchun izoh mavjud emas."));
        reasoning.addClassName("moderation-ai-reasoning");
        aiCard.add(aiTitle, score, reasoning);

        content.add(meta, photoSection, aiCard);
        dialog.add(content);

        Button closeButton = new Button("Yopish", event -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        dialog.getFooter().add(closeButton);

        if (report.getStatus() == ReportStatus.PENDING) {
            Button startButton = new Button("Ko'rib chiqishni boshlash", event ->
                    requestStatusChange(report, ReportStatus.IN_PROGRESS, dialog));
            startButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            dialog.getFooter().add(startButton);
        }
        Button rejectButton = new Button("Rad etish", event ->
                requestStatusChange(report, ReportStatus.REJECTED, dialog));
        rejectButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        Button approveButton = new Button("Tasdiqlash", event ->
                requestStatusChange(report, ReportStatus.RESOLVED, dialog));
        approveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        dialog.getFooter().add(rejectButton, approveButton);
        dialog.open();
    }

    private Div buildDetailImage(String imageUrl, String label) {
        Div imageCard = new Div();
        imageCard.addClassName("moderation-detail-image-card");
        Span caption = new Span(label);
        caption.addClassName("moderation-detail-image-caption");
        if (imageUrl == null || imageUrl.isBlank()) {
            Div placeholder = new Div(new Span("Surat mavjud emas"));
            placeholder.addClassName("moderation-detail-image-placeholder");
            imageCard.add(caption, placeholder);
        } else {
            Image image = new Image(imageUrl, label);
            image.addClassName("moderation-detail-image");
            imageCard.add(caption, image);
        }
        return imageCard;
    }

    private Div detailValue(String label, String value) {
        Div item = new Div();
        item.addClassName("moderation-detail-value");
        Span key = new Span(label);
        key.addClassName("moderation-detail-key");
        Span text = new Span(value);
        text.addClassName("moderation-detail-text");
        item.add(key, text);
        return item;
    }

    private void requestStatusChange(ConfirmWasteDto report, ReportStatus status, Dialog detailsDialog) {
        String title;
        String message;
        String confirmLabel;
        String theme;
        if (status == ReportStatus.RESOLVED) {
            title = "Xabarni tasdiqlaysizmi?";
            message = "Xabar hal qilingan deb belgilanadi va foydalanuvchiga mukofot tangalari beriladi.";
            confirmLabel = "Tasdiqlash";
            theme = "primary success";
        } else if (status == ReportStatus.REJECTED) {
            title = "Xabarni rad etasizmi?";
            message = "Rad etilgan xabar moderatsiya navbatidan olib tashlanadi.";
            confirmLabel = "Rad etish";
            theme = "error primary";
        } else {
            title = "Ko'rib chiqishni boshlaysizmi?";
            message = "Xabar holati ko'rib chiqilmoqda deb belgilanadi.";
            confirmLabel = "Boshlash";
            theme = "primary";
        }

        ConfirmDialog confirmation = new ConfirmDialog();
        confirmation.setHeader(title);
        confirmation.setText(message);
        confirmation.setCancelable(true);
        confirmation.setCancelText("Bekor qilish");
        confirmation.setConfirmText(confirmLabel);
        confirmation.setConfirmButtonTheme(theme);
        confirmation.addConfirmListener(event -> {
            try {
                ConfirmWasteDto update = new ConfirmWasteDto();
                update.setReportId(report.getReportId());
                update.setReporterId(report.getReporterId());
                update.setStatus(status);
                adminService.confirmReport(update);
                detailsDialog.close();
                Notification.show(statusSuccessMessage(status), 3500, Position.TOP_END);
                if (currentPage > 0 && reports.size() == 1) {
                    currentPage--;
                }
                loadReports();
            } catch (RuntimeException e) {
                log.error("Could not update moderation status for report {}", report.getReportId(), e);
                Notification.show("Xabar holatini yangilashda xatolik yuz berdi.",
                        4500, Position.TOP_END);
            }
        });
        confirmation.open();
    }

    private String statusSuccessMessage(ReportStatus status) {
        return switch (status) {
            case IN_PROGRESS -> "Xabar ko'rib chiqishga olindi.";
            case RESOLVED -> "Xabar tasdiqlandi va mukofot berildi.";
            case REJECTED -> "Xabar rad etildi.";
            case PENDING -> "Xabar holati yangilandi.";
        };
    }

    private String statusLabel(ReportStatus status) {
        if (status == null) {
            return "Noma'lum";
        }
        return switch (status) {
            case PENDING -> "Kutilmoqda";
            case IN_PROGRESS -> "Ko'rib chiqilmoqda";
            case RESOLVED -> "Tasdiqlandi";
            case REJECTED -> "Rad etildi";
        };
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private String nonBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
