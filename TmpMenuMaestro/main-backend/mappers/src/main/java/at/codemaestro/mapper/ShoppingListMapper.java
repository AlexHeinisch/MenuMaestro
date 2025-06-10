package at.codemaestro.mapper;

import at.codemaestro.domain.ingredient.Ingredient;
import at.codemaestro.domain.ingredient_computation.IngredientUse;
import at.codemaestro.domain.shopping_list.ShoppingList;
import at.codemaestro.domain.shopping_list.ShoppingListItem;
import at.codemaestro.mapper.util.BasePageableMapper;
import org.apache.commons.lang3.tuple.Pair;
import org.mapstruct.Context;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.openapitools.model.CloseShoppingListDto;
import org.openapitools.model.ShoppingListDto;
import org.openapitools.model.ShoppingListIngredientDto;
import org.openapitools.model.ShoppingListListPaginatedDto;
import org.openapitools.model.ShoppingListPreviewEntryDto;
import org.openapitools.model.ShoppingListStatus;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, uses = {IngredientMapper.class})
public interface ShoppingListMapper extends BasePageableMapper<ShoppingListListPaginatedDto, ShoppingListDto>  {

    @Mapping(target = "id", source = "right.ingredient.id")
    @Mapping(target = "unit", source = "right.unit")
    @Mapping(target = "name", source = "right.ingredient.name")
    @Mapping(target = "amount", source = "left")
    @Mapping(target = "totalAmount", source = "right.amount")
    ShoppingListPreviewEntryDto toPreviewEntryDto(Pair<Double, IngredientUse> amountsPair);

    @Mapping(target = "stashId",source = "stashId")
    @Mapping(target = "shoppingListDto",source = "shoppingListDto")
    CloseShoppingListDto toCloseShoppingListDto(Long stashId, ShoppingListDto shoppingListDto);

    List<ShoppingListPreviewEntryDto> toPreviewEntryDtos(Collection<Pair<Double, IngredientUse>> amountsPairs);

    @Mapping(target = "status", source = "isClosed")
    @Mapping(target = "ingredients", source = "items")
    ShoppingListDto toShoppingListDto(ShoppingList shoppingList, @Context Map<Long, Ingredient> ingredientsById);

    @IterableMapping(qualifiedByName = "shoppingListItem")
    List<ShoppingListIngredientDto> toShoppingListIngredientDtoList(Collection<ShoppingListItem> item, @Context Map<Long, Ingredient> ingredientsById);

    @Named("shoppingListItem")
    default ShoppingListIngredientDto toShoppingListIngredientDto(ShoppingListItem item, @Context Map<Long, Ingredient> ingredientsById) {
        if (item == null) {
            return null;
        }
        if (item.getCustomItemName() != null) {
            return toShoppingListIngredientDtoCustomItem(item);
        } else {
            return toShoppingListIngredientDtoIngredientItem(item, ingredientsById.get(item.getIngredientId()));
        }
    }

    @Named("shoppingListItemIngredient")
    @Mapping(target = "ingredient.id", source = "shoppingListItem.ingredientId")
    @Mapping(target = "ingredient.amount", source = "shoppingListItem.amount")
    @Mapping(target = "ingredient.unit", source = "shoppingListItem.unit")
    @Mapping(target = "ingredient.name", source = "ingredient.name")
    @Mapping(target = "category", source = "ingredient.category")
    @Mapping(target = "checkedBy", source = "shoppingListItem.checkedByAccountUsername")
    @Mapping(target = "id", source = "shoppingListItem.id")
    ShoppingListIngredientDto toShoppingListIngredientDtoIngredientItem(ShoppingListItem shoppingListItem, Ingredient ingredient);

    @Named("shoppingListItemCustom")
    @Mapping(target = "ingredient.id", ignore = true)
    @Mapping(target = "ingredient.amount", source = "shoppingListItem.amount")
    @Mapping(target = "ingredient.unit", source = "shoppingListItem.unit")
    @Mapping(target = "ingredient.name", source = "customItemName")
    @Mapping(target = "category", constant = "OTHER")
    @Mapping(target = "checkedBy", source = "checkedByAccountUsername")
    @Mapping(target = "id", source = "shoppingListItem.id")
    ShoppingListIngredientDto toShoppingListIngredientDtoCustomItem(ShoppingListItem shoppingListItem);

    List<ShoppingListDto> toShoppingListDtoList(List<ShoppingList> shoppingLists, @Context Map<Long, Ingredient> ingredientsById);

    default ShoppingListStatus toShoppingListStatus(boolean isClosed) {
        return isClosed ? ShoppingListStatus.CLOSED : ShoppingListStatus.OPEN;
    }

    default Boolean toIsClosed(ShoppingListStatus status) {
        if (status == null) {
            return null;
        }
        return ShoppingListStatus.CLOSED.equals(status);
    }

    default OffsetDateTime toOffsetDateTime(Instant val) {
        if (val == null) {
            return null;
        }
        return val.atOffset(ZoneOffset.UTC);
    }
}
