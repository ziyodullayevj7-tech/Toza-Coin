package teamwork.view.general;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import teamwork.view.layout.landingPage.AboutUsSection;
import teamwork.view.layout.landingPage.CallToActionBanner;
import teamwork.view.layout.landingPage.ContactSection;
import teamwork.view.layout.landingPage.FaqSection;
import teamwork.view.layout.landingPage.FeaturedCampaignsSection;
import teamwork.view.layout.landingPage.Hero;
import teamwork.view.layout.landingPage.HowItWorksSection;
import teamwork.view.layout.landingPage.LeaderboardSection;
import teamwork.view.layout.landingPage.MainLayout;
import teamwork.view.layout.landingPage.MapPreviewSection;
import teamwork.view.layout.landingPage.ReviewSection;
import teamwork.view.layout.landingPage.RewardsCatalogSection;
import teamwork.view.layout.landingPage.StatsSummaryBar;
import teamwork.view.layout.landingPage.TrustFairnessSection;
import teamwork.view.layout.landingPage.WasteCategorySection;

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
        add(new HowItWorksSection());
        add(new TrustFairnessSection());
        add(new WasteCategorySection());
        add(new FeaturedCampaignsSection());
        add(new MapPreviewSection());
        add(new RewardsCatalogSection());
        add(new LeaderboardSection());
        add(new ReviewSection());
        add(new AboutUsSection());
        add(new FaqSection());
        add(new ContactSection());
        add(new CallToActionBanner());
    }
}
