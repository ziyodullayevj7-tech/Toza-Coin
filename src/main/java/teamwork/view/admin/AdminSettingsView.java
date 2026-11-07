package teamwork.view.admin;

import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import teamwork.i18n.I18n;
import teamwork.view.layout.admin.*;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import teamwork.service.AdminOverviewService;

@Route(value = "admin/settings", layout = AdminLayout.class)
@RolesAllowed("ADMIN")
public class AdminSettingsView extends AdminPage implements com.vaadin.flow.router.HasDynamicTitle {
    public AdminSettingsView() {
        super("Sozlamalar", "Make this workspace comfortable for you.");
        teamwork.view.components.LanguageSelector language = new teamwork.view.components.LanguageSelector();
        language.addClassName("admin-settings-language");
        add(panel("Til", "Choose the language used across your workspace.", language),
                panel("Workspace appearance", "The background control in the sidebar lets you pause or resume motion. Reduced motion and data-saving preferences are respected."),
                panel("Administrator", "Use the account action in the sidebar to sign out securely."));
    }

    @Override
    public String getPageTitle() { return I18n.t("Sozlamalar | TozaCoin"); }
}
