package teamwork.view.layout;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;

@CssImport("./themes/tozacoin/how-it-works.css")
public class HowItWorksSection extends Div {

    public HowItWorksSection() {
        addClassName("how-it-works-wrapper");

        Div container = new Div();
        container.addClassName("how-it-works-container");

        // Header Title Block
        Div headerBlock = new Div();
        headerBlock.addClassName("how-it-works-header");

        Span badge = new Span("QANDAY ISHLAYDI");
        badge.addClassName("how-it-works-badge");

        H2 title = new H2("4 qadam — juda oson");
        title.addClassName("how-it-works-title");

        Paragraph subtitle = new Paragraph("TozaCoin bilan ekologik harakatlar uchun real mukofot olasiz.");
        subtitle.addClassName("how-it-works-subtitle");

        headerBlock.add(badge, title, subtitle);

        // 4 Step Cards Grid
        Div grid = new Div();
        grid.addClassName("how-it-works-grid");

        grid.add(
            createStepCard("🔍", "blue", "01", "01", "blue", "Ifloslikni toping", "Atrofingizdagi chiqindi yoki ifloslangan joyni aniqlang."),
            createStepCard("📷", "grey", "02", "02", "green", "Rasm oling", "Telefon kamerasi orqali joyni suratga oling."),
            createStepCard("✅", "green", "03", "03", "orange", "Harakatni tanlang", "O'zingiz tozalash yoki mas'ul tashkilotga xabar bering."),
            createStepCard("🪙", "yellow", "04", "04", "amber", "Tanga oling", "Harakatingiz tasdiqlangandan so'ng tanga hisobingizga tushadi.")
        );

        container.add(headerBlock, grid);
        add(container);
    }

    private Div createStepCard(String iconEmoji, String badgeColor, String watermarkNum, String stepNumText, String numColor, String titleText, String descText) {
        Div card = new Div();
        card.addClassName("step-card");

        // Card Top Row (Icon Badge + Watermark)
        Div topRow = new Div();
        topRow.addClassName("step-card-top");

        Span iconBadge = new Span(iconEmoji);
        iconBadge.addClassName("step-icon-badge");
        iconBadge.addClassName(badgeColor);

        Span watermark = new Span(watermarkNum);
        watermark.addClassName("step-watermark");

        topRow.add(iconBadge, watermark);

        // Step Number Label
        Span stepNumber = new Span(stepNumText);
        stepNumber.addClassName("step-number");
        stepNumber.addClassName(numColor);

        // Card Title & Description
        H3 cardTitle = new H3(titleText);
        cardTitle.addClassName("step-card-title");

        Paragraph cardDesc = new Paragraph(descText);
        cardDesc.addClassName("step-card-desc");

        card.add(topRow, stepNumber, cardTitle, cardDesc);
        return card;
    }
}
