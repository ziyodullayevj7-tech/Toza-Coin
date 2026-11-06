package teamwork.view.layout.landingPage;

import teamwork.i18n.I18n;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "how-it-works", layout = MainLayout.class)
@AnonymousAllowed
public class HowItWorksSection extends Div {
    public HowItWorksSection() {
        setId("how-it-works");
        addClassNames("eco-section", "eco-how");
        Span label = I18n.localize(new Span("QANDAY ISHLAYDI"));
        label.addClassName("eco-eyebrow");
        Div grid = I18n.localize(new Div(
                step("01", VaadinIcon.CAMERA, "Ko'ring va xabar qiling", "Chiqindini suratga oling. Joylashuvni belgilang va qisqa izoh yozing."),
                step("02", VaadinIcon.MAP_MARKER, "Birgalikda tozalaymiz", "O'zingiz tozalang yoki xabaringizni boshqalar tozalashi uchun yuboring."),
                step("03", VaadinIcon.CHECK_CIRCLE, "Natijani ko'ring", "Tozalashdan keyingi suratni yuboring. Tekshiruvdan so'ng tangalar hisobingizga tushadi.")));
        grid.addClassName("eco-steps");
        add(label, I18n.localize(new H2("Yaxshi o'zgarish uchun oddiy qadamlar.")), grid);
    }

    private Div step(String number, VaadinIcon icon, String title, String text) {
        Span index = I18n.localize(new Span(number));
        index.addClassName("eco-step-number");
        Div symbol = I18n.localize(new Div(icon.create()));
        symbol.addClassName("eco-step-icon");
        Div card = I18n.localize(new Div(index, symbol, I18n.localize(new H3(title)), I18n.localize(new Paragraph(text))));
        card.addClassName("eco-step");
        return card;
    }
}
