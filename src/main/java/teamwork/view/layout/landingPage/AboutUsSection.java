package teamwork.view.layout.landingPage;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "about-us", layout = MainLayout.class)
@AnonymousAllowed
@CssImport("./themes/tozacoin/about-us.css")
public class AboutUsSection extends Div {

    private final Span statVal1 = new Span("2025");
    private final Span statVal2 = new Span("47+");
    private final Span statVal3 = new Span("14/14");
    private final Span statVal4 = new Span("4.9 ★");

    public AboutUsSection() {
        addClassName("about-us-wrapper");

        Div container = new Div();
        container.addClassName("about-us-container");

        // Left Column
        Div leftCol = new Div();
        leftCol.addClassName("about-us-left");

        Span badge = new Span("HAQIDA");
        badge.addClassName("about-us-badge");

        H2 title = new H2("Biz kimligimiz va maqsadimiz");
        title.addClassName("about-us-title");

        Paragraph p1 = new Paragraph("TozaCoin — O'zbekiston ekologiya vazirligi hamkorligida yaratilgan fuqarolik platformasi. Bizning maqsadimiz: har bir fuqaroni tabiatni muhofaza qilishda faol ishtirokchiga aylantirish.");
        p1.addClassName("about-us-p1");

        Paragraph p2 = new Paragraph("Biz texnologiya, iqtisodiy rag'bat va jamoatchilik nazoratini birlashtirgan tizim yaratdik. Har bir xabar yoki tozalash harakati ro'yxatga olinadi, tasdiqlangandan keyin mukofotlanadi.");
        p2.addClassName("about-us-p2");

        // 2x2 Stats Grid
        Div statsGrid = new Div();
        statsGrid.addClassName("about-us-stats-grid");

        statsGrid.add(
            createStatCard(statVal1, "Ta'sischilik yili"),
            createStatCard(statVal2, "Hamkor tashkilotlar"),
            createStatCard(statVal3, "Qamrab olingan viloyatlar"),
            createStatCard(statVal4, "Jamoatchilik reytingi")
        );

        leftCol.add(badge, title, p1, p2, statsGrid);

        // Right Column
        Div rightCol = new Div();
        rightCol.addClassName("about-us-right");

        Image img = new Image("images/about-us.jpg", "Volunteers Cleaning Nature");
        img.addClassName("about-us-img");

        // 3 Recognition Cards Row
        Div recognitionRow = new Div();
        recognitionRow.addClassName("about-us-recognition-row");

        recognitionRow.add(
            createRecognitionCard("🏛️", "Davlat hamkorligi", "Ekologiya vazirligi"),
            createRecognitionCard("🌐", "Xalqaro qo'llab-quvvatlash", "UNDP & UNEP"),
            createRecognitionCard("🏆", "Milliy mukofot", "Innovatsiya 2025")
        );

        rightCol.add(img, recognitionRow);

        container.add(leftCol, rightCol);
        add(container);
    }

    // Public method to update stats dynamically from a backend Service
    public void setStats(String foundedYear, String partnersCount, String regionsCount, String rating) {
        statVal1.setText(foundedYear);
        statVal2.setText(partnersCount);
        statVal3.setText(regionsCount);
        statVal4.setText(rating);
    }

    private Div createStatCard(Span numSpan, String labelText) {
        Div card = new Div();
        card.addClassName("about-stat-card");

        numSpan.addClassName("about-stat-number");

        Span label = new Span(labelText);
        label.addClassName("about-stat-label");

        card.add(numSpan, label);
        return card;
    }

    private Div createRecognitionCard(String iconEmoji, String titleText, String subText) {
        Div card = new Div();
        card.addClassName("recognition-card");

        Span iconBadge = new Span(iconEmoji);
        iconBadge.addClassName("recognition-icon-badge");

        Span title = new Span(titleText);
        title.addClassName("recognition-title");

        Span sub = new Span(subText);
        sub.addClassName("recognition-subtitle");

        card.add(iconBadge, title, sub);
        return card;
    }
}
