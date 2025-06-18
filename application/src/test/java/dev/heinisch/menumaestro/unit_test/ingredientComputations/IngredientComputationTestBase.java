package dev.heinisch.menumaestro.unit_test.ingredientComputations;

import dev.heinisch.menumaestro.domain.ingredient.Ingredient;
import dev.heinisch.menumaestro.domain.ingredient.IngredientUnit;
import dev.heinisch.menumaestro.domain.ingredient_computation.IngredientComputationService;
import dev.heinisch.menumaestro.domain.ingredient_computation.IngredientUnitConversionService;
import dev.heinisch.menumaestro.domain.ingredient_computation.IngredientUse;
import dev.heinisch.menumaestro.domain.menu.Meal;
import dev.heinisch.menumaestro.domain.recipe.RecipeIngredientUse;
import dev.heinisch.menumaestro.domain.recipe.RecipeValue;
import dev.heinisch.menumaestro.domain.stash.StashEntry;
import dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultIngredientTestData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class IngredientComputationTestBase {

    IngredientComputationService ingredientComputationService;
    IngredientUnitConversionService ingredientUnitConversionService;

    Ingredient ingredient1;
    Ingredient ingredient2;
    Ingredient ingredient3;
    Ingredient ingredient4;

    Map<Long, Ingredient> ingredientsMap;

    int idSeq;

    @BeforeEach
    public void setUp() {
        ingredientUnitConversionService = new IngredientUnitConversionService();
        ingredientComputationService = new IngredientComputationService(ingredientUnitConversionService);

        ingredient1 = DefaultIngredientTestData.defaultIngredient1(1L);
        ingredient2 = DefaultIngredientTestData.defaultIngredient2(2L);
        ingredient3 = DefaultIngredientTestData.defaultIngredient3(3L);
        ingredient4 = DefaultIngredientTestData.defaultIngredient4(4L);
        ingredientsMap = Map.of(1L, ingredient1, 2L, ingredient2, 3L, ingredient3, 4L, ingredient4);
    }

    Meal mealScaledWithIngredients(String mealName, int numberOfPeople, int recipeServings, Set<RecipeIngredientUse> ingredients) {
        int id = idSeq++;
        return Meal.builder()
                .numberOfPeople(numberOfPeople)
                .id((long) id)
                .position(id)
                .name(mealName)
                .isDone(false)
                .recipe(RecipeValue.builder()
                        .ingredients(ingredients)
                        .cookingAppliances(Collections.emptySet())
                        .servings(recipeServings)
                        .build())
                .build();
    }


    StashEntry stashEntry(Long ingredientId, IngredientUnit unit, float amount) {
        return StashEntry.builder()
                .ingredientId(ingredientId)
                .unit(unit)
                .amount((double) amount)
                .build();
    }

    RecipeIngredientUse useIngredient(Ingredient ingredient, IngredientUnit unit, float amount) {
        return RecipeIngredientUse.builder()
                .ingredient(ingredient)
                .unit(unit)
                .amount(amount)
                .build();
    }

    Ingredient mockLoadIngredient(Long id) {
        return Objects.requireNonNull(ingredientsMap.get(id), "mock ingredient not found");
    }

    void assertIngredientUsesSetEqual(List<IngredientUse> expectedUses, List<IngredientUse> actualUses, String checkName) {
        var expected = new ArrayList<>(expectedUses);
        var actual = new ArrayList<>(actualUses);
        expected.sort(Comparator.comparing(IngredientUse::amount));
        actual.sort(Comparator.comparing(IngredientUse::amount));
        Assertions.assertEquals(expected.size(), actual.size(), checkName + " same size");
        for (int i = 0; i < expected.size(); i++) {
            int index = i;
            Assertions.assertAll(
                    () -> Assertions.assertEquals(expected.get(index).ingredient().getId(), actual.get(index).ingredient().getId(), checkName + index),
                    () -> Assertions.assertEquals(expected.get(index).unit(), actual.get(index).unit(), checkName + index),
                    () -> Assertions.assertEquals(expected.get(index).amount(), actual.get(index).amount(), 0.0001, checkName + index)
            );
        }
    }
}
