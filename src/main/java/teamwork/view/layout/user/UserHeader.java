package teamwork.view.layout.user;

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
    private final H2 titleLabel = new H2("Boshqaruv paneli");
    private final Button btnReport = new Button("Xabar berish", VaadinIcon.PLUS.create());
    private final Div btnNotification = new Div();
    private final Div userAvatarBtn = new Div();

    public UserHeader() {
        addClassName("user-header-bar");

        // Left Section: Drawer Toggle + Page Title
        Div leftSection = new Div();
        leftSection.addClassName("header-left-section");

        drawerToggle.addClassName("header-drawer-toggle");
        titleLabel.addClassName("header-page-title");
        leftSection.add(drawerToggle, titleLabel);

        // Right Actions Block
        Div actionsWrapper = new Div();
        actionsWrapper.addClassName("header-right-actions");

        // + Xabar berish Button
        btnReport.addClassName("btn-report-action");
        btnReport.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnReport.addClickListener(event -> UI.getCurrent().navigate(ReportWasteView.class));

        // Notification Bell Button with badge 2
        btnNotification.addClassName("header-notification-btn");
        Icon bellIcon = VaadinIcon.BELL.create();
        Span badgeDot = new Span("2");
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
                .orElse("JN");

        userAvatarBtn.addClassName("user-avatar-badge");
        userAvatarBtn.setText(initials);
        userAvatarBtn.getStyle().set("cursor", "pointer");

        actionsWrapper.add(btnReport, btnNotification, userAvatarBtn);

        add(leftSection, actionsWrapper);
    }

    public DrawerToggle getDrawerToggle() {
        return drawerToggle;
    }

    public void setPageTitle(String title) {
        titleLabel.setText(title);
    }

    public void addReportClickListener(ComponentEventListener<ClickEvent<Button>> listener) {
        btnReport.addClickListener(listener);
    }

    public void addNotificationClickListener(Runnable callback) {
        btnNotification.addClickListener(e -> callback.run());
    }
}
