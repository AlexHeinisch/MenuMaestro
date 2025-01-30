package at.codemaestro.domain.menu;

import at.codemaestro.domain.ingredient.Ingredient;
import at.codemaestro.domain.ingredient_computation.IngredientComputationService;
import at.codemaestro.domain.stash.Stash;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class MenuComputationService {
    private final IngredientComputationService ingredientComputationService;

    public void computeMetadata(Menu menu, Function<Long, Ingredient> ingredientLoader) {
        var allItems = new ArrayList<>(menu.getItems());
        allItems.sort(Comparator.comparing(MenuItem::getPosition));
        addTransientDefaultSnapshot(allItems);
        for (MenuItem item : allItems) {
            if (item instanceof Snapshot snapshot) {
                computeSnapshotMetadata(allItems, snapshot, menu.getStash(), ingredientLoader);
            }
        }
    }

    /**
     * If needed, creates a transient snapshot for meals that don't belong to any snapshot.
     *
     * @param items sorted list.
     */
    private void addTransientDefaultSnapshot(List<MenuItem> items) {
        if (!items.isEmpty() && !(items.getLast() instanceof Snapshot)) {
            Snapshot transientSnapshot = Snapshot.builder()
                    .name("")
                    .position(items.getLast().getPosition() + 1)
                    .build();
            items.add(transientSnapshot);
        }
    }

    /**
     * @param items sorted list
     */
    private void computeSnapshotMetadata(List<MenuItem> items, Snapshot snapshot, Stash stash, Function<Long, Ingredient> ingredientLoader) {
        var includedMeals = getIncludedMeals(items, snapshot);
        var computeResult = ingredientComputationService.computeMissingIngredientsWithTimelineHeuristic(includedMeals, includedMeals, stash, ingredientLoader);
        for (Meal meal : includedMeals) {
            if (meal.getMenu().getStatus() == MenuStatus.CLOSED) {
                meal.setStatus(MealStatus.CLOSED);
            } else {
                meal.setStatus(meal.getIsDone()
                    ? MealStatus.DONE
                    : Objects.requireNonNull(computeResult.mealStatusPerMeal().get(meal.getId())));
            }
        }

        Set<Long> mealIngredientIds = ingredientComputationService.ingredientsOfNotDoneRecipes(includedMeals);
        Set<Long> availableIngredientIds = ingredientComputationService.availableIngredientIds(mealIngredientIds, computeResult.missingIngredients());
        SnapshotMetadata metadata = new SnapshotMetadata(includedMeals.size(),
                mealIngredientIds.size(), availableIngredientIds.size());
        snapshot.setMetadata(metadata);
    }

    public Set<Meal> getIncludedMeals(Menu menu, Set<Snapshot> snapshots) {
        List<MenuItem> items = new ArrayList<>(menu.getItems());
        items.sort(Comparator.comparing(MenuItem::getPosition));
        return getIncludedMeals(items, snapshots);
    }

    /**
     * @param items sorted list
     */
    private Set<Meal> getIncludedMeals(List<MenuItem> items, Set<Snapshot> snapshots) {
        // meals should not be part of multiple snapshots anyway
        Set<Meal> includedMeals = new HashSet<>();
        for (Snapshot snapshot : snapshots) {
            includedMeals.addAll(getIncludedMeals(items, snapshot));
        }
        return includedMeals;
    }

    /**
     * @param items sorted list
     */
    private List<Meal> getIncludedMeals(List<MenuItem> items, Snapshot snapshot) {
        if (!items.contains(snapshot)) {
            throw new IllegalArgumentException();
        }
        int snapshotIndex = items.indexOf(snapshot);
        ArrayList<Meal> includedMeals = new ArrayList<>();
        // search backward from the snapshot to the previous snapshot
        for (int i = snapshotIndex - 1; i >= 0; i--) {
            MenuItem item = items.get(i);
            if (item instanceof Meal meal) {
                includedMeals.add(meal);
            } else {
                break;
            }
        }
        return includedMeals;
    }
}
