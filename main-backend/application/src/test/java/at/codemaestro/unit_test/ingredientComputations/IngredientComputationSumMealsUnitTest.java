package at.codemaestro.unit_test.ingredientComputations;

import at.codemaestro.domain.ingredient.IngredientUnit;
import at.codemaestro.domain.ingredient_computation.IngredientComputationService;
import at.codemaestro.domain.ingredient_computation.IngredientUse;
import at.codemaestro.domain.menu.Meal;
import at.codemaestro.domain.stash.Stash;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static at.codemaestro.domain.ingredient.IngredientUnit.*;
import static java.util.Collections.emptyList;

/**
 * Tests units
 */
public class IngredientComputationSumMealsUnitTest extends IngredientComputationTestBase {


    @Test
    void givenMeals_sameIngredientWithEqualUnitSummed_withDifferentUnitListedSeparate() {
        var meal1 = mealScaledWithIngredients("meal 1", 1, 1, Set.of(
                useIngredient(ingredient1, IngredientUnit.LITRES, 0.5f),
                useIngredient(ingredient2, GRAMS, 1.3f)
        ));
        var meal2 = mealScaledWithIngredients("meal 2", 1, 1, Set.of(
                useIngredient(ingredient1, IngredientUnit.LITRES, 0.7f),
                useIngredient(ingredient2, GRAMS, 1.3f)
        ));
        var meal3 = mealScaledWithIngredients("meal 3", 1, 1, Set.of(
                useIngredient(ingredient1, IngredientUnit.LITRES, 0.1f),
                useIngredient(ingredient3, IngredientUnit.PIECE, 1f)
        ));
        var meal4 = mealScaledWithIngredients("meal 3", 1, 1, Set.of(
                useIngredient(ingredient1, IngredientUnit.KILOGRAMS, 0.5f),
                useIngredient(ingredient3, IngredientUnit.PIECE, 1f)
        ));
        var result = Assertions.assertDoesNotThrow(() -> ingredientComputationService.computeMissingIngredientsWithTimelineHeuristic(List.of(meal1, meal2, meal3, meal4),
                List.of(meal1, meal2, meal3, meal4),
                Stash.builder().entries(new HashSet<>()).build(),
                this::mockLoadIngredient
        ));
        var sortedResult = new ArrayList<>(result.missingIngredients());
        Assertions.assertEquals(4, sortedResult.size());
        sortedResult.sort(Comparator.comparing(IngredientUse::amount));
            // 1: 0.5 listed separate
        // 1: 0.5+0.7+0.1 but not +0.5 = 1.3
        // 2: 1.3+1.3  =  2.6
        // 3: 1+1     = 2



        Assertions.assertAll(
                () -> Assertions.assertEquals(1.3, sortedResult.get(0).amount(), 1e-7),
                () -> Assertions.assertEquals(ingredient1, sortedResult.get(0).ingredient()),
                () -> Assertions.assertEquals(IngredientUnit.LITRES, sortedResult.get(0).unit())

        );
        Assertions.assertAll(
                () -> Assertions.assertEquals(2, sortedResult.get(1).amount()),
                () -> Assertions.assertEquals(ingredient3, sortedResult.get(1).ingredient()),
                () -> Assertions.assertEquals(IngredientUnit.PIECE, sortedResult.get(1).unit())
        );
        Assertions.assertAll(
                () -> Assertions.assertEquals(2.6, sortedResult.get(2).amount(), 1e-7),
                () -> Assertions.assertEquals(ingredient2, sortedResult.get(2).ingredient()),
                () -> Assertions.assertEquals(GRAMS, sortedResult.get(2).unit())
        );
        Assertions.assertAll(// its changing kilos to gramms
                () -> Assertions.assertEquals(500, sortedResult.get(3).amount()),
                () -> Assertions.assertEquals(ingredient1, sortedResult.get(3).ingredient()),
                () -> Assertions.assertEquals(GRAMS, sortedResult.get(3).unit())
        );
    }

    @Test
    void givenMeal30PeopleRescalesRecipeFor4People() {
        var meal1 = mealScaledWithIngredients("meal 1", 30, 4, Set.of(
                useIngredient(ingredient2, IngredientUnit.KILOGRAMS, 1.3f),
                useIngredient(ingredient1, IngredientUnit.TEASPOONS, 2f)
        ));
        var result = Assertions.assertDoesNotThrow(() -> ingredientComputationService.computeMissingIngredientsWithTimelineHeuristic(List.of(meal1), List.of(meal1), Stash.builder().entries(new HashSet<>()).build(), this::mockLoadIngredient));
        var sortedResult = new ArrayList<>(result.missingIngredients());
        sortedResult.sort(Comparator.comparing(IngredientUse::amount));
        Assertions.assertAll(
                () -> Assertions.assertEquals(9.75, sortedResult.get(0).amount(), 1e-7),
                () -> Assertions.assertEquals(ingredient2, sortedResult.get(0).ingredient()),
                () -> Assertions.assertEquals(IngredientUnit.KILOGRAMS, sortedResult.get(0).unit())
        );
        Assertions.assertAll(
                () -> Assertions.assertEquals(15, sortedResult.get(1).amount(), 1e-7),
                () -> Assertions.assertEquals(ingredient1, sortedResult.get(1).ingredient()),
                () -> Assertions.assertEquals(IngredientUnit.TEASPOONS, sortedResult.get(1).unit())
        );
    }

