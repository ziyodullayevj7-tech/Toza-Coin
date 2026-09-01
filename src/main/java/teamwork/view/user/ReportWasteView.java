package teamwork.view.user;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import teamwork.dto.attach.AttachDto;
import teamwork.dto.location.LocationDto;
import teamwork.dto.wasteReport.WasteReportDto;
import teamwork.enums.ReportActionEnum;
import teamwork.enums.ReportStatus;
import teamwork.enums.SeverityLevelEnum;
import teamwork.enums.WasteTypeEnum;
import teamwork.service.AttachService;
import teamwork.service.WasteReportService;
import teamwork.view.layout.user.UserLayout;

import java.util.List;

@Route(value = "user/report", layout = UserLayout.class)
@PageTitle("Ifloslik xabar berish | TozaCoin")
@PermitAll
@CssImport("./themes/tozacoin/user-layout.css")
public class ReportWasteView extends VerticalLayout {

    @Autowired
    private AttachService attachService;
    @Autowired
    private WasteReportService wasteReportService;

    // ------------------------------------------------------------------------
    // Draft DTO Objects (Ready for future Service & Repository calls)
    // ------------------------------------------------------------------------
    private final WasteReportDto wasterReportDto = new WasteReportDto();

    private int currentStep = 1;
    private final Div stepProgressBar = new Div();
    private final Div cardContainer = new Div();

    // Step 1 UI components
    private final Button btnCapture = new Button("📷 Kamera orqali rasm olish");
    private final Button btnNextStep1 = new Button("Davom etish →");
    private final Div cameraBox = new Div();

    private String capturedBase64Image;

    // Step 2 UI components
    private Span locationPillText;
    private TextField streetAddressInput;
    private Button btnNextStep2;

    // Step 3 Selected State & Next Button
    private Div selectedCategoryCard = null;
    private Div selectedSeverityCard = null;
    private final Button btnNextStep3 = new Button("Davom etish →");

    // Step 4 Selected State & Next Button
    private Div selectedActionCard = null;
    private final Button btnNextStep4 = new Button("Davom etish →");

    public ReportWasteView() {
        setWidthFull();
        setSpacing(false);
        setPadding(false);

        // Empty location initialization (Resolved via live GPS)
        LocationDto locationDto = new LocationDto();
        locationDto.setLatitude(0.0);
        locationDto.setLongitude(0.0);
        locationDto.setRegion(null);
        locationDto.setDistrict(null);

        wasterReportDto.setLocation(locationDto);
        wasterReportDto.setReportStatus(ReportStatus.PENDING);

        // Top Step Progress Indicator (Qadam 1/5 to 5/5)
        add(buildStepProgressBar());

        // Main Card Container
        cardContainer.setWidthFull();
        add(cardContainer);

        // Render Step 1 Card on load
        renderStepCard(1);
    }

    /* ------------------------------------------------------------------------
     * Step Progress Bar Renderer with Inline Java Layout Styles
     * ------------------------------------------------------------------------ */
    private Div buildStepProgressBar() {
        stepProgressBar.setWidthFull();
        stepProgressBar.getStyle()
                .set("background-color", "#f8fafc")
                .set("padding", "24px 0")
                .set("border-bottom", "1px solid #e2e8f0");
        refreshStepProgressBar();
        return stepProgressBar;
    }

    private void refreshStepProgressBar() {
        stepProgressBar.removeAll();

        Div container = new Div();
        container.getStyle()
                .set("max-width", "720px")
                .set("margin", "0 auto")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "16px");

