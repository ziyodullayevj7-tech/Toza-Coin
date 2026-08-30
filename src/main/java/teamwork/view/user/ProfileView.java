package teamwork.view.user;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import teamwork.view.layout.user.UserLayout;

@Route(value = "user/profile", layout = UserLayout.class)
@PageTitle("Profil | TozaCoin")
@PermitAll
@CssImport("./themes/tozacoin/user-layout.css")
public class ProfileView extends VerticalLayout {

    public ProfileView() {
        setSizeFull();
        setSpacing(true);
        setPadding(true);
        add(new H2("Profil"));
    }
}
