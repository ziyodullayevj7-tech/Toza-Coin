package teamwork.view.user;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import teamwork.dto.myReport.MyReportResponseDto;
import teamwork.entity.AttachEntity;
import teamwork.enums.ReportStatus;
import teamwork.enums.SeverityLevelEnum;
import teamwork.enums.WasteTypeEnum;
import teamwork.service.AttachService;
import teamwork.service.MyReportsService;
import teamwork.view.layout.user.UserLayout;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Route(value = "user/my-reports", layout = UserLayout.class)
@PageTitle("Mening xabarlarim | TozaCoin")
@PermitAll
@CssImport("./themes/tozacoin/user-layout.css")
public class MyReportsView extends VerticalLayout {

    private final MyReportsService myReportsService;
    private final AttachService attachService;

    private int currentPage = 0;
    private final int pageSize = 6;
    private ReportStatus selectedStatus = null; // null means 'Barchasi' (All)

    private final Div statsRow = new Div();
    private final Div filterTabsRow = new Div();
    private final Div cardsContainer = new Div();
    private final HorizontalLayout paginationControls = new HorizontalLayout();

    private final Span totalCountSpan = new Span("0");
    private final Span resolvedCountSpan = new Span("0");
    private final Span coinsEarnedSpan = new Span("0");

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    public MyReportsView(MyReportsService myReportsService, AttachService attachService) {
        this.myReportsService = myReportsService;
        this.attachService = attachService;

        setWidthFull();
        setSpacing(false);
        setPadding(false);
        addClassName("dashboard-content-wrapper");

        // 1. Page Header (Title + Subtitle)
        add(buildHeaderSection());

        // 2. Top Stats Bar (Jami xabar, Hal qilindi, Ishlandi)
        add(buildTopStatsBar());

        // 3. Filter Tabs Bar (Barchasi, Kutilmoqda, Tasdiqlandi, Hal qilindi, Rad etildi)
        add(buildFilterTabsBar());

        // 4. Cards Container
        cardsContainer.setWidthFull();
        cardsContainer.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "16px");
        add(cardsContainer);

        // 5. Pagination Controls
        buildPaginationControls();
        add(paginationControls);

