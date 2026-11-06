package teamwork.view.general;

import teamwork.i18n.I18n;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import teamwork.service.MapService;
import teamwork.view.layout.landingPage.*;

@Route(value = "", layout = MainLayout.class)
@AnonymousAllowed
public class LandingView extends VerticalLayout implements com.vaadin.flow.router.HasDynamicTitle {
    public LandingView(MapService mapService) {
        setPadding(false);
        setSpacing(false);
        setWidthFull();
        addClassName("simple-landing");
        add(new Hero(), new HowItWorksSection(), new MapPreviewSection(mapService),
                new AboutUsSection(), new ContactSection());
    }

    @Override
    public String getPageTitle() {
        return I18n.t("TozaCoin — Toza muhit o'zimizdan boshlanadi");
    }
}
