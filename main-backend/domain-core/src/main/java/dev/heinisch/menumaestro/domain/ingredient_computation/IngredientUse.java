package dev.heinisch.menumaestro.domain.ingredient_computation;

import dev.heinisch.menumaestro.domain.ingredient.Ingredient;
import dev.heinisch.menumaestro.domain.ingredient.IngredientUnit;
import dev.heinisch.menumaestro.domain.recipe.RecipeIngredientUse;
import dev.heinisch.menumaestro.domain.shopping_list.ShoppingListItem;
import dev.heinisch.menumaestro.domain.stash.StashEntry;

import java.util.Objects;
import java.util.function.Function;

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
