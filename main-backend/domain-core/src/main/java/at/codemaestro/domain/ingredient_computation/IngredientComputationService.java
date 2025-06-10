package at.codemaestro.domain.ingredient_computation;

import at.codemaestro.domain.ingredient.Ingredient;
import at.codemaestro.domain.ingredient.IngredientUnit;
import at.codemaestro.domain.menu.Meal;
import at.codemaestro.domain.menu.MealStatus;
import at.codemaestro.domain.recipe.RecipeIngredientUse;
import at.codemaestro.domain.stash.Stash;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Domain service
 */
@Service
@RequiredArgsConstructor
public class IngredientComputationService {
    private final IngredientUnitConversionService ingredientUnitConversionService;

    public Set<Long> ingredientsOfNotDoneRecipes(Collection<Meal> meals) {
        return meals.stream().filter(meal -> !meal.getIsDone()).flatMap(m -> m.getRecipe().getIngredients().stream()).map(RecipeIngredientUse::getIngredientId).collect(Collectors.toSet());
    }

    public Stream<IngredientUse> ingredientsOfOpenMeals(Collection<Meal> meals) {
        return meals.stream().filter(meal -> !meal.getIsDone()).flatMap(meal -> meal.getRecipe().getIngredients().stream().map(IngredientUse::fromRecipeIngredientUse).map(ingredientUse -> ingredientUse.scale(meal.getRecipe().getServings(), meal.getNumberOfPeople())));
    }

    private static Stream<IngredientUse> sumAmountsWithSameIngredientAndUnit(Map<IngredientUseKey, List<IngredientUse>> usedIngredientsByIngredientAndUnit) {
        return usedIngredientsByIngredientAndUnit.entrySet().stream().map(entry -> {
            double amount = entry.getValue().stream().mapToDouble(IngredientUse::amount).sum();
            return new IngredientUse(entry.getKey().ingredient(), entry.getKey().unit(), ((float) amount));
        });
    }

    public Stream<IngredientUse> sumIngredients(List<IngredientUse> ingredients) {
        Map<Long, List<IngredientUnit>> unitsMap = ingredientUnitConversionService.unitsOfIngredients(ingredients);
        Stream<IngredientUse> stream = ingredients.stream();
        Map<IngredientUseKey, List<IngredientUse>> usedIngredientsByIngredientAndUnit = groupByIngredientAndUnit(unitsMap, stream);
        return sumAmountsWithSameIngredientAndUnit(usedIngredientsByIngredientAndUnit);
    }

    private Map<IngredientUseKey, List<IngredientUse>> groupByIngredientAndUnit(Map<Long, List<IngredientUnit>> unitsMap, Stream<IngredientUse> stream) {
        return stream.map(i -> ingredientUnitConversionService.convertHeuristic(unitsMap, i))
                .collect(Collectors.groupingBy(IngredientUseKey::fromIngredientUse));
    }

    /**
     * Performs an "inner join" on the ingredient key to get (x, y) amount pairs. Useful for getting (x out of y) amounts.
     */
    public List<Pair<Double, IngredientUse>> mapToIngredientAmountPair(List<IngredientUse> leftIngredients, List<IngredientUse> rightIngredients) {
        var rightIngredientsMap = rightIngredients.stream()
                .collect(Collectors.toMap(IngredientUseKey::fromIngredientUse, Function.identity()));
        return leftIngredients.stream()
                .map(i -> Pair.of(i.amount(), rightIngredientsMap.get(IngredientUseKey.fromIngredientUse(i))))
                .filter(pair -> Objects.nonNull(pair.getRight()))
                .toList();
    }

