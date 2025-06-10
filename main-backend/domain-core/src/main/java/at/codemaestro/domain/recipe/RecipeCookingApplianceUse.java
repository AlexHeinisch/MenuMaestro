package at.codemaestro.domain.recipe;

import at.codemaestro.domain.cooking_appliance.CookingAppliance;
import at.codemaestro.domain.ingredient.IngredientUnit;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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

@Entity
@Getter
@NoArgsConstructor
public class RecipeCookingApplianceUse {

    @Id
    @EmbeddedId
    @NotNull
    @Valid
    private RecipeCookingApplianceUseId id;

    @ManyToOne
    @MapsId("recipeId")
    private RecipeValue recipe;

    @ManyToOne
    @MapsId("cookingApplianceId")
    private CookingAppliance cookingAppliance;

    @Column(nullable = false)
    @NotNull
    private Integer amount;

    @Embeddable
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    @Getter
    public static class RecipeCookingApplianceUseId {
        /**
         * May be null at create-time
         */
        private Long recipeId;

        @NotNull
        private Long cookingApplianceId;
    }

    @Builder
    public RecipeCookingApplianceUse(RecipeValue recipe, CookingAppliance cookingAppliance, int amount) {
        this.id = new RecipeCookingApplianceUseId(recipe != null ? recipe.getId() : null, cookingAppliance.getId());
        this.amount = amount;
        this.recipe = recipe;
        this.cookingAppliance = cookingAppliance;
    }

    void setRecipe(RecipeValue recipe) {
        this.recipe = recipe;
        this.id.recipeId = recipe.getId();
    }

    public Long getCookingApplianceId() {
        return id.getCookingApplianceId();
    }
}
