package teamwork.view.user;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import teamwork.view.layout.user.UserLayout;

@Route(value = "user/dashboard", layout = UserLayout.class)
@PageTitle("Boshqaruv paneli | TozaCoin")
@PermitAll
@CssImport("./themes/tozacoin/user-layout.css")
public class UserDashboard extends VerticalLayout {

    public UserDashboard() {
        setSizeFull();
        setSpacing(false);
        setPadding(false);

        // Placeholder content area for dashboard
        Div contentPlaceholder = new Div();
        contentPlaceholder.addClassName("dashboard-content-wrapper");
        add(contentPlaceholder);
    }
}
