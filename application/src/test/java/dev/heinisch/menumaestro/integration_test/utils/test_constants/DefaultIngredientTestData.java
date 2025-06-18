package dev.heinisch.menumaestro.integration_test.utils.test_constants;

import dev.heinisch.menumaestro.domain.ingredient.Ingredient;
import dev.heinisch.menumaestro.domain.ingredient.IngredientCategory;
import dev.heinisch.menumaestro.domain.ingredient.IngredientStatus;
import dev.heinisch.menumaestro.domain.ingredient.IngredientUnit;

import java.util.List;

public class DefaultIngredientTestData {
    public static final String DEFAULT_INGREDIENT_NAME_1 = "Ingredient-Alpha";
    public static final IngredientUnit DEFAULT_INGREDIENT_UNIT_1 = IngredientUnit.GRAMS;
    public static final IngredientCategory DEFAULT_INGREDIENT_CATEGORY_1 = IngredientCategory.PANTRY_STAPLES;
    public static final IngredientStatus DEFAULT_INGREDIENT_STATUS_1 = IngredientStatus.PUBLIC;

    public static final String DEFAULT_INGREDIENT_NAME_2 = "Ingredient-Beta";
    public static final IngredientUnit DEFAULT_INGREDIENT_UNIT_2 = IngredientUnit.LITRES;
    public static final IngredientCategory DEFAULT_INGREDIENT_CATEGORY_2 = IngredientCategory.DAIRY_AND_EGGS;
    public static final IngredientStatus DEFAULT_INGREDIENT_STATUS_2 = IngredientStatus.PUBLIC;

    public static final String DEFAULT_INGREDIENT_NAME_3 = "Ingredient-Gamma";
    public static final IngredientUnit DEFAULT_INGREDIENT_UNIT_3 = IngredientUnit.PIECE;
    public static final IngredientCategory DEFAULT_INGREDIENT_CATEGORY_3 = IngredientCategory.BEVERAGES;
    public static final IngredientStatus DEFAULT_INGREDIENT_STATUS_3 = IngredientStatus.PUBLIC;

    public static final String DEFAULT_INGREDIENT_NAME_4 = "Ingredient-Delta";
    public static final IngredientUnit DEFAULT_INGREDIENT_UNIT_4 = IngredientUnit.KILOGRAMS;
    public static final IngredientCategory DEFAULT_INGREDIENT_CATEGORY_4 = IngredientCategory.FRESH_PRODUCE;
    public static final IngredientStatus DEFAULT_INGREDIENT_STATUS_4 = IngredientStatus.PUBLIC;


    public static Ingredient defaultIngredient1() {
        return defaultIngredient1(null);
    }

    public static Ingredient defaultIngredient2() {
        return defaultIngredient2(null);
    }

    public static Ingredient defaultIngredient3() {
        return defaultIngredient3(null);
    }

    public static Ingredient defaultIngredient4() {
        return defaultIngredient4(null);
    }

    public static Ingredient defaultIngredient1(Long id) {
        return Ingredient.builder()
                .id(id)
                .name(DEFAULT_INGREDIENT_NAME_1)
                .defaultUnit(DEFAULT_INGREDIENT_UNIT_1)
                .category(DEFAULT_INGREDIENT_CATEGORY_1)
                .status(DEFAULT_INGREDIENT_STATUS_1)
                .build();
    }

    public static Ingredient defaultIngredient2(Long id) {
        return Ingredient.builder()
                .id(id)
                .name(DEFAULT_INGREDIENT_NAME_2)
                .defaultUnit(DEFAULT_INGREDIENT_UNIT_2)
                .category(DEFAULT_INGREDIENT_CATEGORY_2)
                .status(DEFAULT_INGREDIENT_STATUS_2)
                .build();
    }

    public static Ingredient defaultIngredient3(Long id) {
        return Ingredient.builder()
                .id(id)
                .name(DEFAULT_INGREDIENT_NAME_3)
                .defaultUnit(DEFAULT_INGREDIENT_UNIT_3)
                .category(DEFAULT_INGREDIENT_CATEGORY_3)
                .status(DEFAULT_INGREDIENT_STATUS_3)
                .build();
    }

    public static Ingredient defaultIngredient4(Long id) {
        return Ingredient.builder()
                .id(id)
                .name(DEFAULT_INGREDIENT_NAME_4)
                .defaultUnit(DEFAULT_INGREDIENT_UNIT_4)
                .category(DEFAULT_INGREDIENT_CATEGORY_4)
                .status(DEFAULT_INGREDIENT_STATUS_4)
                .build();
    }

    public static List<Ingredient> getDefaultIngredients() {
        return List.of(
                Ingredient.builder().name(DEFAULT_INGREDIENT_NAME_1).defaultUnit(DEFAULT_INGREDIENT_UNIT_1).category(DEFAULT_INGREDIENT_CATEGORY_1).status(DEFAULT_INGREDIENT_STATUS_1).build(),
                Ingredient.builder().name(DEFAULT_INGREDIENT_NAME_2).defaultUnit(DEFAULT_INGREDIENT_UNIT_2).category(DEFAULT_INGREDIENT_CATEGORY_2).status(DEFAULT_INGREDIENT_STATUS_2).build(),
                Ingredient.builder().name(DEFAULT_INGREDIENT_NAME_3).defaultUnit(DEFAULT_INGREDIENT_UNIT_3).category(DEFAULT_INGREDIENT_CATEGORY_3).status(DEFAULT_INGREDIENT_STATUS_3).build(),
                Ingredient.builder().name(DEFAULT_INGREDIENT_NAME_4).defaultUnit(DEFAULT_INGREDIENT_UNIT_4).category(DEFAULT_INGREDIENT_CATEGORY_4).status(DEFAULT_INGREDIENT_STATUS_4).build()
        );
    }
}
