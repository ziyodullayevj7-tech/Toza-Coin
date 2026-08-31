package teamwork.view.user;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import teamwork.config.CustomUserDetails;
import teamwork.util.SecurityUtils;
import teamwork.view.layout.user.UserLayout;

import java.util.Optional;

@Route(value = "user/dashboard", layout = UserLayout.class)
@PageTitle("Boshqaruv paneli | TozaCoin")
@PermitAll
@CssImport("./themes/tozacoin/user-layout.css")
public class UserDashboard extends VerticalLayout {

    // Ready for future Service & Repository binding
    public record UserBannerInfo(String fullName, String tierName, String tierEmoji, int rank, int coinBalance) {}

    public UserDashboard() {
        setWidthFull();
        setSpacing(false);
        setPadding(false);
        addClassName("dashboard-content-wrapper");

        // 1. Top Green Welcome Banner (Service/Repo Ready)
        add(buildWelcomeBanner(null));

        // 2. 4 Metrics Cards Grid
        add(buildMetricsGrid());

        // 3. Middle Section: Weekly Activity & Notifications Grid
        add(buildMiddleRowGrid());

        // 4. Lower Section: Recent Activity & Nearby Issues Grid
        add(buildLowerRowGrid());

        // 5. Bottom Section: Recommended Campaigns
        add(buildRecommendedCampaignsSection());
    }

    /* ------------------------------------------------------------------------
     * 1. Welcome Green Banner Block (Service / Repo Ready)
     * ------------------------------------------------------------------------ */
    public Div buildWelcomeBanner(UserBannerInfo info) {
        if (info == null) {
            Optional<CustomUserDetails> userOpt = SecurityUtils.getCurrentUser();
            String fullName = userOpt
                    .map(u -> {
                        String name = u.getName() != null ? u.getName() : "";
                        String surname = u.getSurname() != null ? u.getSurname() : "";
                        return (name + " " + surname).trim();
                    })
                    .filter(s -> !s.isEmpty())
                    .orElse("Jasur Nazarov");

            info = new UserBannerInfo(fullName, "Kumush daraja", "🏅", 47, 4820);
        }

        Div banner = new Div();
        banner.addClassName("welcome-banner");

        // Left Content
        Div leftContent = new Div();
        leftContent.addClassName("welcome-left");

        Span greetingSub = new Span("Xush kelibsiz 👋");
        greetingSub.addClassName("welcome-sub-greeting");

        H2 userNameH2 = new H2(info.fullName());
        userNameH2.addClassName("welcome-user-name");

        Div badgesRow = new Div();
        badgesRow.addClassName("wb-badges-container");

        Span tierBadge = new Span(info.tierEmoji() + " " + info.tierName());
        tierBadge.addClassName("wb-tier-pill");

        Span rankBadge = new Span("Umumiy reyting: #" + info.rank());
        rankBadge.addClassName("wb-rank-text");

        badgesRow.add(tierBadge, rankBadge);
        leftContent.add(greetingSub, userNameH2, badgesRow);

        // Right Balance Block
        Div rightBalance = new Div();
        rightBalance.addClassName("welcome-right-balance");

        Div amountRow = new Div();
        amountRow.addClassName("balance-amount-row");

        Div coinIcon = new Div(new Span("🪙"));
        coinIcon.addClassName("coin-icon-circle");

        Div numberCol = new Div();
        numberCol.addClassName("balance-number-col");

        Span balanceNumber = new Span(String.format("%,d", info.coinBalance()));
        balanceNumber.addClassName("balance-number");

        Span balanceSub = new Span("Tangalar balansi");
        balanceSub.addClassName("balance-label-sub");

        numberCol.add(balanceNumber, balanceSub);
        amountRow.add(coinIcon, numberCol);

        Button btnHamyon = new Button("Hamyon →");
        btnHamyon.addClassName("btn-hamyon-action");
        btnHamyon.addClickListener(e -> UI.getCurrent().navigate(WalletView.class));

        rightBalance.add(amountRow, btnHamyon);
        banner.add(leftContent, rightBalance);
        return banner;
    }

