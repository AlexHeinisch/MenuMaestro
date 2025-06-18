package dev.heinisch.menumaestro.domain.shopping_list;

import dev.heinisch.menumaestro.domain.ingredient.IngredientUnit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Invariant: (ingredientId!=NULL XOR customItemName!=NULL)
 */
@Entity
@NoArgsConstructor
@Getter
@Setter
public class ShoppingListItem {
    @Id
    @GeneratedValue(generator = "seq_shopping_list_item_id", strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(optional = false)
    @NotNull
    private ShoppingList shoppingList;

    /**
     * Foreign Key (Ingredient)
     */
    @Column
    private Long ingredientId;

    @Column
    private String customItemName;

    @Column(nullable = false)
    @NotNull
    private Double amount;

    @Column(nullable = false)
    @NotNull
    private IngredientUnit unit;

    @Column(nullable = false)
    @NotNull
    private Boolean isChecked;

    /**
     * Foreign Key (account)
     */
    @Column
    private String checkedByAccountUsername;

    @Builder
    public ShoppingListItem(ShoppingList shoppingList, Long ingredientId, String customItemName, Double amount, IngredientUnit unit, Boolean isChecked, String checkedByAccountUsername) {
        this.shoppingList = shoppingList;
        this.ingredientId = ingredientId;
        this.customItemName = customItemName;
        this.amount = amount;
        this.unit = unit;
        this.isChecked = isChecked;
        this.checkedByAccountUsername = checkedByAccountUsername;
    }
}
