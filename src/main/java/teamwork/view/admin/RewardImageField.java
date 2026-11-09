package teamwork.view.admin;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.UploadHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import teamwork.dto.attach.AttachDto;
import teamwork.exceptions.AppBadRequestException;
import teamwork.i18n.I18n;
import teamwork.service.AttachService;
import java.io.IOException;
import java.util.Base64;

/** Stages one image; only Save writes it through the attachment service. */
public class RewardImageField extends Div {
    private static final Logger log = LoggerFactory.getLogger(RewardImageField.class);
    private final AttachService attachments;
    private final Upload upload;
    private final Image preview = new Image();
    private final Span name = new Span();
    private final Div error = new Div();
    private final Button remove;
    private String path;
    private byte[] pending;
    private String filename;
    private AttachDto temporaryAttachment;
    private boolean uploading;
    private boolean disposed;
    private boolean saved;

    public RewardImageField(AttachService attachments, String currentPath) {
        this.attachments = attachments;
        path = currentPath;
        addClassName("reward-image-field");
        setId("reward-image-field");
        preview.setAlt(I18n.t("Mukofot rasmi"));
        preview.addClassName("reward-image-preview");
        preview.setVisible(path != null && !path.isBlank());
        if (preview.isVisible()) preview.setSrc(path);
        name.setText(preview.isVisible() ? I18n.t("Joriy rasm") : "");
        name.addClassName("reward-image-filename");
        remove = I18n.localize(new Button("Rasmni olib tashlash", e -> clearImage()));
        remove.setId("reward-image-remove");
        remove.setVisible(preview.isVisible());
        upload = new Upload((UploadHandler) event -> {
            try (var stream = event.getInputStream()) {
                // Bound the actual stream too; a forged size header cannot bypass the limit.
                byte[] bytes = stream.readNBytes(AttachService.MAX_REWARD_IMAGE_BYTES + 1);
                String receivedName = event.getFileName();
                event.getUI().access(() -> receive(bytes, receivedName));
            } catch (IOException exception) {
                event.getUI().access(() -> fail("Rasmni yuklab bo'lmadi. Qayta urinib ko'ring."));
                throw exception;
            }
        });
        upload.setId("reward-image-upload");
        upload.setMaxFiles(1);
        upload.setMaxFileSize(AttachService.MAX_REWARD_IMAGE_BYTES);
        upload.setAcceptedMimeTypes("image/jpeg", "image/png");
        upload.setAcceptedFileExtensions(".jpg", ".jpeg", ".png");
        upload.setUploadButton(I18n.localize(new Button("Rasm tanlash")));
        upload.setDropLabel(I18n.localize(new Span("JPG yoki PNG faylni shu yerga tashlang")));
        upload.getElement().addEventListener("upload-start", e -> {
            uploading = true;
            remove.setEnabled(false);
        });
        upload.getElement().addEventListener("upload-abort", e -> finish());
        upload.getElement().addEventListener("upload-error", e -> fail("Rasmni yuklab bo'lmadi. Qayta urinib ko'ring."));
        upload.addFileRejectedListener(e -> fail("JPG yoki PNG fayl tanlang. Eng katta hajm: 5 MB."));
        error.addClassName("reward-form-error");
        error.getElement().setAttribute("role", "alert");
        error.setVisible(false);
        Span helper = I18n.localize(new Span("Ixtiyoriy. JPG yoki PNG, 5 MB gacha."));
        helper.addClassName("reward-image-help");
        add(I18n.localize(new Span("Mukofot rasmi")), helper, upload, preview, name, remove, error);
    }

    void receive(byte[] bytes, String originalName) {
        if (disposed) return;
        try {
            String type = attachments.validateRewardImage(bytes, originalName);
            discardTemporary();
            pending = bytes;
            filename = originalName;
            preview.setSrc("data:" + type + ";base64," + Base64.getEncoder().encodeToString(bytes));
            preview.setVisible(true);
            name.setText(originalName);
            remove.setVisible(true);
            error.setVisible(false);
        } catch (AppBadRequestException exception) {
            I18n.text(error, exception.getMessage());
            error.setVisible(true);
        } finally {
            finish();
            upload.clearFileList();
        }
    }

    String imagePathForSave() {
        if (uploading) throw new AppBadRequestException("Wait for the image upload to finish.");
        if (pending != null && temporaryAttachment == null) {
            temporaryAttachment = attachments.uploadRewardImage(pending, filename);
        }
        return temporaryAttachment != null ? temporaryAttachment.getPath() : path;
    }

    void markSaved() { saved = true; pending = null; }

    void dispose() {
        disposed = true;
        if (!saved) discardTemporary();
        pending = null;
    }

    private void clearImage() {
        discardTemporary();
        pending = null;
        filename = null;
        path = null;
        preview.setVisible(false);
        name.setText("");
        remove.setVisible(false);
        error.setVisible(false);
        upload.clearFileList();
    }

    private void fail(String message) {
        if (disposed) return;
        finish();
        I18n.text(error, message);
        error.setVisible(true);
    }

    private void finish() { uploading = false; remove.setEnabled(true); }

    private void discardTemporary() {
        if (temporaryAttachment != null) {
            try { attachments.delete(temporaryAttachment.getId()); }
            catch (RuntimeException exception) { log.warn("Could not remove unsaved reward image {}", temporaryAttachment.getId(), exception); }
            temporaryAttachment = null;
        }
    }
}
