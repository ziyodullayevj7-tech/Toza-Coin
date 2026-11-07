package teamwork.view.layout.user;

import teamwork.i18n.I18n;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
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
                .orElse("Foydalanuvchi");

        String initials = userOpt
                .map(u -> {
                    String n = u.getName() != null && !u.getName().isEmpty() ? u.getName().substring(0, 1).toUpperCase() : "";
                    String s = u.getSurname() != null && !u.getSurname().isEmpty() ? u.getSurname().substring(0, 1).toUpperCase() : "";
                    return n + s;
                })
                .filter(s -> !s.isEmpty())
                .orElse("TC");

        // Left Block: Avatar & User Details
        Div userProfileInfo = I18n.localize(new Div());
        userProfileInfo.addClassName("user-profile-info");

        Div avatarBadge = I18n.localize(new Div(I18n.localize(new Span(initials))));
        avatarBadge.addClassName("user-avatar-badge");

        Div userDetails = I18n.localize(new Div());
        userDetails.addClassName("user-details");

        Span userNameSpan = I18n.userText(new Span(), userOpt.isPresent() ? fullName : null, "Foydalanuvchi");
        userNameSpan.addClassName("user-name-text");
        userNameSpan.setTitle(fullName);

        Span coinSubSpan = I18n.localize(new Span("TozaCoin a’zosi"));
        coinSubSpan.addClassName("user-sub-text");

        userDetails.add(userNameSpan, coinSubSpan);
        userProfileInfo.add(avatarBadge, userDetails);

        // Keep the account details and action on separate rows to avoid truncating the action.
        Icon logoutIcon = VaadinIcon.SIGN_OUT.create();
        logoutIcon.getElement().setAttribute("aria-hidden", "true");
        Button btnLogout = I18n.localize(new Button("Chiqish", logoutIcon));
        btnLogout.addClassNames("btn-chiqish", "user-shell-action");
        I18n.attribute(btnLogout, "aria-label", "Chiqish");

        btnLogout.addClickListener(e -> UI.getCurrent().getPage().setLocation("/logout"));

        add(userProfileInfo, btnLogout);
    }
}
