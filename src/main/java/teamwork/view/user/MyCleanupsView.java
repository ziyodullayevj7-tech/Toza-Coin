package teamwork.view.user;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import teamwork.view.layout.user.UserLayout;

import java.util.List;

@Route(value = "user/my-cleanups", layout = UserLayout.class)
@PageTitle("Mening tozalashlarim | TozaCoin")
@PermitAll
@CssImport("./themes/tozacoin/user-layout.css")
public class MyCleanupsView extends VerticalLayout {

    public MyCleanupsView() {

        UI.getCurrent().getPage().executeJs(
                "const style = document.createElement('style');" +
                        "style.innerHTML = `" +
                        "  .task-card {" +
                        "    transition: transform 0.25s ease, box-shadow 0.25s ease;" +
                        "  }" +
                        "  .task-card:hover {" +
                        "    transform: translateY(-4px);" +
                        "    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.08);" +
                        "  }" +
                        "`;" +
                        "document.head.appendChild(style);"
        );
        setWidthFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        getStyle().set("background-color", "#F9FAFB");
        getStyle().set("min-height", "100vh");
        getStyle().set("padding", "24px 16px");

        // Asosiy o'rtadagi konteyner
        VerticalLayout container = new VerticalLayout();
        container.setMaxWidth("760px");
        container.setWidthFull();
        container.setPadding(false);
        container.setSpacing(true);

        // 1. Header (Orqaga tugmasi + Sarlavha)
        container.add(createHeader());

        // 2. "Qanday ishlaydi" yashil banner
        container.add(createInfoBanner());

        // 3. Muammolar ro'yxati (Kartochkalar)
        List<CardData> items = List.of(
                new CardData(
                        "Plastik chiqindi — Yunusobod parkida",
                        "Yunusobod tumani, Toshkent", "0.4 km", "2026-08-14",
                        "Yuqori", "#EF4444", "#FEE2E2", "#DC2626", 50,
                        "https://images.unsplash.com/photo-1532996122724-e3c354a0b15b?w=400"
                ),
                new CardData(
                        "Noqonuniy tashlash — Sirdaryo ko'chasi",
                        "Mirzo Ulug'bek tumani, Toshkent", "1.2 km", "2026-08-13",
                        "O'rta", "#F59E0B", "#FEF3C7", "#D97706", 50,
                        "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=400"
                ),
                new CardData(
                        "Jamoat ifloslanishi — Chilonzor bozori",
                        "Chilonzor tumani, Toshkent", "2.1 km", "2026-08-12",
                        "Past", "#10B981", "#D1FAE5", "#059669", 50,
                        "https://images.unsplash.com/photo-1618477461853-cf6ed80faba5?w=400"
                )
        );

        items.forEach(item -> container.add(createCard(item)));

        add(container);
    }

    private HorizontalLayout createHeader() {
        Button backBtn = new Button(VaadinIcon.ARROW_LEFT.create(), e -> UI.getCurrent().getPage().getHistory().back());
        backBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backBtn.getStyle()
                .set("background-color", "#FFFFFF")
                .set("border", "1px solid #E5E7EB")
                .set("border-radius", "12px")
                .set("width", "42px")
                .set("height", "42px")
                .set("cursor", "pointer")
                .set("color", "#374151");

        H2 title = new H2("Tozalash jarayoni");
        title.getStyle().set("margin", "0").set("font-size", "22px").set("color", "#111827");

        Paragraph subtitle = new Paragraph("Tozalamoqchi bo'lgan muammoni tanlang");
        subtitle.getStyle().set("margin", "0").set("font-size", "14px").set("color", "#6B7280");

        VerticalLayout titleWrapper = new VerticalLayout(title, subtitle);
        titleWrapper.setPadding(false);
        titleWrapper.setSpacing(false);

        HorizontalLayout header = new HorizontalLayout(backBtn, titleWrapper);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setSpacing(true);
        header.getStyle().set("margin-bottom", "8px");
        return header;
    }

