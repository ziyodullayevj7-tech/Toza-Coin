package teamwork.view.layout;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.UnorderedList;

@CssImport("./themes/tozacoin/footer.css")
public class Footer extends com.vaadin.flow.component.html.Footer {

    public Footer() {
        addClassName("footer-container");

        // Top Section (4 Columns)
        Div footerTop = new Div();
        footerTop.addClassName("footer-top");

        // Col 1: Brand Info
        Div brandCol = new Div();
        brandCol.addClassName("footer-brand-col");

        Anchor logoLink = new Anchor("", "");
        logoLink.addClassName("footer-logo-link");

        Div logoBadge = new Div();
        logoBadge.setText("TC");
        logoBadge.addClassName("footer-logo-badge");

        Span textToza = new Span("Toza");
        textToza.addClassName("text-toza");

        Span textCoin = new Span("Coin");
        textCoin.addClassName("text-coin");

        Div logoText = new Div(textToza, textCoin);
        logoText.addClassName("footer-logo-text");

        logoLink.add(logoBadge, logoText);

        Paragraph desc = new Paragraph("O'zbekistonda ekologik harakatni qo'llab-quvvatlash va ifloslanishga qarshi kurashish platformasi.");
        desc.addClassName("footer-description");

        Div socials = new Div();
        socials.addClassName("footer-socials");

        Anchor social1 = new Anchor("#", "📷");
        social1.addClassName("social-icon-btn");
        social1.getElement().setAttribute("title", "Instagram");

        Anchor social2 = new Anchor("#", "✈️");
        social2.addClassName("social-icon-btn");
        social2.getElement().setAttribute("title", "Telegram");

        Anchor social3 = new Anchor("#", "💼");
        social3.addClassName("social-icon-btn");
        social3.getElement().setAttribute("title", "LinkedIn");

        Anchor social4 = new Anchor("#", "▶️");
        social4.addClassName("social-icon-btn");
        social4.getElement().setAttribute("title", "YouTube");

        socials.add(social1, social2, social3, social4);
        brandCol.add(logoLink, desc, socials);

        // Col 2: Platforma
        Div colPlatforma = createNavColumn("Platforma",
            new LinkItem("#how-it-works", "Qanday ishlaydi"),
            new LinkItem("#campaigns", "Kampaniyalar"),
            new LinkItem("#rating", "Reyting"),
            new LinkItem("#rewards", "Mukofotlar do'koni")
        );

        // Col 3: Kompaniya
        Div colKompaniya = createNavColumn("Kompaniya",
            new LinkItem("#about", "Biz haqimizda"),
            new LinkItem("#blog", "Blog"),
            new LinkItem("#contact", "Aloqa"),
            new LinkItem("#partners", "Sheriklik")
        );

        // Col 4: Huquqiy
        Div colHuquqiy = createNavColumn("Huquqiy",
            new LinkItem("#privacy", "Maxfiylik siyosati"),
            new LinkItem("#terms", "Foydalanish shartlari"),
            new LinkItem("#cookie", "Cookie siyosati"),
            new LinkItem("#faq", "FAQ")
        );

        footerTop.add(brandCol, colPlatforma, colKompaniya, colHuquqiy);

        // Divider
        Div divider = new Div();
        divider.addClassName("footer-divider");

        // Bottom Bar
        Div footerBottom = new Div();
        footerBottom.addClassName("footer-bottom");

        Span copyright = new Span("© 2026 TozaCoin. Barcha huquqlar himoyalangan.");
        copyright.addClassName("footer-copyright");

        Span madeIn = new Span("🌿 O'zbekiston uchun yasalgan");
        madeIn.addClassName("footer-made-in");

        footerBottom.add(copyright, madeIn);

        add(footerTop, divider, footerBottom);
    }

    private Div createNavColumn(String title, LinkItem... items) {
        Div col = new Div();
        col.addClassName("footer-col");

        H4 colTitle = new H4(title);
        colTitle.addClassName("footer-col-title");

        UnorderedList list = new UnorderedList();
        list.addClassName("footer-links-list");

        for (LinkItem item : items) {
            Anchor link = new Anchor(item.href, item.text);
            link.addClassName("footer-link");
            list.add(new ListItem(link));
        }

        col.add(colTitle, list);
        return col;
    }

    private static class LinkItem {
        final String href;
        final String text;

        LinkItem(String href, String text) {
            this.href = href;
            this.text = text;
        }
    }
}
