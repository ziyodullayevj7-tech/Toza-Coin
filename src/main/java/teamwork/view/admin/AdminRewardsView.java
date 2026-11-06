package teamwork.view.admin;

import teamwork.i18n.I18n;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import teamwork.view.layout.admin.AdminLayout;

@Route(value = "admin/rewards", layout = AdminLayout.class)
@RolesAllowed("ADMIN")
public class AdminRewardsView extends VerticalLayout implements com.vaadin.flow.router.HasDynamicTitle {
    @Override
    public String getPageTitle() {
        return I18n.t("Rewards | TozaCoin");
    }
}
