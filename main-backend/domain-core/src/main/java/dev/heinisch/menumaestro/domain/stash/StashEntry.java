package dev.heinisch.menumaestro.domain.stash;

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
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class StashEntry {

    @Id
    @EmbeddedId
    private StashEntryId id;

    @ManyToOne(optional = false)
    @MapsId("stashId")
    @NotNull
    private Stash stash;

    @Column(nullable = false)
    @NotNull
    private Double amount;

    @Data
    @Embeddable
    public static class StashEntryId {

        @Column
        private Long stashId;

        @Column
        private Long ingredientId;

        @Enumerated(EnumType.STRING)
        @Column
        private IngredientUnit unit;
    }

    @Builder
    public StashEntry(Stash stash, Long ingredientId, IngredientUnit unit, Double amount) {
        this.stash = stash;
        this.id = new StashEntryId();
        this.id.setIngredientId(ingredientId);
        this.id.setUnit(unit);
        if (stash != null) {
            this.id.stashId = stash.getId();
        }
        this.amount = amount;
    }

    public Long getIngredientId() {
        return id.getIngredientId();
    }

    public IngredientUnit getUnit() {
        return id.getUnit();
    }
}
