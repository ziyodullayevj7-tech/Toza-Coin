package teamwork.view.user;

import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.function.SerializableBiConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import teamwork.dto.wasteReport.CleanReportedWasteDto;
import teamwork.exceptions.BaseException;
import teamwork.i18n.I18n;
import teamwork.service.AttachService;
import teamwork.service.WasteReportService;
import teamwork.util.SecurityUtils;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Connects the existing claim and cleanup-submission services to a single, resumable dialog. */
public class CommunityCleanupDialog extends Dialog {
    private static final Logger log = LoggerFactory.getLogger(CommunityCleanupDialog.class);
    private final WasteReportService reports;
    private final AttachService attachments;
    private final SerializableBiConsumer<CommunityCleanupTask, Boolean> changed;
    private CommunityCleanupTask task;
    private final Div body = new Div();
    private final Div error = new Div();
    private final Div camera = new Div();
    private final Div photo = new Div();
    private Button cameraButton;
    private Button captureButton;
    private Button submit;
    private boolean cameraBusy;
    private boolean submitted;

    public CommunityCleanupDialog(CommunityCleanupTask task, WasteReportService reports, AttachService attachments,
                                  SerializableBiConsumer<CommunityCleanupTask, Boolean> changed) {
        this.task = task;this.reports = reports;this.attachments = attachments;this.changed = changed;
        addClassNames("reward-dialog", "community-cleanup-dialog");
        setWidth("640px");
        error.addClassName("reward-form-error");error.getElement().setAttribute("role", "alert");error.setVisible(false);
        camera.addClassName("cleanup-camera");photo.addClassName("cleanup-photo-preview");
        addOpenedChangeListener(e -> { if (!e.isOpened()) stopCamera(); });
        addDetachListener(e -> stopCamera());
        body.addClassName("cleanup-dialog-content");
        add(body, error);
        render();
    }