        Div headerTitleRow = new Div();
        headerTitleRow.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "12px");

        // Back Arrow Button (←) to return to previous step (disabled on Step 6 completion)
        if (currentStep > 1 && currentStep < 6) {
            Button btnBack = new Button("←");
            btnBack.addClassName("btn-step-back-circle");
            btnBack.getStyle()
                    .set("width", "40px")
                    .set("height", "40px")
                    .set("min-width", "40px")
                    .set("border-radius", "50%")
                    .set("background-color", "#ffffff")
                    .set("border", "1px solid #cbd5e1")
                    .set("font-size", "18px")
                    .set("font-weight", "700")
                    .set("color", "334155")
                    .set("cursor", "pointer")
                    .set("box-shadow", "0 2px 6px rgba(0,0,0,0.04)");
            btnBack.addClickListener(e -> renderStepCard(currentStep - 1));
            headerTitleRow.add(btnBack);
        }

        Div titleTextGroup = new Div();
        H3 headerTitle = new H3("Ifloslik xabar berish");
        headerTitle.getStyle().set("margin", "0").set("font-size", "20px").set("font-weight", "700");

        String subtitleText = (currentStep == 6) ? "Murojaat yuborildi 🎉" : "Qadam " + currentStep + " / 5";
        Span headerSubtitle = new Span(subtitleText);
        headerSubtitle.getStyle().set("font-size", "13px").set("color", "#64748b").set("margin-left", "8px");
        headerTitle.add(headerSubtitle);
        titleTextGroup.add(headerTitle);

        headerTitleRow.add(titleTextGroup);

        // Horizontal Step Columns Bar
        Div barsRow = new Div();
        barsRow.getStyle()
                .set("display", "flex")
                .set("width", "100%")
                .set("gap", "12px");

        String[] stepNames = {"Rasm", "Joylashuv", "Tafsilotlar", "Yo'lni tanlang", "Tasdiqlash"};
        for (int i = 1; i <= 5; i++) {
            Div stepCol = new Div();
            stepCol.getStyle()
                    .set("flex", "1")
                    .set("display", "flex")
                    .set("flex-direction", "column")
                    .set("align-items", "center")
                    .set("gap", "6px");

            Div barLine = new Div();
            barLine.getStyle()
                    .set("width", "100%")
                    .set("height", "6px")
                    .set("border-radius", "3px")
                    .set("background-color", (i <= currentStep || currentStep == 6) ? "#15803d" : "#cbd5e1");

            Span label = new Span(stepNames[i - 1]);
            label.getStyle()
                    .set("font-size", "12px")
                    .set("font-weight", (i == currentStep || currentStep == 6) ? "700" : "600")
                    .set("color", (i <= currentStep || currentStep == 6) ? "#15803d" : "#64748b");

            stepCol.add(barLine, label);
            barsRow.add(stepCol);
        }

        container.add(headerTitleRow, barsRow);
        stepProgressBar.add(container);
    }

    private void renderStepCard(int step) {
        this.currentStep = step;
        refreshStepProgressBar();
        cardContainer.removeAll();

        switch (step) {
            case 1 -> cardContainer.add(buildStep1CameraCard());
            case 2 -> cardContainer.add(buildStep2LocationCard());
            case 3 -> cardContainer.add(buildStep3DetailsCard());
            case 4 -> cardContainer.add(buildStep4ActionChoiceCard());
            case 5 -> cardContainer.add(buildStep5ConfirmationCard());
            case 6 -> cardContainer.add(buildStep6SuccessCard());
        }
    }

    private Div createBaseCard() {
        Div card = new Div();
        card.addClassName("report-step-card");
        card.getStyle()
                .set("max-width", "680px")
                .set("margin", "24px auto")
                .set("background-color", "#ffffff")
                .set("border-radius", "20px")
                .set("padding", "32px")
                .set("border", "1px solid #e2e8f0")
                .set("box-shadow", "0 4px 20px rgba(0, 0, 0, 0.04)")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "16px");
        return card;
    }

    /* ------------------------------------------------------------------------
     * Step 1: Camera Card
     * ------------------------------------------------------------------------ */
    private Div buildStep1CameraCard() {
        Div card = createBaseCard();

        H2 title = new H2("Rasm oling");
        Paragraph subtitle = new Paragraph("Ifloslangan joyning rasmini telefon kamerasidan oling.");

        cameraBox.addClassName("camera-preview-dashed-box");
        cameraBox.setId("camera-stream-box");

        if (wasterReportDto.getImageUrl() != null) {
            Image imgPreview = new Image(wasterReportDto.getImageUrl(), "Captured Camera Photo");
            imgPreview.getStyle().set("width", "100%").set("height", "100%").set("object-fit", "cover");
            cameraBox.removeAll();
            cameraBox.add(imgPreview);
            btnCapture.setText("🔄 Qayta rasmga olish");
        } else {
            Div placeholder = new Div();
            placeholder.addClassName("camera-placeholder-content");
            placeholder.add(new Span("📷"), new H3("Hali rasm olinmadi"), new Paragraph("Pastdagi tugmani bosib kamera oching"));
            cameraBox.removeAll();
            cameraBox.add(placeholder);
        }

        btnCapture.addClassName("btn-camera-capture");
        btnCapture.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnCapture.addClickListener(e -> startOrCaptureCamera());

        Paragraph note = new Paragraph("Suiiste'molning oldini olish uchun surat faqat kameradan olinadi.");
        note.addClassName("camera-note-text");

        btnNextStep1.addClassName("btn-step-next-green");
        btnNextStep1.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNextStep1.setEnabled(wasterReportDto.getImageUrl() != null);
        btnNextStep1.addClickListener(e -> renderStepCard(2));

        card.add(title, subtitle, cameraBox, btnCapture, note, btnNextStep1);
        return card;
    }

    /* ------------------------------------------------------------------------
     * Step 2: Yandex-Style Satellite Location Card
     * ------------------------------------------------------------------------ */
    private Div buildStep2LocationCard() {
        Div card = createBaseCard();

        H2 title = new H2("Joylashuvni tasdiqlang");
        Paragraph subtitle = new Paragraph("GPS orqali aniqlangan joylashuv. To'g'rilab qo'ying.");

        Div mapContainer = new Div();
        mapContainer.addClassName("yandex-map-container");

        Div mapDiv = new Div();
        mapDiv.setId("yandex-satellite-map");
        mapDiv.addClassName("yandex-map-div");

        Div centerPin = new Div(new Span("📍"));
        centerPin.addClassName("yandex-center-pin");

        Button btnRecenterGps = new Button("🎯");
        btnRecenterGps.addClassName("btn-recenter-gps");
        btnRecenterGps.addClickListener(e -> recenterMapToUserGps());

        mapContainer.add(mapDiv, centerPin, btnRecenterGps);

        Div locationPill = new Div();
        locationPill.addClassName("location-result-pill");

        Span pillIcon = new Span("📍");
        String dist = wasterReportDto.getLocation().getDistrict();
        String region = wasterReportDto.getLocation().getRegion();
        boolean hasLocation = dist != null && !dist.isBlank() && region != null && !region.isBlank();
        this.locationPillText = new Span(hasLocation ? dist + ", " + region : "⏳ Joylashuv aniqlanmoqda...");
        locationPill.add(pillIcon, locationPillText);

        // Precise Location Selector Button (Full Screen Map Modal)
        Button btnOpenFullMap = new Button("🗺️ Xaritadan aniqroq joyni tanlash");
        btnOpenFullMap.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnOpenFullMap.getStyle()
                .set("width", "100%")
                .set("height", "46px")
                .set("border-radius", "14px")
                .set("font-weight", "700")
                .set("margin-top", "4px")
                .set("cursor", "pointer");
        btnOpenFullMap.addClickListener(e -> openFullScreenMapModal());

        Div addressGroup = new Div();
        addressGroup.addClassName("address-input-group");

        Span addressLabel = new Span("Aniq manzil (ixtiyoriy)");
        this.streetAddressInput = new TextField();
        this.streetAddressInput.setPlaceholder("Ko'cha, uy raqami...");
        this.streetAddressInput.addClassName("street-address-input");
        if (wasterReportDto.getLocation().getStreetAddress() != null) {
            this.streetAddressInput.setValue(wasterReportDto.getLocation().getStreetAddress());
        }
        this.streetAddressInput.addValueChangeListener(e -> wasterReportDto.getLocation().setStreetAddress(e.getValue()));

        addressGroup.add(addressLabel, streetAddressInput);

        this.btnNextStep2 = new Button("Davom etish →");
        this.btnNextStep2.addClassName("btn-step-next-green");
        this.btnNextStep2.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        this.btnNextStep2.setEnabled(hasLocation);
        this.btnNextStep2.addClickListener(e -> {
            wasterReportDto.getLocation().setStreetAddress(streetAddressInput.getValue());
            renderStepCard(3);
        });

        card.add(title, subtitle, mapContainer, locationPill, btnOpenFullMap, addressGroup, btnNextStep2);
        initYandexSatelliteMap();
        return card;
    }

    /* ------------------------------------------------------------------------
     * Step 3: Details Card (Categories, Severity, Description)
     * ------------------------------------------------------------------------ */
    private Div buildStep3DetailsCard() {
        Div card = createBaseCard();

        H2 title = new H2("Tafsilotlarni kiriting");

        // 1. Category Section (Toifa - 3 Column Grid)
        Span catLabel = new Span("Toifa");
        catLabel.getStyle().set("font-size", "14px").set("font-weight", "700").set("color", "#0f172a").set("margin-top", "8px");

        Div categoryGrid = new Div();
        categoryGrid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(3, 1fr)")
                .set("gap", "12px")
                .set("width", "100%");

        List<CategoryItem> categories = List.of(
                new CategoryItem("Plastik chiqindi", "🍾", WasteTypeEnum.PLASTIC),
                new CategoryItem("Noqonuniy tashlash", "🚫", WasteTypeEnum.ILLEGAL_DUMP),
                new CategoryItem("Suv ifloslanishi", "💧", WasteTypeEnum.WATER),
                new CategoryItem("Havo ifloslanishi", "💨", WasteTypeEnum.AIR),
                new CategoryItem("Elektron chiqindi", "💻", WasteTypeEnum.ELECTRONIC),
                new CategoryItem("Boshqa", "🗑️", WasteTypeEnum.OTHER)
        );

        for (CategoryItem item : categories) {
            Div catCard = new Div();
            styleSelectionCard(catCard);

            if (wasterReportDto.getWasteType() == item.type) {
                applyPurpleSelectedStyle(catCard);
                this.selectedCategoryCard = catCard;
            }

            Span icon = new Span(item.emoji);
            icon.getStyle().set("font-size", "24px");

            Span name = new Span(item.name);
            name.getStyle().set("font-size", "13px").set("font-weight", "600").set("color", "#0f172a");

            catCard.add(icon, name);
            catCard.addClickListener(e -> {
                if (selectedCategoryCard != null) removeSelectedStyle(selectedCategoryCard);
                applyPurpleSelectedStyle(catCard);
                selectedCategoryCard = catCard;
                wasterReportDto.setWasteType(item.type);
                updateStep3NextButtonState();
            });

            categoryGrid.add(catCard);
        }

        // 2. Severity Section (Og'irlik darajasi - 3 Column Grid)
        Span sevLabel = new Span("Og'irlik darajasi");
        sevLabel.getStyle().set("font-size", "14px").set("font-weight", "700").set("color", "#0f172a").set("margin-top", "12px");

        Div severityGrid = new Div();
        severityGrid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(3, 1fr)")
                .set("gap", "12px")
                .set("width", "100%");

        List<SeverityItem> severities = List.of(
                new SeverityItem("Past", "Kichik miqdordagi chiqindi", "🟢", SeverityLevelEnum.LOW),
                new SeverityItem("O'rta", "O'rta miqdordagi chiqindi", "🟡", SeverityLevelEnum.MEDIUM),
                new SeverityItem("Yuqori", "Katta miqdordagi chiqindi", "🔴", SeverityLevelEnum.HIGH)
        );

        for (SeverityItem item : severities) {
            Div sevCard = new Div();
            styleSelectionCard(sevCard);

            if (wasterReportDto.getSeverityLevel() == item.level) {
                applyOrangeSelectedStyle(sevCard);
                this.selectedSeverityCard = sevCard;
            }

            Span dot = new Span(item.dotEmoji);
            dot.getStyle().set("font-size", "16px");

            H4 name = new H4(item.title);
            name.getStyle().set("margin", "4px 0 2px 0").set("font-size", "14px").set("font-weight", "700");

            Paragraph desc = new Paragraph(item.desc);
            desc.getStyle().set("margin", "0").set("font-size", "11px").set("color", "#64748b").set("text-align", "center");

            sevCard.add(dot, name, desc);
            sevCard.addClickListener(e -> {
                if (selectedSeverityCard != null) removeSelectedStyle(selectedSeverityCard);
                applyOrangeSelectedStyle(sevCard);
                selectedSeverityCard = sevCard;
                wasterReportDto.setSeverityLevel(item.level);
                updateStep3NextButtonState();
            });

            severityGrid.add(sevCard);
        }

        // 3. Description Field (Tavsif)
        Span descLabel = new Span("Tavsif");
        descLabel.getStyle().set("font-size", "14px").set("font-weight", "700").set("color", "#0f172a").set("margin-top", "12px");

        TextArea descriptionInput = new TextArea();
        descriptionInput.setPlaceholder("Holat haqida qisqacha yozing...");
        descriptionInput.setWidthFull();
        descriptionInput.getStyle().set("min-height", "90px");
        if (wasterReportDto.getDescription() != null) {
            descriptionInput.setValue(wasterReportDto.getDescription());
        }
        descriptionInput.addValueChangeListener(e -> wasterReportDto.setDescription(e.getValue()));

        // Next Button
        btnNextStep3.addClassName("btn-step-next-green");
        btnNextStep3.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        updateStep3NextButtonState();
        btnNextStep3.addClickListener(e -> {
            wasterReportDto.setDescription(descriptionInput.getValue());
            renderStepCard(4);
        });

        card.add(title, catLabel, categoryGrid, sevLabel, severityGrid, descLabel, descriptionInput, btnNextStep3);
        return card;
    }

    private void styleSelectionCard(Div card) {
        card.getStyle()
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "14px")
                .set("padding", "16px 12px")
                .set("background-color", "#f8fafc")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("gap", "6px")
                .set("cursor", "pointer")
                .set("transition", "all 0.2s ease");
    }

    private void applyPurpleSelectedStyle(Div card) {
        card.getStyle()
                .set("border", "2px solid #8b5cf6")
                .set("background-color", "#f5f3ff")
                .set("box-shadow", "0 0 0 3px rgba(139, 92, 246, 0.15)");
    }

    private void applyOrangeSelectedStyle(Div card) {
        card.getStyle()
                .set("border", "2px solid #f59e0b")
                .set("background-color", "#fffbeb")
                .set("box-shadow", "0 0 0 3px rgba(245, 158, 11, 0.15)");
    }

    private void removeSelectedStyle(Div card) {
        styleSelectionCard(card);
    }

    private void updateStep3NextButtonState() {
        boolean isValid = (wasterReportDto.getWasteType() != null && wasterReportDto.getSeverityLevel() != null);
        btnNextStep3.setEnabled(isValid);
    }

    /* ------------------------------------------------------------------------
     * Step 4: Action Choice Card (Yo'lni tanlang)
     * ------------------------------------------------------------------------ */
    private Div buildStep4ActionChoiceCard() {
        Div card = createBaseCard();

        Div headerEmoji = new Div(new Span("🤔"));
        headerEmoji.getStyle().set("font-size", "42px").set("text-align", "center");

        H2 title = new H2("Yo'lni tanlang");
        title.getStyle().set("text-align", "center").set("margin", "0");

        Paragraph subtitle = new Paragraph("Harakatingizga qarab mukofot miqdori farq qiladi");
        subtitle.getStyle().set("text-align", "center").set("color", "#64748b").set("margin", "0 0 16px 0");

        // Option 1: O'zim tozalayman (+50 coins)
        Div option1Card = buildActionOptionCard(
                "O'zim tozalayman",
                "Joyni tozalab, keyingi holat suratini yuborasiz — ko'proq tanga",
                "🧹",
                "🪙 50",
                "#fef3c7",
                "#d97706",
                ReportActionEnum.CLEAN_MYSELF,
                50
        );

        // Option 2: Mas'ul tashkilotga yuborish (+15 coins)
        Div option2Card = buildActionOptionCard(
                "Mas'ul tashkilotga yuborish",
                "Murojaat tegishli xizmatga yo'naltiriladi — kamroq tanga",
                "📬",
                "🪙 15",
                "#e0f2fe",
                "#0284c7",
                ReportActionEnum.SEND_TO_OTHERS,
                15
        );

        // Yellow Notice Box
        Div noticeBox = new Div();
        noticeBox.getStyle()
                .set("background-color", "#fffbeb")
                .set("border", "1px solid #fde68a")
                .set("border-radius", "12px")
                .set("padding", "16px")
                .set("color", "#92400e")
                .set("font-size", "13px");
        noticeBox.add(new Paragraph("ℹ️ Eslatma: o'zingiz qayd etgan muammo uchun ball minimal. To'liq tanga boshqa fuqaro qayd etgan muammoni tozalaganda beriladi."));

        // Next Button
        btnNextStep4.addClassName("btn-step-next-green");
        btnNextStep4.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNextStep4.setEnabled(wasterReportDto.getReportAction() != null);
        btnNextStep4.addClickListener(e -> renderStepCard(5));

        card.add(headerEmoji, title, subtitle, option1Card, option2Card, noticeBox, btnNextStep4);
        return card;
    }

    private Div buildActionOptionCard(String titleText, String descText, String emoji, String coinText, String badgeBg, String badgeColor, ReportActionEnum actionType, int rewardCoins) {
        Div optionCard = new Div();
        optionCard.getStyle()
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "16px")
                .set("padding", "20px")
                .set("background-color", "#f8fafc")
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "16px")
                .set("cursor", "pointer")
                .set("transition", "all 0.2s ease");

        if (wasterReportDto.getReportAction() == actionType) {
            applyActionSelectedStyle(optionCard);
            this.selectedActionCard = optionCard;
        }

        Span optIcon = new Span(emoji);
        optIcon.getStyle().set("font-size", "28px");

        Div optTextGroup = new Div();
        optTextGroup.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "2px");

        H3 optTitle = new H3(titleText);
        optTitle.getStyle().set("margin", "0").set("font-size", "16px").set("font-weight", "700");

        Paragraph optDesc = new Paragraph(descText);
        optDesc.getStyle().set("margin", "0").set("font-size", "12px").set("color", "#64748b");

        Span selectedTag = new Span("✓ Tanlandi");
        selectedTag.getStyle().set("font-size", "12px").set("font-weight", "700").set("color", "#2563eb").set("margin-top", "4px");
        if (wasterReportDto.getReportAction() == actionType) {
            selectedTag.getStyle().set("display", "inline-block");
        } else {
            selectedTag.getStyle().set("display", "none");
        }

        optTextGroup.add(optTitle, optDesc, selectedTag);

        Div optBadge = new Div(new Span(coinText));
        optBadge.getStyle()
                .set("margin-left", "auto")
                .set("background-color", badgeBg)
                .set("color", badgeColor)
                .set("font-weight", "700")
                .set("padding", "8px 14px")
                .set("border-radius", "20px");

        optionCard.add(optIcon, optTextGroup, optBadge);
        optionCard.addClickListener(e -> {
            if (selectedActionCard != null) {
                selectedActionCard.getStyle()
                        .set("border", "1px solid #e2e8f0")
                        .set("background-color", "#f8fafc")
                        .set("box-shadow", "none");

                selectedActionCard.getChildren()
                        .filter(child -> child instanceof Div)
                        .flatMap(child -> ((Div) child).getChildren())
                        .filter(sub -> sub instanceof Span && "✓ Tanlandi".equals(((Span) sub).getText()))
                        .forEach(tag -> tag.getElement().getStyle().set("display", "none"));
            }

            applyActionSelectedStyle(optionCard);
            selectedTag.getStyle().set("display", "inline-block");
            selectedActionCard = optionCard;

            wasterReportDto.setReportAction(actionType);
            wasterReportDto.setRewardCoins(rewardCoins);
            btnNextStep4.setEnabled(true);
        });

        return optionCard;
    }

    private void applyActionSelectedStyle(Div card) {
        card.getStyle()
                .set("border", "2px solid #3b82f6")
                .set("background-color", "#eff6ff")
                .set("box-shadow", "0 0 0 3px rgba(59, 130, 246, 0.15)");
    }

    /* ------------------------------------------------------------------------
     * Step 5: Confirmation & Summary Recap Card (Tasdiqlash)
     * ------------------------------------------------------------------------ */
    private Div buildStep5ConfirmationCard() {
        Div card = createBaseCard();

        H2 title = new H2("Xulosa");

        // Photo Preview Banner
        Div photoBanner = new Div();
        photoBanner.getStyle()
                .set("width", "100%")
                .set("height", "200px")
                .set("border-radius", "16px")
                .set("overflow", "hidden")
                .set("border", "1px solid #e2e8f0");

        String previewSrc = (this.capturedBase64Image != null && !this.capturedBase64Image.isBlank())
                ? this.capturedBase64Image
                : wasterReportDto.getImageUrl();

        if (previewSrc != null) {
            Image img = new Image(previewSrc, "Captured Photo");
            img.getStyle().set("width", "100%").set("height", "100%").set("object-fit", "cover");
            photoBanner.add(img);
        }

        // Summary Recap Rows
        Div summaryList = new Div();
        summaryList.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("border", "1px solid #e2e8f0")
                .set("border-radius", "14px")
                .set("overflow", "hidden");

        summaryList.add(
                createRecapRow("Toifa", formatCategoryName(wasterReportDto.getWasteType())),
                createRecapRow("Og'irlik", formatSeverityName(wasterReportDto.getSeverityLevel())),
                createRecapRow("Joylashuv", wasterReportDto.getLocation().getDistrict() + ", " + wasterReportDto.getLocation().getRegion()),
                createRecapRow("Harakat", wasterReportDto.getReportAction() == ReportActionEnum.CLEAN_MYSELF ? "O'zim tozalayman" : "Mas'ul tashkilotga yuborish")
        );

        // Expected Reward Card
        Div rewardBox = new Div();
        rewardBox.getStyle()
                .set("background-color", "#e0f2fe")
                .set("border", "1px solid #bae6fd")
                .set("border-radius", "14px")
                .set("padding", "20px")
                .set("display", "flex")
                .set("justify-content", "space-between")
                .set("align-items", "center");

        Div rewardLabelGroup = new Div(new Span("Kutilayotgan mukofot"));
        rewardLabelGroup.getStyle().set("color", "#0369a1").set("font-weight", "600");

        int coins = wasterReportDto.getRewardCoins() != null ? wasterReportDto.getRewardCoins() : 15;
        Div rewardAmountGroup = new Div(new Span("🪙 " + coins + " tanga"));
        rewardAmountGroup.getStyle().set("color", "#0284c7").set("font-weight", "800").set("font-size", "22px");

        rewardBox.add(rewardLabelGroup, rewardAmountGroup);

        // Final Submit Button
        Button btnSubmitFinal = new Button("Yuborish ✓");
        btnSubmitFinal.addClassName("btn-step-next-green");
        btnSubmitFinal.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnSubmitFinal.addClickListener(e -> onFinalSubmitConfirmed());

        card.add(title, photoBanner, summaryList, rewardBox, btnSubmitFinal);
        return card;
    }

    private Div createRecapRow(String label, String value) {
        Div row = new Div();
        row.getStyle()
                .set("display", "flex")
                .set("justify-content", "space-between")
                .set("padding", "14px 20px")
                .set("background-color", "#f8fafc")
                .set("border-bottom", "1px solid #e2e8f0");

        Span lbl = new Span(label);
        lbl.getStyle().set("color", "#64748b").set("font-weight", "600");

        Span val = new Span(value);
        val.getStyle().set("color", "#0f172a").set("font-weight", "700");

        row.add(lbl, val);
        return row;
    }

    private void onFinalSubmitConfirmed() {
        boolean create = wasteReportService.create(wasterReportDto);
        if (!create) {
            Notification.show("Something went wrong", 4000, Notification.Position.MIDDLE);
            return;
        }
        renderStepCard(6);
    }

    private Div buildStep6SuccessCard() {
        Div card = createBaseCard();
        card.getStyle().set("text-align", "center").set("align-items", "center");

        Div celebrationBadge = new Div(new Span("🎉 🪙"));
        celebrationBadge.getStyle().set("font-size", "54px").set("margin-bottom", "8px");

        H2 title = new H2("Murojaatingiz muvaffaqiyatli qabul qilindi!");
        title.getStyle().set("margin", "0").set("color", "#15803d").set("font-size", "22px").set("font-weight", "800");

        Paragraph subtitle = new Paragraph("Rasm va joylashuv ma'lumotlari tizimga saqlandi.");
        subtitle.getStyle().set("color", "#64748b").set("margin", "4px 0 20px 0").set("font-size", "14px");

        // Gamified Reward Box
        Div rewardCard = new Div();
        rewardCard.getStyle()
                .set("width", "100%")
                .set("background", "linear-gradient(135deg, #fef3c7 0%, #fde68a 100%)")
                .set("border", "1px solid #f59e0b")
                .set("border-radius", "16px")
                .set("padding", "20px")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("gap", "8px")
                .set("box-shadow", "0 4px 14px rgba(245, 158, 11, 0.15)");

        Span rewardTitle = new Span("Hisoblangan mukofot");
        rewardTitle.getStyle().set("font-size", "13px").set("font-weight", "700").set("color", "#b45309");

        int coins = wasterReportDto.getRewardCoins() != null ? wasterReportDto.getRewardCoins() : 15;
        H1 coinAmount = new H1("+" + coins + " Tanga 🪙");
        coinAmount.getStyle().set("margin", "0").set("font-size", "32px").set("font-weight", "900").set("color", "#d97706");

        Span statusBadge = new Span("🟡 Holati: Ko'rib chiqilmoqda (Pending)");
        statusBadge.getStyle()
                .set("background-color", "#ffffff")
                .set("color", "#92400e")
                .set("font-size", "12px")
                .set("font-weight", "700")
                .set("padding", "6px 14px")
                .set("border-radius", "20px")
                .set("margin-top", "4px");

        rewardCard.add(rewardTitle, coinAmount, statusBadge);

        // Two Primary CTA Action Buttons
        Div btnRow = new Div();
        btnRow.getStyle()
                .set("display", "flex")
                .set("gap", "14px")
                .set("width", "100%")
                .set("margin-top", "24px");

        Button btnNewReport = new Button("+ Yangi xabar berish");
        btnNewReport.addClassName("btn-step-next-green");
        btnNewReport.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNewReport.getStyle().set("flex", "1");
        btnNewReport.addClickListener(e -> {
            resetFormState();
            renderStepCard(1);
        });

        Button btnMyReports = new Button("📋 Mening xabarlarim");
        btnMyReports.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        btnMyReports.getStyle()
                .set("flex", "1")
                .set("height", "48px")
                .set("border-radius", "12px")
                .set("font-weight", "700")
                .set("cursor", "pointer");
        btnMyReports.addClickListener(e -> UI.getCurrent().navigate("user/my-reports"));

        btnRow.add(btnNewReport, btnMyReports);

        card.add(celebrationBadge, title, subtitle, rewardCard, btnRow);
        return card;
    }

    private void resetFormState() {
        wasterReportDto.setImageUrl(null);
        wasterReportDto.setWasteType(null);
        wasterReportDto.setSeverityLevel(null);
        wasterReportDto.setDescription(null);
        wasterReportDto.setReportAction(null);
        wasterReportDto.setRewardCoins(null);
        wasterReportDto.setReportStatus(ReportStatus.PENDING);

        this.capturedBase64Image = null;
        this.selectedCategoryCard = null;
        this.selectedSeverityCard = null;
        this.selectedActionCard = null;

        LocationDto locationDto = new LocationDto();
        locationDto.setLatitude(0.0);
        locationDto.setLongitude(0.0);
        locationDto.setRegion(null);
        locationDto.setDistrict(null);
        wasterReportDto.setLocation(locationDto);

        btnCapture.setText("📷 Kamera orqali rasm olish");
        btnNextStep1.setEnabled(false);
    }

    private String formatCategoryName(WasteTypeEnum cat) {
        if (cat == null) return "Plastik chiqindi";
        return switch (cat) {
            case PLASTIC -> "Plastik chiqindi";
            case ILLEGAL_DUMP -> "Noqonuniy tashlash";
            case WATER -> "Suv ifloslanishi";
            case AIR -> "Havo ifloslanishi";
            case ELECTRONIC -> "Elektron chiqindi";
            case OTHER -> "Boshqa";
        };
    }

    private String formatSeverityName(SeverityLevelEnum sev) {
        if (sev == null) return "O'rta";
        return switch (sev) {
            case LOW -> "Past";
            case MEDIUM -> "O'rta";
            case HIGH -> "Yuqori";
        };
    }

    // Camera JS methods
    private void startOrCaptureCamera() {
        UI.getCurrent().getPage().executeJs(
                "let overlay = document.querySelector('#camera-fullscreen-overlay');" +
                        "if (overlay) overlay.remove();" +
                        "overlay = document.createElement('div');" +
                        "overlay.id = 'camera-fullscreen-overlay';" +
                        "overlay.className = 'fullscreen-camera-overlay';" +
                        "const video = document.createElement('video');" +
                        "video.id = 'live-camera-video';" +
                        "video.autoplay = true;" +
                        "video.playsInline = true;" +
                        "video.className = 'fullscreen-video-stream';" +
                        "video.style.transform = 'scaleX(-1)';" +
                        "const topControls = document.createElement('div');" +
                        "topControls.className = 'camera-top-controls';" +
                        "const closeBtn = document.createElement('button');" +
                        "closeBtn.className = 'btn-close-camera';" +
                        "closeBtn.innerHTML = '❌ Yopish';" +
                        "closeBtn.onclick = function() {" +
                        "   if (video.srcObject) { video.srcObject.getTracks().forEach(t => t.stop()); }" +
                        "   overlay.remove();" +
                        "};" +
                        "topControls.appendChild(closeBtn);" +
                        "const bottomControls = document.createElement('div');" +
                        "bottomControls.className = 'camera-bottom-controls';" +
                        "const shutterBtn = document.createElement('button');" +
                        "shutterBtn.className = 'btn-shutter-capture';" +
                        "shutterBtn.innerHTML = '📸 Suratga olish';" +
                        "const element = $0;" +
                        "shutterBtn.onclick = function() {" +
                        "   const canvas = document.createElement('canvas');" +
                        "   canvas.width = video.videoWidth || 1920;" +
                        "   canvas.height = video.videoHeight || 1080;" +
                        "   const ctx = canvas.getContext('2d');" +
                        "   ctx.translate(canvas.width, 0);" +
                        "   ctx.scale(-1, 1);" +
                        "   ctx.drawImage(video, 0, 0, canvas.width, canvas.height);" +
                        "   const base64Data = canvas.toDataURL('image/jpeg', 0.85);" +
                        "   if (video.srcObject) { video.srcObject.getTracks().forEach(t => t.stop()); }" +
                        "   overlay.remove();" +
                        "   const box = document.querySelector('#camera-stream-box');" +
                        "   if (box) {" +
                        "       box.innerHTML = '<img src=\"' + base64Data + '\" style=\"width:100%; height:100%; object-fit:contain; background-color:#0f172a; border-radius:14px;\" />';" +
                        "   }" +
                        "   element.$server.saveCameraSnapshot(base64Data);" +
                        "};" +
                        "bottomControls.appendChild(shutterBtn);" +
                        "overlay.appendChild(video);" +
                        "overlay.appendChild(topControls);" +
                        "overlay.appendChild(bottomControls);" +
                        "document.body.appendChild(overlay);" +
                        "navigator.mediaDevices.getUserMedia({" +
                        "   video: { facingMode: { ideal: 'environment' }, width: { ideal: 1920 }, height: { ideal: 1080 } }" +
                        "})" +
                        ".then(stream => { video.srcObject = stream; })" +
                        ".catch(err => { alert('Kamerani ochishda xatolik: ' + err); overlay.remove(); });",
                this.getElement()
        );
    }

    @ClientCallable
    public void saveCameraSnapshot(String base64Image) {
        try {
            this.capturedBase64Image = base64Image;
            AttachDto attachDTO = attachService.uploadBase64CameraPhoto(base64Image);
            this.wasterReportDto.setImageUrl(attachDTO.getPath());

            btnCapture.setText("🔄 Qayta rasmga olish");
            btnNextStep1.setEnabled(true);

            Notification.show("Surat muvaffaqiyatli olindi! ✅", 2500, Notification.Position.BOTTOM_END);
        } catch (Exception e) {
            Notification.show("Xatolik: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }

    private void openFullScreenMapModal() {
        UI.getCurrent().getPage().executeJs(
                "let overlay = document.querySelector('#fullscreen-map-overlay');" +
                        "if (overlay) overlay.remove();" +
                        "overlay = document.createElement('div');" +
                        "overlay.id = 'fullscreen-map-overlay';" +
                        "overlay.className = 'fullscreen-map-overlay';" +
                        "const mapDiv = document.createElement('div');" +
                        "mapDiv.id = 'full-satellite-map';" +
                        "mapDiv.className = 'fullscreen-map-div';" +
                        "const topBar = document.createElement('div');" +
                        "topBar.className = 'map-top-bar';" +
                        "topBar.id = 'full-map-address-pill';" +
                        "topBar.innerHTML = '📍 Joylashuv aniqlanmoqda...';" +
                        "const centerPin = document.createElement('div');" +
                        "centerPin.className = 'yandex-center-pin';" +
                        "centerPin.innerHTML = '<span>📍</span>';" +
                        "const recenterBtn = document.createElement('button');" +
                        "recenterBtn.className = 'btn-recenter-gps';" +
                        "recenterBtn.innerHTML = '🎯';" +
                        "const bottomControls = document.createElement('div');" +
                        "bottomControls.className = 'map-bottom-controls';" +
                        "const confirmBtn = document.createElement('button');" +
                        "confirmBtn.className = 'btn-confirm-map-location';" +
                        "confirmBtn.innerHTML = '✓ Joylashuvni tasdiqlash';" +
                        "confirmBtn.onclick = function() {" +
                        "   if (window.fullYandexMapInstance && window.yandexMapInstance) {" +
                        "       const center = window.fullYandexMapInstance.getCenter();" +
                        "       window.yandexMapInstance.setView([center.lat, center.lng], 17);" +
                        "       setTimeout(() => { window.yandexMapInstance.invalidateSize(); }, 200);" +
                        "   }" +
                        "   overlay.remove();" +
                        "};" +
                        "bottomControls.appendChild(confirmBtn);" +
                        "overlay.appendChild(mapDiv);" +
                        "overlay.appendChild(topBar);" +
                        "overlay.appendChild(centerPin);" +
                        "overlay.appendChild(recenterBtn);" +
                        "overlay.appendChild(bottomControls);" +
                        "document.body.appendChild(overlay);" +
                        "const element = $0;" +
                        "const initLat = $1;" +
                        "const initLng = $2;" +
                        "const fullMap = L.map('full-satellite-map', { zoomControl: false }).setView([initLat, initLng], 17);" +
                        "window.fullYandexMapInstance = fullMap;" +
                        "L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {" +
                        "   maxZoom: 19, attribution: 'Esri Satellite'" +
                        "}).addTo(fullMap);" +
                        "L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager_only_labels/{z}/{x}/{y}{r}.png', {" +
                        "   maxZoom: 19" +
                        "}).addTo(fullMap);" +
                        "fullMap.on('moveend', function() {" +
                        "   const center = fullMap.getCenter();" +
                        "   window.fetchReverseGeocode(center.lat, center.lng, element);" +
                        "});" +
                        "recenterBtn.onclick = function() {" +
                        "   if ('geolocation' in navigator) {" +
                        "       element.$server.setGeocodingLoadingState();" +
                        "       navigator.geolocation.getCurrentPosition(pos => {" +
                        "           const lat = pos.coords.latitude;" +
                        "           const lng = pos.coords.longitude;" +
                        "           fullMap.flyTo([lat, lng], 17, { duration: 1.2 });" +
                        "           window.fetchReverseGeocode(lat, lng, element);" +
                        "       }, err => { alert('GPS joylashuvni aniqlab bo\\'lmadi'); }, { enableHighAccuracy: true, timeout: 10000 });" +
                        "   }" +
                        "};" +
                        "window.fetchReverseGeocode(initLat, initLng, element);" +
                        "setTimeout(() => { fullMap.invalidateSize(); }, 300);",
                this.getElement(),
                wasterReportDto.getLocation().getLatitude() != 0.0 ? wasterReportDto.getLocation().getLatitude() : 41.3110,
                wasterReportDto.getLocation().getLongitude() != 0.0 ? wasterReportDto.getLocation().getLongitude() : 69.2405
        );
    }

    private void initYandexSatelliteMap() {
        UI.getCurrent().getPage().executeJs(
                "window.reverseGeocodeTimer = null;" +
                        "window.fetchReverseGeocode = function(lat, lng, element) {" +
                        "   if (!element || !element.$server) return;" +
                        "   clearTimeout(window.reverseGeocodeTimer);" +
                        "   window.reverseGeocodeTimer = setTimeout(function() {" +
                        "       fetch('https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=' + lat + '&longitude=' + lng + '&localityLanguage=uz')" +
                        "           .then(function(res) { return res.json(); })" +
                        "           .then(function(bd) {" +
                        "               let dist = bd.locality || bd.city || 'Tuman';" +
                        "               let reg = bd.principalSubdivision || 'Viloyat';" +
                        "               if (typeof dist === 'string' && dist.toLowerCase().includes('district')) dist = dist.replace(/district/gi, 'tumani').trim();" +
                        "               if (typeof reg === 'string' && reg.toLowerCase() === 'tashkent') reg = 'Toshkent shahri';" +
                        "               if (typeof reg === 'string' && reg.toLowerCase().includes('region')) reg = reg.replace(/region/gi, 'viloyati').trim();" +
                        "               element.$server.updateLocationFromCenterWithAddress(lat, lng, dist, reg);" +
                        "           })" +
                        "           .catch(function() {" +
                        "               fetch('https://nominatim.openstreetmap.org/reverse?format=json&lat=' + lat + '&lon=' + lng + '&accept-language=uz,en')" +
                        "                   .then(function(res) { return res.json(); })" +
                        "                   .then(function(data) {" +
                        "                       if (data && data.address) {" +
                        "                           const dist = data.address.suburb || data.address.district || data.address.county || data.address.city_district || data.address.town || data.address.village || 'Tuman';" +
                        "                           const reg = data.address.state || data.address.region || data.address.city || 'Viloyat';" +
                        "                           element.$server.updateLocationFromCenterWithAddress(lat, lng, dist, reg);" +
                        "                       } else {" +
                        "                           element.$server.updateLocationFromCenterWithAddress(lat, lng, 'Aniqlangan manzil', 'Toshkent shahri');" +
                        "                       }" +
                        "                   })" +
                        "                   .catch(function() {" +
                        "                       element.$server.updateLocationFromCenterWithAddress(lat, lng, 'Aniqlangan manzil', 'Toshkent shahri');" +
                        "                   });" +
                        "           });" +
                        "   }, 300);" +
                        "};" +
                        "window.initYandexLeafletMap = function(element) {" +
                        "   const storedLat = $1;" +
                        "   const storedLng = $2;" +
                        "   const initialLat = storedLat !== 0.0 ? storedLat : 41.3110;" +
                        "   const initialLng = storedLng !== 0.0 ? storedLng : 69.2405;" +
                        "   const container = document.getElementById('yandex-satellite-map');" +
                        "   if (container) { container.innerHTML = ''; }" +
                        "   const map = L.map('yandex-satellite-map', { zoomControl: false }).setView([initialLat, initialLng], 17);" +
                        "   window.yandexMapInstance = map;" +
                        "   L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', { maxZoom: 19, attribution: 'Esri Satellite' }).addTo(map);" +
                        "   L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager_only_labels/{z}/{x}/{y}{r}.png', { maxZoom: 19 }).addTo(map);" +
                        "   map.on('moveend', function() { const c = map.getCenter(); window.fetchReverseGeocode(c.lat, c.lng, element); });" +
                        "   setTimeout(() => { map.invalidateSize(); }, 300);" +
                        "   if (storedLat !== 0.0) {" +
                        "       window.fetchReverseGeocode(storedLat, storedLng, element);" +
                        "   } else if ('geolocation' in navigator) {" +
                        "       navigator.geolocation.getCurrentPosition(pos => {" +
                        "           const userLat = pos.coords.latitude;" +
                        "           const userLng = pos.coords.longitude;" +
                        "           map.flyTo([userLat, userLng], 17, { duration: 1.2 });" +
                        "           window.fetchReverseGeocode(userLat, userLng, element);" +
                        "       }, err => {" +
                        "           window.fetchReverseGeocode(initialLat, initialLng, element);" +
                        "       }, { enableHighAccuracy: true, timeout: 8000 });" +
                        "   } else {" +
                        "       window.fetchReverseGeocode(initialLat, initialLng, element);" +
                        "   }" +
                        "};" +
                        "if (!window.L) {" +
                        "   const link = document.createElement('link');" +
                        "   link.rel = 'stylesheet';" +
                        "   link.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css';" +
                        "   document.head.appendChild(link);" +
                        "   const script = document.createElement('script');" +
                        "   script.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js';" +
                        "   script.onload = () => { window.initYandexLeafletMap($0); };" +
                        "   document.head.appendChild(script);" +
                        "} else {" +
                        "   window.initYandexLeafletMap($0);" +
                        "}",
                this.getElement(),
                wasterReportDto.getLocation().getLatitude(),
                wasterReportDto.getLocation().getLongitude()
        );
    }

    private void recenterMapToUserGps() {
        UI.getCurrent().getPage().executeJs(
                "if (window.yandexMapInstance && 'geolocation' in navigator) {" +
                        "   navigator.geolocation.getCurrentPosition(pos => {" +
                        "       const lat = pos.coords.latitude;" +
                        "       const lng = pos.coords.longitude;" +
                        "       window.yandexMapInstance.flyTo([lat, lng], 17, { duration: 1.2 });" +
                        "       window.fetchReverseGeocode(lat, lng, $0);" +
                        "   }, err => { alert('GPS joylashuvni aniqlab bo\\'lmadi'); }, { enableHighAccuracy: true, timeout: 10000 });" +
                        "}",
                this.getElement()
        );
    }

    @ClientCallable
    public void setGeocodingLoadingState() {
        if (this.locationPillText != null) {
            this.locationPillText.setText("⏳ Joylashuv aniqlanmoqda...");
        }
    }

    @ClientCallable
    public void updateLocationFromCenterWithAddress(double lat, double lng, String district, String region) {
        this.wasterReportDto.getLocation().setLatitude(lat);
        this.wasterReportDto.getLocation().setLongitude(lng);
        this.wasterReportDto.getLocation().setDistrict(district);
        this.wasterReportDto.getLocation().setRegion(region);

        if (this.locationPillText != null) {
            this.locationPillText.setText(district + ", " + region);
        }

        if (this.btnNextStep2 != null) {
            this.btnNextStep2.setEnabled(true);
        }

        UI.getCurrent().getPage().executeJs(
                "const topPill = document.querySelector('#full-map-address-pill');" +
                        "if (topPill) { topPill.innerHTML = '📍 ' + $0 + ', ' + $1; }",
                district, region
        );
    }

    @ClientCallable
    public void onGpsFailed(String errorMsg) {
        UI.getCurrent().getPage().executeJs(
                "const loadingBox = document.querySelector('#gps-loading-box');" +
                        "if (loadingBox) {" +
                        "   loadingBox.innerHTML = '<div style=\"font-size:36px;margin-bottom:8px;\">⚠️</div><h3 style=\"margin:0;color:#c2410c;font-size:16px;\">GPS joylashuvni avtomatik aniqlab bo\\'lmadi</h3><p style=\"margin:4px 0 16px 0;color:#9a3412;font-size:13px;\">Iltimos, pastdagi tugma orqali xaritadan joylashuvingizni belgilang</p><button class=\"btn-confirm-map-location\" onclick=\"$0.$server.openFullMapFromFailedGps()\">🗺️ Xaritadan joyni tanlash</button>';" +
                        "}",
                this.getElement()
        );
    }

    @ClientCallable
    public void openFullMapFromFailedGps() {
        openFullScreenMapModal();
    }

    private record CategoryItem(String name, String emoji, WasteTypeEnum type) {}
    private record SeverityItem(String title, String desc, String dotEmoji, SeverityLevelEnum level) {}
}