package teamwork.view.admin;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import teamwork.view.layout.admin.AdminLayout;

@Route(value = "admin/users", layout = AdminLayout.class)
@PageTitle("Users | TozaCoin")
@RolesAllowed("ADMIN")
public class AdminUsersView extends VerticalLayout {
}
