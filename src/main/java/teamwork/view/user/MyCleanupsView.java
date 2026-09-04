package teamwork.view.user;
import teamwork.dto.myReport.MyReportResponseDto;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.data.domain.Page;
import teamwork.service.MyReportsService;
import teamwork.view.layout.user.UserLayout;

@Route(value = "dashboard/my-cleanups", layout = UserLayout.class)
@PageTitle("Mening tozalashlarim")
public class MyCleanupsView extends VerticalLayout {

    private final MyReportsService myReportsService;
    private final VerticalLayout listContainer = new VerticalLayout();

    public MyCleanupsView(MyReportsService myReportsService) {
        this.myReportsService = myReportsService;

        setWidthFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        getStyle().set("background-color", "#F9FAFB")
                .set("min-height", "100vh")
                .set("padding", "24px 16px");


        UI.getCurrent().getPage().executeJs(
                "const styleId = 'cleanup-card-styles';" +
                        "if (!document.getElementById(styleId)) {" +
                        "  const style = document.createElement('style');" +
                        "  style.id = styleId;" +
                        "  style.innerHTML = `" +
                        "    .cleanup-card { transition: transform 0.2s ease, box-shadow 0.2s ease; cursor: pointer; }" +
                        "    .cleanup-card:hover { transform: translateY(-4px); box-shadow: 0 10px 20px rgba(0,0,0,0.08); }" +
                        "  `;" +
                        "  document.head.appendChild(style);" +
                        "}"
        );

        VerticalLayout container = new VerticalLayout();
        container.setMaxWidth("760px");
        container.setWidthFull();
        container.setPadding(false);
        container.setSpacing(true);

        // Header qismi
        container.add(createHeader());

        // Ro'yxat konteyneri
        listContainer.setWidthFull();
        listContainer.setPadding(false);
        listContainer.setSpacing(true);
        container.add(listContainer);

        // Service orqali ma'lumotlarni yuklash
        loadReports();

        add(container);
    }

    private void loadReports() {
        listContainer.removeAll();


        Page<MyReportResponseDto> reportsPage = myReportsService.getMyRprtsPagination(0, 20, null);

        if (reportsPage == null || reportsPage.isEmpty()) {
            listContainer.add(createEmptyState());
        } else {
            reportsPage.getContent().forEach(dto -> listContainer.add(createReportCard(dto)));
        }
    }

    private HorizontalLayout createHeader() {
        Button backBtn = new Button(VaadinIcon.ARROW_LEFT.create(), e -> UI.getCurrent().getPage().getHistory().back());
        backBtn.getStyle()
                .set("background-color", "#FFFFFF")
                .set("border", "1px solid #E5E7EB")
                .set("border-radius", "12px")
                .set("width", "42px")
                .set("height", "42px")
                .set("cursor", "pointer")
                .set("color", "#374151");

        H2 title = new H2("Mening tozalashlarim");
        title.getStyle().set("margin", "0").set("font-size", "22px").set("color", "#111827");

        Paragraph subtitle = new Paragraph("Siz yuborgan tozalash hisobotlari tarixi");
        subtitle.getStyle().set("margin", "0").set("font-size", "14px").set("color", "#6B7280");

        VerticalLayout titles = new VerticalLayout(title, subtitle);
        titles.setPadding(false);
        titles.setSpacing(false);

        HorizontalLayout header = new HorizontalLayout(backBtn, titles);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.getStyle().set("margin-bottom", "12px");
        return header;
    }

    private Div createReportCard(MyReportResponseDto dto) {
        Div card = new Div();
        card.addClassName("cleanup-card");
        card.setWidthFull();
        card.getStyle()
                .set("display", "flex")
                .set("gap", "16px")
                .set("background-color", "#FFFFFF")
                .set("border", "1px solid #E5E7EB")
                .set("border-radius", "18px")
                .set("padding", "16px")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.04)");


        Div imgWrapper = new Div();
        imgWrapper.getStyle()
                .set("position", "relative")
                .set("width", "110px")
                .set("height", "100px")
                .set("flex-shrink", "0");

        String imageUrl = dto.getPhotoUrl() != null && !dto.getPhotoUrl().isBlank()
                ? dto.getPhotoUrl()
                : "https://images.unsplash.com/photo-1532996122724-e3c354a0b15b?w=400";

