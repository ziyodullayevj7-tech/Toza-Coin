package teamwork.view.user;

import teamwork.i18n.I18n;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import teamwork.view.layout.user.UserLayout;

@Route(value = "user/wallet", layout = UserLayout.class)
@RolesAllowed({"USER", "ADMIN"})
@CssImport("./themes/tozacoin/user-layout.css")
public class WalletView extends VerticalLayout implements com.vaadin.flow.router.HasDynamicTitle {

    public WalletView() {
        setSizeFull();
        setSpacing(true);
        setPadding(true);
        add(I18n.localize(new H2("Tanga hamyon")));
    }

    @Override
    public String getPageTitle() {
        return I18n.t("Tanga hamyon | TozaCoin");
    }
}
