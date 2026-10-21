package teamwork.view.user;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import teamwork.entity.ProfileEntity;
import teamwork.service.AuthService;
import teamwork.view.layout.user.UserLayout;

@Route(value = "user/profile", layout = UserLayout.class)
@PageTitle("Profil | TozaCoin")
@RolesAllowed({"USER", "ADMIN"})
@CssImport("./themes/tozacoin/user-layout.css")
public class ProfileView extends VerticalLayout {

    private final AuthService authService;
    private ProfileEntity currentProfile;
    private boolean editMode = false;

    private final TextField firstNameField = new TextField("Ism");
    private final TextField lastNameField = new TextField("Familiya");
    private final EmailField emailField = new EmailField("Email");
    private final TextField phoneField = new TextField("Telefon raqam");
    private final TextField districtField = new TextField("Tuman");
    private final TextField mahallaField = new TextField("Mahalla");

    public ProfileView(AuthService authService) {
        this.authService = authService;
        this.currentProfile = authService.getCurrentProfileEntity();
        if (this.currentProfile == null) {
            this.currentProfile = new ProfileEntity();
        }

        setWidthFull();
        setPadding(false);
        setSpacing(false);
        addClassName("profile-page-shell");
        setAlignItems(FlexComponent.Alignment.CENTER);

        renderProfile();
    }

    private void renderProfile() {
        removeAll();

        Div card = new Div();
        card.addClassName("profile-card");

        Div topBar = createHeaderBar();
        Div profileInfo = createProfileSummary();
        Div statsRow = createStatsRow();
        Div tabsRow = createTabsRow();
        Div achievements = createAchievementsGrid();

        card.add(topBar, profileInfo, statsRow, tabsRow, achievements);

        if (editMode) {
            card.add(createEditForm());
        }

        add(card);
    }

    private Div createHeaderBar() {
        Div banner = new Div();
        banner.addClassName("profile-banner");

        Div avatarWrap = new Div();
        avatarWrap.addClassName("profile-avatar-wrap");

        Div avatar = new Div(getInitials(currentProfile));
        avatar.addClassName("profile-avatar");
        avatarWrap.add(avatar);

        Button editButton = new Button("Tahirlash", e -> beginEditMode());
        editButton.addClassName("profile-edit-btn");
        editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        banner.add(avatarWrap, editButton);
        return banner;
    }

    private Div createProfileSummary() {
        Div summary = new Div();
        summary.addClassName("profile-summary");

        String fullName = buildFullName();
        H2 name = new H2(fullName);
        name.addClassName("profile-name");

        Div meta = new Div();
        meta.addClassName("profile-meta");
        Span dot = new Span("●");
        dot.addClassName("profile-meta-dot");
        Span location = new Span("Toshkent");
        Span at = new Span("•");
        Span username = new Span("@" + safeText(currentProfile.getUsername()).replace("@", ""));
        meta.add(dot, location, at, username);

        Span bio = new Span(buildBio());
        bio.addClassName("profile-bio");

        Div badges = new Div();
        badges.addClassName("profile-badges");

        Span tierBadge = new Span("🥇 Kumush daraja");
        tierBadge.addClassName("profile-tier-badge");
        tierBadge.getStyle().set("background", "rgba(255, 189, 89, 0.18)");
        tierBadge.getStyle().set("color", "#b67300");
        tierBadge.getStyle().set("border", "1px solid rgba(255, 170, 54, 0.45)");

        Span rankBadge = new Span("Umumiy reyting: #47");
        rankBadge.addClassName("profile-rank-badge");
        rankBadge.getStyle().set("background", "rgba(52, 168, 83, 0.12)");
        rankBadge.getStyle().set("color", "#1d7f4e");
        rankBadge.getStyle().set("border", "1px solid rgba(29, 127, 78, 0.22)");

        badges.add(tierBadge, rankBadge);

        summary.add(name, meta, bio, badges);
        return summary;
    }

    private Div createStatsRow() {
        Div row = new Div();
        row.addClassName("profile-stats-grid");

        row.add(
                createStatCard("🪙", formatNumber(getCoinBalance()), "Jami tanga"),
                createStatCard("📊", String.valueOf(getReportCount()), "Xabarlar"),
                createStatCard("✅", String.valueOf(getCleanupCount()), "Tozalanganlar"),
                createStatCard("🔥", String.valueOf(getStreakDays()), "Kunlik seriya")
        );

        return row;
    }

    private Div createTabsRow() {
        Div row = new Div();
        row.addClassName("profile-tabs");

        Button awardsTab = new Button("🏆 Yutuqlar");
        awardsTab.addClassNames("profile-tab", "active");
        awardsTab.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button historyTab = new Button("📋 Faoliyat tarixi");
        historyTab.addClassName("profile-tab");
        historyTab.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        row.add(awardsTab, historyTab);
        return row;
    }

    private Div createAchievementsGrid() {
        Div grid = new Div();
        grid.addClassName("achievements-grid");

        grid.add(
                createAchievementCard("🌱", "Yashil kamarbard", "10 ta muvaffaqiyatli tozalash", "bronze"),
                createAchievementCard("🏅", "Kumush daraja", "100 ta xabar yuborish", "silver"),
                createAchievementCard("🔥", "12 kun seriya", "12 kun ketma-ket faollik", "orange"),
                createAchievementCard("👤", "Qahramonlik", "50 ta tozalash operatsiyasi", "blue")
        );

        return grid;
    }