    /**
     * Missing / used computation under the assumption that the selected meals exist in isolation and other meals will not take away ingredients.
     */
    public UsedStashAndMissingAndTotalIngredients computeMissingIngredientsSimple(Collection<Meal> selectedMeals, Collection<Meal> allMeals, Stash stash, Function<Long, Ingredient> ingredientLoader) {
        List<IngredientUse> availableIngredients = sumIngredients(stash.getEntries().stream().map(e -> IngredientUse.fromStashEntry(e, ingredientLoader)).map(use -> ingredientUnitConversionService.convertIfPossible(use, use.ingredient().getDefaultUnit())).toList())
                .collect(Collectors.toCollection(ArrayList::new));
        Map<Long, List<IngredientUnit>> map = ingredientUnitConversionService.unitsOfIngredients(availableIngredients);
        var availableIngredientsKeyed = availableIngredients.stream().collect(Collectors.toMap(IngredientUseKey::fromIngredientUse, Function.identity()));
        List<IngredientUse> ingredientsOfOpenMeals = ingredientsOfOpenMeals(selectedMeals)
                .map(i -> ingredientUnitConversionService.convertHeuristic(map, i)).toList();
        Set<IngredientUseKey> ingredientUseKeysOfMeals = ingredientsOfOpenMeals.stream().map(IngredientUseKey::fromIngredientUse).collect(Collectors.toSet());
        Set<IngredientUseKey> overlappingKeys = new HashSet<>(availableIngredientsKeyed.keySet());
        overlappingKeys.retainAll(ingredientUseKeysOfMeals);
        List<IngredientUse> ingredientUsesSum = sumIngredients(Stream.concat(availableIngredients.stream(), ingredientsOfOpenMeals.stream().map(i -> i.scale(1, -1))).toList()).toList();
        List<IngredientUse> stashIngredientsAmountUsed = ingredientUsesSum.stream()
                .filter(ingredientUse -> overlappingKeys.contains(IngredientUseKey.fromIngredientUse(ingredientUse)))
                .map(i -> {
                    var stashIngredient = availableIngredientsKeyed.get(IngredientUseKey.fromIngredientUse(i));
                    double amountConsumed = stashIngredient.amount() - Math.max(0, i.amount());
                    return new IngredientUse(i.ingredient(), i.unit(), amountConsumed);
                })
                .toList();
        List<IngredientUse> missingIngredients = ingredientUsesSum.stream()
                .filter(i -> i.amount() < 0)
                .filter(ingredientUnitConversionService::ingredientAmountNotMinisculeHeuristic)
                .map(i -> i.scale(1, -1))
                .toList();
        ingredientsOfOpenMeals = sumIngredients(ingredientsOfOpenMeals).toList();
        return UsedStashAndMissingAndTotalIngredients.builder()
                .usedStashIngredients(stashIngredientsAmountUsed)
                .missingIngredients(missingIngredients)
                .totalIngredients(ingredientsOfOpenMeals).build();
    }

    /**
     * An ingredient is available IFF is used in some meal but not marked as missing.
     */
    public Set<Long> availableIngredientIds(Collection<Long> mealIngredientIds, List<IngredientUse> missingIngredients) {
        Set<Long> missingIngredientIds = missingIngredients.stream().map(i -> i.ingredient().getId()).collect(Collectors.toSet());
        Set<Long> availableIngredientIds = new HashSet<>(mealIngredientIds);
        availableIngredientIds.removeAll(missingIngredientIds);
        return availableIngredientIds;
    }

    @Builder
    public record UsedStashAndMissingAndTotalIngredients(List<IngredientUse> usedStashIngredients,
                                                         List<IngredientUse> missingIngredients,
                                                         List<IngredientUse> totalIngredients) {
    }

