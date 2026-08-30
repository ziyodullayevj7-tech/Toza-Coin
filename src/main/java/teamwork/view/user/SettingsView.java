package teamwork.view.user;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import teamwork.view.layout.user.UserLayout;

@Route(value = "user/settings", layout = UserLayout.class)
@PageTitle("Sozlamalar | TozaCoin")
@PermitAll
@CssImport("./themes/tozacoin/user-layout.css")
public class SettingsView extends VerticalLayout {

    public SettingsView() {
        setSizeFull();
        setSpacing(true);
        setPadding(true);
        add(new H2("Sozlamalar"));
    }
}
