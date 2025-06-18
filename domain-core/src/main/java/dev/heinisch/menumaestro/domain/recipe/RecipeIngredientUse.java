package dev.heinisch.menumaestro.domain.recipe;

import dev.heinisch.menumaestro.domain.ingredient.Ingredient;
import dev.heinisch.menumaestro.domain.ingredient.IngredientUnit;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Describes an ingredient used within a recipe. Value object, part of {@link RecipeValue}.
 */
@Entity
@Getter
@NoArgsConstructor
public class RecipeIngredientUse {

    @Id
    @EmbeddedId
    @NotNull
    @Valid
    private RecipeIngredientUseId id;

    @ManyToOne
    @MapsId("recipeId")
    private RecipeValue recipe;

    @ManyToOne
    @MapsId("ingredientId")
    private Ingredient ingredient;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private IngredientUnit unit;

    @Column(nullable = false)
    @NotNull
    private Float amount;

    @Embeddable
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    @Getter
    public static class RecipeIngredientUseId {
        /**
         * May be null at create-time
         */
        private Long recipeId;

        @NotNull
        private Long ingredientId;
    }

    @Builder
    public RecipeIngredientUse(RecipeValue recipeValue, Ingredient ingredient, Float amount, IngredientUnit unit) {
        this.id = new RecipeIngredientUseId(recipeValue != null ? recipeValue.getId() : null, ingredient.getId());
        this.ingredient = ingredient;
        this.amount = amount;
        this.unit = unit;
        this.recipe = recipeValue;
    }

    void setRecipe(RecipeValue recipe) {
        this.recipe = recipe;
        this.id.recipeId = recipe.getId();
    }

    public Long getIngredientId() {
        return id.getIngredientId();
    }
}
