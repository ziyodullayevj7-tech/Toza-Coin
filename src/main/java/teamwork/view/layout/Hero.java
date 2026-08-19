package teamwork.view.layout;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.button.Button;

@CssImport("./themes/tozacoin/hero.css")
public class Hero extends Div {

    public Hero() {
        addClassName("hero-container-wrapper");

        Div heroSection = new Div();
        heroSection.addClassName("hero-section");

        // --- Left Column ---
        Div heroLeft = new Div();
        heroLeft.addClassName("hero-left");

        // Top Pill Badge
        Div pillBadge = new Div();
        pillBadge.addClassName("hero-pill-badge");
        pillBadge.add(new Span("🌿"), new Span("O'zbekistondagi eng yirik ekologik harakat"));

        // Main Title
        H1 title = new H1();
        title.addClassName("hero-title");

        Span textPart1 = new Span("Tabiatni tozala — ");
        Span textTanga = new Span("tanga");
        textTanga.addClassName("highlight-yellow");
        Span textIshla = new Span(" ishla");

        title.add(textPart1, textTanga, textIshla);

        // Subtitle
        Paragraph subtitle = new Paragraph("Atrofingizdagi ifloslikni xabarlang yoki o'zingiz tozalang. Har bir yaxshi harakatingiz uchun TozaCoin tangalar oling va mukofotlarga almashtiring.");
        subtitle.addClassName("hero-subtitle");

        // CTA Buttons
        Div buttons = new Div();
        buttons.addClassName("hero-buttons");

        Anchor btnPrimary = new Anchor("#register", "🏷️ Bepul boshlash");
        btnPrimary.addClassName("btn-hero-primary");

        Anchor btnSecondary = new Anchor("#map", "🗺️ Xaritani ko'rish");
        btnSecondary.addClassName("btn-hero-secondary");

        buttons.add(btnPrimary, btnSecondary);

        // Search Bar Container
        Div searchBar = new Div();
        searchBar.addClassName("hero-search-bar");

        Input searchInput = new Input();
        searchInput.setPlaceholder("📍 Joylashovingizni kiriting...");
        searchInput.addClassName("hero-search-input");

        Button btnSearch = new Button("Qidirish");
        btnSearch.addClassName("btn-hero-search");

        searchBar.add(searchInput, btnSearch);

        heroLeft.add(pillBadge, title, subtitle, buttons, searchBar);

        // --- Right Column ---
        Div heroRight = new Div();
        heroRight.addClassName("hero-right");

        Image heroImg = new Image("images/hero-volunteers.jpg", "TozaCoin Ko'ngillilari");
        heroImg.addClassName("hero-img-card");

        // Stat Cards Row
        Div statsRow = new Div();
        statsRow.addClassName("hero-stats-row");

        statsRow.add(
            createStatCard("12.8K", "Faol foydalanuvchilar", ""),
            createStatCard("5.2M", "Tarqatilgan tanga", "yellow"),
            createStatCard("247 500 kg", "tozalangan chiqindi", "cyan")
        );

        heroRight.add(heroImg, statsRow);

        // Add both columns to main Hero section
        heroSection.add(heroLeft, heroRight);
        add(heroSection);
    }

    private Div createStatCard(String valueText, String labelText, String colorClass) {
        Div card = new Div();
        card.addClassName("stat-card");

        Span val = new Span(valueText);
        val.addClassName("stat-value");
        if (!colorClass.isEmpty()) {
            val.addClassName(colorClass);
        }

        Span lbl = new Span(labelText);
        lbl.addClassName("stat-label");

        card.add(val, lbl);
        return card;
    }
}
