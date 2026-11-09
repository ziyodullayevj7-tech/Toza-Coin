package teamwork.TozaCoin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;
import teamwork.entity.AttachEntity;
import teamwork.exceptions.AppBadRequestException;
import teamwork.repository.AttachRepository;
import teamwork.service.AttachService;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RewardImageUploadTests {
    private final AttachService service = new AttachService();
    @TempDir Path directory;

    @Test
    void acceptsRealPngAndJpegAndRejectsOtherOrFakeImages() throws Exception {
        assertEquals("image/png", service.validateRewardImage(image("png", 2), "ticket.PNG"));
        assertEquals("image/jpeg", service.validateRewardImage(image("jpg", 2), "ticket.jpeg"));
        assertThrows(AppBadRequestException.class, () -> service.validateRewardImage(image("gif", 2), "fake.png"));
        assertThrows(AppBadRequestException.class, () -> service.validateRewardImage("fake".getBytes(), "fake.jpg"));
        assertThrows(AppBadRequestException.class, () -> service.validateRewardImage(image("png", 2), "fake.svg"));
    }

    @Test
    void rejectsEmptyOversizedAndExcessiveDimensionImages() {
        assertThrows(AppBadRequestException.class, () -> service.validateRewardImage(new byte[0], "empty.png"));
        assertThrows(AppBadRequestException.class, () -> service.validateRewardImage(
                new byte[AttachService.MAX_REWARD_IMAGE_BYTES + 1], "large.png"));
        assertThrows(AppBadRequestException.class, () -> service.validateRewardImage(image("png", 6001), "wide.png"));
    }

    @Test
    void savesThroughExistingAttachEntityStorageWithDetectedFormat() throws Exception {
        AttachRepository repository = mock(AttachRepository.class);
        Map<String, AttachEntity> saved = new HashMap<>();
        when(repository.save(any())).thenAnswer(call -> {
            AttachEntity entity = call.getArgument(0);
            saved.put(entity.getId(), entity);
            return entity;
        });
        when(repository.findById(anyString())).thenAnswer(call -> Optional.ofNullable(saved.get(call.getArgument(0))));
        ReflectionTestUtils.setField(service, "attachRepository", repository);
        ReflectionTestUtils.setField(service, "attacheFolder", directory.toString());
        byte[] bytes = image("png", 2);
        var dto = service.uploadRewardImage(bytes, "renamed.jpg");
        assertTrue(dto.getId().endsWith(".png"));
        assertEquals("image/png", dto.getType());
        AttachEntity stored = saved.get(dto.getId());
        assertEquals(service.getPublicUrl(stored), dto.getPath());
        assertArrayEquals(bytes, Files.readAllBytes(service.getFilePath(stored)));
    }

    private byte[] image(String format, int width) throws Exception {
        var output = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(width, 1, BufferedImage.TYPE_INT_RGB), format, output);
        return output.toByteArray();
    }
}
