package teamwork.view.user;

import teamwork.i18n.I18n;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
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

import java.time.Duration;
import java.time.Instant;

@Route(value = "user/report", layout = UserLayout.class)
@RolesAllowed({"USER", "ADMIN"})
@CssImport("./themes/tozacoin/user-layout.css")
@CssImport("./themes/tozacoin/report-flow.css")
public class ReportWasteView extends VerticalLayout implements com.vaadin.flow.router.HasDynamicTitle {

    @Autowired
    private AttachService attachService;
    @Autowired
    private WasteReportService wasteReportService;

    // ------------------------------------------------------------------------
    // Draft DTO Objects (Ready for future Service & Repository calls)
    // ------------------------------------------------------------------------
    private final WasteReportDto wasterReportDto = new WasteReportDto();

    private int currentStep = 1;
    private final Div stepProgressBar = I18n.localize(new Div());
    private final Div cardContainer = I18n.localize(new Div());

    // Step 1 UI components
    private final Button btnCapture = I18n.localize(new Button("Kamera orqali rasm olish",
            event -> startOrCaptureCamera(false)));
    private final Button btnNextStep1 = I18n.localize(new Button("Davom etish →",
            event -> renderStepCard(2)));
    private final Div cameraBox = I18n.localize(new Div());
    private final Div afterPhotoBox = I18n.localize(new Div());
    private final Button btnAfterPhotoCapture = I18n.localize(new Button("Tozalangandan keyingi surat",
            event -> startOrCaptureCamera(true)));
    private final Button btnAfterPhotoNext = I18n.localize(new Button("Davom etish →",
            event -> renderStepCard(7)));
    private Instant cleaningStartedAt;
    private Long cleaningDurationMillis;
    private boolean openAfterPhotoCameraOnEntry;

    // Step 2 UI components
    private Span locationPillText;
    private TextField streetAddressInput;
    private Button btnNextStep2;

    // Step 4 Selected State & Next Button
    private final Button btnNextStep4 = I18n.localize(new Button("Davom etish →",
            event -> advanceFromActionChoice()));

    public ReportWasteView() {
        addClassName("eco-report-page");
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
        wasterReportDto.setWasteType(WasteTypeEnum.OTHER);
        wasterReportDto.setSeverityLevel(SeverityLevelEnum.UNDER_REVIEW);

        // Progress adapts to the selected report action.
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
        stepProgressBar.addClassName("jungle-report-progress");
        stepProgressBar.getStyle()
                .set("background-color", "#f8fafc")
                .set("padding", "24px 0")
                .set("border-bottom", "1px solid #e2e8f0");
        refreshStepProgressBar();
        return stepProgressBar;
    }

    private void refreshStepProgressBar() {
        stepProgressBar.removeAll();

        Div container = I18n.localize(new Div());
        container.addClassName("report-progress-inner");
        container.getStyle()
                .set("max-width", "720px")
                .set("margin", "0 auto")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "16px");