    /* ------------------------------------------------------------------------
     * 2. 4 Metric Cards Grid Block
     * ------------------------------------------------------------------------ */
    private Div buildMetricsGrid() {
        Div grid = new Div();
        grid.addClassName("metrics-grid");

        grid.add(
                createMetricCard("📍", "pink", "47", "cyan-text", "Yuborilgan xabarlar"),
                createMetricCard("🧹", "green", "23", "green-text", "Tozalanganlar"),
                createMetricCard("🪙", "amber", "4,820", "amber-text", "Ishlangan tanga"),
                createMetricCard("⚡", "purple", "892", "purple-text", "Ta'sir ko'rsatkichi")
        );

        return grid;
    }

    private Div createMetricCard(String emojiIcon, String iconTheme, String value, String valueTheme, String label) {
        Div card = new Div();
        card.addClassName("metric-card");

        Div iconBox = new Div(new Span(emojiIcon));
        iconBox.addClassNames("metric-icon-box", iconTheme);

        Span valueSpan = new Span(value);
        valueSpan.addClassNames("metric-value", valueTheme);

        Span labelSpan = new Span(label);
        labelSpan.addClassName("metric-label");

        card.add(iconBox, valueSpan, labelSpan);
        return card;
    }

    /* ------------------------------------------------------------------------
     * 3. Middle Section: Weekly Activity & Notifications Grid
     * ------------------------------------------------------------------------ */
    private Div buildMiddleRowGrid() {
        Div bottomGrid = new Div();
        bottomGrid.addClassName("dashboard-bottom-grid");

        bottomGrid.add(buildWeeklyActivityCard(), buildNotificationsCard());
        return bottomGrid;
    }

    private Div buildWeeklyActivityCard() {
        Div chartCard = new Div();
        chartCard.addClassName("dashboard-card");

        Div chartHeader = new Div();
        chartHeader.addClassName("card-header-row");

        H2 chartTitle = new H2("Haftalik faoliyat");
        chartTitle.addClassName("card-title");

        Div legend = new Div();
        legend.addClassName("card-legend");

        Span dot1 = new Span();
        dot1.addClassNames("legend-dot", "green");
        Span l1 = new Span(dot1, new Span(" Xabarlar"));

        Span dot2 = new Span();
        dot2.addClassNames("legend-dot", "amber");
        Span l2 = new Span(dot2, new Span(" Tozalanganlar"));

        legend.add(l1, l2);
        chartHeader.add(chartTitle, legend);

        // Chart Bar Visualization
        Div barsContainer = new Div();
        barsContainer.addClassName("chart-bars-container");

        String[] days = {"Du", "Se", "Ch", "Pa", "Ju", "Sh", "Ya"};
        int[][] heights = {
                {40, 20}, {30, 25}, {75, 45}, {25, 15}, {55, 30}, {100, 60}, {45, 20}
        };

        for (int i = 0; i < days.length; i++) {
            Div dayCol = new Div();
            dayCol.addClassName("chart-day-col");

            Div stack = new Div();
            stack.addClassName("chart-bar-stack");

            Div greenBar = new Div();
            greenBar.addClassNames("bar-segment", "green");
            greenBar.getStyle().set("height", heights[i][0] + "px");

            Div amberBar = new Div();
            amberBar.addClassNames("bar-segment", "amber");
            amberBar.getStyle().set("height", heights[i][1] + "px");

            stack.add(greenBar, amberBar);
            Span dayLabel = new Span(days[i]);
            dayLabel.addClassName("day-label");

            dayCol.add(stack, dayLabel);
            barsContainer.add(dayCol);
        }

        chartCard.add(chartHeader, barsContainer);
        return chartCard;
    }

    private Div buildNotificationsCard() {
        Div notificationsCard = new Div();
        notificationsCard.addClassName("dashboard-card");

        Div notifHeader = new Div();
        notifHeader.addClassName("card-header-row");

        H2 notifTitle = new H2("Bildirishnomalar");
        notifTitle.addClassName("card-title");

        Span countBadge = new Span("2 yangi");
        countBadge.addClassName("nav-badge-pill");
        countBadge.getStyle().set("background-color", "#fef2f2").set("color", "#ef4444");

        notifHeader.add(notifTitle, countBadge);

        Div notifList = new Div();
        notifList.addClassName("notifications-list");

        notifList.add(
                createNotificationItem("🪙", "amber", "50 tanga hisoblandi — Park tozalash", "2 soat oldin", true),
                createNotificationItem("✅", "green", "Hisobotingiz tasdiqlandi", "5 soat oldin", true),
                createNotificationItem("📣", "pink", "Yangi kampaniya: Amudaryo", "1 kun oldin", false)
        );

        notificationsCard.add(notifHeader, notifList);
        return notificationsCard;
    }

