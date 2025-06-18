package dev.heinisch.menumaestro.domain.menu;

public record SnapshotMetadata(
        int numberOfMealsIncluded,
        int numberOfTotalIngredients,
        int numberOfAvailableIngredients
) {
}
