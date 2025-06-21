package dev.heinisch.menumaestro.utils.test_constants;

import org.openapitools.model.RecipeCreateEditDto;
import org.openapitools.model.RecipeVisibility;

public class DefaultRecipeTestData {
    public static final String DEFAULT_RECIPE_NAME_1 = "Test Recipe 1: Alpha";
    public static final String DEFAULT_RECIPE_DESCRIPTION_1 = "Test Recipe 1: Description";
    public static final int DEFAULT_RECIPE_SERVINGS_1 = 4;
    public static final RecipeVisibility DEFAULT_RECIPE_VISIBILITY_1 = RecipeVisibility.PUBLIC;
    public static final String DEFAULT_RECIPE_AUTHOR_1 = "Recipe1 Author Name";

    public static RecipeCreateEditDto defaultRecipeCreateEditDto1() {
        return new RecipeCreateEditDto()
                .name(DEFAULT_RECIPE_NAME_1)
                .description(DEFAULT_RECIPE_DESCRIPTION_1)
                .servings(DEFAULT_RECIPE_SERVINGS_1)
                .visibility(DEFAULT_RECIPE_VISIBILITY_1)
                .author(DEFAULT_RECIPE_AUTHOR_1);
    }

    public static final String DEFAULT_RECIPE_NAME_2 = "Test Recipe 2: Beta";
    public static final String DEFAULT_RECIPE_DESCRIPTION_2 = "Test Recipe 2: Description";
    public static final int DEFAULT_RECIPE_SERVINGS_2 = 6;
    public static final RecipeVisibility DEFAULT_RECIPE_VISIBILITY_2 = RecipeVisibility.PUBLIC;
    public static final String DEFAULT_RECIPE_AUTHOR_2 = "Recipe2 Author Name";

    public static RecipeCreateEditDto defaultRecipeCreateEditDto2() {
        return new RecipeCreateEditDto()
                .name(DEFAULT_RECIPE_NAME_2)
                .description(DEFAULT_RECIPE_DESCRIPTION_2)
                .servings(DEFAULT_RECIPE_SERVINGS_2)
                .visibility(DEFAULT_RECIPE_VISIBILITY_2)
                .author(DEFAULT_RECIPE_AUTHOR_2);
    }

    public static final String DEFAULT_RECIPE_NAME_3 = "Test Recipe 3: Alpha";
    public static final String DEFAULT_RECIPE_DESCRIPTION_3 = "Test Recipe 3: Description";
    public static final int DEFAULT_RECIPE_SERVINGS_3 = 8;
    public static final RecipeVisibility DEFAULT_RECIPE_VISIBILITY_3 = RecipeVisibility.PUBLIC;
    public static final String DEFAULT_RECIPE_AUTHOR_3 = "Recipe 3 Author Name";

    public static RecipeCreateEditDto defaultRecipeCreateEditDto3() {
        return new RecipeCreateEditDto()
                .name(DEFAULT_RECIPE_NAME_3)
                .description(DEFAULT_RECIPE_DESCRIPTION_3)
                .servings(DEFAULT_RECIPE_SERVINGS_3)
                .visibility(DEFAULT_RECIPE_VISIBILITY_3)
                .author(DEFAULT_RECIPE_AUTHOR_3);
    }
}
