package dev.heinisch.menumaestro.mapper;

import dev.heinisch.menumaestro.domain.ingredient.Ingredient;
import dev.heinisch.menumaestro.domain.ingredient.IngredientUnit;
import dev.heinisch.menumaestro.domain.ingredient_computation.IngredientUse;
import dev.heinisch.menumaestro.mapper.util.BasePageableMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.openapitools.model.*;
import org.springframework.data.domain.Page;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface IngredientMapper extends BasePageableMapper<IngredientListPaginatedDto, IngredientDto> {

    @Mapping(target = "totalElements", source = "totalElements")
    IngredientWithCategoryListPaginatedDto mapPageableIngredientWithCategory(Page<IngredientDtoWithCategory> page);

    @Mapping(target = "username",source = "username")
    IngredientDtoWithCategory toIngredientWithCategoryDto(Ingredient ingredient);

    IngredientDto toIngredientDto(Ingredient ingredient);

    List<IngredientUseDto> toIngredientDtoList(Collection<IngredientUse> ingredientUses);

    @Mapping(target = "id", source = "ingredient.id")
    @Mapping(target = "name", source = "ingredient.name")
    IngredientUseDto toIngredientUseDto(IngredientUse ingredientUse);

    IngredientUnit toIngredientUnit(IngredientUnitDto ingredientUnitDto);

    IngredientUnitDto toIngredientUnitDto(IngredientUnit ingredientUnit);
}