        // Initial Data Load
        refreshViewData();
    }

    /* =========================================================================
     * 1. HEADER SECTION
     * ========================================================================= */
    private Div buildHeaderSection() {
        Div header = new Div();
        header.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "4px")
                .set("margin-bottom", "8px");

        H2 title = new H2("Mening xabarlarim");
        title.getStyle()
                .set("font-size", "26px")
                .set("font-weight", "800")
                .set("color", "#0f172a")
                .set("margin", "0");

        Span subtitle = new Span("Siz yuborgan barcha ifloslik xabarlari");
        subtitle.getStyle()
                .set("font-size", "14px")
                .set("color", "#64748b")
                .set("font-weight", "500");

        header.add(title, subtitle);
        return header;
    }

    /* =========================================================================
     * 2. TOP METRICS CARDS BAR (Jami xabar | Hal qilindi | Ishlandi)
     * ========================================================================= */
    private Div buildTopStatsBar() {
        statsRow.getStyle()
                .set("display", "flex")
                .set("justify-content", "flex-end")
                .set("gap", "16px")
                .set("margin-bottom", "12px")
                .set("flex-wrap", "wrap");

        statsRow.add(
                createStatCard(totalCountSpan, "Jami xabar", "#0f172a"),
                createStatCard(resolvedCountSpan, "Hal qilindi", "#16a34a"),
                createCoinStatCard(coinsEarnedSpan, "Ishlandi")
        );

        return statsRow;
    }

    private Div createStatCard(Span valueSpan, String labelText, String textColor) {
        Div card = new Div();
        card.getStyle()
                .set("background-color", "#ffffff")
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "14px")
                .set("padding", "12px 28px")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.02)")
                .set("min-width", "110px");

        valueSpan.getStyle()
                .set("font-size", "22px")
                .set("font-weight", "800")
                .set("color", textColor)
                .set("line-height", "1.2");

        Span label = new Span(labelText);
        label.getStyle()
                .set("font-size", "12px")
                .set("color", "#64748b")
                .set("font-weight", "600")
                .set("margin-top", "2px");

        card.add(valueSpan, label);
        return card;
    }

    private Div createCoinStatCard(Span coinsSpan, String labelText) {
        Div card = new Div();
        card.getStyle()
                .set("background-color", "#ffffff")
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "14px")
                .set("padding", "12px 28px")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.02)")
                .set("min-width", "110px");

        HorizontalLayout row = new HorizontalLayout();
        row.setAlignItems(Alignment.CENTER);
        row.setSpacing(true);
        row.getStyle().set("gap", "6px");

        Span coinIcon = new Span("🪙");
        coinIcon.getStyle().set("font-size", "18px");

        coinsSpan.getStyle()
                .set("font-size", "22px")
                .set("font-weight", "800")
                .set("color", "#d97706")
                .set("line-height", "1.2");

        row.add(coinIcon, coinsSpan);

        Span label = new Span(labelText);
        label.getStyle()
                .set("font-size", "12px")
                .set("color", "#64748b")
                .set("font-weight", "600")
                .set("margin-top", "2px");

        card.add(row, label);
        return card;
    }

    /* =========================================================================
     * 3. FILTER TABS BAR (Barchasi, Kutilmoqda, Tasdiqlandi, Hal qilindi, Rad etildi)
     * ========================================================================= */
    private Div buildFilterTabsBar() {
        filterTabsRow.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "10px")
                .set("margin-bottom", "16px")
                .set("flex-wrap", "wrap");

        return filterTabsRow;
    }

    private void updateFilterTabs() {
        filterTabsRow.removeAll();

        long allCount = myReportsService.getCountByStatus(null);
        long pendingCount = myReportsService.getCountByStatus(ReportStatus.PENDING);
        long inProgressCount = myReportsService.getCountByStatus(ReportStatus.IN_PROGRESS);
        long resolvedCount = myReportsService.getCountByStatus(ReportStatus.RESOLVED);
        long rejectedCount = myReportsService.getCountByStatus(ReportStatus.REJECTED);
        Integer totalCoins = myReportsService.getTotalCoinsEarned();

        // Update Stat Cards with REAL numbers directly from DB
        totalCountSpan.setText(String.valueOf(allCount));
        resolvedCountSpan.setText(String.valueOf(resolvedCount));
        coinsEarnedSpan.setText(String.valueOf(totalCoins));

        filterTabsRow.add(
                createTabButton("Barchasi (" + allCount + ")", null),
                createTabButton("Kutilmoqda (" + pendingCount + ")", ReportStatus.PENDING),
                createTabButton("Tasdiqlandi (" + inProgressCount + ")", ReportStatus.IN_PROGRESS),
                createTabButton("Hal qilindi (" + resolvedCount + ")", ReportStatus.RESOLVED),
                createTabButton("Rad etildi (" + rejectedCount + ")", ReportStatus.REJECTED)
        );
    }

    private Button createTabButton(String label, ReportStatus status) {
        Button btn = new Button(label);
        boolean isActive = (selectedStatus == status);

        btn.getStyle()
                .set("border-radius", "20px")
                .set("font-size", "13px")
                .set("font-weight", "600")
                .set("padding", "6px 18px")
                .set("cursor", "pointer")
                .set("transition", "all 0.18s ease");

        if (isActive) {
            btn.getStyle()
                    .set("background-color", "#15803d")
                    .set("color", "#ffffff")
                    .set("border", "1px solid #15803d")
                    .set("box-shadow", "0 2px 8px rgba(21, 128, 61, 0.25)");
        } else {
            btn.getStyle()
                    .set("background-color", "#ffffff")
                    .set("color", "#475569")
                    .set("border", "1px solid #e2e8f0");
        }

        btn.addClickListener(e -> {
            selectedStatus = status;
            currentPage = 0;
            refreshViewData();
        });

        return btn;
    }

    /* =========================================================================
     * 4. REPORT CARDS RENDERING & REAL PHOTO STREAMING
     * ========================================================================= */
    private void refreshViewData() {
        cardsContainer.removeAll();
        updateFilterTabs();

        Page<MyReportResponseDto> pageData = myReportsService.getMyRprtsPagination(currentPage, pageSize, selectedStatus);
        List<MyReportResponseDto> reports = pageData.getContent();

        if (reports.isEmpty()) {
            cardsContainer.add(buildEmptyStateView());
        } else {
            for (MyReportResponseDto dto : reports) {
                cardsContainer.add(buildReportCard(dto));
            }
        }

        updatePaginationControls(pageData);
    }

    private Div buildReportCard(MyReportResponseDto dto) {
        Div card = new Div();
        card.addClassName("dashboard-card");
        card.getStyle()
                .set("display", "flex")
                .set("flex-direction", "row")
                .set("align-items", "center")
                .set("justify-content", "space-between")
                .set("padding", "16px 20px")
                .set("border-radius", "16px")
                .set("border", "1px solid #e2e8f0")
                .set("background-color", "#ffffff")
                .set("cursor", "pointer")
                .set("transition", "transform 0.18s ease, box-shadow 0.18s ease")
                .set("gap", "20px");

        // Hover Effect
        card.getElement().addEventListener("mouseover", e ->
                card.getStyle().set("transform", "translateY(-2px)").set("box-shadow", "0 8px 20px rgba(0,0,0,0.06)"));
        card.getElement().addEventListener("mouseout", e ->
                card.getStyle().set("transform", "none").set("box-shadow", "none"));

        // Open detail modal on click
        card.addClickListener(e -> openDetailModal(dto));

        // LEFT: Real Photo Thumbnail Component
        Div photoBox = buildPhotoThumbnail(dto.getPhotoUrl(), getWasteTypeTitle(dto), dto.getWasteType());

        // MIDDLE: Content Details (Title, Location, Badges)
        Div middleContent = new Div();
        middleContent.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "6px")
                .set("flex", "1");

        H3 cardTitle = new H3(getWasteTypeTitle(dto));
        cardTitle.getStyle()
                .set("font-size", "16px")
                .set("font-weight", "700")
                .set("color", "#0f172a")
                .set("margin", "0");

        String locText = formatLocation(dto);
        Span locSpan = new Span("📍 " + locText);
        locSpan.getStyle()
                .set("font-size", "13px")
                .set("color", "#0284c7")
                .set("font-weight", "500");

        // Badges Row (Severity & Waste Category)
        Div badgesRow = new Div();
        badgesRow.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "8px")
                .set("margin-top", "2px");

        badgesRow.add(
                buildSeverityBadge(dto.getSeverityLevel()),
                buildCategoryBadge(dto.getWasteType())
        );

        middleContent.add(cardTitle, locSpan, badgesRow);

        // RIGHT: Status Badge, Reward Coins & Date
        Div rightContent = new Div();
        rightContent.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "flex-end")
                .set("justify-content", "space-between")
                .set("gap", "12px")
                .set("flex-shrink", "0");

        Span statusBadge = buildStatusBadge(dto.getReportStatus());

        Span coinsSpan = new Span("🪙 +" + (dto.getRewardCoins() != null ? dto.getRewardCoins() : 15));
        coinsSpan.getStyle()
                .set("font-size", "14px")
                .set("font-weight", "700")
                .set("color", "#d97706");

        String dateStr = (dto.getDate() != null) ? dto.getDate().format(dateFormatter) : "";
        Span dateSpan = new Span(dateStr);
        dateSpan.getStyle()
                .set("font-size", "12px")
                .set("color", "#94a3b8")
                .set("font-weight", "500");

        rightContent.add(statusBadge, coinsSpan, dateSpan);

        card.add(photoBox, middleContent, rightContent);
        return card;
    }

    /**
     * Photo Thumbnail Box with Rounded Corners and Instant Real Image Streaming
     */
    private Div buildPhotoThumbnail(String photoUrl, String altTitle, WasteTypeEnum wasteType) {
        Div box = new Div();
        box.getStyle()
                .set("width", "110px")
                .set("height", "90px")
                .set("min-width", "110px")
                .set("border-radius", "14px")
                .set("overflow", "hidden")
                .set("background-color", "#f1f5f9")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("flex-shrink", "0");

        Image img = createReportImage(photoUrl, altTitle, wasteType);
        box.add(img);
        return box;
    }

    /**
     * Creates Real Image using Senior Engineer standard static resource handler (/attaches/YYYY/MM/DD/file.jpg)
     */
    private Image createReportImage(String photoUrl, String altTitle, WasteTypeEnum wasteType) {
        Image img = new Image();
        img.setAlt(altTitle);
        img.getStyle()
                .set("width", "100%")
                .set("height", "100%")
                .set("object-fit", "cover");

        if (photoUrl != null && !photoUrl.isBlank()) {
            // 1. Base64 Camera Data URI
            if (photoUrl.startsWith("data:image")) {
                img.setSrc(photoUrl);
                return img;
            }

            // 2. Extract attach file ID from path or URL
            String fileId = photoUrl;
            if (fileId.contains("/open/")) {
                fileId = fileId.substring(fileId.lastIndexOf("/open/") + 6);
            } else if (fileId.contains("/attaches/")) {
                fileId = fileId.substring(fileId.lastIndexOf("/") + 1);
            } else if (fileId.contains("/")) {
                fileId = fileId.substring(fileId.lastIndexOf("/") + 1);
            }

            // 3. Lookup AttachEntity and use whitelisted Spring Boot static resource URL (/attaches/YYYY/MM/DD/file.jpg)
            try {
                Optional<AttachEntity> attachOpt = attachService.findEntity(fileId);
                if (attachOpt.isPresent()) {
                    img.setSrc(attachService.getPublicUrl(attachOpt.get()));
                    return img;
                }
            } catch (Exception ignored) {
            }

            // 4. Direct HTTP or HTTPS URL if external
            if (photoUrl.startsWith("http://") || photoUrl.startsWith("https://")) {
                img.setSrc(photoUrl);
                return img;
            }

            img.setSrc("/attaches/" + fileId);
        } else {
            img.setSrc(getRealFallbackPhotoUrl(wasteType));
        }

        img.getElement().addEventListener("error", e -> img.setSrc(getRealFallbackPhotoUrl(wasteType)));
        return img;
    }

    private String getRealFallbackPhotoUrl(WasteTypeEnum wasteType) {
        if (wasteType == WasteTypeEnum.PLASTIC) {
            return "https://images.unsplash.com/photo-1532996122724-e3c354a0b15b?w=400&q=80";
        } else if (wasteType == WasteTypeEnum.ILLEGAL_DUMP) {
            return "https://images.unsplash.com/photo-1611284446314-60a58ac0deb9?w=400&q=80";
        } else if (wasteType == WasteTypeEnum.WATER) {
            return "https://images.unsplash.com/photo-1621451537084-482c73073a0f?w=400&q=80";
        } else if (wasteType == WasteTypeEnum.AIR) {
            return "https://images.unsplash.com/photo-1611273426858-450d8e3c9fce?w=400&q=80";
        }
        return "https://images.unsplash.com/photo-1532996122724-e3c354a0b15b?w=400&q=80";
    }

    /* =========================================================================
     * 5. CARD DETAIL MODAL DIALOG (Matching Screenshot 2)
     * ========================================================================= */
    private void openDetailModal(MyReportResponseDto dto) {
        Dialog dialog = new Dialog();
        dialog.setWidth("520px");
        dialog.setMaxWidth("92vw");
        dialog.setCloseOnOutsideClick(true);

        // Outer Modal Container
        Div modalContainer = new Div();
        modalContainer.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("border-radius", "20px")
                .set("overflow", "hidden")
                .set("position", "relative")
                .set("background-color", "#ffffff");

        // Top Banner Image with Close Button
        Div imageBanner = new Div();
        imageBanner.getStyle()
                .set("position", "relative")
                .set("width", "100%")
                .set("height", "240px")
                .set("background-color", "#0f172a");

        Image modalImg = createReportImage(dto.getPhotoUrl(), getWasteTypeTitle(dto), dto.getWasteType());
        modalImg.getStyle()
                .set("width", "100%")
                .set("height", "100%")
                .set("object-fit", "cover");

        // Close X Button
        Button closeBtn = new Button("✕");
        closeBtn.getStyle()
                .set("position", "absolute")
                .set("top", "12px")
                .set("right", "12px")
                .set("width", "36px")
                .set("height", "36px")
                .set("border-radius", "50%")
                .set("background", "rgba(255, 255, 255, 0.85)")
                .set("color", "#334155")
                .set("border", "none")
                .set("font-size", "16px")
                .set("font-weight", "700")
                .set("cursor", "pointer")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("backdrop-filter", "blur(4px)");

        closeBtn.addClickListener(e -> dialog.close());

        imageBanner.add(modalImg, closeBtn);

        // Body Content
        Div bodyContent = new Div();
        bodyContent.getStyle()
                .set("padding", "20px 24px")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "12px");

        // Title
        H3 modalTitle = new H3(getWasteTypeTitle(dto));
        modalTitle.getStyle()
                .set("font-size", "20px")
                .set("font-weight", "800")
                .set("color", "#0f172a")
                .set("margin", "0");

        // Badges Row
        Div modalBadges = new Div();
        modalBadges.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "8px")
                .set("flex-wrap", "wrap");

        modalBadges.add(
                buildStatusBadge(dto.getReportStatus()),
                buildSeverityBadge(dto.getSeverityLevel()),
                buildCategoryBadge(dto.getWasteType())
        );

        // Description Paragraph
        Paragraph descPara = new Paragraph(
                (dto.getDescription() != null && !dto.getDescription().isBlank())
                        ? dto.getDescription()
                        : "Ushbu ifloslik joyi yuzasidan xabar qoldirilgan."
        );
        descPara.getStyle()
                .set("font-size", "14px")
                .set("color", "#475569")
                .set("line-height", "1.5")
                .set("margin", "4px 0");

        // Location & Date Row
        HorizontalLayout locDateRow = new HorizontalLayout();
        locDateRow.setWidthFull();
        locDateRow.setJustifyContentMode(JustifyContentMode.BETWEEN);
        locDateRow.setAlignItems(Alignment.CENTER);

        Span modalLoc = new Span("📍 " + formatLocation(dto));
        modalLoc.getStyle()
                .set("font-size", "13px")
                .set("color", "#64748b")
                .set("font-weight", "500");

        String dateStr = (dto.getDate() != null) ? dto.getDate().format(dateFormatter) : "";
        Span modalDate = new Span(dateStr);
        modalDate.getStyle()
                .set("font-size", "13px")
                .set("color", "#94a3b8")
                .set("font-weight", "500");

        locDateRow.add(modalLoc, modalDate);

        // Bottom Yellow Alert Box ("+15 tanga hisoblandi")
        Div yellowRewardBox = new Div();
        yellowRewardBox.getStyle()
                .set("background-color", "#fef9c3")
                .set("border", "1px solid #fef08a")
                .set("border-radius", "12px")
                .set("padding", "12px 18px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "10px")
                .set("margin-top", "8px");

        Span coinEmoji = new Span("🪙");
        coinEmoji.getStyle().set("font-size", "20px");

        int coins = (dto.getRewardCoins() != null) ? dto.getRewardCoins() : 15;
        Span rewardText = new Span("+" + coins + " tanga hisoblandi");
        rewardText.getStyle()
                .set("font-size", "14px")
                .set("font-weight", "700")
                .set("color", "#854d0e");

        yellowRewardBox.add(coinEmoji, rewardText);

        bodyContent.add(modalTitle, modalBadges, descPara, locDateRow, yellowRewardBox);

        modalContainer.add(imageBanner, bodyContent);
        dialog.add(modalContainer);
        dialog.open();
    }

    /* =========================================================================
     * 6. BADGE & UI UTILITY METHODS
     * ========================================================================= */
    private Span buildStatusBadge(ReportStatus status) {
        Span badge = new Span();
        badge.getStyle()
                .set("font-size", "12px")
                .set("font-weight", "700")
                .set("padding", "4px 12px")
                .set("border-radius", "14px")
                .set("line-height", "1.2");

        if (status == null) status = ReportStatus.PENDING;

        switch (status) {
            case RESOLVED -> {
                badge.setText("Hal qilindi");
                badge.getStyle().set("background-color", "#e0f2fe").set("color", "#0284c7");
            }
            case IN_PROGRESS -> {
                badge.setText("Tasdiqlandi");
                badge.getStyle().set("background-color", "#dcfce7").set("color", "#16a34a");
            }
            case REJECTED -> {
                badge.setText("Rad etildi");
                badge.getStyle().set("background-color", "#fef2f2").set("color", "#dc2626");
            }
            default -> {
                badge.setText("Ko'rib chiqilmoqda");
                badge.getStyle().set("background-color", "#fef3c7").set("color", "#d97706");
            }
        }
        return badge;
    }

    private Span buildSeverityBadge(SeverityLevelEnum severity) {
        Span badge = new Span();
        badge.getStyle()
                .set("font-size", "12px")
                .set("font-weight", "600")
                .set("padding", "3px 10px")
                .set("border-radius", "12px");

        if (severity == SeverityLevelEnum.HIGH) {
            badge.setText("Yuqori");
            badge.getStyle().set("background-color", "#fef2f2").set("color", "#ef4444");
        } else if (severity == SeverityLevelEnum.MEDIUM) {
            badge.setText("O'rta");
            badge.getStyle().set("background-color", "#fff7ed").set("color", "#ea580c");
        } else {
            badge.setText("Past");
            badge.getStyle().set("background-color", "#f0fdf4").set("color", "#16a34a");
        }

        return badge;
    }

    private Span buildCategoryBadge(WasteTypeEnum wasteType) {
        Span badge = new Span();
        badge.getStyle()
                .set("font-size", "12px")
                .set("font-weight", "500")
                .set("padding", "3px 10px")
                .set("border-radius", "12px")
                .set("background-color", "#f1f5f9")
                .set("color", "#475569");

        String label = (wasteType != null) ? wasteType.name() : "Chiqindi";
        if (wasteType == WasteTypeEnum.PLASTIC) label = "Qattiq chiqindi";
        else if (wasteType == WasteTypeEnum.ILLEGAL_DUMP) label = "Noqonuniy tashlash";
        else if (wasteType == WasteTypeEnum.WATER) label = "Suv ifloslanishi";
        else if (wasteType == WasteTypeEnum.AIR) label = "Havo ifloslanishi";

        badge.setText(label);
        return badge;
    }

    private String getWasteTypeTitle(MyReportResponseDto dto) {
        if (dto.getWasteType() == WasteTypeEnum.PLASTIC) {
            return "Plastik chiqindi to'plami";
        } else if (dto.getWasteType() == WasteTypeEnum.ILLEGAL_DUMP) {
            return "Noqonuniy tashlash — avtomobil qismlari";
        } else if (dto.getWasteType() == WasteTypeEnum.WATER) {
            return "Suv havzasi ifloslanishi";
        } else if (dto.getWasteType() == WasteTypeEnum.AIR) {
            return "Havo ifloslanishi — zavod tutuni";
        }
        return "Ifloslik xabari";
    }

    private String formatLocation(MyReportResponseDto dto) {
        StringBuilder sb = new StringBuilder();
        if (dto.getDistrict() != null && !dto.getDistrict().isBlank()) {
            sb.append(dto.getDistrict());
        }
        if (dto.getStreetAddress() != null && !dto.getStreetAddress().isBlank()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(dto.getStreetAddress());
        }
        if (sb.isEmpty()) {
            return (dto.getRegion() != null && !dto.getRegion().isBlank()) ? dto.getRegion() : "Toshkent shahri";
        }
        return sb.toString();
    }

    private Div buildEmptyStateView() {
        Div emptyBox = new Div();
        emptyBox.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("padding", "60px 20px")
                .set("background-color", "#ffffff")
                .set("border-radius", "16px")
                .set("border", "1px solid #e2e8f0")
                .set("gap", "12px");

        Span icon = new Span("📋");
        icon.getStyle().set("font-size", "44px");

        H3 emptyTitle = new H3("Xabarlar topilmadi");
        emptyTitle.getStyle().set("font-size", "18px").set("color", "#0f172a").set("margin", "0");

        Span emptySub = new Span("Hozircha siz yuborgan bunday ifloslik xabarlari mavjud emas.");
        emptySub.getStyle().set("font-size", "14px").set("color", "#64748b");

        emptyBox.add(icon, emptyTitle, emptySub);
        return emptyBox;
    }

    /* =========================================================================
     * 7. PAGINATION CONTROLS
     * ========================================================================= */
    private void buildPaginationControls() {
        paginationControls.setWidthFull();
        paginationControls.setJustifyContentMode(JustifyContentMode.CENTER);
        paginationControls.setAlignItems(Alignment.CENTER);
        paginationControls.getStyle()
                .set("margin-top", "24px")
                .set("gap", "12px");
    }

    private void updatePaginationControls(Page<MyReportResponseDto> pageData) {
        paginationControls.removeAll();

        if (pageData.getTotalPages() <= 1) {
            return;
        }

        Button prevBtn = new Button("← Avvalgi");
        prevBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        prevBtn.setEnabled(pageData.hasPrevious());
        prevBtn.addClickListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                refreshViewData();
            }
        });

        Span pageInfo = new Span((currentPage + 1) + " / " + pageData.getTotalPages());
        pageInfo.getStyle()
                .set("font-size", "14px")
                .set("font-weight", "600")
                .set("color", "#475569");

        Button nextBtn = new Button("Keyingi →");
        nextBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        nextBtn.setEnabled(pageData.hasNext());
        nextBtn.addClickListener(e -> {
            if (currentPage < pageData.getTotalPages() - 1) {
                currentPage++;
                refreshViewData();
            }
        });

        paginationControls.add(prevBtn, pageInfo, nextBtn);
    }
}