        Div headerTitleRow = I18n.localize(new Div());
        headerTitleRow.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "12px");

        // Back Arrow Button (←) to return to the previous step before submission.
        int completionStep = 8;
        if (currentStep > 1 && currentStep < completionStep) {
            Button btnBack = I18n.localize(new Button("←"));
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
            I18n.attribute(btnBack, "aria-label", "Avvalgi");
            btnBack.addClickListener(e -> renderStepCard(currentStep == 4 ? 2 : currentStep - 1));
            headerTitleRow.add(btnBack);
        }

        Div titleTextGroup = I18n.localize(new Div());
        H3 headerTitle = I18n.localize(new H3("Report"));
        headerTitle.getStyle().set("margin", "0").set("font-size", "20px").set("font-weight", "700");

        boolean cleanFlow = wasterReportDto.getReportAction() == ReportActionEnum.CLEAN_MYSELF;
        int totalSteps = cleanFlow ? 6 : 4;
        int progressStep = currentStep == completionStep ? totalSteps
                : currentStep >= 4 ? currentStep - 1 : currentStep;
        String subtitleText = currentStep == completionStep
                ? "Murojaat yuborildi"
                : "Qadam " + progressStep + " / " + totalSteps;
        Span headerSubtitle = I18n.localize(new Span(subtitleText));
        headerSubtitle.getStyle().set("font-size", "13px").set("color", "#64748b").set("margin-left", "8px");
        headerTitle.add(headerSubtitle);
        titleTextGroup.add(headerTitle);

        headerTitleRow.add(titleTextGroup);

        // Horizontal Step Columns Bar
        Div barsRow = I18n.localize(new Div());
        barsRow.getStyle()
                .set("display", "flex")
                .set("width", "100%")
                .set("gap", "12px");

        String[] stepNames = cleanFlow
                ? new String[]{"Oldin rasm", "Joylashuv", "Harakat", "Tozalash", "Keyin rasm", "Tasdiqlash"}
                : new String[]{"Rasm", "Joylashuv", "Harakat", "Tasdiqlash"};
        for (int i = 1; i <= totalSteps; i++) {
            Div stepCol = I18n.localize(new Div());
            stepCol.addClassName("report-progress-step");
            if (i == progressStep) stepCol.getElement().setAttribute("aria-current", "step");
            stepCol.getElement().setAttribute("data-state", i < progressStep ? "complete" : i == progressStep ? "current" : "upcoming");
            stepCol.getStyle()
                    .set("flex", "1")
                    .set("display", "flex")
                    .set("flex-direction", "column")
                    .set("align-items", "center")
                    .set("gap", "6px");

            Div barLine = I18n.localize(new Div());
            barLine.getStyle()
                    .set("width", "100%")
                    .set("height", "6px")
                    .set("border-radius", "3px")
                    .set("background-color", (i <= progressStep || currentStep == completionStep) ? "#15803d" : "#cbd5e1");

            Span label = I18n.localize(new Span(stepNames[i - 1]));
            label.getStyle()
                    .set("font-size", "12px")
                    .set("font-weight", (i == progressStep || currentStep == completionStep) ? "700" : "600")
                    .set("color", (i <= progressStep || currentStep == completionStep) ? "#15803d" : "#64748b");

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
            case 4 -> cardContainer.add(buildStep4ActionChoiceCard());
            case 5 -> cardContainer.add(wasterReportDto.getReportAction() == ReportActionEnum.CLEAN_MYSELF
                    ? buildCleaningTimerCard() : buildStep5ConfirmationCard());
            case 6 -> {
                cardContainer.add(buildAfterPhotoCard());
                if (openAfterPhotoCameraOnEntry) {
                    openAfterPhotoCameraOnEntry = false;
                    startOrCaptureCamera(true);
                }
            }
            case 7 -> cardContainer.add(buildStep5ConfirmationCard());
            case 8 -> cardContainer.add(buildStep6SuccessCard());
        }
    }

    private Div createBaseCard() {
        Div card = I18n.localize(new Div());
        card.addClassName("report-step-card");
        return card;
    }

    /* ------------------------------------------------------------------------
     * Step 1: Camera Card
     * ------------------------------------------------------------------------ */
    private Div buildStep1CameraCard() {
        Div card = createBaseCard();
        card.addClassName("report-flow-card");

        H2 title = I18n.localize(new H2("Rasm oling"));
        Paragraph subtitle = I18n.localize(new Paragraph("Ifloslangan joyning rasmini telefon kamerasidan oling."));

        cameraBox.addClassName("camera-preview-dashed-box");
        cameraBox.setId("camera-stream-box");

        if (wasterReportDto.getBeforeImageUrl() != null) {
            Image imgPreview = I18n.localize(new Image(wasterReportDto.getBeforeImageUrl(), "Tozalashdan oldingi surat"));
            imgPreview.getStyle().set("width", "100%").set("height", "100%").set("object-fit", "cover");
            cameraBox.removeAll();
            cameraBox.add(imgPreview);
            I18n.text(btnCapture, "Qayta rasmga olish");
        } else {
            Div placeholder = I18n.localize(new Div());
            placeholder.addClassName("camera-placeholder-content");
            placeholder.add(VaadinIcon.CAMERA.create(), I18n.localize(new H3("Hali rasm olinmadi")), I18n.localize(new Paragraph("Pastdagi tugmani bosib kamera oching")));
            cameraBox.removeAll();
            cameraBox.add(placeholder);
        }

        btnCapture.addClassName("btn-camera-capture");
        btnCapture.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Paragraph note = I18n.localize(new Paragraph("Suiiste'molning oldini olish uchun surat faqat kameradan olinadi."));
        note.addClassName("camera-note-text");

        btnNextStep1.addClassName("btn-step-next-green");
        btnNextStep1.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnNextStep1.setEnabled(wasterReportDto.getBeforeImageUrl() != null);

        btnCapture.setIcon(VaadinIcon.CAMERA.create());
        card.add(stepEmblem(VaadinIcon.CAMERA), title, subtitle, cameraBox, btnCapture, note, btnNextStep1);
        return card;
    }

    /* ------------------------------------------------------------------------
     * Step 2: Yandex-Style Satellite Location Card
     * ------------------------------------------------------------------------ */
    private Div buildStep2LocationCard() {
        Div card = createBaseCard();
        card.addClassName("report-flow-card");

        H2 title = I18n.localize(new H2("Joylashuvni tasdiqlang"));
        Paragraph subtitle = I18n.localize(new Paragraph("GPS orqali aniqlangan joylashuv. To'g'rilab qo'ying."));

        Div mapContainer = I18n.localize(new Div());
        mapContainer.addClassName("yandex-map-container");

        Div mapDiv = I18n.localize(new Div());
        mapDiv.setId("yandex-satellite-map");
        mapDiv.addClassName("yandex-map-div");

        Div centerPin = I18n.localize(new Div(VaadinIcon.MAP_MARKER.create()));
        centerPin.addClassName("yandex-center-pin");

        Button btnRecenterGps = I18n.localize(new Button("🎯"));
        btnRecenterGps.setText("");
        btnRecenterGps.setIcon(VaadinIcon.CROSSHAIRS.create());
        I18n.attribute(btnRecenterGps, "aria-label", "Joylashuv");
        btnRecenterGps.addClassName("btn-recenter-gps");
        btnRecenterGps.addClickListener(e -> recenterMapToUserGps());

        mapContainer.add(mapDiv, centerPin, btnRecenterGps);

        Div locationPill = I18n.localize(new Div());
        locationPill.addClassName("location-result-pill");

        var pillIcon = VaadinIcon.MAP_MARKER.create();
        pillIcon.getElement().setAttribute("aria-hidden", "true");
        String dist = wasterReportDto.getLocation().getDistrict();
        String region = wasterReportDto.getLocation().getRegion();
        boolean hasLocation = dist != null && !dist.isBlank() && region != null && !region.isBlank();
        this.locationPillText = I18n.localize(new Span(hasLocation ? dist + ", " + region : "⏳ Joylashuv aniqlanmoqda..."));
        locationPill.add(pillIcon, locationPillText);

        // Precise Location Selector Button (Full Screen Map Modal)
        Button btnOpenFullMap = I18n.localize(new Button("Xaritadan aniqroq joyni tanlash"));
        btnOpenFullMap.setIcon(VaadinIcon.MAP_MARKER.create());
        btnOpenFullMap.addClassName("report-map-picker");
        btnOpenFullMap.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        btnOpenFullMap.getStyle()
                .set("width", "100%")
                .set("height", "46px")
                .set("border-radius", "14px")
                .set("font-weight", "700")
                .set("margin-top", "4px")
                .set("cursor", "pointer");
        btnOpenFullMap.addClickListener(e -> openFullScreenMapModal());

        Div addressGroup = I18n.localize(new Div());
        addressGroup.addClassName("address-input-group");

        this.streetAddressInput = I18n.localize(new TextField());
        I18n.placeholder(this.streetAddressInput, "Ko'cha, uy raqami...");
        this.streetAddressInput.addClassName("street-address-input");
        if (wasterReportDto.getLocation().getStreetAddress() != null) {
            this.streetAddressInput.setValue(wasterReportDto.getLocation().getStreetAddress());
        }
        this.streetAddressInput.addValueChangeListener(e -> wasterReportDto.getLocation().setStreetAddress(e.getValue()));

        I18n.label(streetAddressInput, "Aniq manzil (ixtiyoriy)");
        addressGroup.add(streetAddressInput);

        this.btnNextStep2 = I18n.localize(new Button("Davom etish →"));
        this.btnNextStep2.addClassName("btn-step-next-green");
        this.btnNextStep2.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        this.btnNextStep2.setEnabled(hasLocation);
        this.btnNextStep2.addClickListener(e -> {
            wasterReportDto.getLocation().setStreetAddress(streetAddressInput.getValue());
            renderStepCard(4);
        });

        locationPill.getElement().setAttribute("role", "status");
        card.add(stepEmblem(VaadinIcon.MAP_MARKER), title, subtitle, mapContainer, locationPill, btnOpenFullMap, addressGroup, btnNextStep2);
        initYandexSatelliteMap();
        return card;
    }

    /* ------------------------------------------------------------------------
     * Step 4: Action Choice Card (Yo'lni tanlang)
     * ------------------------------------------------------------------------ */
    private Div stepEmblem(VaadinIcon icon) {
        Div emblem = new Div(icon.create());
        emblem.addClassName("report-step-emblem");
        emblem.getElement().setAttribute("aria-hidden", "true");
        return emblem;
    }

    private Div buildStep4ActionChoiceCard() {
        Div card = createBaseCard();
        card.addClassNames("report-flow-card", "report-action-step");
        H2 title = I18n.localize(new H2("Yo'lni tanlang"));
        Paragraph subtitle = I18n.localize(new Paragraph(
                "Tozalashni o'zingiz bajaring yoki muammo haqida xabar bering"));

        RadioButtonGroup<ReportActionEnum> actions = new RadioButtonGroup<>();
        actions.addClassName("report-action-options");
        I18n.attribute(actions, "aria-label", "Yo'lni tanlang");
        actions.setItems(ReportActionEnum.CLEAN_MYSELF, ReportActionEnum.SEND_TO_OTHERS);
        actions.setRenderer(new ComponentRenderer<>(this::buildActionOptionCard));
        actions.setValue(wasterReportDto.getReportAction());
        actions.addValueChangeListener(event -> {
            ReportActionEnum action = event.getValue();
            wasterReportDto.setReportAction(action);
            wasterReportDto.setRewardCoins(action == ReportActionEnum.CLEAN_MYSELF ? 50 : 15);
            I18n.text(btnNextStep4, action == ReportActionEnum.SEND_TO_OTHERS
                    ? "Xabarni yuborish \u2192" : "Davom etish \u2192");
            btnNextStep4.setEnabled(action != null);
            refreshStepProgressBar();
        });

        Div notice = new Div(VaadinIcon.INFO_CIRCLE.create(), I18n.localize(new Paragraph(
                "Surat yuborilgach, AI tekshiruvi fonda bajariladi. Kutishingiz shart emas.")));
        notice.addClassName("report-flow-notice");
        btnNextStep4.setDisableOnClick(true);
        btnNextStep4.addClassName("btn-step-next-green");
        btnNextStep4.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        I18n.text(btnNextStep4, wasterReportDto.getReportAction() == ReportActionEnum.SEND_TO_OTHERS
                ? "Xabarni yuborish \u2192" : "Davom etish \u2192");
        btnNextStep4.setEnabled(wasterReportDto.getReportAction() != null);
        card.add(stepEmblem(VaadinIcon.RECYCLE), title, subtitle, actions, notice, btnNextStep4);
        return card;
    }

    private Div buildActionOptionCard(ReportActionEnum action) {
        boolean cleaning = action == ReportActionEnum.CLEAN_MYSELF;
        Div option = new Div();
        option.addClassName("report-action-option");
        Div icon = stepEmblem(cleaning ? VaadinIcon.RECYCLE : VaadinIcon.MAP_MARKER);
        Div copy = new Div();
        copy.addClassName("report-action-copy");
        Span title = I18n.localize(new Span(cleaning ? "O'zim tozalayman" : "Xabar berish"));
        title.addClassName("report-action-title");
        Paragraph description = I18n.localize(new Paragraph(cleaning
                ? "Joyni tozalang. AI tekshiruvdan o'tsa 50 tanga beriladi."
                : "Faqat suratni yuboring. AI tasdiqlasa 15 tanga beriladi."));
        copy.add(title, description);
        Span reward = I18n.localize(new Span((cleaning ? 50 : 15) + " tanga"));
        reward.addClassName("report-action-reward");
        option.add(icon, copy, reward);
        return option;
    }

    private void advanceFromActionChoice() {
        if (wasterReportDto.getReportAction() == ReportActionEnum.CLEAN_MYSELF) {
            if (cleaningStartedAt == null) {
                cleaningStartedAt = Instant.now();
            }
            renderStepCard(5);
        } else if (wasterReportDto.getReportAction() == ReportActionEnum.SEND_TO_OTHERS) {
            onFinalSubmitConfirmed(btnNextStep4);
        }
    }

    private Div buildCleaningTimerCard() {
        Div card = createBaseCard();
        card.addClassNames("report-flow-card", "cleaning-timer-card");

        Div icon = stepEmblem(VaadinIcon.CLOCK);
        H2 title = I18n.localize(new H2("Tozalash vaqti"));
        Paragraph subtitle = I18n.localize(new Paragraph(
                "Ishni boshlang. Tayyor bo'lgach, keyingi tugmani bosib tozalangan joyni suratga oling."));
        subtitle.addClassName("cleaning-timer-description");

        Span timer = I18n.localize(new Span("00:00"));
        timer.setId("cleaning-elapsed-timer");
        timer.getElement().setAttribute("role", "timer");
        I18n.attribute(timer, "aria-label", "Tozalash vaqti");
        timer.addClassName("cleaning-elapsed-timer");
        long startedAtMillis = cleaningStartedAt == null ? System.currentTimeMillis() : cleaningStartedAt.toEpochMilli();
        long completedDuration = cleaningDurationMillis == null ? -1 : cleaningDurationMillis;
        UI.getCurrent().getPage().executeJs(
                "if (window._tozaCleaningTimer) clearInterval(window._tozaCleaningTimer);" +
                        "const timer = document.getElementById('cleaning-elapsed-timer');" +
                        "if (timer) {" +
                        "  const startedAt = $0;" +
                        "  const stoppedAt = $1;" +
                        "  const render = () => {" +
                        "    const total = Math.floor((stoppedAt >= 0 ? stoppedAt : Date.now() - startedAt) / 1000);" +
                        "    const hours = Math.floor(total / 3600);" +
                        "    const minutes = Math.floor((total % 3600) / 60);" +
                        "    const seconds = total % 60;" +
                        "    timer.textContent = (hours ? String(hours).padStart(2, '0') + ':' : '') +" +
                        "      String(minutes).padStart(2, '0') + ':' + String(seconds).padStart(2, '0');" +
                        "  };" +
                        "  render();" +
                        "  if (stoppedAt < 0) window._tozaCleaningTimer = setInterval(render, 1000);" +
                        "}",
                startedAtMillis, completedDuration
        );

        Span transparencyNote = I18n.localize(new Span("Tozalash vaqti saqlanadi va AI tekshiruviga yuboriladi."));
        transparencyNote.addClassName("cleaning-timer-note");

        Button finishCleaning = I18n.localize(new Button("Tozalash tugadi — suratga olish →"));
        finishCleaning.addClassName("btn-step-next-green");
        finishCleaning.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        finishCleaning.addClickListener(event -> {
            if (cleaningDurationMillis == null && cleaningStartedAt != null) {
                cleaningDurationMillis = Math.max(0, Duration.between(cleaningStartedAt, Instant.now()).toMillis());
                wasterReportDto.setCleanPeriod(cleaningDurationMillis / 1000.0);
            }
            UI.getCurrent().getPage().executeJs(
                    "if (window._tozaCleaningTimer) clearInterval(window._tozaCleaningTimer);" +
                            "window._tozaCleaningTimer = null;");
            openAfterPhotoCameraOnEntry = true;
            renderStepCard(6);
        });

        card.add(icon, title, subtitle, timer, transparencyNote, finishCleaning);
        return card;
    }

    private Div buildAfterPhotoCard() {
        Div card = createBaseCard();
        card.addClassName("report-flow-card");
        H2 title = I18n.localize(new H2("Tozalashdan keyingi surat"));
        Paragraph subtitle = I18n.localize(new Paragraph(
                "Xuddi shu joyni kameradan suratga oling. AI ikkala surat va sarflangan vaqtni tekshiradi."));
        subtitle.addClassName("camera-note-text");

        afterPhotoBox.setId("camera-after-stream-box");
        afterPhotoBox.addClassName("camera-preview-dashed-box");
        afterPhotoBox.removeAll();
        if (wasterReportDto.getAfterImageUrl() != null) {
            Image preview = I18n.localize(new Image(wasterReportDto.getAfterImageUrl(), "Tozalashdan keyingi surat"));
            preview.getStyle().set("width", "100%").set("height", "100%").set("object-fit", "cover");
            afterPhotoBox.add(preview);
        } else {
            Div placeholder = I18n.localize(new Div());
            placeholder.addClassName("camera-placeholder-content");
            placeholder.add(VaadinIcon.CAMERA.create(), I18n.localize(new H3("Tozalash tugagach suratga oling")),
                    I18n.localize(new Paragraph("Surat admin tasdig'i uchun hisobotga biriktiriladi.")));
            afterPhotoBox.add(placeholder);
        }

        btnAfterPhotoCapture.addClassName("btn-camera-capture");
        btnAfterPhotoCapture.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnAfterPhotoNext.addClassName("btn-step-next-green");
        btnAfterPhotoNext.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnAfterPhotoNext.setEnabled(wasterReportDto.getAfterImageUrl() != null);

        btnAfterPhotoCapture.setIcon(VaadinIcon.CAMERA.create());
        card.add(stepEmblem(VaadinIcon.CAMERA), title, subtitle, afterPhotoBox, btnAfterPhotoCapture, btnAfterPhotoNext);
        return card;
    }

    private static String formatDuration(Double secondsValue) {
        long totalSeconds = secondsValue == null ? 0 : Math.max(0, (long) Math.floor(secondsValue));
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return hours > 0
                ? I18n.format("%d soat %02d daqiqa %02d soniya", hours, minutes, seconds)
                : I18n.format("%d daqiqa %02d soniya", minutes, seconds);
    }

    /* ------------------------------------------------------------------------
     * Step 5: Confirmation & Summary Recap Card (Tasdiqlash)
     * ------------------------------------------------------------------------ */
    private Div buildStep5ConfirmationCard() {
        Div card = createBaseCard();
        card.addClassName("report-flow-card");

        boolean cleaning = wasterReportDto.getReportAction() == ReportActionEnum.CLEAN_MYSELF;
        H2 title = I18n.localize(new H2(cleaning ? "Tozalash hisoboti" : "Xulosa"));

        Div photoBanner = I18n.localize(new Div());
        photoBanner.addClassName("report-photo-comparison");
        photoBanner.add(buildReportPhoto("Tozalashdan oldin", wasterReportDto.getBeforeImageUrl()));
        if (cleaning) {
            photoBanner.add(buildReportPhoto("Tozalashdan keyin", wasterReportDto.getAfterImageUrl()));
        }

        // Summary Recap Rows
        Div summaryList = I18n.localize(new Div());
        summaryList.addClassName("report-summary-list");

        summaryList.add(
                createRecapRow("Toifa", formatCategoryName(wasterReportDto.getWasteType())),
                createRecapRow("Og'irlik", formatSeverityName(wasterReportDto.getSeverityLevel())),
                createRecapRow("Joylashuv", wasterReportDto.getLocation().getDistrict() + ", " + wasterReportDto.getLocation().getRegion()),
                createRecapRow("Harakat", wasterReportDto.getReportAction() == ReportActionEnum.CLEAN_MYSELF
                        ? "O'zim tozalayman" : "Xabar berish")
        );
        if (cleaning) {
            summaryList.add(createRecapRow("Tozalash vaqti", formatDuration(wasterReportDto.getCleanPeriod())));
        }

        Div rewardBox = new Div();
        rewardBox.addClassName("report-summary-reward");
        Span rewardLabel = I18n.localize(new Span("AI tasdiqlasa beriladi"));
        int coins = wasterReportDto.getRewardCoins() != null ? wasterReportDto.getRewardCoins() : 0;
        Span rewardAmount = I18n.localize(new Span(coins + " tanga"));
        rewardBox.add(rewardLabel, rewardAmount);

        // Final Submit Button
        Button btnSubmitFinal = I18n.localize(new Button("Yuborish ✓"));
        btnSubmitFinal.addClassName("btn-step-next-green");
        btnSubmitFinal.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnSubmitFinal.setDisableOnClick(true);
        btnSubmitFinal.addClickListener(e -> onFinalSubmitConfirmed(btnSubmitFinal));

        card.add(stepEmblem(VaadinIcon.CHECK_CIRCLE), title, photoBanner, summaryList, rewardBox, btnSubmitFinal);
        return card;
    }

    private Div buildReportPhoto(String label, String imageUrl) {
        Div photo = I18n.localize(new Div());
        photo.addClassName("report-photo-preview");
        if (imageUrl != null && !imageUrl.isBlank()) {
            Image image = I18n.localize(new Image(imageUrl, label));
            image.getStyle().set("width", "100%").set("height", "100%").set("object-fit", "cover");
            photo.add(image);
        } else {
            photo.add(I18n.localize(new Span("Surat mavjud emas")));
        }
        Span caption = I18n.localize(new Span(label));
        caption.addClassName("report-photo-caption");
        photo.add(caption);
        return photo;
    }

    private Div createRecapRow(String label, String value) {
        Div row = I18n.localize(new Div());
        row.addClassName("report-summary-row");
        Span lbl = I18n.localize(new Span(label));
        Span val = I18n.localize(new Span(value));
        row.add(lbl, val);
        return row;
    }

    private void onFinalSubmitConfirmed(Button submitButton) {
        try {
            if (!wasteReportService.create(wasterReportDto)) {
                I18n.notify("Something went wrong", 4000, Notification.Position.MIDDLE);
                submitButton.setEnabled(true);
                return;
            }
            renderStepCard(8);
        } catch (RuntimeException exception) {
            org.slf4j.LoggerFactory.getLogger(ReportWasteView.class)
                    .error("Could not submit waste report", exception);
            I18n.notify("Something went wrong", 4000, Notification.Position.MIDDLE);
            submitButton.setEnabled(true);
        }
    }

    private Div buildStep6SuccessCard() {
        Div card = createBaseCard();
        card.addClassName("report-received-card");
        boolean cleaning = wasterReportDto.getReportAction() == ReportActionEnum.CLEAN_MYSELF;

        Div emblem = new Div(new Span("✓"));
        emblem.addClassName("report-received-emblem");
        emblem.getElement().setAttribute("aria-hidden", "true");
        Span eyebrow = I18n.localize(new Span("Murojaat yuborildi"));
        eyebrow.addClassName("report-received-eyebrow");
        H2 title = I18n.localize(new H2(cleaning
                ? "Tozalash hisoboti yuborildi!" : "Xabaringiz qabul qilindi!"));
        Paragraph subtitle = I18n.localize(new Paragraph(cleaning
                ? "Suratlar va tozalash vaqti saqlandi. AI tekshiruvi fonda bajariladi — kutishingiz shart emas."
                : "Surat va joylashuv saqlandi. AI tekshiruvi fonda bajariladi — kutishingiz shart emas."));
        subtitle.addClassName("report-received-description");

        Div rewardCard = new Div();
        rewardCard.addClassName("report-pending-reward");
        Span coin = new Span("T");
        coin.addClassName("report-coin-mark");
        coin.getElement().setAttribute("aria-hidden", "true");
        Div rewardInfo = new Div();
        rewardInfo.addClassName("report-reward-info");
        Span rewardLabel = I18n.localize(new Span("Kutilayotgan mukofot"));
        int coins = wasterReportDto.getRewardCoins() != null ? wasterReportDto.getRewardCoins() : 0;
        H3 amount = I18n.localize(new H3(coins + " tanga"));
        Span rewardNote = I18n.localize(new Span("AI tasdiqlasa beriladi"));
        rewardInfo.add(rewardLabel, amount, rewardNote);
        rewardCard.add(coin, rewardInfo);

        Span status = I18n.localize(new Span("AI tekshiruvi kutilmoqda"));
        status.addClassName("report-review-status");
        Paragraph explanation = I18n.localize(new Paragraph(
                "AI suratni tekshirib, chiqindi turi va og'irlik darajasini aniqlaydi. Natijani mening xabarlarim bo'limida kuzating."));
        explanation.addClassName("report-review-explanation");

        Button myReports = I18n.localize(new Button("Mening xabarlarim",
                e -> UI.getCurrent().navigate("user/my-reports")));
        myReports.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        myReports.addClassName("report-received-primary");
        Button newReport = I18n.localize(new Button("+ Yangi xabar berish", e -> {
            resetFormState();
            renderStepCard(1);
        }));
        newReport.addClassName("report-received-secondary");
        Div actions = new Div(myReports, newReport);
        actions.addClassName("report-received-actions");
        card.add(emblem, eyebrow, title, subtitle, rewardCard, status, explanation, actions);
        return card;
    }

    private void resetFormState() {
        wasterReportDto.setBeforeImageUrl(null);
        wasterReportDto.setAfterImageUrl(null);
        wasterReportDto.setCleanPeriod(null);
        wasterReportDto.setWasteType(WasteTypeEnum.OTHER);
        wasterReportDto.setSeverityLevel(SeverityLevelEnum.UNDER_REVIEW);
        wasterReportDto.setDescription(null);
        wasterReportDto.setReportAction(null);
        wasterReportDto.setRewardCoins(null);
        wasterReportDto.setReportStatus(ReportStatus.PENDING);

        this.cleaningStartedAt = null;
        this.cleaningDurationMillis = null;
        this.openAfterPhotoCameraOnEntry = false;

        LocationDto locationDto = new LocationDto();
        locationDto.setLatitude(0.0);
        locationDto.setLongitude(0.0);
        locationDto.setRegion(null);
        locationDto.setDistrict(null);
        wasterReportDto.setLocation(locationDto);

        I18n.text(btnCapture, "Kamera orqali rasm olish");
        btnNextStep1.setEnabled(false);
        I18n.text(btnAfterPhotoCapture, "Tozalangandan keyingi surat");
        btnAfterPhotoNext.setEnabled(false);
        UI.getCurrent().getPage().executeJs(
                "if (window._tozaCleaningTimer) clearInterval(window._tozaCleaningTimer);" +
                        "window._tozaCleaningTimer = null;");
    }

    private String formatCategoryName(WasteTypeEnum cat) {
        if (cat == null) return "Boshqa";
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
        if (sev == null) return "Tekshiruv ostida";
        return switch (sev) {
            case LOW -> "Past";
            case MEDIUM -> "O'rta";
            case HIGH -> "Yuqori";
            case UNDER_REVIEW -> "Tekshiruv ostida";
        };
    }

    // Camera JS methods
    private void startOrCaptureCamera(boolean afterPhoto) {
        UI.getCurrent().getPage().executeJs(
                """
                        const previousOverlay = document.querySelector('#camera-fullscreen-overlay');
                        if (previousOverlay) previousOverlay.remove();

                        const overlay = document.createElement('div');
                        overlay.id = 'camera-fullscreen-overlay';
                        overlay.className = 'fullscreen-camera-overlay report-camera-overlay';
                        const video = document.createElement('video');
                        video.id = 'live-camera-video';
                        video.autoplay = true;
                        video.playsInline = true;
                        video.muted = true;
                        video.className = 'fullscreen-video-stream';
                        const topControls = document.createElement('div');
                        topControls.className = 'camera-top-controls';
                        const closeBtn = document.createElement('button');
                        closeBtn.className = 'btn-close-camera';
                        closeBtn.type = 'button';
                        closeBtn.textContent = '❌ Yopish';
                        closeBtn.onclick = function() {
                            if (video.srcObject) video.srcObject.getTracks().forEach(track => track.stop());
                            overlay.remove();
                        };
                        topControls.appendChild(closeBtn);

                        const status = document.createElement('div');
                        status.className = 'camera-status-message';
                        status.setAttribute('role', 'status');
                        window.tozaSetText(status, 'Kamera ishga tushmoqda...');
                        const bottomControls = document.createElement('div');
                        bottomControls.className = 'camera-bottom-controls';
                        const shutterBtn = document.createElement('button');
                        shutterBtn.className = 'btn-shutter-capture';
                        shutterBtn.type = 'button';
                        shutterBtn.textContent = '📸 Suratga olish';
                        shutterBtn.disabled = true;
                        const element = $0;
                        const isAfterPhoto = $1;

                        video.addEventListener('loadeddata', function() {
                            if (video.videoWidth > 0 && video.videoHeight > 0) {
                                shutterBtn.disabled = false;
                                window.tozaSetText(status, 'Suratga olishga tayyor');
                            }
                        }, { once: true });

                        shutterBtn.onclick = function() {
                            if (video.readyState < HTMLMediaElement.HAVE_CURRENT_DATA
                                    || !video.videoWidth || !video.videoHeight) {
                                shutterBtn.disabled = true;
                                window.tozaSetText(status, 'Kamera tasviri hali tayyor emas. Biroz kuting.');
                                return;
                            }

                            shutterBtn.disabled = true;
                            window.tozaSetText(status, 'Surat saqlanmoqda...');
                            const canvas = document.createElement('canvas');
                            canvas.width = video.videoWidth;
                            canvas.height = video.videoHeight;
                            const context = canvas.getContext('2d');
                            if (!context) {
                                window.tozaSetText(status, 'Suratni tayyorlab bo‘lmadi. Qayta urinib ko‘ring.');
                                shutterBtn.disabled = false;
                                return;
                            }
                            if (video.dataset.mirror === 'true') {
                                context.translate(canvas.width, 0);
                                context.scale(-1, 1);
                            }
                            context.drawImage(video, 0, 0, canvas.width, canvas.height);
                            const base64Data = canvas.toDataURL('image/jpeg', 0.85);
                            if (video.srcObject) video.srcObject.getTracks().forEach(track => track.stop());
                            overlay.remove();

                            const box = document.querySelector(
                                isAfterPhoto ? '#camera-after-stream-box' : '#camera-stream-box');
                            if (box) {
                                box.innerHTML = '<img src="' + base64Data
                                    + '" style="width:100%; height:100%; object-fit:contain; background-color:#0f172a; border-radius:14px;" />';
                            }
                            element.$server.saveCameraSnapshot(base64Data, isAfterPhoto);
                        };

                        bottomControls.appendChild(status);
                        bottomControls.appendChild(shutterBtn);
                        overlay.appendChild(video);
                        overlay.appendChild(topControls);
                        overlay.appendChild(bottomControls);
                        document.body.appendChild(overlay);

                        const showCameraError = function(error) {
                            if (video.srcObject) video.srcObject.getTracks().forEach(track => track.stop());
                            shutterBtn.disabled = true;
                            if (error.name === 'NotAllowedError' || error.name === 'PermissionDeniedError') {
                                window.tozaSetText(status, 'Kameraga ruxsat berilmadi. Brauzer sozlamalarida ruxsat bering.');
                            } else if (error.name === 'NotFoundError' || error.name === 'DevicesNotFoundError') {
                                window.tozaSetText(status, 'Qurilmada kamera topilmadi.');
                            } else if (error.name === 'NotReadableError' || error.name === 'TrackStartError') {
                                window.tozaSetText(status, 'Kamera boshqa ilovada band. Uni yoping va qayta urinib ko‘ring.');
                            } else if (error.name === 'SecurityError' || !navigator.mediaDevices) {
                                window.tozaSetText(status, 'Kamera uchun xavfsiz HTTPS ulanishidan yoki localhost manzilidan foydalaning.');
                            } else {
                                status.textContent = window.tozaTranslate('Kamera ochilmadi: ') + (error.message || error.name || 'noma’lum xatolik');
                            }
                        };

                        if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
                            showCameraError({ name: 'SecurityError' });
                        } else {
                            navigator.mediaDevices.getUserMedia({
                                video: {
                                    facingMode: { ideal: 'environment' },
                                    width: { ideal: 1920 },
                                    height: { ideal: 1080 }
                                },
                                audio: false
                            }).then(function(stream) {
                                if (!overlay.isConnected) {
                                    stream.getTracks().forEach(track => track.stop());
                                    return;
                                }
                                video.srcObject = stream;
                                const videoTrack = stream.getVideoTracks()[0];
                                const isFrontCamera = videoTrack && videoTrack.getSettings
                                        && videoTrack.getSettings().facingMode === 'user';
                                video.dataset.mirror = String(isFrontCamera);
                                if (isFrontCamera) video.style.transform = 'scaleX(-1)';
                                return video.play();
                            }).catch(showCameraError);
                        }
                        """,
                this.getElement(), afterPhoto
        );
    }

    @ClientCallable
    public void saveCameraSnapshot(String base64Image, boolean afterPhoto) {
        try {
            AttachDto attachDTO = attachService.uploadBase64CameraPhoto(base64Image);
            if (afterPhoto) {
                wasterReportDto.setAfterImageUrl(attachDTO.getPath());
                I18n.text(btnAfterPhotoCapture, "Keyingi suratni qayta olish");
                btnAfterPhotoNext.setEnabled(true);
                afterPhotoBox.removeAll();
                Image preview = I18n.localize(new Image(attachDTO.getPath(), "Tozalashdan keyingi surat"));
                preview.getStyle().set("width", "100%").set("height", "100%").set("object-fit", "cover");
                afterPhotoBox.add(preview);
            } else {
                wasterReportDto.setBeforeImageUrl(attachDTO.getPath());
                I18n.text(btnCapture, "Qayta rasmga olish");
                btnNextStep1.setEnabled(true);
            }

            I18n.notify("Surat muvaffaqiyatli olindi! ✅", 2500, Notification.Position.BOTTOM_END);
        } catch (Exception e) {
            I18n.notify("Xatolik: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }

    private void openFullScreenMapModal() {
        UI.getCurrent().getPage().executeJs(
                "let overlay = document.querySelector('#fullscreen-map-overlay');" +
                        "if (overlay) overlay.remove();" +
                        "overlay = document.createElement('div');" +
                        "overlay.id = 'fullscreen-map-overlay';" +
                        "overlay.className = 'fullscreen-map-overlay report-map-overlay';" +
                        "const mapDiv = document.createElement('div');" +
                        "mapDiv.id = 'full-satellite-map';" +
                        "mapDiv.className = 'fullscreen-map-div';" +
                        "const topBar = document.createElement('div');" +
                        "topBar.className = 'map-top-bar';" +
                        "topBar.id = 'full-map-address-pill';" +
                        "window.tozaSetText(topBar, '📍 Joylashuv aniqlanmoqda...');" +
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
                        "window.tozaSetText(confirmBtn, '✓ Joylashuvni tasdiqlash');" +
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
                        "       }, err => { alert(window.tozaTranslate('GPS joylashuvni aniqlab bo\\'lmadi')); }, { enableHighAccuracy: true, timeout: 10000 });" +
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
                        "   }, err => { alert(window.tozaTranslate('GPS joylashuvni aniqlab bo\\'lmadi')); }, { enableHighAccuracy: true, timeout: 10000 });" +
                        "}",
                this.getElement()
        );
    }

    @ClientCallable
    public void setGeocodingLoadingState() {
        if (this.locationPillText != null) {
            I18n.text(this.locationPillText, "⏳ Joylashuv aniqlanmoqda...");
        }
    }

    @ClientCallable
    public void updateLocationFromCenterWithAddress(double lat, double lng, String district, String region) {
        this.wasterReportDto.getLocation().setLatitude(lat);
        this.wasterReportDto.getLocation().setLongitude(lng);
        this.wasterReportDto.getLocation().setDistrict(district);
        this.wasterReportDto.getLocation().setRegion(region);

        if (this.locationPillText != null) {
            I18n.text(this.locationPillText, district + ", " + region);
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
        UI.getCurrent().getPage().executeJs("""
                const box = document.querySelector('#gps-loading-box');
                if (box) {
                    box.replaceChildren();
                    const icon = document.createElement('div');
                    icon.textContent = '⚠️';
                    icon.style.fontSize = '36px';
                    const title = document.createElement('h3');
                    title.style.cssText = 'margin:0;color:#c2410c;font-size:16px';
                    window.tozaSetText(title, "GPS joylashuvni avtomatik aniqlab bo'lmadi");
                    const description = document.createElement('p');
                    description.style.cssText = 'margin:4px 0 16px;color:#9a3412;font-size:13px';
                    window.tozaSetText(description, 'Iltimos, pastdagi tugma orqali xaritadan joylashuvingizni belgilang');
                    const button = document.createElement('button');
                    button.className = 'btn-confirm-map-location';
                    window.tozaSetText(button, '🗺️ Xaritadan joyni tanlash');
                    button.onclick = () => $0.$server.openFullMapFromFailedGps();
                    box.append(icon, title, description, button);
                }
                """, getElement());
    }

    @ClientCallable
    public void openFullMapFromFailedGps() {
        openFullScreenMapModal();
    }

    @Override
    public String getPageTitle() {
        return I18n.t("Report | TozaCoin");
    }
}
