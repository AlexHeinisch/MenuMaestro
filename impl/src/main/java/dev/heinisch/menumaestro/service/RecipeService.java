package dev.heinisch.menumaestro.service;

import dev.heinisch.menumaestro.domain.recipe.Recipe;
import dev.heinisch.menumaestro.domain.recipe.RecipeValue;
import dev.heinisch.menumaestro.domain.recipe.RecipeVisibility;
import dev.heinisch.menumaestro.exceptions.ForbiddenException;
import dev.heinisch.menumaestro.exceptions.NotFoundException;
import dev.heinisch.menumaestro.mapper.RecipeMapper;
import dev.heinisch.menumaestro.persistence.AccountRepository;
import dev.heinisch.menumaestro.persistence.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.OrganizationSummaryDto;
import org.openapitools.model.RecipeCreateEditDto;
import org.openapitools.model.RecipeDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.heinisch.menumaestro.domain.recipe.RecipeVisibility.PUBLIC;

/**
 * Implements basic CRUD functionality for Recipes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeMapper recipeMapper;
    private final RecipeValueCreateService recipeValueCreateService;

    private final RecipeRepository recipeRepository;

    private final AccountRepository accountRepository;
    private final OrganizationService organizationService;

    @Transactional
    public RecipeDto createRecipe(RecipeCreateEditDto recipeCreateEditDto) {
        RecipeValue recipeValue = recipeValueCreateService.validateAndCreateNewRecipeValue(recipeCreateEditDto);
        Recipe recipe = Recipe.builder()
                .visibility(RecipeVisibility.valueOf(recipeCreateEditDto.getVisibility().getValue()))
                .recipeValue(recipeValue
                )
                .build();
        return recipeMapper.toRecipeDto(recipeRepository.save(recipe));
    }

    @Transactional(readOnly = true)
    public RecipeDto getRecipeById(Long id) {
        return recipeRepository.findById(id)
                .map(recipeMapper::toRecipeDto)
                .orElseThrow(() -> new NotFoundException(String.format("The recipe with id '%d' was not found!", id)));
    }

    @Transactional(readOnly = true)
    public Page<RecipeDto> getRecipes(
            String name,
            String description,
            String author,
            List<Long> ingredients,
            List<Long> requiredCookingAppliances,
            org.openapitools.model.RecipeVisibility visibility,
            String username,
            boolean isAdmin,
            Pageable pageable) {
        Set<Long> ingredientIds = ingredients != null && !ingredients.isEmpty()
                ? new HashSet<>(ingredients)
                : null;
        Set<Long> cookingApplianceIds = requiredCookingAppliances != null && !requiredCookingAppliances.isEmpty()
                ? new HashSet<>(requiredCookingAppliances)
                : null;
        Set<Long> orgIds = new HashSet<>();

        if (!username.isEmpty()) {
            orgIds = organizationService.getOrganizationsByUsernameAndNameSubstring(username, "", pageable).stream().map(OrganizationSummaryDto::getId).collect(Collectors.toSet());
        }
        return recipeRepository.findByMultipleValues(name, description, author, ingredientIds, cookingApplianceIds, visibility == null ? null : RecipeVisibility.valueOf(visibility.getValue()), username, orgIds, isAdmin, pageable)
                .map(recipeMapper::toRecipeDto);
    }

    @Transactional
    public void deleteRecipeById(Long id) {
        // TBD discuss constraints and when not to allow deletion
        Recipe recipe = recipeRepository.findById(id).orElseThrow(
                () -> new NotFoundException(String.format("Recipe with id '%d' not found!", id))
        );
        recipeRepository.delete(recipe);
    }

    @Transactional(readOnly = true)
    public boolean hasAccessToRecipe(Long recipeId, String username) {
        Optional<Recipe> recipe = recipeRepository.findById(recipeId);
        if (recipe.isEmpty()) {
            return true; // let validation handle not found!
        }
        if (recipe.get().getVisibility() == PUBLIC) {
            return true;
        }
        if (accountRepository.findById(recipe.get().getRecipeValue().getAuthor()).isEmpty()) {
            log.error("Author not found when trying to check access to recipe!");
            throw new ForbiddenException("Could not find author account!? Should not happen!");
        }
        if (accountRepository.findById(username).isEmpty()) {
            log.error("User not found when trying to check access to recipe!");
            throw new ForbiddenException("Could not find principal account!? Should not happen!");
        }
        switch (recipe.get().getVisibility()) {
            case PRIVATE: {
                if (!recipe.get().getRecipeValue().getAuthor().equals(username)) {
                    throw new ForbiddenException(String.format("Account '%s' is not the author of this private recipe!", username));
                }
                break;
            }
            case ORGANIZATION: {
                if (!organizationService.doUsersShareOrganization(username, recipe.get().getRecipeValue().getAuthor())) {
                    throw new ForbiddenException(String.format("Account '%s' does not share an organization with the author of the organization recipe!", username));
                }
                break;
            }
        }
        return true;
    }

    @Transactional
    public RecipeDto editRecipeById(Long id, RecipeCreateEditDto recipeCreateEditDto) {
        Recipe recipe = recipeRepository.findById(id).orElseThrow(
                () -> new NotFoundException(String.format("Recipe with id '%d' not found!", id))
        );
        RecipeValue newRecipeValue = recipeValueCreateService.validateAndCreateNewRecipeValue(recipeCreateEditDto);
        recipe.setRecipeValue(newRecipeValue);
        recipe.setVisibility(RecipeVisibility.valueOf(recipeCreateEditDto.getVisibility().getValue()));

        return recipeMapper.toRecipeDto(recipe);
    }

    public boolean isRecipeOwner(String username, Long recipeId) {
        return recipeRepository.findById(recipeId)
                .map(value -> value.getRecipeValue().getAuthor().equals(username))
                .orElse(true);
    }
}
