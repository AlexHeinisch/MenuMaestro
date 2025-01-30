package at.codemaestro.service;

import at.codemaestro.domain.ingredient.IngredientUnit;
import at.codemaestro.domain.recipe.Recipe;
import at.codemaestro.domain.recipe.RecipeCookingApplianceUse;
import at.codemaestro.domain.recipe.RecipeIngredientUse;
import at.codemaestro.domain.recipe.RecipeValue;
import at.codemaestro.exceptions.ConflictException;
import at.codemaestro.exceptions.ValidationException;
import at.codemaestro.mapper.RecipeMapper;
import at.codemaestro.persistence.CookingApplianceRepository;
import at.codemaestro.persistence.ImageRepository;
import at.codemaestro.persistence.IngredientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.CookingApplianceUseCreateEditDto;
import org.openapitools.model.IngredientUseCreateEditDto;
import org.openapitools.model.RecipeCreateEditDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the creation of a new persistable RecipeValue from a Create- or Edit-dto.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeValueCreateService {

    private final RecipeMapper recipeMapper;

    private final IngredientRepository ingredientRepository;
    private final CookingApplianceRepository cookingApplianceRepository;
    private final ImageRepository imageRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public RecipeValue validateAndCreateNewRecipeValue(RecipeCreateEditDto recipeCreateEditDto) {
        validateIngredientsAndCookingAppliances(recipeCreateEditDto);
        validateRecipeImageId(recipeCreateEditDto);

        return recipeMapper.toNewRecipeValue(
                recipeCreateEditDto,
                mapIngredients(recipeCreateEditDto.getIngredients()),
                mapCookingAppliances(recipeCreateEditDto.getCookingAppliances())
        );
    }

    private void validateIngredientsAndCookingAppliances(RecipeCreateEditDto dto) {
        List<String> conflictErrorMessages = new ArrayList<>();
        dto.getIngredients().forEach(i -> {
            if (ingredientRepository.findById(i.getId()).isEmpty()) {
                conflictErrorMessages.add(String.format("Ingredient with id '%d' does not exist!", i.getId()));
            }
        });
        dto.getCookingAppliances().forEach(c -> {
            if (cookingApplianceRepository.findById(c.getId()).isEmpty()) {
                conflictErrorMessages.add(String.format("Cooking appliance with id '%d' does not exist!", c.getId()));
            }
        });
        if (dto.getIngredients()
            .stream()
            .map(IngredientUseCreateEditDto::getId)
            .distinct()
            .count() != dto.getIngredients().size()) {
            conflictErrorMessages.add("There are duplicate ingredients contained within the recipe");
        }

        if (dto.getCookingAppliances()
            .stream()
            .map(CookingApplianceUseCreateEditDto::getId)
            .distinct()
            .count() != dto.getCookingAppliances().size()) {
            conflictErrorMessages.add("There are duplicate cooking appliances contained within the recipe");
        }
        if (!conflictErrorMessages.isEmpty()) {
            throw new ConflictException("Conflict error occurred!", conflictErrorMessages);
        }
    }

    private List<RecipeIngredientUse> mapIngredients(List<IngredientUseCreateEditDto> ingredientUseCreateEditDtos) {
        return ingredientUseCreateEditDtos.stream()
                .map(
                        dto -> RecipeIngredientUse.builder()
                                .ingredient(ingredientRepository.findById(dto.getId()).orElseThrow())
                                .unit(IngredientUnit.valueOf(dto.getUnit().getValue()))
                                .amount(dto.getAmount())
                                .build()
                ).toList();
    }

    private List<RecipeCookingApplianceUse> mapCookingAppliances(List<CookingApplianceUseCreateEditDto> cookingApplianceUseCreateEditDtos) {
        return cookingApplianceUseCreateEditDtos.stream()
                .map(
                        dto -> RecipeCookingApplianceUse.builder()
                                .cookingAppliance(cookingApplianceRepository.findById(dto.getId()).orElseThrow())
                                .amount(dto.getAmount())
                                .build()
                ).toList();
    }

    private void validateRecipeImageId(RecipeCreateEditDto recipeDto) {
        if (recipeDto.getImageId() != null && !imageRepository.existsById(recipeDto.getImageId())) {
            throw new ValidationException("Recipe image not found");
        }
    }
}
