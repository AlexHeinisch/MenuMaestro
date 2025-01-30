package at.codemaestro.domain.ingredient_computation;

import at.codemaestro.domain.ingredient.Ingredient;
import at.codemaestro.domain.ingredient.IngredientUnit;
import at.codemaestro.domain.recipe.RecipeIngredientUse;
import at.codemaestro.domain.shopping_list.ShoppingListItem;
import at.codemaestro.domain.stash.StashEntry;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public record IngredientUse(
        Ingredient ingredient,
        IngredientUnit unit,
        double amount
) {
    public static IngredientUse fromRecipeIngredientUse(RecipeIngredientUse recipeIngredientUse) {
        return new IngredientUse(recipeIngredientUse.getIngredient(), recipeIngredientUse.getUnit(), recipeIngredientUse.getAmount());
    }

    public static IngredientUse fromStashEntry(StashEntry stashEntry, Function<Long, Ingredient> ingredientLoader) {
        return new IngredientUse(ingredientLoader.apply(stashEntry.getIngredientId()), stashEntry.getUnit(), stashEntry.getAmount());
    }

    public static IngredientUse fromShoppingListItem(ShoppingListItem item, Function<Long, Ingredient> ingredientLoader) {
        return new IngredientUse(ingredientLoader.apply(Objects.requireNonNull(item.getIngredientId())), item.getUnit(), item.getAmount());
    }

    public IngredientUse scale(int fromNumberOfPeople, int toNumberOfPeople) {
        return new IngredientUse(ingredient, unit,
                (float) (amount * (double) toNumberOfPeople / fromNumberOfPeople));
    }
}
