package teamwork.view.admin;

import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import teamwork.i18n.I18n;
import teamwork.view.layout.admin.*;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import teamwork.service.AdminOverviewService;

@Route(value = "admin/dashboard", layout = AdminLayout.class)
@RolesAllowed("ADMIN")
public class AdminDashboard extends AdminPage implements com.vaadin.flow.router.HasDynamicTitle {
    public AdminDashboard(AdminOverviewService service) {
        super("Boshqaruv paneli", "A clear view of your community and the work that needs attention.");
        add(new AdminOverviewPanel(service));
        Div actions = new Div(
                action("Moderatsiya navbati", "Review photos and make report decisions.", VaadinIcon.CHECK_CIRCLE, AdminModerationView.class),
                action("Foydalanuvchilar", "Find accounts and review community activity.", VaadinIcon.USERS, AdminUsersView.class),
                action("Statistika", "Compare user reports, earned coins, balances and activity.", VaadinIcon.CHART, AdminStatisticsView.class));
        actions.addClassName("admin-action-grid");
        add(actions);
    }

    @Override
    public String getPageTitle() { return I18n.t("Boshqaruv paneli | TozaCoin"); }
}
