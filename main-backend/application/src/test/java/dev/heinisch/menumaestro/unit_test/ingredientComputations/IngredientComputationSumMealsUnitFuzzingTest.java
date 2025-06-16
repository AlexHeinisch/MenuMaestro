package dev.heinisch.menumaestro.unit_test.ingredientComputations;

import dev.heinisch.menumaestro.domain.ingredient.Ingredient;
import dev.heinisch.menumaestro.domain.ingredient.IngredientUnit;
import dev.heinisch.menumaestro.domain.ingredient_computation.IngredientComputationService;
import dev.heinisch.menumaestro.domain.ingredient_computation.IngredientUse;
import dev.heinisch.menumaestro.domain.menu.Meal;
import dev.heinisch.menumaestro.domain.recipe.RecipeIngredientUse;
import dev.heinisch.menumaestro.domain.recipe.RecipeValue;
import dev.heinisch.menumaestro.domain.stash.Stash;
import dev.heinisch.menumaestro.domain.stash.StashEntry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class IngredientComputationSumMealsUnitFuzzingTest extends IngredientComputationTestBase {
    Random random = new Random();

    ArrayList<Object[]> parameters;

    @Test
    void fuzzDriver() {
        for (int i = 0; i < 30; i++) {
            parameters = new ArrayList<>();
            var result = fuzzTest();
            for (IngredientUse ingredientUse : result.missingIngredients()) {
                parameters.add(new Object[]{"missing", ingredientUse.ingredient().getId(), ingredientUse.unit(), ingredientUse.amount()});
            }
            for (IngredientUse ingredientUse : result.usedStashIngredients()) {
                parameters.add(new Object[]{"fromStash", ingredientUse.ingredient().getId(), ingredientUse.unit(), ingredientUse.amount()});
            }
            logValues();
            System.out.println("=".repeat(40));
        }
    }

    void logValues() {
        for (Object[] parameter : parameters) {
            System.out.println(Arrays.stream(parameter).map(Object::toString).collect(Collectors.joining(", ")));
        }
    }

    IngredientComputationService.UsedStashAndMissingAndTotalIngredients fuzzTest() {
        List<Ingredient> ingredients = new ArrayList<>(ingredientsMap.values());
        ingredients.remove(3);
        ingredients.remove(2);

        HashSet<StashEntry> stashEntries = new HashSet<>();
        int nStashEntries = random.nextInt(0, 4);
        for (int j = 0; j < nStashEntries; j++) {
            stashEntries.add(stashEntry(ingredients.get(random.nextInt(0, 2)).getId(), randomIngredientUnit(), random.nextFloat(0.01f, 100f)));
        }
        Stash stash = Stash.builder()
                .entries(stashEntries)
                .build();

        int nMeals = random.nextInt(1, 3);
        List<Meal> meals = new ArrayList<>();
        List<Meal> selectedMeals = new ArrayList<>();
        for (int i = 0; i < nMeals; i++) {
            int nIngredients = random.nextInt(1, 5);
            Set<RecipeIngredientUse> ingredientUses = new HashSet<>();
            for (int j = 0; j < nIngredients; j++) {
                ingredientUses.add(recordUseIngredient(ingredients.get(random.nextInt(0, 2)),
                        randomIngredientUnit(),
                        random.nextFloat(0.1f, 10)));
            }
            boolean inShoppingList = true; // random.nextBoolean();
            Meal meal = mealWithPosAndIngredients("meal " + i, i, ingredientUses, inShoppingList);
            meals.add(meal);
            if (inShoppingList) {
                selectedMeals.add(meal);
            }
        }
        // write test input specification

        var result = Assertions.assertDoesNotThrow(() -> ingredientComputationService.computeMissingIngredientsSimple(selectedMeals, meals, stash, this::mockLoadIngredient));
        Assertions.assertDoesNotThrow(() -> ingredientComputationService.computeMissingIngredientsWithTimelineHeuristic(selectedMeals, meals, stash, this::mockLoadIngredient));
        return result;
    }

    private IngredientUnit randomIngredientUnit() {
        return new IngredientUnit[]{IngredientUnit.GRAMS, IngredientUnit.PIECE}[random.nextInt(0, 2)];
    }

    @Override
    StashEntry stashEntry(Long ingredientId, IngredientUnit unit, float amount) {
        parameters.add(new Object[]{"stashEntry", ingredientId, unit, amount});
        return super.stashEntry(ingredientId, unit, amount);
    }

    RecipeIngredientUse recordUseIngredient(Ingredient ingredient, IngredientUnit unit, float amount) {
        parameters.add(new Object[]{"mealIngredient", ingredient.getId(), unit, amount});
        return useIngredient(ingredient, unit, amount);
    }

    Meal mealWithPosAndIngredients(String mealName, int position, Set<RecipeIngredientUse> ingredients, boolean inShoppingList) {
        int id = idSeq++;
        parameters.add(new Object[]{"MEAL", id, inShoppingList});
        return Meal.builder()
                .numberOfPeople(1)
                .id((long) id)
                .position(position)
                .name(mealName)
                .isDone(false)
                .recipe(RecipeValue.builder()
                        .ingredients(ingredients)
                        .cookingAppliances(Collections.emptySet())
                        .servings(1)
                        .build())
                .build();
    }

    @Test
    void heuristicUnitConversionNeverThrows() {
        Ingredient ingredient = new Ingredient();
        for (IngredientUnit unit : IngredientUnit.values()) {
            for (double i = 0.1; i < 2000; i = i * 1.5) {
                double amount = i;
                Assertions.assertDoesNotThrow(() ->
                        ingredientUnitConversionService.heuristicallyConvertToNicerUnit(new IngredientUse(ingredient, unit, amount)));
            }
        }
    }
}
