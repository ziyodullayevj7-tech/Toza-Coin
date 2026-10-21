package teamwork.view.admin;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import teamwork.view.layout.admin.AdminLayout;

@Route(value = "admin/suspiciosActivity", layout = AdminLayout.class)
@PageTitle("Suspicious Reports | TozaCoin")
@RolesAllowed("ADMIN")
public class AdminSuspiciousActivityView extends VerticalLayout {
}
