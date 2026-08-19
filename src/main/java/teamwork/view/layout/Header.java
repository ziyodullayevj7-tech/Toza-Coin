package teamwork.view.layout;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Nav;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

@CssImport("./themes/tozacoin/header.css")
public class Header extends HorizontalLayout {

    public Header() {
        setWidthFull();
        setPadding(false);
        setSpacing(false);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.BETWEEN);
        addClassName("header-container");

        // 1. Logo Branding Section
        Anchor logoLink = new Anchor("", "");
        logoLink.addClassName("header-logo-link");

        Div logoBadge = new Div();
        logoBadge.setText("TC");
        logoBadge.addClassName("header-logo-badge");

        Span textToza = new Span("Toza");
        textToza.addClassName("text-toza");

        Span textCoin = new Span("Coin");
        textCoin.addClassName("text-coin");

        Div logoText = new Div(textToza, textCoin);
        logoText.addClassName("header-logo-text");

        logoLink.add(logoBadge, logoText);

        // 2. Navigation Menu Links
        Nav navMenu = new Nav();
        navMenu.addClassName("header-nav");

        navMenu.add(
            createNavLink("", "Asosiy", true),
            createNavLink("#how-it-works", "Qanday ishlaydi", false),
            createNavLink("#map", "Xarita", false),
            createNavLink("#campaigns", "Kampaniyalar", false),
            createNavLink("#rewards", "Mukofotlar", false),
            createNavLink("#about", "Haqida", false),
            createNavLink("#faq", "FAQ", false),
            createNavLink("#contact", "Aloqa", false)
        );

        // 3. Action Buttons Section
        Div headerActions = new Div();
        headerActions.addClassName("header-actions");

        Anchor btnKirish = new Anchor("login", "Kirish");
        btnKirish.addClassName("btn-kirish");

        Anchor btnRegister = new Anchor("register", "Ro'yxatdan o'tish");
        btnRegister.addClassName("btn-register");

        headerActions.add(btnKirish, btnRegister);

        // Add all 3 sections to Header layout
        add(logoLink, navMenu, headerActions);
    }

    private Anchor createNavLink(String href, String text, boolean active) {
        Anchor link = new Anchor(href, text);
        link.addClassName("header-nav-link");
        if (active) {
            link.addClassName("active");
        }
        return link;
    }
}
