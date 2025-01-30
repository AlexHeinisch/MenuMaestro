package at.codemaestro.service;

import at.codemaestro.domain.image.ImageRecord;
import at.codemaestro.exceptions.NotFoundException;
import at.codemaestro.mapper.ImageUrlMapper;
import at.codemaestro.persistence.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService implements ImageUrlMapper {
    private final ImageRepository imageRepository;

    @Value("${spring.application.uri}")
    private String applicationBaseUrl;

    @Value("${imageBasePath}")
    private String imageBasePath;

    public Set<String> getSupportedImageMimeTypes() {
        return Set.of("image/jpeg", "image/png", "image/gif");
    }

    @Override
    public String urlOfImage(String imageId) {
        return applicationBaseUrl + imageBasePath + "/" + imageId;
    }

    @Transactional
    public String uploadImage(String uploadedBy, String mimeType, Resource file) {
        byte[] imageData;
        try {
            imageData = file.getContentAsByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ImageRecord record = ImageRecord.builder()
                .id(generateId())
                .createdAt(Instant.now())
                .mimeType(mimeType)
                .data(imageData)
                .uploadedBy(uploadedBy)
                .build();
        record = imageRepository.save(record);
        return record.getId();
    }

    private String generateId() {
        return UUID.randomUUID().toString();
    }

    @Transactional(readOnly = true)
    public TypedImageBlob getImageData(String imageId) {
        ImageRecord record = imageRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Image with id " + imageId + " not found!"));
        return new TypedImageBlob(record);
    }

    /**
     * At regular intervals, deletes images that were uploaded but are no / no longer used anywhere.
     * This can happen either if the frontend had issues after uploading and therefore did not set the reference to the image,
     * or if the other entity referring to the image stops referring to the image.
     * In any case, the conditions are (1) the image must have be uploaded a while a go, (2) it is not referenced.
     */
    @Transactional
    public void cleanupImagesTask() {
        int nDeleted = imageRepository.deleteByUploadedBeforeAndNotReferenced(Instant.now().minus(15, ChronoUnit.MINUTES));
        log.info("deleted {} unused images for cleanup reasons", nDeleted);
    }

    public record TypedImageBlob(
            String mimeType,
            byte[] bytes
    ) {
        public TypedImageBlob(ImageRecord record) {
            this(record.getMimeType(), record.getData());
        }
    }
}
