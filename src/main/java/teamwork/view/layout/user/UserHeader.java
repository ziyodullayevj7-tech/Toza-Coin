package teamwork.view.layout.user;

import teamwork.i18n.I18n;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import teamwork.config.CustomUserDetails;
import teamwork.util.SecurityUtils;
import teamwork.view.user.ReportWasteView;

import java.util.Optional;

public class UserHeader extends Div {

    private final DrawerToggle drawerToggle = new DrawerToggle();
    private final H2 titleLabel = I18n.localize(new H2("Boshqaruv paneli"));
    private final Button btnReport = I18n.localize(new Button("Report", VaadinIcon.PLUS.create()));
    private final Div btnNotification = I18n.localize(new Div());
    private final Div userAvatarBtn = I18n.localize(new Div());

    public UserHeader() {
        addClassName("user-header-bar");

        // Left Section: Drawer Toggle + Page Title
        Div leftSection = I18n.localize(new Div());
        leftSection.addClassName("header-left-section");

        drawerToggle.addClassName("header-drawer-toggle");
        Span menuGlyph = new Span(new Span(), new Span(), new Span());
        menuGlyph.addClassName("user-menu-glyph");
        menuGlyph.getElement().setAttribute("aria-hidden", "true");
        drawerToggle.getElement().appendChild(menuGlyph.getElement());
        I18n.attribute(drawerToggle, "aria-label", "Menyu");
        titleLabel.addClassName("header-page-title");
        leftSection.add(drawerToggle, titleLabel);

        // Right Actions Block
        Div actionsWrapper = I18n.localize(new Div());
        actionsWrapper.addClassName("header-right-actions");

        // + Xabar berish Button
        btnReport.addClassName("btn-report-action");
        I18n.title(btnReport, "Report");
        btnReport.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnReport.addClickListener(event -> UI.getCurrent().navigate(ReportWasteView.class));

        // Notification Bell Button with badge 2
        btnNotification.addClassName("header-notification-btn");
        Icon bellIcon = VaadinIcon.BELL.create();
        Span badgeDot = I18n.localize(new Span("2"));
        badgeDot.addClassName("header-badge-dot");
        btnNotification.add(bellIcon, badgeDot);

        // User Avatar Button
        Optional<CustomUserDetails> userOpt = SecurityUtils.getCurrentUser();
        String initials = userOpt
                .map(u -> {
                    String n = u.getName() != null && !u.getName().isEmpty() ? u.getName().substring(0, 1).toUpperCase() : "";
                    String s = u.getSurname() != null && !u.getSurname().isEmpty() ? u.getSurname().substring(0, 1).toUpperCase() : "";
                    return n + s;
                })
                .filter(s -> !s.isEmpty())
                .orElse("TC");

        userAvatarBtn.addClassName("user-avatar-badge");
        I18n.text(userAvatarBtn, initials);
        userAvatarBtn.getStyle().set("cursor", "pointer");

        teamwork.view.components.LanguageSelector language = new teamwork.view.components.LanguageSelector();
        language.addClassName("user-language-control");
        Icon globe = VaadinIcon.GLOBE.create();
        globe.getElement().setAttribute("aria-hidden", "true");
        language.getElement().insertChild(0, globe.getElement());
        actionsWrapper.add(language, btnReport, userAvatarBtn);
        userAvatarBtn.addClickListener(e -> UI.getCurrent().navigate(teamwork.view.user.ProfileView.class));
        userAvatarBtn.getElement().setAttribute("role", "link");
        userAvatarBtn.getElement().setAttribute("tabindex", "0");
        I18n.attribute(userAvatarBtn, "aria-label", "Profil");
        userAvatarBtn.getElement().addEventListener("keydown", e -> UI.getCurrent().navigate(teamwork.view.user.ProfileView.class))
                .setFilter("event.key === 'Enter'");

        add(leftSection, actionsWrapper);
    }

    public DrawerToggle getDrawerToggle() {
        return drawerToggle;
    }

    public void setPageTitle(String title) {
        I18n.text(titleLabel, title);
    }

    public void addReportClickListener(ComponentEventListener<ClickEvent<Button>> listener) {
        btnReport.addClickListener(listener);
    }

    public void addNotificationClickListener(Runnable callback) {
        btnNotification.addClickListener(e -> callback.run());
    }
}
