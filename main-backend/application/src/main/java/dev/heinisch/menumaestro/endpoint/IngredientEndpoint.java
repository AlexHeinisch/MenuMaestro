package dev.heinisch.menumaestro.endpoint;

import dev.heinisch.menumaestro.exceptions.ValidationException;
import dev.heinisch.menumaestro.mapper.IngredientMapper;
import dev.heinisch.menumaestro.service.IngredientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openapitools.api.IngredientsApi;
import org.openapitools.model.CreateIngredientDto;
import org.openapitools.model.IngredientDto;
import org.openapitools.model.IngredientListPaginatedDto;
import org.openapitools.model.IngredientWithCategoryListPaginatedDto;
import org.openapitools.model.ReplaceIngredientRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class IngredientEndpoint implements IngredientsApi {

    private final IngredientService ingredientService;

    private final IngredientMapper ingredientMapper;


    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<IngredientDto> approveIngredient(Long ingredientId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ingredientService.approveIngredient(ingredientId));
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteIngredient(Long ingredientId) {

        if (ingredientService.findIngredientById(ingredientId)==null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
        ingredientService.deleteIngredient(ingredientId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<IngredientWithCategoryListPaginatedDto> ingredientSuggestions(Integer page, Integer size, List<String> sort) {
        Pageable p = page == null && size == null
                ? Pageable.unpaged()
                : PageRequest.of(page == null ? 0 : page, size == null ? 20 : size);

        var result = ingredientMapper.mapPageableIngredientWithCategory(ingredientService.findAllRequestedIngredients(p));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(result);
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<IngredientDto> replaceIngredient(Long ingredientId, ReplaceIngredientRequest replaceIngredientRequest) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ingredientService.replaceIngredient(ingredientId,replaceIngredientRequest));
    }
    @Override
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<IngredientDto> suggestIngredient(CreateIngredientDto createIngredientDto) {
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ingredientService.suggestIngredientAsAdmin(createIngredientDto));
        }
        IngredientDto ingredientDto= ingredientService.suggestIngredient(createIngredientDto,SecurityContextHolder.getContext().getAuthentication().getName());
        if (ingredientDto==null){
            throw new ValidationException("Before sending another new ingredient request, you have to first wait that the admin processes some of your other requests. Please try again later.");

        }
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ingredientDto);
    }
    @Override
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN') or @jwtService.isValidShoppingListToken(#token)")
    public ResponseEntity<IngredientListPaginatedDto> searchIngredients(Integer page, Integer size, List<String> sort, String name, String token) {
        log.info("GET /ingredients?token={}", StringUtils.isBlank(token) ? "<null>" : "<present>");
        log.debug("Search-Params: name='{}' page={}, size={}", name, page, size);
        Pageable p = page == null && size == null
            ? Pageable.unpaged()
            : PageRequest.of(page == null ? 0 : page, size == null ? 20 : size);
        String username=null;
        if(SecurityContextHolder.getContext()!=null) {
            username=SecurityContextHolder.getContext().getAuthentication().getName();
        }

        var result = ingredientMapper.mapPageable(ingredientService.searchIngredients(name, p,username));
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(result);
    }

}
