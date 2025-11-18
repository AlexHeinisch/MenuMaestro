package dev.heinisch.menumaestro.endpoint;

import dev.heinisch.menumaestro.exceptions.ValidationException;
import dev.heinisch.menumaestro.mapper.MenuMapper;
import dev.heinisch.menumaestro.service.MenuService;
import dev.heinisch.menumaestro.validation.MenuConstraints;
import dev.heinisch.menumaestro.validation.PropertyChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.api.MenusApi;
import org.openapitools.model.AddMealToMenuRequest;
import org.openapitools.model.MenuCreateDto;
import org.openapitools.model.MenuDetailDto;
import org.openapitools.model.MenuStatus;
import org.openapitools.model.MenuSummaryDto;
import org.openapitools.model.MenuSummaryListPaginatedDto;
import org.openapitools.model.SnapshotCreateDto;
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
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class MenuEndpoint implements MenusApi {

    private final MenuService menuService;
    private final MenuMapper menuMapper;

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') and @organizationService.hasPermissionsForMenu(#id, authentication.principal, 'PLANNER') and @recipeService.hasAccessToRecipe(#addMealToMenuRequest.recipeId, authentication.principal))")
    public ResponseEntity<Void> addMealToMenu(Long id, AddMealToMenuRequest addMealToMenuRequest) {
        log.info("POST /menus/{}/meals", id);
        log.debug("Request-Body: {}", addMealToMenuRequest);
        validateAddMealToMenuRequest(addMealToMenuRequest);
        menuService.addMealToMenu(id, addMealToMenuRequest);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') and @organizationService.hasPermissionsForMenu(#id, authentication.principal, 'PLANNER'))")
    public ResponseEntity<Void> addSnapshotToMenu(Long id, SnapshotCreateDto snapshotCreateDto) {
        log.info("POST /menus/{}/snapshots", id);
        log.debug("Request-Body: {}", snapshotCreateDto);
        validateSnapshotCreateDto(snapshotCreateDto);
        menuService.addSnapshotToMenu(id, snapshotCreateDto);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') and @organizationService.hasPermissionsForMenu(#id, authentication.principal, 'PLANNER'))")
    public ResponseEntity<Void> changeMenuItemOrder(Long id, List<Long> requestBody) {
        log.info("PUT /menus/{}/items/order", id);
        log.debug("Request-Body: {}", requestBody);
        validateMenuItemIds(requestBody);
        menuService.changeMenuItemOrder(id, requestBody);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') and @organizationService.hasPermissionForOrganization(#menuCreateDto.organizationId, authentication.principal, 'PLANNER'))")
    public ResponseEntity<MenuSummaryDto> createMenu(MenuCreateDto menuCreateDto) {
        log.info("POST /menus");
        log.debug("Request-Body: {}", menuCreateDto);
        validateMenuCreateDto(menuCreateDto);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(menuService.createMenu(menuCreateDto));
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') and @organizationService.hasPermissionsForMenu(#id, authentication.principal, 'ADMIN'))")
    public ResponseEntity<Void> deleteMenuById(Long id) {
        log.info("DELETE /menus/{}", id);
        menuService.deleteMenuById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') and @organizationService.hasPermissionsForMenu(#id, authentication.principal, 'MEMBER'))")
    public ResponseEntity<Boolean> existsShoppingListForMenu(Long id) {
        return ResponseEntity.ok(menuService.existsShoppingListForMenu(id));
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') and @organizationService.hasPermissionsForMenu(#id, authentication.principal, 'MEMBER'))")
    public ResponseEntity<MenuDetailDto> getMenuById(Long id) {
        log.info("GET /menus/{}", id);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(menuService.getMenuById(id));
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<MenuSummaryListPaginatedDto> getMenus(String name, Long organizationId, MenuStatus status, Pageable pageable) {
        log.info("GET /menus?=<>");
        log.debug("Search-Params: name='{}', organizationId='{}', status='{}', page='{}', size='{}'", name, organizationId, status, pageable.getPageNumber(), pageable.getPageSize());
        String username;

        Pageable p = pageable.isPaged()
                ? pageable
                : PageRequest.of(0, 20);
        if (SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(menuMapper.mapPageable(menuService.getMenusAsAdmin(name, organizationId, status, p)));
        }
        username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(menuMapper.mapPageable(menuService.getMenus(name, organizationId, status, username, p)));
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') and @organizationService.hasPermissionsForMenu(#menuId, authentication.principal, 'PLANNER'))")
    public ResponseEntity<Void> removeSnapshotFromMenu(Long menuId, Long snapshotId) {
        log.info("DELETE /menus/{}/snapshots/{}", menuId, snapshotId);
        menuService.removeSnapshotFromMenu(menuId, snapshotId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') and @organizationService.hasPermissionsForMenu(#id, authentication.principal, 'ADMIN'))")
    public ResponseEntity<Void> closeMenuById(Long id) {
        log.info("PATCH /menus/{}", id);
        menuService.closeMenuById(id);
        return ResponseEntity.noContent().build();
    }

    private void validateMenuCreateDto(MenuCreateDto dto) {
        PropertyChecker.begin()
            .append(MenuConstraints.validMenuName(dto.getName()))
            .append(MenuConstraints.validOptionalMenuDescription(dto.getDescription()))
            .append(MenuConstraints.validNumberOfPeople(dto.getNumberOfPeople()))
            .checkThat(dto.getOrganizationId(), "organizationId").notNull().done()
            .finalize(ValidationException::fromPropertyChecker);
    }

    private void validateMenuItemIds(List<Long> itemIds) {
        PropertyChecker.begin()
            .checkThat(itemIds, "body[]")
            .notNull()
            .notEmpty()
            .forEach((c, i) -> c.checkThat(i, "body[].id").notNull())
            .done()
            .finalize(ValidationException::fromPropertyChecker);
    }

    private void validateSnapshotCreateDto(SnapshotCreateDto dto) {
        PropertyChecker.begin()
            .append(MenuConstraints.validSnapshotName(dto.getName()))
            .finalize(ValidationException::fromPropertyChecker);
    }

    private void validateAddMealToMenuRequest(AddMealToMenuRequest dto) {
        PropertyChecker.begin()
            .checkThat(dto.getRecipeId(), "recipeId").notNull().done()
            .finalize(ValidationException::fromPropertyChecker);
    }
}
