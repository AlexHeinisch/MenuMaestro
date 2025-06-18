package dev.heinisch.menumaestro.domain.recipe;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The recipe entity. Allows to store a RecipeValue on its own, without the context of e.g. an event.
 */
@Entity
@NoArgsConstructor
@Getter
@Setter
public class Recipe {
    @Id
    @GeneratedValue(generator = "seq_recipe_id", strategy = GenerationType.SEQUENCE)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @NotNull
    @Valid
    private RecipeValue recipeValue;

    @Column(nullable = false)
    @NotNull
    @Enumerated(EnumType.STRING)
    private RecipeVisibility visibility;

    @Builder
    public Recipe(Long id, RecipeValue recipeValue, RecipeVisibility visibility) {
        this.id = id;
        this.recipeValue = recipeValue;
        this.visibility = visibility;
    }
}
