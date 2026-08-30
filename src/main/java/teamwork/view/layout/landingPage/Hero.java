package teamwork.view.layout.landingPage;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;

@CssImport("./themes/tozacoin/hero.css")
public class Hero extends Div {

    private final Button btnPrimaryCta = new Button("🚀 Muammoni xabar qilish");
    private final Button btnSecondaryCta = new Button("▶️ Qanday ishlaydi?");
    private final TextField searchInput = new TextField();
    private final Button btnSearch = new Button("Qidirish");

    public Hero() {
        addClassName("hero-container-wrapper");

        Div section = new Div();
        section.addClassName("hero-section");

        // Left Column
        Div leftCol = new Div();
        leftCol.addClassName("hero-left");

        Span pillBadge = new Span("🌱 O'zbekistonning birinchi eco-reward platformasi");
        pillBadge.addClassName("hero-pill-badge");

        H1 title = new H1();
        title.addClassName("hero-title");
        title.add(new Span("Tabiatni tozalang, "), createYellowSpan("tanga ishlang"), new Span(" va mukofot oling!"));

        Paragraph subtitle = new Paragraph("Ifloslangan joylarni suratga oling, tozalash ishlarida qatnashing va ekologik harakatlaringiz uchun haqiqiy mukofotlarga almashtiriladigan TozaCoin tangalariga ega bo'ling.");
        subtitle.addClassName("hero-subtitle");

        Div buttons = new Div();
        buttons.addClassName("hero-buttons");

        btnPrimaryCta.addClassName("btn-hero-primary");
        btnSecondaryCta.addClassName("btn-hero-secondary");

        buttons.add(btnPrimaryCta, btnSecondaryCta);

        // Search Bar Block
        Div searchBar = new Div();
        searchBar.addClassName("hero-search-bar");

        searchInput.setPlaceholder("Hudud yoki muammo bo'yicha qidiruv...");
        searchInput.addClassName("hero-search-input");

        btnSearch.addClassName("btn-hero-search");
        searchBar.add(searchInput, btnSearch);

        leftCol.add(pillBadge, title, subtitle, buttons, searchBar);

        // Right Column
        Div rightCol = new Div();
        rightCol.addClassName("hero-right");

        Image heroImg = new Image("images/hero-volunteers.jpg", "Volunteers cleaning nature");
        heroImg.addClassName("hero-img-card");

        Div statsRow = new Div();
        statsRow.addClassName("hero-stats-row");

        statsRow.add(
            createStatCard("👥 12.8K+", "Faol ko'ngillilar", "cyan"),
            createStatCard("♻️ 247.5 tonna", "Tozalangan chiqindi", ""),
            createStatCard("🪙 5.2M+", "Tarqatilgan tanga", "yellow")
        );

        rightCol.add(heroImg, statsRow);

        section.add(leftCol, rightCol);
        add(section);
    }

    private Span createYellowSpan(String text) {
        Span span = new Span(text);
        span.addClassName("highlight-yellow");
        return span;
    }

    private Div createStatCard(String valueText, String labelText, String valueColorClass) {
        Div card = new Div();
        card.addClassName("stat-card");

        Span valSpan = new Span(valueText);
        valSpan.addClassName("stat-value");
        if (!valueColorClass.isEmpty()) {
            valSpan.addClassName(valueColorClass);
        }

        Span labelSpan = new Span(labelText);
        labelSpan.addClassName("stat-label");

        card.add(valSpan, labelSpan);
        return card;
    }

    // Listener bindings
    public void addReportIssueClickListener(ComponentEventListener<ClickEvent<Button>> listener) {
        btnPrimaryCta.addClickListener(listener);
    }

    public void addHowItWorksClickListener(ComponentEventListener<ClickEvent<Button>> listener) {
        btnSecondaryCta.addClickListener(listener);
    }

    public void addSearchClickListener(ComponentEventListener<ClickEvent<Button>> listener) {
        btnSearch.addClickListener(listener);
    }
}
