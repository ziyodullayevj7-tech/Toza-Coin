package teamwork.view.layout.admin;

import teamwork.i18n.I18n;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import teamwork.config.CustomUserDetails;
import teamwork.util.SecurityUtils;

import java.util.Locale;

public class AdminHeader extends Div {

    private final H2 title = I18n.localize(new H2("Boshqaruv paneli"));
    private final Paragraph subtitle = I18n.localize(new Paragraph("TozaCoin boshqaruv paneli"));

    public AdminHeader() {
        addClassName("admin-header");

        Div left = I18n.localize(new Div());
        left.addClassName("admin-header-left");

        DrawerToggle drawerToggle = new DrawerToggle();
        drawerToggle.addClassName("admin-drawer-toggle");

        Div pageInfo = I18n.localize(new Div());
        pageInfo.addClassName("admin-header-page-info");
        title.addClassName("admin-header-title");
        subtitle.addClassName("admin-header-subtitle");
        pageInfo.add(title, subtitle);
        left.add(drawerToggle, pageInfo);

        Div actions = I18n.localize(new Div());
        actions.addClassName("admin-header-actions");

        Span live = I18n.localize(new Span("● Jonli"));
        live.addClassName("admin-live-badge");

        Span avatar = I18n.localize(new Span(SecurityUtils.getCurrentUser()
                .map(AdminHeader::initials)
                .orElse("AD")));
        avatar.addClassName("admin-header-avatar");
        String username = SecurityUtils.getCurrentUser()
                .map(CustomUserDetails::getUsername)
                .orElse("Administrator");
        I18n.title(avatar, username);
        I18n.attribute(avatar, "aria-label", "Administrator profile");

        actions.add(new teamwork.view.components.LanguageSelector(), live, avatar);
        add(left, actions);
    }

    public void setPageTitle(String pageTitle) {
        I18n.text(title, pageTitle);
    }

    private static String initials(CustomUserDetails user) {
        String name = user.getName() == null ? "" : user.getName().trim();
        String surname = user.getSurname() == null ? "" : user.getSurname().trim();
        String initials = (name.isEmpty() ? "" : name.substring(0, 1))
                + (surname.isEmpty() ? "" : surname.substring(0, 1));
        return initials.isEmpty() ? "AD" : initials.toUpperCase(Locale.ROOT);
    }
}
