package dev.heinisch.menumaestro.endpoint;

import dev.heinisch.menumaestro.exceptions.ValidationException;
import dev.heinisch.menumaestro.service.ImageService;
import dev.heinisch.menumaestro.validation.PropertyChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.api.ImagesApi;
import org.openapitools.model.ImageUploadResponseDto;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ImageStorageEndpoint implements ImagesApi {
    private final ImageService imageService;

    @Override
    public ResponseEntity<Resource> downloadImage(String id) {
        log.info("Downloading image {}", id);
        ImageService.TypedImageBlob image = imageService.getImageData(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.mimeType()))
                .body(new ByteArrayResource(image.bytes()));
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public ResponseEntity<ImageUploadResponseDto> uploadImage(MultipartFile file) {
        log.info("Uploading image with type {}, length {}", file.getContentType(), file.getSize());
        String uploaderUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        PropertyChecker.begin()
                .checkThat(file, "file").notNull().done()
                .finalize(ValidationException::fromPropertyChecker);
        Set<String> supportedImageMimeTypes = imageService.getSupportedImageMimeTypes();
        if (!supportedImageMimeTypes.contains(file.getContentType())) {
            throw new ValidationException("Unsupported image mime type: " + file.getContentType() + ", supported are " + supportedImageMimeTypes + "!");
        }
        String imageId = imageService.uploadImage(uploaderUsername, file.getContentType(), file.getResource());
        String imageUrl = imageService.urlOfImage(imageId);
        return ResponseEntity.ok(new ImageUploadResponseDto()
                .identifier(imageId)
                .downloadLink(imageUrl)
        );
    }
}
