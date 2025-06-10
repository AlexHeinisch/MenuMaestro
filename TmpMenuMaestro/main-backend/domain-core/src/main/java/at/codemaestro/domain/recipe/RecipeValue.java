package at.codemaestro.domain.recipe;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

import java.util.HashSet;
import java.util.Set;

/**
 * All information that is part of a recipe. Value Object: the identifier holds relevance only for the purpose of persisting in a relational database.
 * <br>
 * <h2>Intended use</h2>
 * RecipeValue can freely be used in transient form (i.e. not managed as a hibernate entity but a plain object),
 * or it can be persisted as part of some aggregate. This means exactly 1 persisted entity (or value object) holds a reference to this value object;
 * the reference should be marked with orphanRemoval and at least cascade delete.
 */
@Entity
@NoArgsConstructor
@Getter
public class RecipeValue {

    @Id
    @GeneratedValue(generator = "seq_recipe_data_id", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    @NotBlank
    private String name;

    @Column(nullable = false, length = 1024)
    @NotNull
    private String description;

    @Column(nullable = false)
    @Range(min = 1, max = 100)
    @NotNull
    private Integer servings;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @NotNull
    private Set<@Valid RecipeIngredientUse> ingredients;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @NotNull
    private Set<@Valid RecipeCookingApplianceUse> cookingAppliances;

    @Column(nullable = false)
    @NotNull @Setter
    private String author;

    @Column
    private String imageId;

    @Builder
    public RecipeValue(String name, int servings, String description, String author, String imageId, Set<RecipeIngredientUse> ingredients, Set<RecipeCookingApplianceUse> cookingAppliances) {
        this.name = name;
        this.servings = servings;
        this.ingredients = ingredients;
        this.cookingAppliances = cookingAppliances;
        this.description = description;
        this.author = author;
        this.imageId = imageId;
        for (RecipeIngredientUse recipeIngredientUse : ingredients) {
            recipeIngredientUse.setRecipe(this);
        }
        for (RecipeCookingApplianceUse cookingApplianceUse : cookingAppliances) {
            cookingApplianceUse.setRecipe(this);
        }
    }

    public static RecipeValue copyOf(RecipeValue existing) {
        return RecipeValue.builder()
                .name(existing.name)
                .description(existing.description)
                .author(existing.author)
                .servings(existing.servings)
                .imageId(existing.imageId)
                .ingredients(new HashSet<>(existing.ingredients.stream()
                        .map(i -> RecipeIngredientUse.builder()
                                .amount(i.getAmount())
                                .unit(i.getUnit())
                                .ingredient(i.getIngredient())
                                .build())
                        .toList()))
                .cookingAppliances(new HashSet<>(existing.cookingAppliances.stream()
                        .map(a -> RecipeCookingApplianceUse.builder()
                                .cookingAppliance(a.getCookingAppliance())
                                .amount(a.getAmount())
                                .build())
                        .toList()))
                .build();
    }
}
