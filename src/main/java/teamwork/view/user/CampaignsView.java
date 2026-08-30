package teamwork.view.user;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import teamwork.view.layout.user.UserLayout;

@Route(value = "user/campaigns", layout = UserLayout.class)
@PageTitle("Kampaniyalar | TozaCoin")
@PermitAll
@CssImport("./themes/tozacoin/user-layout.css")
public class CampaignsView extends VerticalLayout {

    public CampaignsView() {
        setSizeFull();
        setSpacing(true);
        setPadding(true);
        add(new H2("Kampaniyalar"));
    }
}
