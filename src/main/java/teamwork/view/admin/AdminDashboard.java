package teamwork.view.admin;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import teamwork.view.layout.admin.AdminLayout;

@Route(value = "admin/dashboard", layout = AdminLayout.class)
@PageTitle("Boshqaruv paneli | TozaCoin")
@RolesAllowed("ADMIN")
public class AdminDashboard extends VerticalLayout {
}
