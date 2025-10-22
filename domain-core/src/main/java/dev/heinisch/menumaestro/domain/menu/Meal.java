package dev.heinisch.menumaestro.domain.menu;

import dev.heinisch.menumaestro.domain.recipe.RecipeValue;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Meal extends MenuItem {

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @NotNull
    private RecipeValue recipe;

    @Column(nullable = false)
    @NotNull
    private String name;

    @Column(length = 4096)
    private String description;

    @Column
    @Positive
    @NotNull
    private Integer numberOfPeople;

    @Column
    @NotNull
    @Setter
    private Boolean isDone;

    @Transient
    @Setter(AccessLevel.PACKAGE)
    private MealStatus status;

    @Builder
    public Meal(Long id, String name, String description, Menu menu, Integer position, RecipeValue recipe, Integer numberOfPeople, Boolean isDone) {
        super(id, menu, position);
        this.recipe = recipe;
        this.name = name;
        this.description = description;
        this.numberOfPeople = numberOfPeople;
        this.isDone = isDone;
    }
}