    private Div createNotificationItem(String emojiIcon, String iconTheme, String title, String time, boolean isUnread) {
        Div item = new Div();
        item.addClassName("notification-item");

        Div left = new Div();
        left.addClassName("notification-left");

        Div iconBadge = new Div(new Span(emojiIcon));
        iconBadge.addClassNames("notification-icon-badge", iconTheme);

        Div body = new Div();
        body.addClassName("notification-body");

        Span titleSpan = new Span(title);
        titleSpan.addClassName("notification-title");

        Span timeSpan = new Span(time);
        timeSpan.addClassName("notification-time");

        body.add(titleSpan, timeSpan);
        left.add(iconBadge, body);

        item.add(left);

        if (isUnread) {
            Div dot = new Div();
            dot.addClassName("unread-dot");
            item.add(dot);
        }

        return item;
    }

    /* ------------------------------------------------------------------------
     * 4. Lower Section: Recent Activity & Nearby Issues Grid
     * ------------------------------------------------------------------------ */
    private Div buildLowerRowGrid() {
        Div grid = new Div();
        grid.addClassName("dashboard-bottom-grid");

        grid.add(buildRecentActivityCard(), buildNearbyIssuesCard());
        return grid;
    }

    private Div buildRecentActivityCard() {
        Div card = new Div();
        card.addClassName("dashboard-card");

        Div headerRow = new Div();
        headerRow.addClassName("card-header-row");

        H2 title = new H2("So'nggi faoliyat");
        title.addClassName("card-title");

        Anchor link = new Anchor("#", "Barchasini ko'rish");
        link.addClassName("card-link-action");

        headerRow.add(title, link);

        Div list = new Div();
        list.addClassName("activity-list");

        list.add(
                createActivityItem("📍", "Plastik chiqindi — Yunusobod", "Ko'rib chiqilmoqda", "yellow", "Bugun, 14:23", "+15"),
                createActivityItem("🧹", "Park tozalash — Mirzo Ulug'bek", "Tasdiqlandi", "green", "Kecha, 10:15", "+50"),
                createActivityItem("📍", "Suv ifloslanishi — Chirchiq", "Xabarlandi", "blue", "3 kun oldin", "+15")
        );

        card.add(headerRow, list);
        return card;
    }

    private Div createActivityItem(String emojiIcon, String title, String statusText, String statusTheme, String time, String coins) {
        Div item = new Div();
        item.addClassName("activity-item");

        Div left = new Div();
        left.addClassName("activity-left");

        Div iconBadge = new Div(new Span(emojiIcon));
        iconBadge.addClassName("activity-icon-badge");

        Div details = new Div();
        details.addClassName("activity-details");

        Span titleSpan = new Span(title);
        titleSpan.addClassName("activity-title");

        Div statusRow = new Div();
        statusRow.addClassName("activity-status-row");

        Span statusSpan = new Span(statusText);
        statusSpan.addClassNames("status-text", statusTheme);

        Span timeSpan = new Span(" • " + time);
        timeSpan.addClassName("activity-time");

        statusRow.add(statusSpan, timeSpan);
        details.add(titleSpan, statusRow);
        left.add(iconBadge, details);

        Span coinsSpan = new Span("🪙 " + coins);
        coinsSpan.addClassName("activity-coins");

        item.add(left, coinsSpan);
        return item;
    }