    /**
     * Considers the meals in their natural order, and computes data under the assumption that earlier meals take stash ingredients first.
     * The "simple" computation is a special case of this method, with a contiguous block of selectedMeals and allMeals = selectedMeals.
     */
    public UsedStashMissingIngredientsAndIngredientsPresentPerMeal computeMissingIngredientsWithTimelineHeuristic(Collection<Meal> selectedMeals, Collection<Meal> allMeals, Stash stash, Function<Long, Ingredient> ingredientLoader) {
        List<IngredientUse> availableIngredients = new ArrayList<>(stash.getEntries().stream().map(e -> IngredientUse.fromStashEntry(e, ingredientLoader)).map(use -> ingredientUnitConversionService.convertIfPossible(use, use.ingredient().getDefaultUnit())).toList());
        Map<Long, List<IngredientUnit>> unitsMap = ingredientUnitConversionService.unitsOfIngredients(availableIngredients);
        var selectedMealsOrdered = new ArrayList<>(selectedMeals);
        selectedMealsOrdered.sort(Comparator.comparing(Meal::getPosition));
        int lastMealPosition = -1;
        ArrayList<IngredientUse> missingIngredients = new ArrayList<>();
        Map<Long, MealStatus> allIngredientsPresent = new HashMap<>();
        for (Meal m : selectedMealsOrdered) {
            int lambdaLastMealPosition = lastMealPosition;
            var precedingMeals = allMeals.stream().filter(other -> other.getPosition() < m.getPosition() && other.getPosition() > lambdaLastMealPosition).toList();
            availableIngredients = sumIngredients(Stream.concat(availableIngredients.stream(), ingredientsOfOpenMeals(precedingMeals).map(i -> i.scale(1, -1))).toList()).filter(i -> i.amount() > 0.0049).toList();
            List<IngredientUse> mealIngredients = ingredientsOfOpenMeals(Set.of(m)).map(i -> i.scale(1, -1)).toList();
            var ingredientKeys = availableIngredients.stream().map(IngredientUseKey::fromIngredientUse).collect(Collectors.toSet());
            ingredientKeys.retainAll(mealIngredients.stream().map(IngredientUseKey::fromIngredientUse).collect(Collectors.toSet()));
            Map<Boolean, List<IngredientUse>> result = sumIngredients(Stream.concat(availableIngredients.stream(), mealIngredients.stream()).toList())
                    .filter(ingredientUse -> Math.abs(ingredientUse.amount()) > 0.00001)
                    .filter(ingredientUnitConversionService::ingredientAmountNotMinisculeHeuristic)
                    .collect(Collectors.groupingBy(ingredientUse -> ingredientUse.amount() >= 0));
            result.computeIfAbsent(false, a -> new ArrayList<>());
            result.computeIfAbsent(true, a -> new ArrayList<>());
            missingIngredients.addAll(result.get(false));

            MealStatus mealStatus = result.get(false).isEmpty() ? MealStatus.ALL_INGREDIENTS_PRESENT : !ingredientKeys.isEmpty() ? MealStatus.SOME_INGREDIENTS_MISSING : MealStatus.ALL_INGREDIENTS_MISSING;
            allIngredientsPresent.put(m.getId(), mealStatus);
            availableIngredients = result.get(true);
            lastMealPosition = m.getPosition();
        }
        var missingIngredientsSum = sumAmountsWithSameIngredientAndUnit(groupByIngredientAndUnit(unitsMap, missingIngredients.stream().map(i -> i.scale(1, -1)))).toList();
        return UsedStashMissingIngredientsAndIngredientsPresentPerMeal.builder().missingIngredients(missingIngredientsSum).usedStashIngredients(Collections.emptyList()).mealStatusPerMeal(allIngredientsPresent).build();
    }

    @Builder
    public record UsedStashMissingIngredientsAndIngredientsPresentPerMeal(List<IngredientUse> usedStashIngredients,
                                                                          List<IngredientUse> missingIngredients,
                                                                          Map<Long, MealStatus> mealStatusPerMeal) {
    }

    public Stream<IngredientUse> sumAutoconverting(List<IngredientUse> ingredients) {
        List<IngredientUse> list = sumIngredients(ingredients)
                .map(ingredientUnitConversionService::heuristicallyConvertToNicerUnit)
                .toList();
        // sum() again to make sure that no two equal keys were produced, just to be safe
        return sumIngredients(list);
    }
}
