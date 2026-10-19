package teamwork.view.admin;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import teamwork.view.layout.admin.AdminLayout;

@Route(value = "admin/statistics", layout = AdminLayout.class)
@PageTitle("Statistics | TozaCoin")
@PermitAll
public class AdminStatisticsView extends VerticalLayout {
}
