package dev.heinisch.menumaestro.mapper;

import dev.heinisch.menumaestro.domain.menu.Meal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.openapitools.model.MealDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, uses = RecipeMapper.class)
public interface MealMapper {

    @Mapping(target = "organizationId", source = "menu.organizationId")
    MealDto toMealDto(Meal meal);
}
