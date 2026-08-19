package teamwork.view.layout;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.UnorderedList;

@CssImport("./themes/tozacoin/footer.css")
public class Footer extends Div {

    public Footer() {
        addClassName("footer-wrapper");

        Div container = new Div();
        container.addClassName("footer-container");

        // Main 4 Columns Grid
        Div mainGrid = new Div();
        mainGrid.addClassName("footer-main-grid");

        // Col 1: Brand Info
        Div colBrand = new Div();
        colBrand.addClassName("footer-brand-col");

        Div logoGroup = new Div();
        logoGroup.addClassName("footer-logo-group");

        Image logoIcon = new Image("images/tc-icon.svg", "TC Logo");
        logoIcon.addClassName("footer-logo-icon");

        Span logoText = new Span();
        logoText.addClassName("footer-logo-text");

        Span spanToza = new Span("Toza");
        spanToza.addClassName("text-toza");
        Span spanCoin = new Span("Coin");
        spanCoin.addClassName("text-coin");
        logoText.add(spanToza, spanCoin);

        logoGroup.add(logoIcon, logoText);

        Paragraph brandDesc = new Paragraph("TozaCoin — toza O'zbekiston uchun fuqarolik tashabbusi. Muammolarni xabar qiling, tozalang, tanga yig'ing va mukofotlarga almashtiring.");
        brandDesc.addClassName("footer-brand-desc");

        colBrand.add(logoGroup, brandDesc);

        // Col 2: Platforma
        Div colPlatform = createNavColumn("Platforma", new String[][]{
            {"Qanday ishlaydi?", "#how-it-works"},
            {"Toifalar", "#categories"},
            {"Kampaniyalar", "#campaigns"},
            {"Xarita", "#map"},
            {"Mukofotlar", "#rewards"}
        });

        // Col 3: Kompaniya
        Div colCompany = createNavColumn("Kompaniya", new String[][]{
            {"Biz haqimizda", "#about"},
            {"Reyting", "#leaderboard"},
            {"Fikrlar", "#reviews"},
            {"Savol-javob", "#faq"},
            {"Bog'lanish", "#contact"}
        });

        // Col 4: Huquqiy
        Div colLegal = createNavColumn("Huquqiy", new String[][]{
            {"Foydalanish shartlari", "#terms"},
            {"Maxfiylik siyosati", "#privacy"},
            {"Ochiq ma'lumotlar", "#open-data"}
        });

        mainGrid.add(colBrand, colPlatform, colCompany, colLegal);

        // Bottom Copyright Bar
        Div bottomBar = new Div();
        bottomBar.addClassName("footer-bottom-bar");

        Paragraph copyText = new Paragraph("© 2026 TozaCoin. Barcha huquqlar himoyalangan.");
        copyText.addClassName("footer-copyright-text");

        bottomBar.add(copyText);

        container.add(mainGrid, bottomBar);
        add(container);
    }

    private Div createNavColumn(String colTitle, String[][] links) {
        Div col = new Div();
        col.addClassName("footer-nav-col");

        H4 title = new H4(colTitle);
        title.addClassName("footer-col-title");

        UnorderedList list = new UnorderedList();
        list.addClassName("footer-link-list");

        for (String[] link : links) {
            ListItem item = new ListItem();
            Anchor anchor = new Anchor(link[1], link[0]);
            anchor.addClassName("footer-nav-link");
            item.add(anchor);
            list.add(item);
        }

        col.add(title, list);
        return col;
    }
}
