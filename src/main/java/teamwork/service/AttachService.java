package teamwork.service;

import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import teamwork.dto.attach.AttachDto;
import teamwork.entity.AttachEntity;
import teamwork.exceptions.AppBadRequestException;
import teamwork.repository.AttachRepository;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;


@Service
public class AttachService {
    @Autowired
    private AttachRepository attachRepository;

    @Value("${attache.folder}")
    private String attacheFolder;

    @Value("${server.url}")
    private String attacheUrl;

    // UPLOAD LIVE WebRTC CAMERA PHOTO (Base64 String)
    public AttachDto uploadBase64CameraPhoto(String base64Data) {
        if (base64Data == null || base64Data.isBlank()) {
            throw new AppBadRequestException("Camera image data is empty");
        }

        // PREFIX IF PRESENT
        if (base64Data.contains(",")) {
            base64Data = base64Data.split(",")[1];
        }

        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(base64Data);
        } catch (IllegalArgumentException e) {
            throw new AppBadRequestException("Camera image data is invalid");
        }

        // BYTES SECURITY CHECK (MUST BE JPEG OR PNG)
        if (!isValidImageMagicBytes(bytes)) {
            throw new AppBadRequestException("Photos are only accepted directly from camera");
        }

        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        String fileName = "camera_" + UUID.randomUUID().toString() + ".jpg";

        return upload(inputStream, fileName, "image/jpeg", bytes.length);
    }

    public AttachDto upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new AppBadRequestException("File not found");
        }

        try {
            String pathFolder = getYmDString(); // 2026/08/27
            String key = UUID.randomUUID().toString();
            String extension = getExtension(Objects.requireNonNull(file.getOriginalFilename()));

            File folder = new File(attacheFolder + "/" + pathFolder);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            byte[] bytes = file.getBytes();
            Path path = Paths.get(attacheFolder + "/" + pathFolder + "/" + key + "." + extension);
            Files.write(path, bytes);

            AttachEntity entity = new AttachEntity();
            entity.setId(key + "." + extension);
            entity.setPath(pathFolder);
            entity.setSize(file.getSize());
            entity.setOriginName(file.getOriginalFilename());
            entity.setType(file.getContentType());
            attachRepository.save(entity);

            return toDTO(entity);
        } catch (IOException e) {
            throw new AppBadRequestException("Error uploading file");
        }
    }

    public AttachDto upload(InputStream inputStream, String originalFileName, String contentType, long size) {
        if (
                inputStream == null
                || originalFileName == null
                || originalFileName.isBlank()
        ) {
            throw new AppBadRequestException("File stream or filename is empty");
        }

        try {
            String pathFolder = getYmDString();
            String key = UUID.randomUUID().toString();
            String extension = getExtension(originalFileName);

            File folder = new File(attacheFolder + "/" + pathFolder);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            Path path = Paths.get(attacheFolder + "/" + pathFolder + "/" + key + "." + extension);
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);

            AttachEntity entity = new AttachEntity();
            entity.setId(key + "." + extension);
            entity.setPath(pathFolder);
            entity.setSize(size);
            entity.setOriginName(originalFileName);
            entity.setType(contentType);
            attachRepository.save(entity);

            return toDTO(entity);
        } catch (IOException e) {
            throw new AppBadRequestException("Error uploading file: " + e.getMessage());
        }
    }

    // OPEN FILE
    public ResponseEntity<Resource> open(String fileName) {
        AttachEntity entity = getEntity(fileName);
        Path filePath = Paths.get(
                attacheFolder
                + "/"
                + entity.getPath()
                + "/"
                + entity.getId())
                .normalize();

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                throw new RuntimeException("Resource not found: " + filePath);
            }
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // DELETE
    public Boolean delete(String fileId) {
        AttachEntity entity = getEntity(fileId);
        try {
            Path file = Paths.get(attacheFolder + "/" + entity.getPath() + "/" + entity.getId()).normalize();
            boolean deleted = Files.deleteIfExists(file);
            if (!deleted) {
                throw new AppBadRequestException("Could not delete file from filesystem");
            }
            attachRepository.delete(entity);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppBadRequestException("Something went wrong");
        }
    }

    public Page<AttachDto> pagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AttachEntity> entities = attachRepository.findAllByOrderByCreatedDateDesc(pageable);
        List<AttachEntity> entityList = entities.getContent();
        long totalElement = entities.getTotalElements();
        List<AttachDto> dtos = new LinkedList<>();
        entityList.forEach(e -> dtos.add(toDTO(e)));
        return new PageImpl<>(dtos, pageable, totalElement);
    }

    public ResponseEntity<Resource> download(String filename) {
        AttachEntity entity = getEntity(filename);
        try {
            Path file = Paths.get(
                    attacheFolder
                            + "/"
                            + entity.getPath()
                            + "/"
                            + entity.getId()).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists()) {
                throw new AppBadRequestException("File not found");
            }
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + entity.getOriginName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            throw new AppBadRequestException("Something went wrong");
        }
    }

    public AttachEntity getEntity(String id) {
        return attachRepository.findById(id)
                .orElseThrow(() -> new AppBadRequestException("Attach not found"));
    }

    public Optional<AttachEntity> findEntity(String id) {
        if (id == null || id.isBlank()) return Optional.empty();
        return attachRepository.findById(id);
    }

    public Path getFilePath(AttachEntity entity) {
        if (entity == null) return null;
        return Paths.get(attacheFolder, entity.getPath(), entity.getId()).normalize();
    }

    private String getExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf(".");
        return fileName.substring(lastIndex + 1);
    }

    private String getYmDString() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int day = Calendar.getInstance().get(Calendar.DATE);
        return  year + "/" + month + "/" + day;
    }

    private AttachDto toDTO(AttachEntity entity) {
        AttachDto attachDTO = new AttachDto();
        attachDTO.setId(entity.getId());
        attachDTO.setOriginName(entity.getOriginName());
        attachDTO.setSize(entity.getSize());
        attachDTO.setType(entity.getType());
        attachDTO.setCreatedData(entity.getCreatedDate());
        attachDTO.setPath(openURL(entity.getId()));
        return attachDTO;
    }

    public String getPublicUrl(AttachEntity entity) {
        if (entity == null) return null;
        return "/attaches/" + entity.getPath() + "/" + entity.getId();
    }

    public String openURL(String fileName) {
        Optional<AttachEntity> optional = attachRepository.findById(fileName);
        if (optional.isPresent()) {
            return getPublicUrl(optional.get());
        }
        return "/attaches/" + fileName;
    }

    private boolean isValidImageMagicBytes(byte[] bytes) {
        if (bytes == null || bytes.length < 4) return false;
        boolean isJpeg = (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF;
        boolean isPng = (bytes[0] & 0xFF) == 0x89 && (bytes[1] & 0xFF) == 0x50 && (bytes[2] & 0xFF) == 0x4E && (bytes[3] & 0xFF) == 0x47;
        return isJpeg || isPng;
    }
}
