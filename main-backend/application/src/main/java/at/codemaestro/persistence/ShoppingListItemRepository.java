package at.codemaestro.persistence;

import at.codemaestro.domain.shopping_list.ShoppingListItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShoppingListItemRepository extends JpaRepository<ShoppingListItem, Long> {

    List<ShoppingListItem> findShoppingListItemsByCheckedByAccountUsername(String username);
}
