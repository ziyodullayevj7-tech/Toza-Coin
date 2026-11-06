package teamwork.view.layout.landingPage;

import teamwork.i18n.I18n;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;

@CssImport("./themes/tozacoin/trust-fairness.css")
public class TrustFairnessSection extends Div {

    public TrustFairnessSection() {
        addClassName("trust-fairness-wrapper");

        Div container = I18n.localize(new Div());
        container.addClassName("trust-fairness-container");

        // Header Block
        Div headerBlock = I18n.localize(new Div());
        headerBlock.addClassName("trust-fairness-header");

        Span badge = I18n.localize(new Span("ADOLAT VA ISHONCH"));
        badge.addClassName("trust-fairness-badge");

        H2 title = I18n.localize(new H2("Adolat qanday ta'minlanadi?"));
        title.addClassName("trust-fairness-title");

        Paragraph subtitle = I18n.localize(new Paragraph("TozaCoin tizimi suiiste'molni minimallashtirish va haqiqiy harakatlarni rag'batlantirish uchun mo'ljallangan."));
        subtitle.addClassName("trust-fairness-subtitle");

        headerBlock.add(badge, title, subtitle);

        // 4 Cards Grid
        Div grid = I18n.localize(new Div());
        grid.addClassName("trust-fairness-grid");

        grid.add(
                createTrustCard("📷", "blue", "blue", "Faqat ilova kamerasi + GPS", "Surat galereadan emas, faqat ilova kamerasidan olinadi. GPS koordinatalari avtomatik qo'shiladi."),
                createTrustCard("⏳", "purple", "purple", "24–48 soat kutish davri", "Har bir xabar moderatsiya va AI tekshiruvidan o'tadi. Shoshilmasdan, lekin ishonchli."),
                createTrustCard("⚖️", "yellow", "amber", "O'z xabari uchun minimal ball", "O'zingiz qayd etgan muammoni o'zingiz tozalasangiz ball minimal. To'liq tanga boshqa fuqaro muammosini hal qilganda beriladi."),
                createTrustCard("🤖", "green", "green", "AI va moderatsiya nazorati", "Sun'iy intellekt surat tahlili va jamoat moderatsiyasi — har bir tozalash ikki tomonlama tekshiriladi.")
        );

        container.add(headerBlock, grid);
        add(container);
    }

    private Div createTrustCard(String iconEmoji, String badgeColor, String titleColor, String titleText, String descText) {
        Div card = I18n.localize(new Div());
        card.addClassName("trust-card");

        Span iconBadge = I18n.localize(new Span(iconEmoji));
        iconBadge.addClassName("trust-icon-badge");
        iconBadge.addClassName(badgeColor);

        H3 cardTitle = I18n.localize(new H3(titleText));
        cardTitle.addClassName("trust-card-title");
        cardTitle.addClassName(titleColor);

        Paragraph cardDesc = I18n.localize(new Paragraph(descText));
        cardDesc.addClassName("trust-card-desc");

        card.add(iconBadge, cardTitle, cardDesc);
        return card;
    }
}
