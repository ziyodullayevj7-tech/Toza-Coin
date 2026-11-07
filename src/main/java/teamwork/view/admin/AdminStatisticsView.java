package teamwork.view.admin;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import teamwork.dto.leaderBoard.LeaderBoardDto;
import teamwork.enums.ProfileStatus;
import teamwork.i18n.I18n;
import teamwork.service.AdminService;
import teamwork.view.layout.admin.AdminLayout;
import teamwork.view.layout.admin.AdminPage;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Route(value = "admin/statistics", layout = AdminLayout.class)
@RolesAllowed("ADMIN")
public class AdminStatisticsView extends AdminPage implements com.vaadin.flow.router.HasDynamicTitle {
    private static final int PAGE_SIZE = 10;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final AdminService adminService;
    private final Grid<LeaderBoardDto> grid = new Grid<>();
    private final Div leaders = new Div();
    private final Div tablePanel = new Div();
    private final Div error = new Div();
    private final Span count = new Span();
    private final Span pageSummary = new Span();
    private final Paragraph empty = I18n.localize(new Paragraph("Foydalanuvchilar topilmadi."));
    private final Button refresh = I18n.localize(new Button("Yangilash", VaadinIcon.REFRESH.create()));
    private final Button previous = I18n.localize(new Button("Avvalgi"));
    private final Button next = I18n.localize(new Button("Keyingi"));
    private List<LeaderBoardDto> users = List.of();
    private int currentPage;
    private int totalPages;
    private long totalUsers;
    private boolean loaded;

    public AdminStatisticsView(AdminService adminService) {
        super("Statistika", "Compare user reports, earned coins, balances and activity.");
        this.adminService = adminService;
        addClassName("admin-statistics-page");
        Div toolbar = new Div(I18n.localize(new Paragraph("Ranked by total earned coins")), refresh);
        toolbar.addClassName("admin-statistics-toolbar");
        refresh.setId("statistics-refresh");
        refresh.setDisableOnClick(true);
        refresh.addClickListener(event -> loadPage(0));
        leaders.addClassName("admin-statistics-leaders");
        leaders.setId("statistics-leaders");
        I18n.attribute(leaders, "aria-label", "Top contributors");
        error.addClassName("admin-statistics-error");
        error.getElement().setAttribute("role", "alert");
        error.add(I18n.localize(new Paragraph("Foydalanuvchilarni yuklab bo'lmadi. Qayta urinib ko'ring.")));
        error.setVisible(false);
        configureGrid();
        Div tableHeading = new Div(I18n.localize(new H2("User statistics")),
                new Div(count, I18n.localize(new Span("foydalanuvchi"))));
        tableHeading.addClassName("admin-statistics-table-heading");
        empty.addClassName("admin-statistics-empty");
        tablePanel.addClassNames("admin-statistics-table-panel", "admin-users-panel", "admin-data-panel");
        tablePanel.add(tableHeading, grid, empty);
        Div pagination = new Div(previous, pageSummary, next);
        pagination.addClassName("admin-statistics-pagination");
        pageSummary.getElement().setAttribute("role", "status");
        previous.setId("statistics-previous-page");
        next.setId("statistics-next-page");
        previous.addClickListener(event -> loadPage(currentPage - 1));
        next.addClickListener(event -> loadPage(currentPage + 1));
        add(toolbar, error, leaders, tablePanel, pagination);
        loadPage(0);
    }

    private void configureGrid() {
        grid.setId("statistics-users-grid");
        grid.addClassNames("admin-users-grid", "admin-data-grid");
        grid.setWidthFull();
        grid.setAllRowsVisible(true);
        I18n.attribute(grid, "aria-label", "User statistics");
        grid.addColumn(user -> currentPage * PAGE_SIZE + users.indexOf(user) + 1)
                .setTextAlign(ColumnTextAlign.CENTER).setHeader(header("Reyting")).setWidth("85px").setFlexGrow(0);
        grid.addComponentColumn(this::userCell).setHeader(header("Foydalanuvchi"))
                .setWidth("240px").setFlexGrow(1);
        grid.addColumn(user -> number(user.getReportCount()))
                .setTextAlign(ColumnTextAlign.END).setHeader(header("Xabarlar"))
                .setWidth("110px").setFlexGrow(0);
        grid.addColumn(user -> number(user.getTotalEarnedCoins()))
                .setTextAlign(ColumnTextAlign.END).setHeader(header("Jami ishlab topilgan"))
                .setWidth("175px").setFlexGrow(0);
        grid.addColumn(user -> number(user.getCoinBalance()))
                .setTextAlign(ColumnTextAlign.END).setHeader(header("Coin balance"))
                .setWidth("135px").setFlexGrow(0);
        grid.addColumn(user -> number(user.getStreakDays()))
                .setTextAlign(ColumnTextAlign.END).setHeader(header("Streak (days)"))
                .setWidth("130px").setFlexGrow(0);
        grid.addComponentColumn(user -> status(user.getStatus())).setHeader(header("Holat"))
                .setWidth("125px").setFlexGrow(0);
        grid.addColumn(user -> user.getCreatedDate() == null ? "—" : DATE_FORMAT.format(user.getCreatedDate()))
                .setHeader(header("Qo'shilgan")).setWidth("130px").setFlexGrow(0);
    }

