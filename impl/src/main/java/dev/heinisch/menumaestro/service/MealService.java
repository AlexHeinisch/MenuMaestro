package dev.heinisch.menumaestro.service;

import dev.heinisch.menumaestro.domain.ingredient_computation.IngredientComputationService;
import dev.heinisch.menumaestro.domain.ingredient_computation.IngredientUse;
import dev.heinisch.menumaestro.domain.menu.Meal;
import dev.heinisch.menumaestro.domain.menu.Menu;
import dev.heinisch.menumaestro.domain.menu.MenuComputationService;
import dev.heinisch.menumaestro.domain.recipe.RecipeValue;
import dev.heinisch.menumaestro.domain.menu.MenuStatus;
import dev.heinisch.menumaestro.exceptions.ConflictException;
import dev.heinisch.menumaestro.exceptions.NotFoundException;
import dev.heinisch.menumaestro.exceptions.ValidationException;
import dev.heinisch.menumaestro.mapper.MealMapper;
import dev.heinisch.menumaestro.persistence.IngredientRepository;
import dev.heinisch.menumaestro.persistence.MealRepository;
import dev.heinisch.menumaestro.persistence.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.MealDto;
import org.openapitools.model.MealEditDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MealService {

    private final MealRepository mealRepository;
    private final MenuRepository menuRepository;
    private final MealMapper mealMapper;
    private final MenuComputationService menuComputationService;
    private final IngredientRepository ingredientRepository;
    private final StashService stashService;
    private final RecipeValueCreateService recipeValueCreateService;
    private final IngredientComputationService ingredientComputationService;

    @Transactional(readOnly = true)
    public MealDto getMealById(Long id) {
        Meal meal = mealRepository.findById(id).orElseThrow(() -> new NotFoundException(String.format("Meal with id '%d' not found!", id)));


        menuComputationService.computeMetadata(meal.getMenu(), iid -> ingredientRepository.findById(iid).orElseThrow());
        return mealMapper.toMealDto(meal);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void markCompleted(Long id, Boolean done, Boolean deleteFromStash) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Meal with id '%d' not found!", id)));

        if (meal.getMenu().getStatus() == MenuStatus.CLOSED) {
            throw new ValidationException("Meal cannot be changed, the menu it belongs to was closed!");
        }

        if (meal.getIsDone().equals(done)) {
            throw new ConflictException("Meal with id " + id + " already in state done=" + done + "!");
        }
        if (deleteFromStash && done){
            Menu menu = meal.getMenu();
            List<IngredientUse> ingredients =
             ingredientComputationService.ingredientsOfOpenMeals(List.of(meal))
                    .map(i -> i.scale(1, -1)).toList();
            stashService.addToStash(menu.getStash().getId(),ingredients);
        }
        meal.setIsDone(done);
    }

    @Transactional
    public void deleteMealById(Long id) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Meal with id '%d' not found!", id)));
        Menu menu = menuRepository.findByMealId(id);
        menu.removeMeal(meal);
    }

    @Transactional
    public MealDto editMealById(Long id, MealEditDto mealEditDto) {
        Meal meal = mealRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(String.format("Meal with id '%d' not found!", id)));

        if (meal.getMenu().getStatus() == MenuStatus.CLOSED) {
            throw new ValidationException("Meal cannot be changed, menu it belongs to was closed!");
        }

        meal.setName(mealEditDto.getName());
        meal.setNumberOfPeople(mealEditDto.getNumberOfPeople());

        if (mealEditDto.getRecipe() != null) {
            RecipeValue recipeValue = recipeValueCreateService.validateAndCreateNewRecipeValue(mealEditDto.getRecipe());
            meal.setRecipe(recipeValue);
        }
        menuComputationService.computeMetadata(meal.getMenu(), iid -> ingredientRepository.findById(iid).orElseThrow());
        return mealMapper.toMealDto(mealRepository.save(meal));
    }
}
