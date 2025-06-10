package at.codemaestro.domain.ingredient;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Collection;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Ingredient {

    @Id
    @GeneratedValue(generator = "seq_ingredient_id", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    @NotNull
    private String name;

    @Column(nullable = false)
    @NotNull
    @Enumerated(EnumType.STRING)
    private IngredientUnit defaultUnit;

    @Column(nullable = false)
    @NotNull
    @Enumerated(EnumType.STRING)
    @Setter
    private IngredientStatus status;

    @Setter
    @Column(nullable = true)
    private String username;

    @Column(nullable = false)
    @NotNull
    @Enumerated(EnumType.STRING)
    private IngredientCategory category;

    @ManyToOne(optional = true)
    private Ingredient parent;

    @OneToMany(mappedBy = "parent")
    private Collection<Ingredient> children;

    @PreRemove
    protected void onDeleteSetNull() {
        if (children != null) {
            for (Ingredient i : children) {
                i.parent = null;
            }
        }
    }

    // add when accounts are a thing
    // @ManyToOne(optional = true)
    // private Account requester;
}
