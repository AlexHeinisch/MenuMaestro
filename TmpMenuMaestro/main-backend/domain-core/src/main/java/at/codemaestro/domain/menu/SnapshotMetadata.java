package at.codemaestro.domain.menu;

public record SnapshotMetadata(
        int numberOfMealsIncluded,
        int numberOfTotalIngredients,
        int numberOfAvailableIngredients
) {
}