    private Div createEditForm() {
        Div form = new Div();
        form.addClassName("profile-edit-form");

        firstNameField.setValue(getFirstName());
        lastNameField.setValue(getLastName());
        emailField.setValue(safeText(currentProfile.getUsername()));
        phoneField.setValue(safeText(currentProfile.getPhoneNumber()));
        districtField.setValue(safeText(currentProfile.getDistrict()));
        mahallaField.setValue(safeText(currentProfile.getMahalla()));

        HorizontalLayout fields = new HorizontalLayout();
        fields.addClassName("profile-edit-fields");
        fields.add(firstNameField, lastNameField);

        HorizontalLayout secondRow = new HorizontalLayout();
        secondRow.addClassName("profile-edit-fields");
        secondRow.add(emailField, phoneField);

        HorizontalLayout thirdRow = new HorizontalLayout();
        thirdRow.addClassName("profile-edit-fields");
        thirdRow.add(districtField, mahallaField);

        Button saveButton = new Button("Saqlash", e -> saveProfile());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClassName("profile-save-btn");

        Button cancelButton = new Button("Bekor qilish", e -> cancelEditMode());
        cancelButton.addClassName("profile-cancel-btn");

        HorizontalLayout actions = new HorizontalLayout();
        actions.addClassName("profile-edit-actions");
        actions.add(saveButton, cancelButton);

        form.add(fields, secondRow, thirdRow, actions);
        return form;
    }

    private void beginEditMode() {
        editMode = true;
        renderProfile();
    }

    private void cancelEditMode() {
        editMode = false;
        renderProfile();
    }

    private void saveProfile() {
        try {
            authService.updateCurrentProfile(
                    firstNameField.getValue(),
                    lastNameField.getValue(),
                    emailField.getValue(),
                    phoneField.getValue(),
                    districtField.getValue(),
                    mahallaField.getValue()
            );

            currentProfile = authService.getCurrentProfileEntity();
            if (currentProfile == null) {
                currentProfile = new ProfileEntity();
            }
            editMode = false;
            renderProfile();

            Notification success = Notification.show("Profil ma'lumotlari saqlandi.", 3000, Notification.Position.BOTTOM_END);
            success.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification error = Notification.show(e.getMessage(), 3000, Notification.Position.BOTTOM_END);
            error.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private Div createStatCard(String emoji, String value, String label) {
        Div card = new Div();
        card.addClassName("profile-stat-card");

        Div valueRow = new Div();
        valueRow.addClassName("profile-stat-value-row");

        Span val = new Span(value);
        val.addClassName("profile-stat-value");

        Span icon = new Span(emoji);
        icon.addClassName("profile-stat-icon");

        valueRow.add(val, icon);
        Span title = new Span(label);
        title.addClassName("profile-stat-label");
        card.add(valueRow, title);
        return card;
    }

    private Div createAchievementCard(String emoji, String title, String description, String accent) {
        Div card = new Div();
        card.addClassNames("achievement-card", accent);

        Div iconWrap = new Div(emoji);
        iconWrap.addClassName("achievement-icon");

        H2 itemTitle = new H2(title);
        itemTitle.addClassName("achievement-title");

        Span itemText = new Span(description);
        itemText.addClassName("achievement-text");

        card.add(iconWrap, itemTitle, itemText);
        return card;
    }

    private String buildFullName() {
        return (safeText(getFirstName()) + " " + safeText(getLastName())).trim();
    }

    private String buildBio() {
        String district = safeText(currentProfile.getDistrict());
        if (district.isEmpty()) {
            district = "Toshkent";
        }
        String mahalla = safeText(currentProfile.getMahalla());
        return "Tabiatni sevaman, har kuni tozalash uchun harakat qilaman";
    }

    private String getFirstName() {
        return safeText(currentProfile.getName());
    }

    private String getLastName() {
        return safeText(currentProfile.getSurname());
    }

    private String getInitials(ProfileEntity profile) {
        String first = safeText(profile.getName()).trim();
        String last = safeText(profile.getSurname()).trim();
        if (first.isEmpty() && last.isEmpty()) {
            return "JN";
        }
        StringBuilder sb = new StringBuilder();
        if (!first.isEmpty()) {
            sb.append(first.substring(0, 1).toUpperCase());
        }
        if (!last.isEmpty()) {
            sb.append(last.substring(0, 1).toUpperCase());
        }
        return sb.length() > 0 ? sb.toString() : "JN";
    }

    private int getCoinBalance() {
        Integer val = currentProfile.getCoinBalance();
        return val == null ? 4820 : val;
    }

    private int getReportCount() {
        Integer val = currentProfile.getReportCount();
        return val == null ? 47 : val;
    }

    private int getCleanupCount() {
        return 23;
    }

    private int getStreakDays() {
        Integer val = currentProfile.getStreakDays();
        return val == null ? 12 : val;
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private String formatNumber(int value) {
        return String.format("%,d", value);
    }
}