    private Span header(String text) { return I18n.localize(new Span(text)); }

    private Div userCell(LeaderBoardDto user) {
        Span avatar = new Span(initials(user.getFullName()));
        avatar.addClassName("admin-user-avatar");
        avatar.getElement().setAttribute("aria-hidden", "true");
        Span name = I18n.userText(new Span(), nonBlank(user.getFullName()), "Noma'lum foydalanuvchi");
        name.addClassName("admin-user-name");
        name.setTitle(user.getFullName() == null ? "" : user.getFullName());
        Span location = I18n.userText(new Span(), nonBlank(user.getDistrict()), "Manzil ko'rsatilmagan");
        location.addClassName("admin-user-location");
        Div identity = new Div(name, location);
        identity.addClassName("admin-user-identity");
        Div cell = new Div(avatar, identity);
        cell.addClassName("admin-user-cell");
        return cell;
    }

    private Span status(ProfileStatus value) {
        String label = value == ProfileStatus.ACTIVE ? "Faol" : value == ProfileStatus.BLOCKED
                ? "Bloklangan" : value == ProfileStatus.IN_REGISTRATION ? "Kutilmoqda" : "Ma'lumot yo'q";
        Span badge = I18n.localize(new Span(label));
        badge.addClassNames("admin-user-status", value == ProfileStatus.ACTIVE ? "active"
                : value == ProfileStatus.BLOCKED ? "blocked" : value == ProfileStatus.IN_REGISTRATION ? "pending" : "unknown");
        return badge;
    }

    private void loadPage(int requestedPage) {
        refresh.setEnabled(false);
        previous.setEnabled(false);
        next.setEnabled(false);
        try {
            // Reuse the established ranking and DTO mapping; never rank a single page locally.
            Page<LeaderBoardDto> first = adminService.getUsers(0, PAGE_SIZE);
            int target = Math.max(0, Math.min(requestedPage, Math.max(0, first.getTotalPages() - 1)));
            Page<LeaderBoardDto> page = target == 0 ? first : adminService.getUsers(target, PAGE_SIZE);
            if (page.isEmpty() && target > 0) {
                page = first;
                target = 0;
            }
            currentPage = target;
            totalPages = page.getTotalPages();
            totalUsers = page.getTotalElements();
            users = List.copyOf(page.getContent());
            grid.setItems(users);
            count.setText(number(totalUsers));
            leaders.removeAll();
            int rank = 1;
            for (LeaderBoardDto leader : first.getContent().stream().limit(3).toList()) {
                Div card = new Div(new Span("#" + rank++), userCell(leader),
                        new Span(number(leader.getTotalEarnedCoins())), header("Jami ishlab topilgan"));
                card.addClassName("admin-statistics-leader");
                leaders.add(card);
            }
            leaders.setVisible(!first.isEmpty());
            grid.setVisible(!users.isEmpty());
            empty.setVisible(users.isEmpty());
            tablePanel.setVisible(true);
            error.setVisible(false);
            loaded = true;
        } catch (RuntimeException exception) {
            LoggerFactory.getLogger(getClass()).error("Could not load admin user statistics", exception);
            loaded = false;
            users = List.of();
            grid.setItems(users);
            leaders.removeAll();
            leaders.setVisible(false);
            tablePanel.setVisible(false);
            error.setVisible(true);
        } finally {
            refresh.setEnabled(true);
            I18n.text(pageSummary, loaded ? I18n.format(Locale.ROOT,
                    "Sahifa %d / %d · %s ta foydalanuvchi", totalPages == 0 ? 0 : currentPage + 1,
                    totalPages, number(totalUsers)) : "");
            previous.setEnabled(loaded && currentPage > 0);
            next.setEnabled(loaded && currentPage + 1 < totalPages);
        }
    }

    private static String number(Number value) {
        return NumberFormat.getIntegerInstance(Locale.ROOT).format(value == null ? 0 : value.longValue());
    }

    private static String nonBlank(String value) { return value == null || value.isBlank() ? null : value; }

    private static String initials(String fullName) {
        if (nonBlank(fullName) == null) return "TC";
        String[] parts = fullName.trim().split("\\s+");
        return (parts[0].substring(0, 1) + (parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : ""))
                .toUpperCase(Locale.ROOT);
    }

    @Override
    public String getPageTitle() { return I18n.t("Statistika | TozaCoin"); }
}