    private void render() {
        stopCamera();body.removeAll();getFooter().removeAll();error.setVisible(false);
        boolean active = task.startedAt() > 0;
        I18n.headerTitle(this, active ? "Tozalash jarayoni" : "Tozalash joyi");
        Div original = new Div(VaadinIcon.RECYCLE.create());original.addClassName("cleanup-before-photo");
        if (task.beforeImageUrl() != null && !task.beforeImageUrl().isBlank()) {
            Image image = new Image(task.beforeImageUrl(), I18n.t("Tozalashdan oldin"));
            image.getElement().addEventListener("error", e -> image.setVisible(false));original.add(image);
        }
        body.add(original, I18n.localize(new H2(task.title())), new Paragraph(task.address()), directions(task));
        if (task.rewardCoins() > 0) body.add(I18n.localize(new Span("Tozalash mukofoti: " + task.rewardCoins() + " tanga")));
        body.add(I18n.localize(new Paragraph("Yakuniy surat yuborilgach, tozalash natijasi admin tomonidan tekshiriladi.")));
        Button close = I18n.localize(new Button(active ? "Keyinroq davom etish" : "Yopish", e -> close()));
        getFooter().add(close);
        if (!active) {
            Button start = I18n.localize(new Button("Tozalashni boshlash", VaadinIcon.PLAY.create()));
            start.setId("cleanup-start");start.addThemeVariants(ButtonVariant.LUMO_PRIMARY);start.setDisableOnClick(true);
            boolean ownReport = task.reporterId() != null && task.reporterId().equals(SecurityUtils.getCurrentUserId());
            start.setEnabled(!ownReport);
            if (ownReport) body.add(I18n.localize(new Paragraph("Bu siz yuborgan xabar. Uni boshqa foydalanuvchi tozalashi mumkin.")));
            start.addClickListener(e -> {
                if (task.startedAt() > 0) return;
                try {
                    if (!reports.claimReportForCleaning(task.reportId())) throw new IllegalStateException("Claim failed");
                    task = task.started();changed.accept(task, false);render();
                } catch (BaseException exception) { showError(exception.getMessage());start.setEnabled(false); }
                catch (RuntimeException exception) { log.error("Could not claim report {}", task.reportId(), exception);showError("Tozalashni boshlab bo'lmadi. Xaritani yangilab qayta urinib ko'ring."); }
            });
            getFooter().add(start);
        } else {
            String started = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.systemDefault())
                    .format(Instant.ofEpochMilli(task.startedAt()));
            body.add(I18n.localize(new Span("Boshlangan vaqt: " + started)));
            body.add(I18n.localize(new Paragraph("Tozalashni tugatgach, kamerada yakuniy surat oling va tekshiruvga yuboring.")));
            cameraButton = I18n.localize(new Button(task.afterImageUrl() == null ? "Tugatish va suratga olish" : "Qayta suratga olish", VaadinIcon.CAMERA.create(), e -> startCamera()));
            captureButton = I18n.localize(new Button("Suratga olish", e -> capture()));captureButton.setEnabled(false);
            cameraButton.setId("cleanup-open-camera");captureButton.setId("cleanup-capture");
            Div controls = new Div(cameraButton, captureButton);controls.addClassName("cleanup-camera-controls");
            camera.setVisible(false);renderPhoto();
            body.add(camera, photo, controls);
            submit = I18n.localize(new Button("Tekshiruvga yuborish", e -> submit()));
            submit.setId("cleanup-submit");submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);submit.setDisableOnClick(true);
            submit.setEnabled(task.afterImageUrl() != null && !task.afterImageUrl().isBlank());getFooter().add(submit);
        }
    }

    static Div directions(CommunityCleanupTask task) {
        String coordinates = task.latitude() + "," + task.longitude();
        Anchor web = new Anchor("https://www.google.com/maps/dir/?api=1&destination=" + coordinates, I18n.t("Brauzerda yo'nalish"));
        web.setTarget("_blank");web.getElement().setAttribute("rel", "noopener noreferrer");
        Anchor app = new Anchor();
        app.setText(I18n.t("Xarita ilovasida ochish"));
        // This URI is constructed solely from validated numeric coordinates, not user input.
        app.setUnsafeHref("geo:" + coordinates + "?q=" + coordinates);
        app.addAttachListener(e -> app.getElement().executeJs("return /iPad|iPhone|iPod/.test(navigator.userAgent)")
                .then(Boolean.class, ios -> { if (Boolean.TRUE.equals(ios)) app.setHref("https://maps.apple.com/?daddr=" + coordinates + "&dirflg=w"); }));
        Div links = new Div(app, web);links.addClassName("cleanup-directions");return links;
    }

    private void startCamera() {
        if (cameraBusy || submitted) return;
        cameraBusy = true;cameraButton.setEnabled(false);camera.setVisible(true);error.setVisible(false);
        camera.getElement().executeJs("""
            const host=this;
            if(host._cleanupStream)host._cleanupStream.getTracks().forEach(t=>t.stop());
            return (async()=>{
              try {
                if(!navigator.mediaDevices?.getUserMedia)throw new Error('Camera unavailable');
                const stream=await navigator.mediaDevices.getUserMedia({video:{facingMode:{ideal:'environment'}},audio:false});
                if(!host.isConnected){stream.getTracks().forEach(t=>t.stop());return false;}
                host._cleanupStream=stream;
                clearInterval(host._cleanupWatch);
                host._cleanupWatch=setInterval(()=>{
                  if(!host.isConnected||!host.getClientRects().length){stream.getTracks().forEach(t=>t.stop());clearInterval(host._cleanupWatch);}
                },250);
                const video=document.createElement('video');video.autoplay=true;video.playsInline=true;video.muted=true;
                video.srcObject=stream;host.replaceChildren(video);await video.play();return true;
              } catch(e){if(host._cleanupStream)host._cleanupStream.getTracks().forEach(t=>t.stop());return false;}
            })();
            """).then(Boolean.class, ready -> {
                cameraBusy = false;
                if (!isOpened()) { stopCamera();return; }
                captureButton.setEnabled(Boolean.TRUE.equals(ready));cameraButton.setEnabled(true);
                if (!Boolean.TRUE.equals(ready)) showError("Kamera ochilmadi. Ruxsatlarni tekshiring va HTTPS yoki localhost orqali foydalaning.");
            });
    }

    private void capture() {
        if (cameraBusy || submitted) return;
        cameraBusy=true;captureButton.setEnabled(false);cameraButton.setEnabled(false);submit.setEnabled(false);
        camera.getElement().executeJs("""
            const video=this.querySelector('video');
            if(!video||!video.videoWidth)return '';
            const scale=Math.min(1,1600/Math.max(video.videoWidth,video.videoHeight));
            const canvas=document.createElement('canvas');canvas.width=Math.round(video.videoWidth*scale);canvas.height=Math.round(video.videoHeight*scale);
            canvas.getContext('2d').drawImage(video,0,0,canvas.width,canvas.height);
            return canvas.toDataURL('image/jpeg',0.85);
            """).then(String.class, this::acceptPhoto);
    }

    void acceptPhoto(String data) {
        cameraBusy=false;cameraButton.setEnabled(true);
        if (!isOpened() || submitted) { stopCamera();return; }
        try {
            if (data == null || !data.startsWith("data:image/jpeg;base64,") || data.length() > 7_000_000) throw new IllegalArgumentException("Invalid photo");
            var attachment=attachments.uploadBase64CameraPhoto(data);
            task=task.withPhoto(attachment.getPath());changed.accept(task,false);stopCamera();renderPhoto();error.setVisible(false);
        } catch (RuntimeException exception) { log.warn("Could not save cleanup photo for {}",task.reportId(),exception);showError("Surat saqlanmadi. Qayta suratga oling.");captureButton.setEnabled(true); }
        submit.setEnabled(task.afterImageUrl()!=null && !task.afterImageUrl().isBlank());
    }

    private void renderPhoto() {
        photo.removeAll();photo.setVisible(task.afterImageUrl()!=null && !task.afterImageUrl().isBlank());
        if(photo.isVisible()) photo.add(new Image(task.afterImageUrl(), I18n.t("Tozalashdan keyin")));
    }

    private void submit() {
        if(submitted || cameraBusy || task.afterImageUrl()==null || task.afterImageUrl().isBlank()){submit.setEnabled(!cameraBusy && !submitted);return;}
        submitted=true;stopCamera();cameraButton.setEnabled(false);captureButton.setEnabled(false);
        var dto=new CleanReportedWasteDto();dto.setWasteReportId(task.reportId());dto.setCleanedImageUrl(task.afterImageUrl());
        dto.setCleanPeriod(Math.max(0.01,(System.currentTimeMillis()-task.startedAt())/60000.0));
        try {
            if(!reports.updateReportCleaner(dto))throw new IllegalStateException("Submission failed");
            changed.accept(task,true);close();
            I18n.notify("Tozalash hisoboti moderatsiyaga yuborildi.",4000,com.vaadin.flow.component.notification.Notification.Position.TOP_END);
        } catch (BaseException exception){showError(exception.getMessage());submitted=false;submit.setEnabled(true);cameraButton.setEnabled(true);}
        catch(RuntimeException exception){log.error("Could not submit cleanup {}",task.reportId(),exception);showError("Yuborib bo'lmadi. Xaritani yangilab holatni tekshiring.");}
    }

    private void showError(String message){I18n.text(error,message);error.setVisible(true);}
    private void stopCamera(){camera.getElement().executeJs("clearInterval(this._cleanupWatch);if(this._cleanupStream){this._cleanupStream.getTracks().forEach(t=>t.stop());this._cleanupStream=null;}const v=this.querySelector('video');if(v)v.srcObject=null;");camera.setVisible(false);}
}