    static Stream<Arguments> testParameters() {
        return Stream.of(
                Arguments.of(List.of
                                (List.of(ingredient(1, GRAMS, 7.8))),
                        List.of(
                                ingredient(1, GRAMS, 70)
                        ),
                        List.of(ingredient(1, GRAMS, 7.8)),
                        emptyList()),

                Arguments.of(List.of(), List.of(), List.of(), List.of()),

                Arguments.of(List.of(
                                // meals
                                List.of(ingredient(4, GRAMS, 6)),
                                List.of(ingredient(3, GRAMS, 7.5))
                        ),
                        // stash
                        List.of(
                                ingredient(4, PIECE, 74),
                                ingredient(4, GRAMS, 64)
                        ),
                        // used
                        List.of(ingredient(4, KILOGRAMS, 0.006)),
                        // missing
                        List.of(ingredient(3, GRAMS, 7.5))),

                Arguments.of(
                        // meal
                        List.of(List.of(ingredient(4, PIECE, 10))),
                        // stash
                        List.of(ingredient(4, PIECE, 3)),
                        // 3 used, 7 missing
                        List.of(ingredient(4, PIECE, 3)),
                        List.of(ingredient(4, PIECE, 7))),

                Arguments.of(List.of(
                                // meals
                                List.of(ingredient(4, PIECE, 6.8),
                                        ingredient(4, GRAMS, 5),
                                        ingredient(3, GRAMS, 4)),
                                List.of(ingredient(3, GRAMS, 14),
                                        ingredient(3, PIECE, 1),
                                        ingredient(4, PIECE, 4))),
                        // stash,  used
                        List.of(), List.of(),
                        // missing
                        List.of(ingredient(4, PIECE, 10.8),
                                ingredient(4, KILOGRAMS, 0.005),
                                ingredient(3, GRAMS, 18),
                                ingredient(3, PIECE, 1)))
        );
    }

    static IngredientUseParameter ingredient(long id, IngredientUnit unit, double amount) {
        return new IngredientUseParameter(id, unit, amount);
    }

    IngredientUse convertIngredient(IngredientUseParameter parameter) {
        return new IngredientUse(ingredientsMap.get(parameter.id), parameter.unit, parameter.amount);
    }

    record IngredientUseParameter(long id, IngredientUnit unit, double amount) {
    }

    @ParameterizedTest
    @MethodSource("testParameters")
    void testWithFixedValues(List<List<IngredientUseParameter>> mealsIngredients, List<IngredientUseParameter> stashIngredients,
                             List<IngredientUseParameter> expectedUsedStashIngredients, List<IngredientUseParameter> expectedMissingIngredients) {
        Set<Meal> meals = new HashSet<>();
        for (int i = 0; i < mealsIngredients.size(); i++) {
            var mealIngredients = mealsIngredients.get(i);
            meals.add(mealScaledWithIngredients("meal" + i, 1, 1, mealIngredients.stream()
                    .map(ing -> useIngredient(ingredientsMap.get(ing.id), ing.unit(), (float) ing.amount()))
                    .collect(Collectors.toSet())));
        }
        Stash stash = Stash.builder()
                .entries(stashIngredients.stream()
                        .map(ing -> stashEntry(ing.id, ing.unit(), (float) ing.amount()))
                        .collect(Collectors.toSet()))
                .build();
        IngredientComputationService.UsedStashAndMissingAndTotalIngredients result = Assertions.assertDoesNotThrow(
                () -> ingredientComputationService.computeMissingIngredientsSimple(meals, meals, stash, this::mockLoadIngredient));
        List<IngredientUse> expectUsed = expectedUsedStashIngredients.stream().map(this::convertIngredient).toList();
        List<IngredientUse> expectedMissing = expectedMissingIngredients.stream().map(this::convertIngredient).toList();
        assertIngredientUsesSetEqual(expectUsed, result.usedStashIngredients(), "fromStash");
        assertIngredientUsesSetEqual(expectedMissing, result.missingIngredients(), "missing");
    }
}
