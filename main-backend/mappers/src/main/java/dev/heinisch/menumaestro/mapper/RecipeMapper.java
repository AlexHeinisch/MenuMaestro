package dev.heinisch.menumaestro.mapper;

import dev.heinisch.menumaestro.domain.recipe.Recipe;
import dev.heinisch.menumaestro.domain.recipe.RecipeCookingApplianceUse;
import dev.heinisch.menumaestro.domain.recipe.RecipeIngredientUse;
import dev.heinisch.menumaestro.domain.recipe.RecipeValue;
import dev.heinisch.menumaestro.domain.recipe.RecipeVisibility;
import dev.heinisch.menumaestro.mapper.util.BasePageableMapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.openapitools.model.CookingApplianceUseDto;
import org.openapitools.model.IngredientUseDto;
import org.openapitools.model.RecipeCreateEditDto;
import org.openapitools.model.RecipeDto;
import org.openapitools.model.RecipeListPaginatedDto;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class RecipeMapper implements BasePageableMapper<RecipeListPaginatedDto, RecipeDto> {

    @Autowired
    protected ImageUrlMapper imageUrlMapper;

    @Named("toNewRecipeValue")
    @Mapping(target = "ingredients", source = "ingredientUseList")
    @Mapping(target = "cookingAppliances", source = "cookingApplianceUseList")
    public abstract RecipeValue toNewRecipeValue(RecipeCreateEditDto recipeValue, List<RecipeIngredientUse> ingredientUseList, List<RecipeCookingApplianceUse> cookingApplianceUseList);

    @Mapping(target = "name", source = "recipeValue.name")
    @Mapping(target = "servings", source = "recipeValue.servings")
    @Mapping(target = "ingredients", source = "recipeValue.ingredients")
    @Mapping(target = "description", source = "recipeValue.description")
    @Mapping(target = "author", source = "recipeValue.author")
    @Mapping(target = "cookingAppliances", source = "recipeValue.cookingAppliances")
    @Mapping(target = "imageId", source = "recipeValue.imageId")
    @Mapping(target = "imageLink", expression = "java(imageUrlMapper.mapImageUrlFromImageId(recipe.getRecipeValue().getImageId()))")
    public abstract RecipeDto toRecipeDto(Recipe recipe);

    @Mapping(target = "visibility", constant = "PRIVATE")
    @Mapping(target = "imageLink", expression = "java(imageUrlMapper.mapImageUrlFromImageId(recipeValue.getImageId()))")
    public abstract RecipeDto toRecipeDto(RecipeValue recipeValue);

    @Mapping(target = "id", source = "ingredientId")
    @Mapping(target = "name", source = "ingredient.name")
    public abstract IngredientUseDto toIngredientUseDto(RecipeIngredientUse recipeIngredientUse);

    @Mapping(target = "id", source = "cookingApplianceId")
    @Mapping(target = "name", source = "cookingAppliance.name")
    public abstract CookingApplianceUseDto toCookingApplianceUseDto(RecipeCookingApplianceUse recipeCookingApplianceUse);

    public abstract RecipeVisibility toRecipeVisibility(org.openapitools.model.RecipeVisibility visibility);

    @AfterMapping
    void afterMappingToDto(@MappingTarget RecipeDto recipeDto) {
        recipeDto.getCookingAppliances().sort(Comparator.comparing(CookingApplianceUseDto::getName));
        recipeDto.getIngredients().sort(Comparator.comparing(IngredientUseDto::getName));
    }
}
