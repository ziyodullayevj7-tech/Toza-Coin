package teamwork.view.layout.admin;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import teamwork.config.CustomUserDetails;
import teamwork.util.SecurityUtils;

import java.util.Locale;

public class AdminFooter extends Div {

    public AdminFooter() {
        addClassName("admin-sidebar-footer");

        CustomUserDetails user = SecurityUtils.getCurrentUser().orElse(null);
        String fullName = user == null ? "Administrator"
                : ((user.getName() == null ? "" : user.getName()) + " "
                + (user.getSurname() == null ? "" : user.getSurname())).trim();
        if (fullName.isBlank()) {
            fullName = "Administrator";
        }
        String username = user == null || user.getUsername() == null
                ? "" : user.getUsername();
        String initials = user == null ? "AD" : initials(user);

        Div avatar = new Div(new Span(initials));
        avatar.addClassName("admin-footer-avatar");

        Div details = new Div();
        details.addClassName("admin-footer-details");
        Span name = new Span(fullName);
        name.addClassName("admin-footer-name");
        name.setTitle(fullName);
        Span email = new Span(username);
        email.addClassName("admin-footer-email");
        details.add(name, email);

        Button logout = new Button("⏻");
        logout.addClassName("admin-footer-logout");
        logout.getElement().setAttribute("title", "Chiqish");
        logout.getElement().setAttribute("aria-label", "Chiqish");
        logout.addClickListener(event -> UI.getCurrent().getPage().setLocation("/logout"));

        add(avatar, details, logout);
    }

    private static String initials(CustomUserDetails user) {
        String name = user.getName() == null ? "" : user.getName().trim();
        String surname = user.getSurname() == null ? "" : user.getSurname().trim();
        String initials = (name.isEmpty() ? "" : name.substring(0, 1))
                + (surname.isEmpty() ? "" : surname.substring(0, 1));
        return initials.isEmpty() ? "AD" : initials.toUpperCase(Locale.ROOT);
    }
}
