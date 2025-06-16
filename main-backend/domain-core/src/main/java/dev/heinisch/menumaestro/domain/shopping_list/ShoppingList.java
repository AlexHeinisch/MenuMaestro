package dev.heinisch.menumaestro.domain.shopping_list;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class ShoppingList {
    @Id
    @GeneratedValue(generator = "seq_shopping_list_id", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    @NotNull
    @Length(max = 1000)
    private String name;

    @Column(nullable = false)
    @NotNull
    private Boolean isClosed;

    @Column(nullable = false)
    @NotNull
    private Long organizationId;

    @OneToMany(mappedBy = "shoppingList", cascade = CascadeType.ALL, orphanRemoval = true)
    @NotNull
    private Set<ShoppingListItem> items;

    @Column(nullable = false)
    @NotNull
    private Long menuId;

    @Builder
    public ShoppingList(String name, Boolean isClosed, Long organizationId, Set<ShoppingListItem> items, Long menuId) {
        this.name = name;
        this.isClosed = isClosed;
        this.menuId=menuId;
        this.organizationId = organizationId;
        this.items = items;
        for (ShoppingListItem item : items) {
            item.setShoppingList(this);
        }
    }

    public void addItem(ShoppingListItem item) {
        this.getItems().add(item);
    }
}
