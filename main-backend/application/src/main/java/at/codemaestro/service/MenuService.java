package at.codemaestro.service;

import at.codemaestro.domain.menu.Meal;
import at.codemaestro.domain.menu.Menu;
import at.codemaestro.domain.menu.MenuComputationService;
import at.codemaestro.domain.menu.MenuItem;
import at.codemaestro.domain.menu.Snapshot;
import at.codemaestro.domain.organization.Organization;
import at.codemaestro.domain.recipe.Recipe;
import at.codemaestro.domain.recipe.RecipeValue;
import at.codemaestro.domain.shopping_list.ShoppingList;
import at.codemaestro.domain.stash.Stash;
import at.codemaestro.domain.stash.StashEntry;
import at.codemaestro.exceptions.ConflictException;
import at.codemaestro.exceptions.NotFoundException;
import at.codemaestro.exceptions.ValidationException;
import at.codemaestro.mapper.IngredientMapper;
import at.codemaestro.mapper.MenuMapper;
import at.codemaestro.mapper.OrganizationMapper;
import at.codemaestro.mapper.util.EnumsMapper;
import at.codemaestro.persistence.EntityLockingRepository;
import at.codemaestro.persistence.IngredientRepository;
import at.codemaestro.persistence.MealRepository;
import at.codemaestro.persistence.MenuRepository;
import at.codemaestro.persistence.OrganizationRepository;
import at.codemaestro.persistence.RecipeRepository;
import at.codemaestro.persistence.ShoppingListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.HashSet;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final OrganizationRepository organizationRepository;
    private final RecipeRepository recipeRepository;
    private final MealRepository mealRepository;
    private final IngredientRepository ingredientRepository;
    private final IngredientMapper ingredientMapper;
    private final MenuMapper menuMapper;
    private final OrganizationMapper organizationMapper;
    private final EnumsMapper enumsMapper;
    private final MenuComputationService menuComputationService;
    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingListService shoppingListService;
    private final StashService stashService;
    private final EntityLockingRepository entityLocker;


    @Transactional
    public void addMealToMenu(Long id, AddMealToMenuRequest addMealToMenuRequest) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> NotFoundException.forEntityAndId("Menu", id));

        if (menu.getStatus() == at.codemaestro.domain.menu.MenuStatus.CLOSED) {
            throw new ValidationException("Menu was closed, it cannot be changed!");
        }

        Set<MenuItem> menuItemSet = menu.getItems();

        Recipe recipe = recipeRepository.findById(addMealToMenuRequest.getRecipeId())
                .orElseThrow(() -> NotFoundException.forEntityAndId("Recipe", addMealToMenuRequest.getRecipeId()));
        RecipeValue recipeValue = RecipeValue.copyOf(recipe.getRecipeValue());

        var lastItem = menu.getItems().stream().max(Comparator.comparing(MenuItem::getPosition));
        int mealPos;
        if (lastItem.isPresent() && lastItem.get() instanceof Snapshot) {
            // if there is a snapshot at the bottom of the menu it should stay at the bottom
            mealPos = lastItem.get().getPosition();
            lastItem.get().setPosition(lastItem.get().getPosition() + 1);
        } else {
            mealPos = menuItemSet.size();
        }
        Meal meal = Meal.builder()
                .name(recipe.getRecipeValue().getName())
                .isDone(false)
                .recipe(recipeValue)
                .position(mealPos)
                .menu(menu)
                .numberOfPeople(menu.getNumberOfPeople())
                .build();
        mealRepository.save(meal);
    }

    @Transactional
    public void addSnapshotToMenu(Long id, SnapshotCreateDto snapshotCreateDto) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> NotFoundException.forEntityAndId("Menu", id));

        if (menu.getStatus() == at.codemaestro.domain.menu.MenuStatus.CLOSED) {
            throw new ValidationException("Menu was closed, it cannot be changed!");
        }

        if (menu.getItems().stream()
                .anyMatch(item -> item instanceof Snapshot && ((Snapshot) item).getName().equals(snapshotCreateDto.getName()))) {
            throw new ConflictException("Snapshot with this name already exists!");
        }
        MenuItem snapshot = Snapshot.builder()
                .name(snapshotCreateDto.getName())
                .position(snapshotCreateDto.getPosition())
                .menu(menu)
                .build();
        if (snapshot.getPosition() < 0 || snapshot.getPosition() > menu.getItems().size()) {
            throw new ValidationException("Incorrect snapshot position provided!");
        }
        menu.addMenuItem(snapshot);
    }

    @Transactional
    public void removeSnapshotFromMenu(Long menuId, Long snapshotId) {
        Menu menu = menuRepository.findById(menuId)
            .orElseThrow(() -> NotFoundException.forEntityAndId("Menu", menuId));

        if (menu.getStatus() == at.codemaestro.domain.menu.MenuStatus.CLOSED) {
            throw new ValidationException("Menu was closed, it cannot be changed!");
        }

        var snapshot = menu.getItems().stream().filter(item -> item instanceof Snapshot &&
            Objects.equals(item.getId(), snapshotId)).findFirst();
        if (snapshot.isEmpty()) {
            throw new NotFoundException(
                String.format("Snapshot with id '%d' not found in menu with id '%d'!", snapshotId, menuId));
        } else {
            menu.removeSnapshot((Snapshot)snapshot.get());
        }
    }

    @Transactional
    public MenuSummaryDto createMenu(MenuCreateDto menuCreateDto) {
        Menu menu = menuMapper.toMenu(menuCreateDto);
        if (!organizationRepository.existsById(menuCreateDto.getOrganizationId())) {
            throw new ConflictException("Organization with id " + menuCreateDto.getOrganizationId() + " does not exist!");
        }
        var dto = menuMapper.toMenuSummaryDto(menuRepository.save(menu));
        dto.setOrganization(organizationMapper.toOrganizationSummaryDto(organizationRepository.findById(menu.getOrganizationId()).orElseThrow()));
        return dto;
    }


    @Transactional(readOnly = true)
    public MenuDetailDto getMenuById(Long id) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> NotFoundException.forEntityAndId("Menu", id));
        menuComputationService.computeMetadata(menu, iid -> ingredientRepository.findById(iid).orElseThrow());
        var dto = menuMapper.toMenuDetailDto(menu);
        dto.setOrganization(organizationMapper.toOrganizationSummaryDto(organizationRepository.findById(menu.getOrganizationId()).orElseThrow()));
        return dto;
    }

    @Transactional(readOnly = true)
    public Page<MenuSummaryDto> getMenusAsAdmin(
            String name,
            Long organizationId,
            MenuStatus status,
            Pageable pageable) {

        Page<Menu> menusPage = menuRepository.findByNameAsAdminContainingIgnoreCaseAndOrganizationId(name,
                enumsMapper.toMenuStatus(status), organizationId, pageable);
        return getMenuSummaryDtos(menusPage);
    }

    private Page<MenuSummaryDto> getMenuSummaryDtos(Page<Menu> menusPage) {
        var organizationIds = menusPage.getContent().stream().map(Menu::getOrganizationId).collect(Collectors.toSet());
        Map<Long, OrganizationSummaryDto> organizationsById = organizationRepository.findAllById(organizationIds)
                .stream()
                .map(organizationMapper::toOrganizationSummaryDto)
                .collect(Collectors.toMap(OrganizationSummaryDto::getId, i -> i));
        var result = menusPage.map(menuMapper::toMenuSummaryDto);
        menuMapper.addOrganizationSummaryDto(result.getContent(), organizationsById);
        return result;
    }
    @Transactional(readOnly = true)
    public boolean existsShoppingListForMenu(Long menuId) {
        return shoppingListRepository.existsShoppingListForMenu(menuId) > 0;
    }

    @Transactional(readOnly = true)
    public Page<MenuSummaryDto> getMenus(
            String name,
            Long organizationId,
            MenuStatus status,
            String username,
            Pageable pageable) {
        Set<Long> userOrganizations =organizationRepository.findByMemberUsernameAndNameContainingIgnoreCaseAndNotInvited(username,"", Pageable.unpaged())
                    .stream()
                    .map(Organization::getId)
                    .collect((Collectors.toSet()));


        Page<Menu> menusPage = menuRepository.findByNameAsUserContainingIgnoreCaseAndOrganizationId(name,
                enumsMapper.toMenuStatus(status), organizationId,userOrganizations, pageable);
        return getMenuSummaryDtos(menusPage);
    }

    @Transactional
    public void changeMenuItemOrder(Long id, List<Long> itemIds) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> NotFoundException.forEntityAndId("Menu", id));

        if (menu.getStatus() == at.codemaestro.domain.menu.MenuStatus.CLOSED) {
            throw new ValidationException("Menu was closed, it cannot be changed!");
        }

        var actualMenuItemIdsList = menu.getItems().stream().map(MenuItem::getId).toList();
        if (actualMenuItemIdsList.size() != itemIds.size()) {
            throw new ValidationException("The given item-list does not have the same amount of items as the menu.");
        }
        if (!new HashSet<>(itemIds).equals(new HashSet<>(actualMenuItemIdsList))) {
            throw new ValidationException("The given item-list must contain exactly all items of the menu.");
        }
        menu.assignPositionFromItemIdList(itemIds);
    }

    @Transactional
    public void deleteMenuById(Long id) {
        if (menuRepository.findById(id).isEmpty()) {
            throw new NotFoundException(String.format("Menu with id '%d' not found", id));
        }
        List<ShoppingList>shoppingListList= shoppingListRepository.getShoppingListByMenuId(id);
        for (ShoppingList shoppingList : shoppingListList) {
            shoppingListRepository.deleteById(shoppingList.getId());
        }
        menuRepository.deleteById(id);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void closeMenuById(Long id) {
        Menu menu = menuRepository.findById(id)
            .orElseThrow(() -> NotFoundException.forEntityAndId("Menu", id));
        menu.setStatus(at.codemaestro.domain.menu.MenuStatus.CLOSED);
        Organization organization = organizationRepository.findById(menu.getOrganizationId())
            .orElseThrow();
        List<ShoppingList> shoppingLists = shoppingListRepository.getShoppingListByMenuId(menu.getId()).stream().toList();
        for (ShoppingList shoppingList : shoppingLists) {
            shoppingListService.closeShoppingList(shoppingList.getId());
        }
        entityLocker.lockEntity(Stash.class, menu.getStash().getId(), 50)
            .orElseThrow(() -> NotFoundException.forEntityAndId("Stash", menu.getStash().getId()));
        entityLocker.lockEntity(Stash.class, organization.getStash().getId(), 50)
            .orElseThrow(() -> NotFoundException.forEntityAndId("Stash", organization.getStash().getId()));
        List<IngredientUseCreateEditDto> transferList = new ArrayList<>();
        for (StashEntry entry : menu.getStash().getEntries()) {
            transferList.add(new IngredientUseCreateEditDto(entry.getIngredientId(),
                ingredientMapper.toIngredientUnitDto(entry.getUnit()), entry.getAmount().floatValue()));
        }
        stashService.moveStashIngredients(menu.getStash().getId(), organization.getStash().getId(), transferList);
        menu.getStash().setLocked(true);
    }
}
