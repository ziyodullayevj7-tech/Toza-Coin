package teamwork.view.layout.landingPage;

import teamwork.i18n.I18n;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.router.RouterLink;
import teamwork.view.user.ReportWasteView;

public class Hero extends Div {
    public Hero() {
        addClassName("eco-hero");
        Div content = I18n.localize(new Div());
        content.addClassName("eco-hero-content");
        Span eyebrow = I18n.localize(new Span("TOZA MUHIT. BIRGALIKDA."));
        eyebrow.addClassName("eco-eyebrow");
        H1 heading = I18n.localize(new H1("Toza kelajak sizdan boshlanadi."));
        Paragraph description = I18n.localize(new Paragraph("Chiqindini ko'rdingizmi? Suratga oling va xaritada belgilang. O'zingiz tozalang yoki boshqalarga yordam berish imkonini bering."));
        RouterLink report = I18n.localize(new RouterLink("Report qilish", ReportWasteView.class));
        report.addClassName("eco-button");
        Anchor map = I18n.localize(new Anchor("/#map", "Xaritani ko'rish ↗"));
        map.addClassName("eco-button-outline");
        Div actions = I18n.localize(new Div(report, map));
        actions.addClassName("eco-actions");
        content.add(eyebrow, heading, description, actions);
        Div caption = I18n.localize(new Div(I18n.localize(new Span("Kichik harakat. Katta o'zgarish."))));
        caption.addClassName("eco-hero-caption");
        add(content, caption);
    }
}
