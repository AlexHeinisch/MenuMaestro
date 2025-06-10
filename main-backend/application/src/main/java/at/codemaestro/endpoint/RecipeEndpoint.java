package at.codemaestro.endpoint;

import at.codemaestro.exceptions.ValidationException;
import at.codemaestro.mapper.RecipeMapper;
import at.codemaestro.service.RecipeService;
import at.codemaestro.validation.RecipeValueValidationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openapitools.api.RecipesApi;
import org.openapitools.model.RecipeCreateEditDto;
import org.openapitools.model.RecipeDto;
import org.openapitools.model.RecipeListPaginatedDto;
import org.openapitools.model.RecipeVisibility;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class RecipeEndpoint implements RecipesApi {

    private final RecipeValueValidationHelper validationHelper;
    private final RecipeService recipeService;
    private final RecipeMapper recipeMapper;

    @Override
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<RecipeDto> createRecipe(RecipeCreateEditDto recipeCreateEditDto) {
        log.info("POST /recipes");
        log.debug("Request Body: {}", recipeCreateEditDto);

        validationHelper.validationForRecipeValue(recipeCreateEditDto)
                .finalize(ValidationException::fromPropertyChecker);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(recipeService.createRecipe(recipeCreateEditDto));
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') and @recipeService.isRecipeOwner(principal, #id))")
    public ResponseEntity<Void> deleteRecipeById(Long id) {
        log.info("DELETE /recipes/{}", id);

        recipeService.deleteRecipeById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') and @recipeService.isRecipeOwner(principal, #id))")
    public ResponseEntity<RecipeDto> editRecipeById(Long id, RecipeCreateEditDto recipeCreateEditDto) {
        log.info("PUT /recipes/{}", id);
        log.debug("Request Body: {}", recipeCreateEditDto);

        validationHelper.validationForRecipeValue(recipeCreateEditDto)
                .finalize(ValidationException::fromPropertyChecker);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(recipeService.editRecipeById(id, recipeCreateEditDto));
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or (@recipeService.hasAccessToRecipe(#id,principal))")
    public ResponseEntity<RecipeDto> getRecipeById(Long id) {
        log.info("GET /recipes/{}", id);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(recipeService.getRecipeById(id));
    }

    @Override
    public ResponseEntity<RecipeListPaginatedDto> getRecipes(Integer page, Integer size, String name, String description, String author, List<Long> ingredients, List<Long> requiredCookingAppliances, RecipeVisibility visibility, Pageable pageable) {
        log.info("GET /recipes");
        log.debug("Search-Params: name='{}', desc='{}', author='{}', ingredients='{}', cooking_appliances='{}', visibility={}, page={}, size={}",
            name, description, author, StringUtils.join(ingredients, ','), StringUtils.join(requiredCookingAppliances, ','),
            visibility, page, size);

        String username = "";
        boolean isAdmin = false;
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            username = SecurityContextHolder.getContext().getAuthentication().getName();
            isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        }

        Pageable p = page == null && size == null
            ? Pageable.unpaged()
            : PageRequest.of(page == null ? 0 : page, size == null ? 20 : size);
        var result = recipeMapper.mapPageable(recipeService.getRecipes(
            name,
            description,
            author,
            ingredients,
            requiredCookingAppliances,
            visibility,
            username,
            isAdmin,
            p));
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(result);
    }
}
