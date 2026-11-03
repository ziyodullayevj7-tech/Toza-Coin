package teamwork.view.admin;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import teamwork.dto.leaderBoard.LeaderBoardDto;
import teamwork.enums.ProfileStatus;
import teamwork.service.AdminService;
import teamwork.view.layout.admin.AdminLayout;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Route(value = "admin/users", layout = AdminLayout.class)
@PageTitle("Foydalanuvchilar | TozaCoin")
@RolesAllowed("ADMIN")
@CssImport("./themes/tozacoin/user-layout.css")
public class AdminUsersView extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(AdminUsersView.class);
    private static final int PAGE_SIZE = 10;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final AdminService adminService;
    private final Grid<LeaderBoardDto> usersGrid = new Grid<>();
    private final List<LeaderBoardDto> pageUsers = new ArrayList<>();
    private final TextField searchField = new TextField();
    private final Div statusFilters = new Div();
    private final Div errorMessage = new Div();
    private final Span totalUsers = new Span("0");
    private final Span emptyMessage = new Span("Foydalanuvchilar topilmadi.");
    private final Span pageSummary = new Span();
    private final Button previousButton = new Button("← Oldingi");
    private final Button nextButton = new Button("Keyingi →");

    private int currentPage;
    private int pageCount;
    private long totalCount;
    private ProfileStatus selectedStatus;

    @Autowired
    public AdminUsersView(AdminService adminService) {
        this.adminService = adminService;
        setWidthFull();
        setPadding(false);
        setSpacing(false);
        addClassName("admin-users-page");

        add(buildPageHeader(), buildToolbar(), buildUsersPanel(), buildPagination());
        configureSearch();
        loadUsers();
    }

    private Div buildPageHeader() {
        Div header = new Div();
        header.addClassName("admin-users-header");

        Div titleBlock = new Div();
        H2 title = new H2("Foydalanuvchilar");
        title.addClassName("admin-users-title");
        Paragraph subtitle = new Paragraph("Barcha ro'yxatdan o'tgan foydalanuvchilar");
        subtitle.addClassName("admin-users-subtitle");
        titleBlock.add(title, subtitle);

        Div countBadge = new Div(new Span("👥"), totalUsers, new Span("foydalanuvchi"));
        countBadge.addClassName("admin-users-count");
        header.add(titleBlock, countBadge);
        return header;
    }

    private Div buildToolbar() {
        Div toolbar = new Div();
        toolbar.addClassName("admin-users-toolbar");

        searchField.setPlaceholder("Ism yoki shahar bo'yicha qidirish (joriy sahifa)");
        searchField.setClearButtonVisible(true);
        searchField.setPrefixComponent(new Span("🔎"));
        searchField.addClassName("admin-users-search");

        statusFilters.addClassName("admin-users-filters");
        updateStatusFilters();

        Button refreshButton = new Button("Yangilash", event -> loadUsers());
        refreshButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        refreshButton.addClassName("admin-users-refresh");
        toolbar.add(searchField, statusFilters, refreshButton);
        return toolbar;
    }

    private Div buildUsersPanel() {
        Div panel = new Div();
        panel.addClassName("admin-users-panel");

        errorMessage.addClassName("admin-users-error");
        errorMessage.setVisible(false);
        emptyMessage.addClassName("admin-users-empty");
        configureGrid();
        panel.add(errorMessage, usersGrid, emptyMessage);
        return panel;
    }

    private HorizontalLayout buildPagination() {
        HorizontalLayout pagination = new HorizontalLayout();
        pagination.addClassName("admin-users-pagination");
        pagination.setWidthFull();
        pagination.setAlignItems(Alignment.CENTER);
        pagination.setJustifyContentMode(JustifyContentMode.BETWEEN);

        previousButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        previousButton.addClickListener(event -> {
            if (currentPage > 0) {
                currentPage--;
                loadUsers();
            }
        });
        nextButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        nextButton.addClickListener(event -> {
            if (currentPage + 1 < pageCount) {
                currentPage++;
                loadUsers();
            }
        });
        pagination.add(previousButton, pageSummary, nextButton);
        return pagination;
    }

    private void configureGrid() {
        usersGrid.addClassName("admin-users-grid");
        usersGrid.setWidthFull();
        usersGrid.setAllRowsVisible(false);
        usersGrid.setPageSize(PAGE_SIZE);
        usersGrid.addColumn(user -> currentPage * PAGE_SIZE
                        + pageUsers.indexOf(user) + 1)
                .setHeader("#")
                .setWidth("56px")
                .setFlexGrow(0);
        usersGrid.addComponentColumn(this::buildUserCell)
                .setHeader("Foydalanuvchi")
                .setFlexGrow(1)
                .setWidth("260px");
        usersGrid.addColumn(user -> "#" + (currentPage * PAGE_SIZE
                        + pageUsers.indexOf(user) + 1))
                .setHeader("Reyting")
                .setWidth("90px")
                .setFlexGrow(0);
        usersGrid.addColumn(user -> formatNumber(user.getCoinBalance()))
                .setHeader("Tanga")
                .setWidth("100px")
                .setFlexGrow(0);
        usersGrid.addColumn(user -> "🔥 " + formatNumber(user.getStreakDays()))
                .setHeader("Seriya")
                .setWidth("95px")
                .setFlexGrow(0);
        usersGrid.addColumn(user -> formatNumber(user.getReportCount()))
                .setHeader("Xabar")
                .setWidth("80px")
                .setFlexGrow(0);
        usersGrid.addComponentColumn(user -> buildStatusBadge(user.getStatus()))
                .setHeader("Holat")
                .setWidth("110px")
                .setFlexGrow(0);
        usersGrid.addComponentColumn(this::buildActions)
                .setHeader("Harakat")
                .setWidth("160px")
                .setFlexGrow(0);
    }

    private Div buildUserCell(LeaderBoardDto user) {
        Div cell = new Div();
        cell.addClassName("admin-user-cell");

        Span avatar = new Span(initials(user.getFullName()));
        avatar.addClassName("admin-user-avatar");
        avatar.addClassName("avatar-" + Math.floorMod(
                Objects.hashCode(user.getProfileId()), 6));

        Div identity = new Div();
        identity.addClassName("admin-user-identity");
        Span name = new Span(nonBlank(user.getFullName(), "Noma'lum foydalanuvchi"));
        name.addClassName("admin-user-name");
        Span location = new Span(nonBlank(user.getDistrict(), "Manzil ko'rsatilmagan"));
        location.addClassName("admin-user-location");
        identity.add(name, location);
        cell.add(avatar, identity);
        return cell;
    }

    private HorizontalLayout buildActions(LeaderBoardDto user) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.addClassName("admin-user-actions");
        actions.setSpacing(false);

        Button viewButton = new Button("Ko'rish", event -> openUserDetails(user));
        viewButton.addClassName("admin-user-view-button");
        viewButton.addThemeVariants(ButtonVariant.LUMO_SMALL);

        Button blockButton = new Button("Blok", event -> confirmBlock(user));
        blockButton.addClassName("admin-user-block-button");
        blockButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
        blockButton.setEnabled(user.getStatus() != ProfileStatus.BLOCKED);
        actions.add(viewButton, blockButton);
        return actions;
    }

    private Span buildStatusBadge(ProfileStatus status) {
        String label = statusLabel(status);
        Span badge = new Span(label);
        badge.addClassName("admin-user-status");
        badge.addClassName(statusClass(status));
        return badge;
    }

    private void updateStatusFilters() {
        statusFilters.removeAll();
        statusFilters.add(
                createStatusFilter("Barchasi", null),
                createStatusFilter("Faol", ProfileStatus.ACTIVE),
                createStatusFilter("Bloklangan", ProfileStatus.BLOCKED),
                createStatusFilter("Kutilmoqda", ProfileStatus.IN_REGISTRATION)
        );
    }

    private Button createStatusFilter(String label, ProfileStatus status) {
        Button filter = new Button(label);
        filter.addClassName("admin-users-filter");
        if (selectedStatus == status) {
            filter.addClassName("active");
        }
        filter.addClickListener(event -> {
            selectedStatus = status;
            updateStatusFilters();
            updateGridItems();
        });
        return filter;
    }

    private void configureSearch() {
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(event -> updateGridItems());
    }

    private void loadUsers() {
        try {
            Page<LeaderBoardDto> usersPage = adminService.getUsers(currentPage, PAGE_SIZE);
            pageUsers.clear();
            pageUsers.addAll(usersPage.getContent());
            totalCount = usersPage.getTotalElements();
            pageCount = usersPage.getTotalPages();
            totalUsers.setText(formatNumber(totalCount));
            errorMessage.setVisible(false);
            errorMessage.setText("");
            updateGridItems();
            updatePagination();
        } catch (RuntimeException exception) {
            log.error("Could not load admin user list", exception);
            pageUsers.clear();
            updateGridItems();
            updatePagination();
            errorMessage.setText("Foydalanuvchilarni yuklab bo'lmadi. Qayta urinib ko'ring.");
            errorMessage.setVisible(true);
            Notification.show("Foydalanuvchilar ro'yxatini yuklashda xatolik yuz berdi.",
                    4500, Notification.Position.TOP_END);
        }
    }

    private void updateGridItems() {
        String query = searchField.getValue() == null
                ? ""
                : searchField.getValue().trim().toLowerCase(Locale.ROOT);
        List<LeaderBoardDto> filteredUsers = pageUsers.stream()
                .filter(user -> selectedStatus == null || user.getStatus() == selectedStatus)
                .filter(user -> query.isEmpty()
                        || contains(user.getFullName(), query)
                        || contains(user.getDistrict(), query))
                .toList();
        usersGrid.setItems(filteredUsers);
        emptyMessage.setVisible(filteredUsers.isEmpty() && !errorMessage.isVisible());
        usersGrid.setVisible(!filteredUsers.isEmpty());
    }

    private void updatePagination() {
        pageSummary.setText(String.format(Locale.ROOT, "Sahifa %d / %d · %s ta foydalanuvchi",
                pageCount == 0 ? 0 : currentPage + 1, pageCount, formatNumber(totalCount)));
        previousButton.setEnabled(currentPage > 0);
        nextButton.setEnabled(currentPage + 1 < pageCount);
    }

    private void openUserDetails(LeaderBoardDto user) {
        Dialog drawer = new Dialog();
        drawer.addClassName("admin-user-profile-dialog");
        drawer.setCloseOnEsc(true);
        drawer.setCloseOnOutsideClick(true);
        drawer.setWidth("min(480px, 100vw)");
        drawer.setHeight("100vh");

        HorizontalLayout drawerHeader = new HorizontalLayout();
        drawerHeader.addClassName("admin-user-drawer-header");
        Span title = new Span("Foydalanuvchi profili");
        title.addClassName("admin-user-drawer-title");
        Button closeButton = new Button("×", event -> drawer.close());
        closeButton.addClassName("admin-user-drawer-close");
        closeButton.setAriaLabel("Profil oynasini yopish");
        drawerHeader.add(title, closeButton);

        VerticalLayout content = new VerticalLayout();
        content.addClassName("admin-user-drawer-content");
        content.setPadding(false);
        content.setSpacing(false);

        Span avatar = new Span(initials(user.getFullName()));
        avatar.addClassName("admin-user-drawer-avatar");
        avatar.addClassName("avatar-" + Math.floorMod(
                Objects.hashCode(user.getProfileId()), 6));
        H2 name = new H2(nonBlank(user.getFullName(), "Noma'lum foydalanuvchi"));
        name.addClassName("admin-user-drawer-name");
        Span location = new Span(nonBlank(user.getDistrict(), "Manzil ko'rsatilmagan"));
        location.addClassName("admin-user-drawer-location");

        VerticalLayout identity = new VerticalLayout(avatar, name, location,
                buildStatusBadge(user.getStatus()));
        identity.addClassName("admin-user-drawer-identity");
        identity.setPadding(false);
        identity.setSpacing(false);
        identity.setAlignItems(Alignment.CENTER);

        Div stats = new Div();
        stats.addClassName("admin-user-stats");
        stats.add(
                statCard("Reyting", "#" + (currentPage * PAGE_SIZE
                        + pageUsers.indexOf(user) + 1), "rank"),
                statCard("Tanga", formatNumber(user.getCoinBalance()), "coins"),
                statCard("Seriya", "🔥 " + formatNumber(user.getStreakDays()) + " kun", "streak"),
                statCard("Xabarlar", formatNumber(user.getReportCount()), "reports"),
                statCard("Jami ishlab topilgan", formatNumber(user.getTotalEarnedCoins()), "earned"),
                statCard("Qo'shilgan", formatDate(user), "joined")
        );

        Button blockButton = new Button(user.getStatus() == ProfileStatus.BLOCKED
                ? "Foydalanuvchi bloklangan" : "⛔ Bloklash",
                event -> confirmBlock(user));
        blockButton.addClassName("admin-user-drawer-block");
        blockButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        blockButton.setEnabled(user.getStatus() != ProfileStatus.BLOCKED);
        content.add(identity, stats, blockButton);
        drawer.add(drawerHeader, content);
        drawer.open();
    }

    private Div statCard(String label, String value, String styleName) {
        Div card = new Div();
        card.addClassName("admin-user-stat-card");
        card.addClassName(styleName);
        Span title = new Span(label);
        title.addClassName("admin-user-stat-label");
        Span stat = new Span(value);
        stat.addClassName("admin-user-stat-value");
        card.add(title, stat);
        return card;
    }

    private void confirmBlock(LeaderBoardDto user) {
        if (user.getStatus() == ProfileStatus.BLOCKED) {
            return;
        }
        ConfirmDialog confirmation = new ConfirmDialog();
        confirmation.setHeader("Foydalanuvchini bloklash");
        confirmation.setText(nonBlank(user.getFullName(), "Foydalanuvchi")
                + " bloklansinmi? U tizimdan foydalana olmaydi.");
        confirmation.setCancelable(true);
        confirmation.setCancelText("Bekor qilish");
        confirmation.setConfirmText("Bloklash");
        confirmation.setConfirmButtonTheme("error primary");
        confirmation.addConfirmListener(event -> {
            try {
                if (!Boolean.TRUE.equals(adminService.blockUser(user.getProfileId()))) {
                    Notification.show("Foydalanuvchi bloklanmadi. Profil topilmadi.",
                            4000, Notification.Position.TOP_END);
                    return;
                }
                user.setStatus(ProfileStatus.BLOCKED);
                Notification.show("Foydalanuvchi bloklandi.",
                        3000, Notification.Position.TOP_END);
                loadUsers();
                openUserDetails(user);
            } catch (RuntimeException exception) {
                log.error("Could not block user {}", user.getProfileId(), exception);
                Notification.show("Foydalanuvchini bloklashda xatolik yuz berdi.",
                        4500, Notification.Position.TOP_END);
            }
        });
        confirmation.open();
    }

    private static String statusLabel(ProfileStatus status) {
        if (status == null) {
            return "Noma'lum";
        }
        return switch (status) {
            case ACTIVE -> "Faol";
            case BLOCKED -> "Bloklangan";
            case IN_REGISTRATION -> "Kutilmoqda";
        };
    }

    private static String statusClass(ProfileStatus status) {
        if (status == null) {
            return "unknown";
        }
        return switch (status) {
            case ACTIVE -> "active";
            case BLOCKED -> "blocked";
            case IN_REGISTRATION -> "pending";
        };
    }

    private static String formatDate(LeaderBoardDto user) {
        return user.getCreatedDate() == null ? "Ma'lumot yo'q" : DATE_FORMAT.format(user.getCreatedDate());
    }

    private static String formatNumber(Number value) {
        return value == null ? "0" : String.format(Locale.US, "%,d", value.longValue());
    }

    private static boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private static String nonBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String initials(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "U";
        }
        String[] parts = fullName.trim().split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty() && result.length() < 2) {
                result.append(part.charAt(0));
            }
        }
        return result.toString().toUpperCase(Locale.ROOT);
    }
}
