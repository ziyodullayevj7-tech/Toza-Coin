package teamwork.view.general;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import teamwork.view.layout.Hero;
import teamwork.view.layout.MainLayout;
import teamwork.view.layout.StatsSummaryBar;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Toza Coin")
@AnonymousAllowed
public class LandingView extends VerticalLayout {

    public LandingView() {
        setPadding(false);
        setSpacing(false);
        setWidthFull();

        add(new Hero());
        add(new StatsSummaryBar());
    }
}
