package dev.heinisch.menumaestro.integration_test.image;

import dev.heinisch.menumaestro.domain.image.ImageRecord;
import dev.heinisch.menumaestro.domain.recipe.Recipe;
import dev.heinisch.menumaestro.domain.recipe.RecipeValue;
import dev.heinisch.menumaestro.domain.recipe.RecipeVisibility;
import dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultRecipeTestData;
import dev.heinisch.menumaestro.persistence.ImageRepository;
import dev.heinisch.menumaestro.persistence.RecipeRepository;
import dev.heinisch.menumaestro.service.ImageService;
import dev.heinisch.menumaestro.test_support.DatabaseCleanerExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

@ActiveProfiles({"test", "datagen-off"})
@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ImageService.class))
@ExtendWith(DatabaseCleanerExtension.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ImageCleanupIntegrationTest {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ImageService imageService;

    @Test
    void imageUploadedRecently_notDeleted() {
        imageRepository.save(ImageRecord.builder()
                .id("recently-1")
                .data(new byte[16])
                .mimeType("image/png")
                .createdAt(Instant.now())
                .uploadedBy("someone")
                .build());
        Assertions.assertDoesNotThrow(() -> imageService.cleanupImagesTask());
        List<ImageRecord> allImages = imageRepository.findAll();
        Assertions.assertAll(
                () -> Assertions.assertEquals(1, allImages.size()),
                () -> Assertions.assertEquals("recently-1", allImages.getFirst().getId())
        );
    }

    @Test
    void imageReferencedByOneRecipeValue_notDeleted() {
        imageRepository.save(ImageRecord.builder()
                .id("referenced-1")
                .data(new byte[16])
                .mimeType("image/png")
                .createdAt(Instant.now().minus(2, ChronoUnit.DAYS))
                .uploadedBy("someone")
                .build());
        Recipe recipe = defaultRecipe("referenced-1");
        recipeRepository.save(recipe);
        Assertions.assertDoesNotThrow(() -> imageService.cleanupImagesTask());
        List<ImageRecord> allImages = imageRepository.findAll();
        Assertions.assertAll(
                () -> Assertions.assertEquals(1, allImages.size()),
                () -> Assertions.assertEquals("referenced-1", allImages.getFirst().getId())
        );
    }

    @Test
    void imageReferencedAndNotReferencedByOneRecipeValue_notDeleted() {
        imageRepository.save(ImageRecord.builder()
                .id("semireferenced-1")
                .data(new byte[16])
                .mimeType("image/png")
                .createdAt(Instant.now().minus(2, ChronoUnit.DAYS))
                .uploadedBy("someone")
                .build());
        Recipe recipe = defaultRecipe("semireferenced-1");
        recipeRepository.save(recipe);
        Recipe recipe2 = defaultRecipe(null);
        recipeRepository.save(recipe2);
        Assertions.assertDoesNotThrow(() -> imageService.cleanupImagesTask());
        List<ImageRecord> allImages = imageRepository.findAll();
        Assertions.assertAll(
                () -> Assertions.assertEquals(1, allImages.size()),
                () -> Assertions.assertEquals("semireferenced-1", allImages.getFirst().getId())
        );
    }

    @Test
    void imageNotReferencedByRecipeValue_deleted() {
        imageRepository.save(ImageRecord.builder()
                .id("delete-me-1")
                .data(new byte[16])
                .mimeType("image/png")
                .uploadedBy("someone")
                .createdAt(Instant.now().minus(2, ChronoUnit.DAYS))
                .build());
        Recipe recipe = defaultRecipe(null);
        recipeRepository.save(recipe);
        Assertions.assertDoesNotThrow(() -> imageService.cleanupImagesTask());
        Assertions.assertEquals(0, imageRepository.count());
    }

    private Recipe defaultRecipe(String imageId) {
        return Recipe.builder()
                .recipeValue(RecipeValue.builder()
                        .name(DefaultRecipeTestData.DEFAULT_RECIPE_NAME_1)
                        .author(DefaultRecipeTestData.DEFAULT_RECIPE_AUTHOR_1)
                        .servings(DefaultRecipeTestData.DEFAULT_RECIPE_SERVINGS_1)
                        .cookingAppliances(Collections.emptySet())
                        .ingredients(Collections.emptySet())
                        .imageId(imageId)
                        .description(DefaultRecipeTestData.DEFAULT_RECIPE_DESCRIPTION_1)
                        .build())
                .visibility(RecipeVisibility.PUBLIC)
                .build();
    }
}
