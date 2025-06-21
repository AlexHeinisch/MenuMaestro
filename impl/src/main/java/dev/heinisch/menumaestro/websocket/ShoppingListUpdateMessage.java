package dev.heinisch.menumaestro.websocket;

import dev.heinisch.menumaestro.domain.ingredient.IngredientCategory;
import dev.heinisch.menumaestro.domain.ingredient.IngredientUnit;
import dev.heinisch.menumaestro.domain.shopping_list.ShoppingListItem;
import lombok.Builder;

@Builder
public record ShoppingListUpdateMessage(ShoppingListUpdateType updateType, Long shoppingListItemId, String customName,
                                        String name, IngredientUnit unit, Double amount, Boolean isChecked,
                                        String checkedBy, IngredientCategory category) {

    public static ShoppingListUpdateMessage update(ShoppingListItem item) {
        return ShoppingListUpdateMessage.builder()
            .checkedBy(item.getCheckedByAccountUsername())
            .isChecked(item.getIsChecked())
            .shoppingListItemId(item.getId())
            .amount(item.getAmount())
            .customName(null)
            .name(null)
            .category(null)
            .unit(item.getUnit())
            .updateType(ShoppingListUpdateType.MODIFY)
            .build();
    }

}
