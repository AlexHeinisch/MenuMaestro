package dev.heinisch.menumaestro.endpoint;

import dev.heinisch.menumaestro.exceptions.ValidationException;
import dev.heinisch.menumaestro.service.MealService;
import dev.heinisch.menumaestro.validation.PropertyChecker;
import dev.heinisch.menumaestro.validation.RecipeConstraints;
import dev.heinisch.menumaestro.validation.RecipeValueValidationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.api.MealsApi;
import org.openapitools.model.MealDto;
import org.openapitools.model.MealEditDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MealEndpoint implements MealsApi {

    private final MealService mealService;
    private final RecipeValueValidationHelper validationHelper;

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') and @organizationService.hasPermissionsForMeal(#id, authentication.principal, 'PLANNER'))")
    public ResponseEntity<Void> deleteMealById(Long id) {
        log.info("DELETE /meals/{}", id);
        mealService.deleteMealById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') and @organizationService.hasPermissionsForMeal(#id, authentication.principal, 'PLANNER'))")
    public ResponseEntity<MealDto> editMealById(Long id, MealEditDto mealEditDto) {
        log.info("PATCH /meals/{}", id);
        log.info("Request-Body: {}", mealEditDto);
        validateMealEditDto(mealEditDto);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(mealService.editMealById(id, mealEditDto));
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') and @organizationService.hasPermissionsForMeal(#id, authentication.principal, 'MEMBER'))")
    public ResponseEntity<MealDto> getMealById(Long id) {
        log.info("GET /meals/{}", id);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(mealService.getMealById(id));
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') and @organizationService.hasPermissionsForMeal(#id, authentication.principal, 'PLANNER'))")
    public ResponseEntity<Void> markCompleted(Long id, Boolean done, Boolean deleteFromStash) {
        log.info("PUT /meals/{}/complete", id);
        PropertyChecker.begin().checkThat(done, "done").notNull().done().finalize(ValidationException::fromPropertyChecker);
        mealService.markCompleted(id, done,deleteFromStash);
        return ResponseEntity.noContent().build();
    }

    private void validateMealEditDto(MealEditDto dto) {
        PropertyChecker checks = PropertyChecker.begin()
                .append(RecipeConstraints.validRecipeName(dto.getName()))
                .append(RecipeConstraints.validServings(dto.getNumberOfPeople()));
        if (dto.getRecipe() != null) {
            checks.append(validationHelper.validationForRecipeValue(dto.getRecipe()));
        }
        checks
            .finalize(ValidationException::fromPropertyChecker);
    }
}
