package teamwork.view.layout.landingPage;

import teamwork.i18n.I18n;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "about-us", layout = MainLayout.class)
@AnonymousAllowed
public class AboutUsSection extends Div {
    public AboutUsSection() {
        setId("about");
        addClassNames("eco-section", "eco-about");
        Div artwork = I18n.localize(new Div(I18n.localize(new Span("Tabiat hammamizning uyimiz."))));
        artwork.addClassName("eco-about-art");
        Span label = I18n.localize(new Span("TOZACOIN HAQIDA"));
        label.addClassName("eco-eyebrow");
        Div text = I18n.localize(new Div(label, I18n.localize(new H2("Mahallamizdan boshlaymiz.")),
                I18n.localize(new Paragraph("TozaCoin — O'zbekistonda atrof-muhitni tozalashga hissa qo'shishni osonlashtiradigan platforma. Chiqindi bor joyni bildirish ham, uni tozalash ham foydali harakat.")),
                I18n.localize(new Paragraph("Xabarlar xaritada birlashadi. Tozalash natijalari suratlar orqali tekshiriladi. Tasdiqlangan harakatingiz uchun TozaCoin tangalarini olasiz."))));
        text.addClassName("eco-about-copy");
        add(artwork, text);
    }
}
