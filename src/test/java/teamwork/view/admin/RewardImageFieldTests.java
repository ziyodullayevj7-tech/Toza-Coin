package teamwork.view.admin;

import com.vaadin.flow.component.button.Button;
import org.junit.jupiter.api.Test;
import teamwork.dto.attach.AttachDto;
import teamwork.exceptions.AppBadRequestException;
import teamwork.service.AttachService;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RewardImageFieldTests {
    private final AttachService attachments = mock(AttachService.class);

    @Test
    void retainsExistingImageUnlessExplicitlyRemoved() {
        var field = new RewardImageField(attachments, "/attaches/current.png");
        assertEquals("/attaches/current.png", field.imagePathForSave());
        remove(field).click();
        assertNull(field.imagePathForSave());
        field.dispose();
        verifyNoInteractions(attachments);
    }

    @Test
    void stagesUploadAndOnlyPersistsOnceWhenSaving() {
        byte[] bytes = {1, 2, 3};
        when(attachments.validateRewardImage(bytes, "ticket.png")).thenReturn("image/png");
        when(attachments.uploadRewardImage(bytes, "ticket.png")).thenReturn(attachment("new.png"));
        var field = new RewardImageField(attachments, null);
        field.receive(bytes, "ticket.png");
        verify(attachments, never()).uploadRewardImage(any(), any());
        assertEquals("/attaches/new.png", field.imagePathForSave());
        assertEquals("/attaches/new.png", field.imagePathForSave());
        field.markSaved();
        field.dispose();
        verify(attachments, times(1)).uploadRewardImage(bytes, "ticket.png");
        verify(attachments, never()).delete(any());
    }

    @Test
    void cancelDiscardsStagedBytesWithoutWritingFiles() {
        byte[] bytes = {1};
        when(attachments.validateRewardImage(bytes, "ticket.jpg")).thenReturn("image/jpeg");
        var field = new RewardImageField(attachments, null);
        field.receive(bytes, "ticket.jpg");
        field.dispose();
        verify(attachments, never()).uploadRewardImage(any(), any());
        verify(attachments, never()).delete(any());
    }

    @Test
    void failedSaveThenCancelDeletesOnlyTheNewAttachment() {
        byte[] bytes = {1};
        when(attachments.validateRewardImage(bytes, "ticket.png")).thenReturn("image/png");
        when(attachments.uploadRewardImage(bytes, "ticket.png")).thenReturn(attachment("new.png"));
        var field = new RewardImageField(attachments, "/attaches/original.png");
        field.receive(bytes, "ticket.png");
        field.imagePathForSave();
        field.dispose();
        verify(attachments).delete("new.png");
        verify(attachments, never()).delete("original.png");
    }

    @Test
    void invalidReplacementKeepsCurrentImageAndLateUploadCannotChangeClosedEditor() {
        byte[] bytes = {1};
        when(attachments.validateRewardImage(bytes, "invalid.png")).thenThrow(new AppBadRequestException("Invalid image"));
        var field = new RewardImageField(attachments, "/attaches/original.png");
        field.receive(bytes, "invalid.png");
        assertEquals("/attaches/original.png", field.imagePathForSave());
        field.dispose();
        field.receive(bytes, "late.png");
        verify(attachments, never()).validateRewardImage(bytes, "late.png");
    }

    private Button remove(RewardImageField field) {
        return field.getChildren().filter(Button.class::isInstance).map(Button.class::cast).findFirst().orElseThrow();
    }
    private AttachDto attachment(String id) {
        var dto = new AttachDto();
        dto.setId(id);
        dto.setPath("/attaches/" + id);
        return dto;
    }
}
