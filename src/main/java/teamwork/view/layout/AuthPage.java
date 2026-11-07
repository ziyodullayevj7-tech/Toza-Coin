package teamwork.view.layout;

import teamwork.i18n.I18n;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.RouterLink;
import teamwork.view.general.LandingView;

/**
 * Shared presentation for the two account screens; authentication remains in AuthService.
 */
@CssImport("./themes/tozacoin/auth.css")
public abstract class AuthPage extends Div {
    protected final Div form = I18n.localize(new Div());
    protected final Div feedback = I18n.localize(new Div());

    protected AuthPage(boolean registration, String title, String accent, String subtitle) {
        addClassName("auth-page");
        if (registration) addClassName("auth-register");

        Div scenery = I18n.localize(new Div());
        scenery.addClassName("auth-scenery");
        scenery.getElement().setAttribute("aria-hidden", "true");
        for (String name : new String[]{"auth-ring-left", "auth-ring-right", "auth-orb", "auth-glow"}) {
            Div shape = I18n.localize(new Div());
            shape.addClassName(name);
            scenery.add(shape);
        }

        RouterLink brand = I18n.localize(new RouterLink("", LandingView.class));
        brand.addClassName("auth-brand");
        Div mark = I18n.localize(new Div(VaadinIcon.RECYCLE.create()));
        mark.addClassName("auth-brand-mark");
        brand.add(mark, I18n.localize(new Span("TozaCoin")));
        Anchor home = I18n.localize(new Anchor("/", "Bosh sahifaga qaytish"));
        home.add(VaadinIcon.ARROW_RIGHT.create());
        home.addClassName("auth-home-link");
        Div actions = new Div(home, new teamwork.view.components.LanguageSelector());
        actions.addClassName("auth-top-actions");
        Div top = I18n.localize(new Div(brand, actions));
        top.addClassName("auth-topbar");

        Span eyebrow = I18n.localize(new Span(registration ? "YANGI BOSHLANISH" : "YANA BIRGALIKDA"));
        eyebrow.addClassName("auth-eyebrow");
        H1 heading = I18n.localize(new H1(title));
        heading.setId("auth-heading");
        Span secondLine = I18n.localize(new Span(accent));
        heading.add(secondLine);
        Div underline = I18n.localize(new Div());
        underline.addClassName("auth-title-line");
        Paragraph description = I18n.localize(new Paragraph(subtitle));
        description.addClassName("auth-description");
        form.addClassName("auth-form");
        form.getElement().setAttribute("role", "form");
        form.getElement().setAttribute("aria-labelledby", "auth-heading");
        feedback.addClassName("auth-feedback");
        feedback.getElement().setAttribute("role", "alert");
        feedback.setVisible(false);
        form.add(eyebrow, heading, underline, description, feedback);

        Div aside = I18n.localize(new Div());
        aside.addClassName("auth-aside");
        Span greeting = I18n.localize(new Span(registration ? "Kelajakni birga yaratamiz" : "Qaytganingizdan xursandmiz"));
        Div lock = I18n.localize(new Div(VaadinIcon.LOCK.create()));
        lock.addClassName("auth-lock");
        Div pill = I18n.localize(new Div(greeting, lock));
        pill.addClassName("auth-welcome-pill");
        Div symbol = I18n.localize(new Div(VaadinIcon.RECYCLE.create()));
        symbol.addClassName("auth-impact-symbol");
        Span note = I18n.localize(new Span("BIR YAXSHI HARAKATDAN"));
        note.addClassName("auth-note-label");
        Div message = I18n.localize(new Div(symbol, note, I18n.localize(new H2("Tozaroq dunyo. Yashilroq kelajak.")),
                I18n.localize(new Paragraph("Chiqindini xabar qiling, tozalashga hissa qo'shing. O'zgarish sizdan boshlanadi."))));
        message.addClassName("auth-message");
        Div footer = I18n.localize(new Div(VaadinIcon.RECYCLE.create(), I18n.localize(new Span("Tabiat uchun. Birgalikda."))));
        footer.addClassName("auth-aside-footer");
        aside.add(pill, message, footer);

        Div card = I18n.localize(new Div(form, aside));
        card.addClassName("auth-glass-card");
        Div bottom = I18n.localize(new Div(I18n.localize(new Span("© 2026 TozaCoin")), I18n.localize(new Span("Toza muhit o'zimizdan boshlanadi."))));
        bottom.addClassName("auth-page-footer");
        Div content = I18n.localize(new Div(top, card, bottom));
        content.addClassName("auth-content");
        add(scenery, content);
    }

    protected void showFeedback(String text, boolean success) {
        I18n.text(feedback, text);
        feedback.getElement().setAttribute("role", success ? "status" : "alert");
        feedback.setClassName("auth-feedback" + (success ? " auth-feedback-success" : ""));
        feedback.setVisible(true);
    }

    protected void clearFeedback() {
        feedback.setVisible(false);
    }
}
