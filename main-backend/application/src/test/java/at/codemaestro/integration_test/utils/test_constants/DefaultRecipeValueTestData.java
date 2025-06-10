package at.codemaestro.integration_test.utils.test_constants;

import at.codemaestro.domain.recipe.RecipeValue;

import java.util.Collections;

public class DefaultRecipeValueTestData {

    public static final String RECIPE_VALUE_NAME = "Test RecipeValue: Name";
    public static final String RECIPE_VALUE_DESCRIPTION = "Test RecipeValue: Description";
    public static final int RECIPE_VALUE_SERVINGS = 4;
    public static final String RECIPE_VALUE_AUTHOR = "Test RecipeValue: Author";


    public static RecipeValue defaultRecipeValue() {
        return RecipeValue.builder()
                .author(RECIPE_VALUE_AUTHOR)
                .ingredients(Collections.emptySet())
                .cookingAppliances(Collections.emptySet())
                .servings(RECIPE_VALUE_SERVINGS)
                .name(RECIPE_VALUE_NAME)
                .description(RECIPE_VALUE_DESCRIPTION)
                .build();
    }
}
