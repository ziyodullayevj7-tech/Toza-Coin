package teamwork.view.layout.landingPage;

import teamwork.i18n.I18n;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.router.RouterLink;
import teamwork.view.general.RegistrationView;
import teamwork.view.general.home.LoginView;
import teamwork.view.components.ScrollHeaderBehavior;

public class Header extends Div {
    public Header() {
        addClassName("eco-header");
        getElement().setAttribute("role", "banner");
        Anchor brand = I18n.localize(new Anchor("/", "TozaCoin"));
        brand.addClassName("eco-brand");
        Nav nav = new Nav(I18n.localize(new Anchor("/#how-it-works", "Qanday ishlaydi")), I18n.localize(new Anchor("/#map", "Xarita")), I18n.localize(new Anchor("/#about", "Haqida")), I18n.localize(new Anchor("/#contact", "Aloqa")));
        nav.addClassName("eco-nav");
        I18n.attribute(nav, "aria-label", "Asosiy navigatsiya");
        RouterLink login = I18n.localize(new RouterLink("Kirish", LoginView.class));
        RouterLink register = I18n.localize(new RouterLink("Qo'shilish ↗", RegistrationView.class));
        register.addClassName("eco-button");
        I18n.title(register, "Qo'shilish");
        I18n.attribute(register, "aria-label", "Qo'shilish");
        Div actions = I18n.localize(new Div(new teamwork.view.components.LanguageSelector(), login, register));
        actions.addClassName("eco-header-actions");
        add(brand, nav, actions, new ScrollHeaderBehavior());
    }
}