    private Div createInfoBanner() {
        Div banner = new Div();
        banner.setWidthFull();
        banner.getStyle()
                .set("background-color", "#ECFDF5")
                .set("border", "1px solid #A7F3D0")
                .set("border-radius", "16px")
                .set("padding", "14px 18px")
                .set("font-size", "14px")
                .set("color", "#064E3B")
                .set("line-height", "1.5")
                .set("margin-bottom", "12px");

        Span broom = new Span("🧹 ");
        Span boldText = new Span("Qanday ishlaydi: ");
        boldText.getStyle().set("font-weight", "700").set("color", "#064E3B");

        Span info = new Span("Ro'yxatdan ochiq muammoni tanlang \u2192 \"oldin\" rasmini ko'ring \u2192 joylashuvga boring \u2192 kamera bilan \"keyin\" rasmini oling \u2192 tasdiqlash");

        banner.add(broom, boldText, info);
        return banner;
    }

    private Div createCard(CardData data) {

        Div card = new Div();
        card.addClassName("task-card");
        card.setWidthFull();
        card.getStyle()
                .set("display", "flex")
                .set("gap", "16px")
                .set("background-color", "#FFFFFF")
                .set("border", "1px solid #E5E7EB")
                .set("border-radius", "18px")
                .set("padding", "16px")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.04)")
                .set("cursor", "pointer");



        Div imgWrapper = new Div();
        imgWrapper.getStyle()
                .set("position", "relative")
                .set("width", "115px")
                .set("height", "110px")
                .set("flex-shrink", "0");

        Image img = new Image(data.imageUrl(), data.title());
        img.setWidth("100%");
        img.setHeight("100%");
        img.getStyle()
                .set("object-fit", "cover")
                .set("border-radius", "12px");

        Span badge = new Span("!");
        badge.getStyle()
                .set("position", "absolute")
                .set("top", "-6px")
                .set("right", "-6px")
                .set("width", "22px")
                .set("height", "22px")
                .set("background-color", data.badgeBg())
                .set("color", "#FFFFFF")
                .set("border-radius", "50%")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("font-size", "12px")
                .set("font-weight", "bold")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.15)");

        imgWrapper.add(img, badge);


        VerticalLayout details = new VerticalLayout();
        details.setPadding(false);
        details.setSpacing(false);
        details.setWidthFull();
        details.getStyle().set("justify-content", "space-between");

        Div textGroup = new Div();
        H3 title = new H3(data.title());
        title.getStyle()
                .set("margin", "0 0 6px 0")
                .set("font-size", "16px")
                .set("font-weight", "700")
                .set("color", "#111827");

        Div meta = new Div();
        meta.getStyle()
                .set("font-size", "13px")
                .set("color", "#6B7280")
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("gap", "12px");

        meta.add(new Span("📍 " + data.location()));
        meta.add(new Span("📏 " + data.distance()));
        meta.add(new Span("🗓 " + data.date()));

        textGroup.add(title, meta);


        HorizontalLayout tags = new HorizontalLayout();
        tags.setSpacing(true);
        tags.getStyle().set("margin-top", "10px");

        Span priorityPill = new Span(data.priorityText());
        priorityPill.getStyle()
                .set("background-color", data.priorityBg())
                .set("color", data.priorityTextColor())
                .set("font-size", "12px")
                .set("font-weight", "600")
                .set("padding", "3px 10px")
                .set("border-radius", "8px");

        Span coinPill = new Span("🪙 +" + data.coins() + " tanga");
        coinPill.getStyle()
                .set("background-color", "#FEF3C7")
                .set("color", "#D97706")
                .set("font-size", "12px")
                .set("font-weight", "600")
                .set("padding", "3px 10px")
                .set("border-radius", "8px");

        tags.add(priorityPill, coinPill);

        details.add(textGroup, tags);
        card.add(imgWrapper, details);

        return card;
    }

    // View ichidagi yordamchi data record
    private record CardData(
            String title,
            String location,
            String distance,
            String date,
            String priorityText,
            String badgeBg,
            String priorityBg,
            String priorityTextColor,
            int coins,
            String imageUrl
    ) {}
    }

