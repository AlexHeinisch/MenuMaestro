package at.codemaestro.domain.ingredient_computation;

import at.codemaestro.domain.ingredient.Ingredient;
import at.codemaestro.domain.ingredient.IngredientUnit;

import java.util.Objects;

public record IngredientUseKey(Ingredient ingredient, IngredientUnit unit) {
    public static IngredientUseKey fromIngredientUse(IngredientUse ingredientUse) {
        return new IngredientUseKey(ingredientUse.ingredient(), ingredientUse.unit());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        IngredientUseKey that = (IngredientUseKey) o;
        return unit == that.unit && Objects.equals(ingredient.getId(), that.ingredient.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingredient.getId(), unit);
    }
}