    private Div buildNearbyIssuesCard() {
        Div card = new Div();
        card.addClassName("dashboard-card");

        Div headerRow = new Div();
        headerRow.addClassName("card-header-row");

        H2 title = new H2("Yaqin atrofdagi muammolar");
        title.addClassName("card-title");

        Anchor link = new Anchor("#", "Xaritada ko'rish →");
        link.addClassName("card-link-action");

        headerRow.add(title, link);

        // Map Preview Container
        Div mapPreview = new Div();
        mapPreview.addClassName("dashboard-map-preview");

        Div centerBadge = new Div(new Span("🗺️ 3 ta muammo topildi"));
        centerBadge.addClassName("map-badge-center");
        mapPreview.add(centerBadge);

        // Issues List Below Map
        Div issuesList = new Div();
        issuesList.addClassName("issues-list");

        issuesList.add(
                createIssueItem("red", "Plastik chiqindi", "Yunusobod, 500m", "Yuqori", "red"),
                createIssueItem("orange", "Noqonuniy tashlash", "Sergeli, 1.2km", "O'rta", "orange"),
                createIssueItem("green", "Jamoat ifloslanishi", "Chilonzor, 2.1km", "Past", "green")
        );

        card.add(headerRow, mapPreview, issuesList);
        return card;
    }

    private Div createIssueItem(String dotTheme, String title, String distance, String priorityText, String priorityTheme) {
        Div item = new Div();
        item.addClassName("issue-item");

        Div left = new Div();
        left.addClassName("issue-left");

        Div dot = new Div();
        dot.addClassNames("issue-dot", dotTheme);

        Div info = new Div();
        info.addClassName("issue-info");

        Span titleSpan = new Span(title);
        titleSpan.addClassName("issue-title");

        Span subSpan = new Span(distance);
        subSpan.addClassName("issue-sub");

        info.add(titleSpan, subSpan);
        left.add(dot, info);

        Span priorityPill = new Span(priorityText);
        priorityPill.addClassNames("priority-pill", priorityTheme);

        item.add(left, priorityPill);
        return item;
    }

    /* ------------------------------------------------------------------------
     * 5. Bottom Section: Recommended Campaigns
     * ------------------------------------------------------------------------ */
    private Div buildRecommendedCampaignsSection() {
        Div section = new Div();
        section.addClassName("dashboard-card");

        Div headerRow = new Div();
        headerRow.addClassName("card-header-row");

        H2 title = new H2("Tavsiya etilgan kampaniyalar");
        title.addClassName("card-title");

        Anchor link = new Anchor("#", "Barchasini ko'rish");
        link.addClassName("card-link-action");

        headerRow.add(title, link);

        Div campaignsGrid = new Div();
        campaignsGrid.addClassName("campaigns-grid");

        campaignsGrid.add(
                createCampaignCard("Chirchiq daryosi tozalash", "15 Avg", 84, 68),
                createCampaignCard("Parklar haftaligi", "22 Avg", 120, 45)
        );

        section.add(headerRow, campaignsGrid);
        return section;
    }

    private Div createCampaignCard(String title, String dateText, int attendeesCount, int progressPercent) {
        Div card = new Div();
        card.addClassName("campaign-card");

        // Top Row
        Div topRow = new Div();
        topRow.addClassName("campaign-top-row");

        H2 titleH2 = new H2(title);
        titleH2.addClassName("campaign-title");

        Span dateBadge = new Span("🗓️ " + dateText);
        dateBadge.addClassName("campaign-date-badge");

        topRow.add(titleH2, dateBadge);

        // Attendees Row
        Span attendeesSpan = new Span("👥 " + attendeesCount + " ishtirokchi");
        attendeesSpan.addClassName("campaign-attendees");

        // Progress Bar Block
        Div progressBlock = new Div();
        progressBlock.addClassName("campaign-progress-block");

        Div labelRow = new Div();
        labelRow.addClassName("progress-label-row");
        Span maqsadSpan = new Span("Maqsad");
        Span percentSpan = new Span(progressPercent + "%");
        labelRow.add(maqsadSpan, percentSpan);

        Div track = new Div();
        track.addClassName("progress-track");

        Div fill = new Div();
        fill.addClassName("progress-fill");
        fill.getStyle().set("width", progressPercent + "%");
        track.add(fill);

        progressBlock.add(labelRow, track);

        // Join Button
        Button btnJoin = new Button("Qo'shilish");
        btnJoin.addClassName("btn-join-campaign");
        btnJoin.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        card.add(topRow, attendeesSpan, progressBlock, btnJoin);
        return card;
    }
}
