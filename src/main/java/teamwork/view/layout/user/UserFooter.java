package teamwork.view.layout.user;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import teamwork.config.CustomUserDetails;
import teamwork.util.SecurityUtils;

import java.util.Optional;

public class UserFooter extends Div {

    public UserFooter() {
        addClassName("sidebar-footer");

        // Fetch Logged-in User via SecurityUtils
        Optional<CustomUserDetails> userOpt = SecurityUtils.getCurrentUser();

        String fullName = userOpt
                .map(u -> {
                    String name = u.getName() != null ? u.getName() : "";
                    String surname = u.getSurname() != null ? u.getSurname() : "";
                    return (name + " " + surname).trim();
                })
                .filter(s -> !s.isEmpty())
                .orElse("Jasur Nazarov");

        String initials = userOpt
                .map(u -> {
                    String n = u.getName() != null && !u.getName().isEmpty() ? u.getName().substring(0, 1).toUpperCase() : "";
                    String s = u.getSurname() != null && !u.getSurname().isEmpty() ? u.getSurname().substring(0, 1).toUpperCase() : "";
                    return n + s;
                })
                .filter(s -> !s.isEmpty())
                .orElse("JN");

        // Left Block: Avatar & User Details
        Div userProfileInfo = new Div();
        userProfileInfo.addClassName("user-profile-info");

        Div avatarBadge = new Div(new Span(initials));
        avatarBadge.addClassName("user-avatar-badge");

        Div userDetails = new Div();
        userDetails.addClassName("user-details");

        Span userNameSpan = new Span(fullName);
        userNameSpan.addClassName("user-name-text");
        userNameSpan.setTitle(fullName);

        Span coinSubSpan = new Span("🪙 4,820 tanga...");
        coinSubSpan.addClassName("user-sub-text");

        userDetails.add(userNameSpan, coinSubSpan);
        userProfileInfo.add(avatarBadge, userDetails);

        // Right Block: Chiqish (Logout) Button
        Div btnLogout = new Div();
        btnLogout.addClassName("btn-chiqish");
        Icon logoutIcon = VaadinIcon.SIGN_OUT.create();
        logoutIcon.getStyle().set("font-size", "14px");
        Span logoutText = new Span("Chiqish");
        logoutText.addClassName("btn-chiqish-text");
        btnLogout.add(logoutIcon, logoutText);

        btnLogout.addClickListener(e -> UI.getCurrent().getPage().setLocation("/logout"));

        add(userProfileInfo, btnLogout);
    }
}
