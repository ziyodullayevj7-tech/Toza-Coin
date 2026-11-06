package teamwork.view.layout.landingPage;

import teamwork.i18n.I18n;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;

@CssImport("./themes/tozacoin/cta-banner.css")
public class CallToActionBanner extends Div {

    private final Button btnRegister = I18n.localize(new Button("🚀 Bepul ro'yxatdan o'tish"));
    private final Button btnAppSoon = I18n.localize(new Button("📱 Ilova yaqinda"));

    public CallToActionBanner() {
        addClassName("cta-banner-wrapper");

        Div container = I18n.localize(new Div());
        container.addClassName("cta-banner-container");

        Span icon = I18n.localize(new Span("🌍"));
        icon.addClassName("cta-banner-icon");

        H2 title = I18n.localize(new H2("Harakatga qo'shiling"));
        title.addClassName("cta-banner-title");

        Paragraph subtitle = I18n.localize(new Paragraph("O'zbekistonni tozaroq qilishga yordam bering. Bugun ro'yxatdan o'ting — bepul!"));
        subtitle.addClassName("cta-banner-subtitle");

        Div buttonsRow = I18n.localize(new Div());
        buttonsRow.addClassName("cta-banner-buttons");

        btnRegister.addClassName("btn-cta-primary");
        btnRegister.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        btnAppSoon.addClassName("btn-cta-secondary");

        buttonsRow.add(btnRegister, btnAppSoon);

        container.add(icon, title, subtitle, buttonsRow);
        add(container);
    }

    // ClickListener support for "Bepul ro'yxatdan o'tish" button
    public void addRegisterClickListener(ComponentEventListener<ClickEvent<Button>> listener) {
        btnRegister.addClickListener(listener);
    }

    public Button getRegisterButton() {
        return btnRegister;
    }

    // ClickListener support for "Ilova yaqinda" button
    public void addAppSoonClickListener(ComponentEventListener<ClickEvent<Button>> listener) {
        btnAppSoon.addClickListener(listener);
    }

    public Button getAppSoonButton() {
        return btnAppSoon;
    }
}