        Image img = new Image(imageUrl, "Hisobot rasmi");
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
                .set("background-color", getSeverityBadgeColor(dto.getSeverityLevel()))
                .set("color", "#FFFFFF")
                .set("border-radius", "50%")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("font-size", "12px")
                .set("font-weight", "bold")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.15)");

        imgWrapper.add(img, badge);

        // Matnlar qismi
        VerticalLayout details = new VerticalLayout();
        details.setPadding(false);
        details.setSpacing(false);
        details.setWidthFull();
        details.getStyle().set("justify-content", "space-between");

        Div textGroup = new Div();

        String wasteType = dto.getWasteType() != null ? dto.getWasteType().toString() : "Chiqindi";
        String locationShort = dto.getDistrict() != null ? dto.getDistrict() : (dto.getRegion() != null ? dto.getRegion() : "Noma'lum joy");
        H3 title = new H3(wasteType + " — " + locationShort);
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

        String fullLoc = (dto.getDistrict() != null ? dto.getDistrict() + ", " : "") +
                (dto.getRegion() != null ? dto.getRegion() : "");
        meta.add(new Span("📍 " + (fullLoc.isBlank() ? "Manzil kiritilmagan" : fullLoc)));

        if (dto.getDate() != null) {
            meta.add(new Span("🗓 " + dto.getDate()));
        }

        textGroup.add(title, meta);

        // Pastki teglar: Holat (Status) va Tanga
        HorizontalLayout tags = new HorizontalLayout();
        tags.setSpacing(true);
        tags.getStyle().set("margin-top", "10px");

        // Status
        String statusText = dto.getReportStatus() != null ? dto.getReportStatus().name() : "RESOLVED";
        Span statusPill = new Span(statusText);
        statusPill.getStyle()
                .set("background-color", "#F3F4F6")
                .set("color", "#374151")
                .set("font-size", "12px")
                .set("font-weight", "600")
                .set("padding", "3px 10px")
                .set("border-radius", "8px");

        // Tangalar
        int coins = dto.getRewardCoins() != null ? dto.getRewardCoins() : 0;
        Span coinPill = new Span("🪙 +" + coins + " tanga");
        coinPill.getStyle()
                .set("background-color", "#FEF3C7")
                .set("color", "#D97706")
                .set("font-size", "12px")
                .set("font-weight", "600")
                .set("padding", "3px 10px")
                .set("border-radius", "8px");

        tags.add(statusPill, coinPill);

        details.add(textGroup, tags);
        card.add(imgWrapper, details);

        // Kartochka bosilganda qisqa ma'lumot modal oynasini ochish
        card.addClickListener(e -> openDetailsDialog(dto, imageUrl, wasteType));

        return card;
    }

    private void openDetailsDialog(MyReportResponseDto dto, String imageUrl, String wasteType) {
        Dialog dialog = new Dialog();
        dialog.setWidth("440px");
        dialog.setMaxWidth("92vw");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        H3 dialogTitle = new H3(wasteType);
        dialogTitle.getStyle().set("margin", "0 0 6px 0").set("color", "#111827");

        Image modalImg = new Image(imageUrl, "Hisobot rasmi");
        modalImg.setWidthFull();
        modalImg.setHeight("180px");
        modalImg.getStyle().set("object-fit", "cover").set("border-radius", "12px");

        String fullAddress = (dto.getStreetAddress() != null ? dto.getStreetAddress() + ", " : "") +
                (dto.getDistrict() != null ? dto.getDistrict() + ", " : "") +
                (dto.getRegion() != null ? dto.getRegion() : "");
        Paragraph loc = new Paragraph("📍 Manzil: " + (fullAddress.isBlank() ? "Noma'lum" : fullAddress));
        loc.getStyle().set("margin", "4px 0 0 0").set("font-size", "14px").set("color", "#374151");

        Paragraph desc = new Paragraph("📝 Izoh: " + (dto.getDescription() != null ? dto.getDescription() : "Izoh qoldirilmagan"));
        desc.getStyle().set("margin", "2px 0 0 0").set("font-size", "14px").set("color", "#4B5563");

        Paragraph coin = new Paragraph("🪙 Mukofot: " + (dto.getRewardCoins() != null ? dto.getRewardCoins() : 0) + " tanga");
        coin.getStyle().set("margin", "2px 0 0 0").set("font-size", "14px").set("font-weight", "600").set("color", "#D97706");

        Button closeBtn = new Button("Yopish", e -> dialog.close());
        closeBtn.setWidthFull();
        closeBtn.getStyle()
                .set("background-color", "#111827")
                .set("color", "#FFFFFF")
                .set("border-radius", "10px")
                .set("margin-top", "12px")
                .set("cursor", "pointer");

        content.add(dialogTitle, modalImg, loc, desc, coin, closeBtn);
        dialog.add(content);
        dialog.open();
    }

    private String getSeverityBadgeColor(Object severity) {
        if (severity == null) return "#10B981";
        String val = severity.toString().toUpperCase();
        if (val.contains("HIGH") || val.contains("YUQORI")) return "#EF4444";
        if (val.contains("MEDIUM") || val.contains("ORTA") || val.contains("MIDDLE")) return "#F59E0B";
        return "#10B981";
    }

    private Div createEmptyState() {
        Div empty = new Div();
        empty.setWidthFull();
        empty.getStyle()
                .set("text-align", "center")
                .set("padding", "48px 16px")
                .set("color", "#9CA3AF");
        empty.add(new H3("Hozircha hech qanday tozalash hisoboti yo'q"));
        empty.add(new Paragraph("Tozalash uchun hisobotlar yuboring va tangalarga ega bo'ling."));
        return empty;
    }
}