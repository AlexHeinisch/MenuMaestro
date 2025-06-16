package dev.heinisch.menumaestro.mapper;

/**
 * Allows to convert an image id to image url, from inside mapper.
 */
public interface ImageUrlMapper {
    String urlOfImage(String imageId);

    default String mapImageUrlFromImageId(String imageId) {
        return imageId == null
                ? null
                : urlOfImage(imageId);
    }
}
