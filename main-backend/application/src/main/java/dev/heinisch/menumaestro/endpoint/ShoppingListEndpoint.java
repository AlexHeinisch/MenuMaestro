package dev.heinisch.menumaestro.endpoint;

import dev.heinisch.menumaestro.exceptions.ConflictException;
import dev.heinisch.menumaestro.exceptions.ValidationException;
import dev.heinisch.menumaestro.mapper.ShoppingListMapper;
import dev.heinisch.menumaestro.validation.PropertyChecker;

import java.util.List;

import dev.heinisch.menumaestro.validation.ShoppingListConstraints;
import io.micrometer.common.util.StringUtils;
import org.openapitools.api.ShoppingListApi;
import org.openapitools.model.*;
import org.openapitools.model.ShoppingListCreateDto;
import org.openapitools.model.ShoppingListDto;
import org.openapitools.model.ShoppingListEditDto;
import org.openapitools.model.ShoppingListStatus;
import org.openapitools.model.ShoppingListTokenDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import dev.heinisch.menumaestro.service.ShoppingListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ShoppingListEndpoint implements ShoppingListApi {

    private final ShoppingListService shoppingListService;
    private final ShoppingListMapper shoppingListMapper;

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') and @organizationService.hasPermissionForOrganization(#shoppingListCreateDto.organizationId, principal, 'PLANNER'))")
    public ResponseEntity<ShoppingListDto> createShoppingList(ShoppingListCreateDto shoppingListCreateDto) {
        log.info("POST /shopping-lists");
        log.debug("Request-Body: {}", shoppingListCreateDto);
        validateCreateDto(shoppingListCreateDto);
        return ResponseEntity.ok(shoppingListService.createShoppingList(shoppingListCreateDto));
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') and @organizationService.hasPermissionsForShoppingList(#id, principal, 'SHOPPER'))")
    public ResponseEntity<ShoppingListTokenDto> createShoppingListToken(Long id) {
        log.info("POST /shopping-lists/{}/token", id);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(shoppingListService.generateAccessToken(id));
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or @jwtService.isValidShoppingListToken(#id, #token) or (hasRole('ROLE_USER') and @organizationService.hasPermissionsForShoppingList(#id, principal, 'MEMBER'))")
    public ResponseEntity<ShoppingListDto> getShoppingListById(Long id, String token) {
        log.info("GET /shopping-lists/{}?token={}", id, StringUtils.isBlank(token) ? "<null>" : "<present>");
        return ResponseEntity.ok(shoppingListService.getShoppingListById(id));
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or @jwtService.isValidShoppingListToken(#id, #token) or (hasRole('ROLE_USER') and @organizationService.hasPermissionsForShoppingList(#id, principal, 'SHOPPER'))")
    public ResponseEntity<ShoppingListDto> editShoppingList(Long id, ShoppingListEditDto shoppingListEditDto, String token) {
        log.info("PATCH /shopping-lists/{}?token={}", id, StringUtils.isBlank(token) ? "<null>" : "<present>");
        log.debug("Request-Body: {}", shoppingListEditDto);
        return ResponseEntity.status(HttpStatus.OK).body(shoppingListService.editShoppingList(id, shoppingListEditDto));
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or @jwtService.isValidShoppingListToken(#id, #token) or (hasRole('ROLE_USER') and @organizationService.hasPermissionsForShoppingList(#id, principal, 'SHOPPER'))")
    public ResponseEntity<ShoppingListDto> addItemToShoppingList(Long id, ShoppingListIngredientAddDto shoppingListIngredientAddDto, String token) {
        log.info("POST /shopping-lists/{}/items?token={}", id, StringUtils.isBlank(token) ? "<null>" : "<present>");

        validateShoppingListIngredientAddDto(shoppingListIngredientAddDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(shoppingListService.addItemToShoppingList(id, shoppingListIngredientAddDto));
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<ShoppingListListPaginatedDto> searchShoppingLists(Integer page, Integer size, String name, ShoppingListStatus status, Long menuId) {
        log.info("GET /shopping-lists");
        log.debug("Search-Params: name={} status={} menuId={} page={} size={}", name, status, menuId, page, size);

        Pageable p = page == null && size == null
                ? Pageable.unpaged()
                : PageRequest.of(page == null ? 0 : page, size == null ? 20 : size);

        if (SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            ShoppingListListPaginatedDto result = shoppingListMapper.mapPageable(shoppingListService.searchShoppingListsAsAdmin(name, status, menuId, p));
            return ResponseEntity.ok(result);
        }
        ShoppingListListPaginatedDto result = shoppingListMapper.mapPageable(shoppingListService.searchShoppingLists(name, status, menuId, SecurityContextHolder.getContext().getAuthentication().getName(), p));
        return ResponseEntity.ok(result);
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') and @organizationService.hasPermissionForOrganization(#shoppingListCreateDto.organizationId, principal, 'PLANNER'))")
    public ResponseEntity<List<ShoppingListPreviewEntryDto>> getShoppingListPreview(ShoppingListCreateDto shoppingListCreateDto) {
        log.info("POST /shopping-lists");
        log.debug("Request-Body: {}", shoppingListCreateDto);
        validateCreateDto(shoppingListCreateDto);
        return ResponseEntity.ok(shoppingListService.getShoppingListPreviewData(shoppingListCreateDto));
    }

    private void validateShoppingListIngredientAddDto(ShoppingListIngredientAddDto dto) {
        PropertyChecker.begin()
            .checkThat(dto.getAmount(), "amount").notNull().positive().done()
            .checkThat(dto.getUnit(), "unit").notNull().done()
            .finalize(ValidationException::fromPropertyChecker);

        if ((dto.getExistingIngredientId() == null
            && dto.getCustomIngredientName() == null)
            || (dto.getExistingIngredientId() != null
            && dto.getCustomIngredientName() != null)) {
            throw new ConflictException("Either an existing ingredient ID or a custom ingredient name must be provided, but not both!");
        }
    }
    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') and @organizationService.hasPermissionsForShoppingList(#id, principal, 'SHOPPER'))")
    public ResponseEntity<CloseShoppingListDto> closeShoppingList(Long id) {
        return ResponseEntity.ok(shoppingListService.closeShoppingList(id));
    }

    private void validateCreateDto(ShoppingListCreateDto dto) {
        PropertyChecker.begin()
            .append(ShoppingListConstraints.validShoppingListName(dto.getName()))
            .checkThat(dto.getMenuId(), "menuId").notNull().done()
            .checkThat(dto.getOrganizationId(), "organizationId").notNull().done()
            .checkThat(dto.getSnapshotIds(), "snapshotIds").notNull()
            .forEach((c, i) -> c.checkThat(i, "snapshotIds[].id").notNull())
            .done()
            .finalize(ValidationException::fromPropertyChecker);
    }
}
