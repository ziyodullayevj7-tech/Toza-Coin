package teamwork.view.layout.landingPage;

import teamwork.i18n.I18n;
import com.vaadin.flow.component.html.*;

public class Footer extends Div {
    public Footer() {
        addClassName("eco-footer");
        Anchor brand = I18n.localize(new Anchor("/", "TozaCoin"));
        brand.addClassName("eco-brand");
        Div links = I18n.localize(new Div(I18n.localize(new Anchor("/#how-it-works", "Qanday ishlaydi")), I18n.localize(new Anchor("/#map", "Xarita")), I18n.localize(new Anchor("/#about", "Haqida")), I18n.localize(new Anchor("/#contact", "Aloqa"))));
        links.addClassName("eco-footer-links");
        add(brand, links, I18n.localize(new Span("© 2026 TozaCoin")));
    }
}
