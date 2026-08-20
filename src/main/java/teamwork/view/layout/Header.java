package teamwork.view.layout;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Nav;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.RouterLink;

@CssImport("./themes/tozacoin/header.css")
public class Header extends Div {

    private final Button btnKirish = new Button("Kirish");
    private final Button btnRegister = new Button("Ro'yxatdan o'tish");

    public Header() {
        addClassName("header-container");

        // Brand Logo Block
        RouterLink logoLink = new RouterLink("", teamwork.view.general.LandingView.class);
        logoLink.addClassName("header-logo-link");
        logoLink.setHighlightCondition(HighlightConditions.never());

        Span logoBadge = new Span("TC");
        logoBadge.addClassName("header-logo-badge");

        Span logoText = new Span();
        logoText.addClassName("header-logo-text");

        Span textToza = new Span("Toza");
        textToza.addClassName("text-toza");

        Span textCoin = new Span("Coin");
        textCoin.addClassName("text-coin");

        logoText.add(textToza, textCoin);
        logoLink.add(logoBadge, logoText);

        // Navigation Links Block (8 Router Items)
        Nav nav = new Nav();
        UnorderedList navList = new UnorderedList();
        navList.addClassName("header-nav");

        navList.add(
            createRouterNavItem("Asosiy", teamwork.view.general.LandingView.class, true),
            createRouterNavItem("Qanday ishlaydi", HowItWorksSection.class, false),
            createRouterNavItem("Xarita", MapPreviewSection.class, false),
            createRouterNavItem("Kampaniyalar", FeaturedCampaignsSection.class, false),
            createRouterNavItem("Mukofotlar", RewardsCatalogSection.class, false),
            createRouterNavItem("Haqida", AboutUsSection.class, false),
            createRouterNavItem("FAQ", FaqSection.class, false),
            createRouterNavItem("Aloqa", ContactSection.class, false)
        );

        nav.add(navList);

        // Action Buttons Block
        Div actions = new Div();
        actions.addClassName("header-actions");

        btnKirish.addClassName("btn-kirish");
        btnRegister.addClassName("btn-register");
        btnRegister.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        actions.add(btnKirish, btnRegister);

        add(logoLink, nav, actions);
    }

    private ListItem createRouterNavItem(String title, Class<? extends com.vaadin.flow.component.Component> target, boolean exact) {
        ListItem item = new ListItem();
        RouterLink link = new RouterLink(title, target);
        link.addClassName("header-nav-link");
        if (exact) {
            link.setHighlightCondition(HighlightConditions.sameLocation());
        }
        item.add(link);
        return item;
    }

    // Public methods for adding click listeners to Kirish and Register buttons
    public void addLoginClickListener(ComponentEventListener<ClickEvent<Button>> listener) {
        btnKirish.addClickListener(listener);
    }

    public void addRegisterClickListener(ComponentEventListener<ClickEvent<Button>> listener) {
        btnRegister.addClickListener(listener);
    }
}
