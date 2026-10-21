package teamwork.view.admin;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import teamwork.view.layout.admin.AdminLayout;

@Route(value = "admin/statistics", layout = AdminLayout.class)
@PageTitle("Statistics | TozaCoin")
@RolesAllowed("ADMIN")
public class AdminStatisticsView extends VerticalLayout {
}
