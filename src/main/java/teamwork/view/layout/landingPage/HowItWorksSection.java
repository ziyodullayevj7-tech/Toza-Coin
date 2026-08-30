package teamwork.view.layout.landingPage;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "how-it-works", layout = MainLayout.class)
@AnonymousAllowed
@CssImport("./themes/tozacoin/how-it-works.css")
public class HowItWorksSection extends Div {

    public HowItWorksSection() {
        addClassName("how-it-works-wrapper");

        Div container = new Div();
        container.addClassName("how-it-works-container");

        // Header Block
        Div headerBlock = new Div();
        headerBlock.addClassName("how-it-works-header");

        Span badge = new Span("JARAYON");
        badge.addClassName("how-it-works-badge");

        H2 title = new H2("Qanday ishlaydi?");
        title.addClassName("how-it-works-title");

        Paragraph subtitle = new Paragraph("4 ta oddiy qadamda toza muhitga hissa qo'shing va tangalar ishlang.");
        subtitle.addClassName("how-it-works-subtitle");

        headerBlock.add(badge, title, subtitle);

        // 4 Step Cards Grid
        Div grid = new Div();
        grid.addClassName("how-it-works-grid");

        grid.add(
            createStepCard("📍", "blue", "01", "blue", "Ifloslikni toping", "Ko'cha, park yoki jamoat joyida ifloslangan hudud yoki chiqindilarni ko'rsangiz, to'xtang."),
            createStepCard("📷", "grey", "02", "green", "Rasm oling", "TozaCoin kamerasidan foydalanib rasmga oling. Geolocation avtomatik saqlanadi."),
            createStepCard("⚡", "green", "03", "orange", "Harakatni tanlang", "O'zingiz tozalang yoki jamoaviy tozalash kampaniyasiga a'zo bo'ling."),
            createStepCard("🪙", "yellow", "04", "amber", "Tanga oling", "Xabar tasdiqlangach, hamyoningizga TozaCoin tangalari kelib tushadi.")
        );

        container.add(headerBlock, grid);
        add(container);
    }

    private Div createStepCard(String iconEmoji, String badgeColorClass, String stepNumStr, String numColorClass, String titleText, String descText) {
        Div card = new Div();
        card.addClassName("step-card");

        Div topRow = new Div();
        topRow.addClassName("step-card-top");

        Span iconBadge = new Span(iconEmoji);
        iconBadge.addClassName("step-icon-badge");
        iconBadge.addClassName(badgeColorClass);

        Span watermark = new Span(stepNumStr);
        watermark.addClassName("step-watermark");

        topRow.add(iconBadge, watermark);

        Span numLabel = new Span("QADAM " + stepNumStr);
        numLabel.addClassName("step-number");
        numLabel.addClassName(numColorClass);

        H3 cardTitle = new H3(titleText);
        cardTitle.addClassName("step-card-title");

        Paragraph cardDesc = new Paragraph(descText);
        cardDesc.addClassName("step-card-desc");

        card.add(topRow, numLabel, cardTitle, cardDesc);
        return card;
    }
}
