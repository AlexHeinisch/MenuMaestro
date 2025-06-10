package at.codemaestro.mapper;

import at.codemaestro.domain.ingredient.Ingredient;
import at.codemaestro.domain.stash.Stash;
import at.codemaestro.domain.stash.StashEntry;
import at.codemaestro.domain.stash.projections.StashIdName;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.openapitools.model.IngredientUseDto;
import org.openapitools.model.StashResponseDto;
import org.openapitools.model.StashSearchResponseDto;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface StashMapper {
    @Mapping(target = "ingredients", source = "stash.entries")
    @Mapping(target = "name", source = "menuName")
    @Mapping(target = "correspondingOrganizationId", source = "correspondingOrgId")
    StashResponseDto toStashResponseDto(Stash stash, String menuName, Long correspondingOrgId, @Context Map<Long, Ingredient> ingredientsById);

    default IngredientUseDto toIngredientUseDto(StashEntry stashEntry, @Context Map<Long, Ingredient> ingredientsById) {
        return toIngredientUseDto(stashEntry, ingredientsById.get(stashEntry.getIngredientId()));
    }

    @Mapping(target = "id", source = "stashEntry.ingredientId")
    @Mapping(target = "unit", source = "stashEntry.unit")
    @Mapping(target = "amount", source = "stashEntry.amount")
    @Mapping(target = "name", source = "ingredient.name")
    IngredientUseDto toIngredientUseDto(StashEntry stashEntry, Ingredient ingredient);

    StashSearchResponseDto toStashSearchDto(StashIdName stash);
    List<StashSearchResponseDto> toStashSearchDtoList(List<StashIdName> list);
}
