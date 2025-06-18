package dev.heinisch.menumaestro.service;

import dev.heinisch.menumaestro.domain.ingredient.Ingredient;
import dev.heinisch.menumaestro.domain.ingredient.IngredientUnit;
import dev.heinisch.menumaestro.domain.ingredient_computation.IngredientComputationService;
import dev.heinisch.menumaestro.domain.ingredient_computation.IngredientUse;
import dev.heinisch.menumaestro.domain.menu.Meal;
import dev.heinisch.menumaestro.domain.menu.Menu;
import dev.heinisch.menumaestro.domain.menu.MenuComputationService;
import dev.heinisch.menumaestro.domain.menu.Snapshot;
import dev.heinisch.menumaestro.domain.organization.Organization;
import dev.heinisch.menumaestro.domain.shopping_list.ShoppingList;
import dev.heinisch.menumaestro.domain.shopping_list.ShoppingListItem;
import dev.heinisch.menumaestro.exceptions.ConflictException;
import dev.heinisch.menumaestro.exceptions.NotFoundException;
import dev.heinisch.menumaestro.exceptions.ValidationException;
import dev.heinisch.menumaestro.mapper.ShoppingListMapper;
import dev.heinisch.menumaestro.persistence.EntityLockingRepository;
import dev.heinisch.menumaestro.persistence.IngredientRepository;
import dev.heinisch.menumaestro.persistence.MenuRepository;
import dev.heinisch.menumaestro.persistence.OrganizationRepository;
import dev.heinisch.menumaestro.persistence.ShoppingListRepository;
import dev.heinisch.menumaestro.websocket.ShoppingListUpdateMessage;
import dev.heinisch.menumaestro.websocket.ShoppingListUpdateType;
import dev.heinisch.menumaestro.websocket.WebsocketProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.CloseShoppingListDto;
import org.openapitools.model.ShoppingListCreateDto;
import org.openapitools.model.ShoppingListDto;
import org.openapitools.model.ShoppingListEditDto;
import org.openapitools.model.ShoppingListIngredientAddDto;
import org.openapitools.model.ShoppingListIngredientEditDto;
import org.openapitools.model.ShoppingListPreviewEntryDto;
import org.openapitools.model.ShoppingListStatus;
import org.openapitools.model.ShoppingListTokenDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingListService {

    private final ShoppingListRepository shoppingListRepository;
    private final OrganizationRepository organizationRepository;
    private final MenuRepository menuRepository;

    private final ShoppingListMapper shoppingListMapper;

    private final MenuComputationService menuComputationService;
    private final IngredientComputationService ingredientComputationService;
    private final IngredientRepository ingredientRepository;
    private final JwtService jwtService;
    private final StashService stashService;

    private final SimpMessagingTemplate messagingTemplate;
    private final WebsocketProperties websocketProperties;
    private final EntityLockingRepository entityLockingRepository;

    @Transactional
    public ShoppingListDto createShoppingList(ShoppingListCreateDto shoppingListDto) {
        Menu menu = checkOrgAndGetMenu(shoppingListDto);
        var includedSnapshots = checkAndGetSnapshots(shoppingListDto, menu);
        Set<Meal> meals = menuComputationService.getIncludedMeals(menu, includedSnapshots);
        Set<Meal> allMeals = menu.getItems().stream()
                .filter(Meal.class::isInstance)
                .map(Meal.class::cast)
                .collect(Collectors.toSet());
        var ingredientUses = ingredientComputationService.computeMissingIngredientsSimple(meals, allMeals, menu.getStash(),
                id -> ingredientRepository.findById(id).orElseThrow()).missingIngredients();
        Set<ShoppingListItem> items = ingredientUses.stream()
                .map(usedIngredient -> ShoppingListItem.builder()
                        .ingredientId(usedIngredient.ingredient().getId())
                        .unit(usedIngredient.unit())
                        .isChecked(false)
                        .amount(usedIngredient.amount())
                        .build()
                ).collect(Collectors.toSet());
        var shoppingList = ShoppingList.builder()
                .name(shoppingListDto.getName())
                .organizationId(shoppingListDto.getOrganizationId())
                .isClosed(false)
                .menuId(shoppingListDto.getMenuId())
                .items(items)
                .build();
        shoppingList = shoppingListRepository.save(shoppingList);
        Set<Long> ingredientIds = ingredientUses.stream().map(use -> use.ingredient().getId()).collect(Collectors.toSet());
        Map<Long, Ingredient> ingredientsById = ingredientRepository.findAllById(ingredientIds)
                .stream().collect(Collectors.toMap(Ingredient::getId, Function.identity()));
        return shoppingListMapper.toShoppingListDto(shoppingList, ingredientsById);
    }

    @Transactional(readOnly = true)
    public List<ShoppingListPreviewEntryDto> getShoppingListPreviewData(ShoppingListCreateDto shoppingListDto) {
        Menu menu = checkOrgAndGetMenu(shoppingListDto);
        var includedSnapshots = checkAndGetSnapshots(shoppingListDto, menu);
        Set<Meal> meals = menuComputationService.getIncludedMeals(menu, includedSnapshots);
        Set<Meal> allMeals = menu.getItems().stream()
                .filter(Meal.class::isInstance)
                .map(Meal.class::cast)
                .collect(Collectors.toSet());
        var ingredientUses = ingredientComputationService.computeMissingIngredientsSimple(meals, allMeals, menu.getStash(),
                id -> ingredientRepository.findById(id).orElseThrow());
        var ingredientUsePairs = ingredientComputationService.mapToIngredientAmountPair(ingredientUses.usedStashIngredients(),
                ingredientUses.totalIngredients());

        return shoppingListMapper.toPreviewEntryDtos(ingredientUsePairs);
    }

    private static Set<Snapshot> checkAndGetSnapshots(ShoppingListCreateDto shoppingListDto, Menu menu) {
        var snapshotIds = new HashSet<>(shoppingListDto.getSnapshotIds());
        var includedSnapshots = menu.getItems()
                .stream()
                .filter(item -> item instanceof Snapshot && snapshotIds.contains(item.getId()))
                .map(Snapshot.class::cast)
                .collect(Collectors.toSet());
        if (snapshotIds.size() != includedSnapshots.size()) {
            throw new ValidationException("Some snapshots don't exist in the menu with id " + shoppingListDto.getMenuId() + "!");
        }
        return includedSnapshots;
    }

    private Menu checkOrgAndGetMenu(ShoppingListCreateDto shoppingListDto) {
        if (!organizationRepository.existsById(shoppingListDto.getOrganizationId())) {
            throw new ValidationException("No organization with id " + shoppingListDto.getOrganizationId() + "!");
        }
        Menu menu = menuRepository.findById(shoppingListDto.getMenuId()).orElseThrow(() -> new ValidationException("No menu with id " + shoppingListDto.getMenuId() + "!"));
        if (!menu.getOrganizationId().equals(shoppingListDto.getOrganizationId())) {
            throw new ValidationException("Given menu is from a different organization!");
        }
        return menu;
    }

    @Transactional
    public ShoppingListTokenDto generateAccessToken(Long shoppingListId) {
        if (!shoppingListRepository.existsById(shoppingListId)) {
            throw NotFoundException.forEntityAndId("ShoppingList", shoppingListId);
        }
        return new ShoppingListTokenDto()
                .token(jwtService.generateShoppingListAccessToken(shoppingListId));
    }

    @Transactional(readOnly = true)
    public ShoppingListDto getShoppingListById(Long id) {
        ShoppingList shoppingList = shoppingListRepository.findById(id).orElseThrow(() -> new NotFoundException("Shopping list with id " + id + " not found!"));
        return shoppingListMapper.toShoppingListDto(shoppingList, mapIngredients(shoppingList, null));
    }

    @Transactional(readOnly = true)
    public Page<ShoppingListDto> searchShoppingListsAsAdmin(String name, ShoppingListStatus status, Long menuId, Pageable pageable) {
        Boolean isClosed = shoppingListMapper.toIsClosed(status);

        Page<ShoppingList> shoppingLists = shoppingListRepository.searchShoppingListsByNameAndShoppingListStatusAdmin(name, isClosed, menuId, pageable);

        Set<Long> ingredientIds = shoppingLists.stream()
                .flatMap(sl -> sl.getItems().stream())
                .map(ShoppingListItem::getIngredientId)
                .collect(Collectors.toSet());
        Map<Long, Ingredient> ingredientsById = ingredientRepository.findAllById(ingredientIds)
                .stream().collect(Collectors.toMap(Ingredient::getId, Function.identity()));
        return shoppingLists.map(shoppingList -> shoppingListMapper.toShoppingListDto(shoppingList, ingredientsById));
    }

    @Transactional(readOnly = true)
    public Page<ShoppingListDto> searchShoppingLists(String name, ShoppingListStatus status, Long menuId, String username, Pageable pageable) {
        Boolean isClosed = shoppingListMapper.toIsClosed(status);
        Set<Long> organizationIds = organizationRepository.findByMemberUsernameAndNameContainingIgnoreCaseAndNotInvited(username, "", Pageable.unpaged())
                .stream()
                .map(Organization::getId)
                .collect((Collectors.toSet()));
        Page<ShoppingList> shoppingLists = shoppingListRepository.searchShoppingListsByNameAndShoppingListStatus(name, isClosed, menuId, organizationIds, pageable);
        Set<Long> ingredientIds = shoppingLists.stream()
                .flatMap(sl -> sl.getItems().stream())
                .map(ShoppingListItem::getIngredientId)
                .collect(Collectors.toSet());
        Map<Long, Ingredient> ingredientsById = ingredientRepository.findAllById(ingredientIds)
                .stream().collect(Collectors.toMap(Ingredient::getId, Function.identity()));
        return shoppingLists.map(shoppingList -> shoppingListMapper.toShoppingListDto(shoppingList, ingredientsById));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ShoppingListDto editShoppingList(Long id, ShoppingListEditDto shoppingListEditDto) {
        ShoppingList shoppingList = entityLockingRepository.lockEntity(ShoppingList.class, id, 50)
                .orElseThrow(() -> new NotFoundException("Shopping list with id " + id + " not found!"));
        if (shoppingList.getIsClosed()) {
            throw new ValidationException("Shopping-List is closed!");
        }
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        for (ShoppingListIngredientEditDto shoppingListIngredientEditDto : shoppingListEditDto.getIngredients()) {
            ShoppingListItem shoppingListItem = shoppingList.getItems()
                    .stream()
                    .filter(item -> item.getId().equals(shoppingListIngredientEditDto.getId()))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("Shopping list item with id " +
                            shoppingListIngredientEditDto.getId() + " not found in shopping list with id " +
                            shoppingList.getId() + "!"));
            if (shoppingListItem.getIsChecked()
                    && shoppingListIngredientEditDto.getChecked()) {
                throw new ConflictException("Item was already checked off!");
            }
            shoppingListItem.setIsChecked(shoppingListIngredientEditDto.getChecked());
            shoppingListItem.setCheckedByAccountUsername(shoppingListIngredientEditDto.getChecked() ? username : null);
            messagingTemplate.convertAndSend(
                    websocketProperties.getTopics().getShoppingListTopicPrefix() + "/" + id,
                    ShoppingListUpdateMessage.update(shoppingListItem)
            );
        }
        return shoppingListMapper.toShoppingListDto(shoppingList, mapIngredients(shoppingList, null));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ShoppingListDto addItemToShoppingList(Long id, ShoppingListIngredientAddDto shoppingListIngredientAddDto) {
        ShoppingList shoppingList = entityLockingRepository.lockEntity(ShoppingList.class, id, 50)
                .orElseThrow(() -> new NotFoundException("Shopping list with id " + id + " not found!"));
        if (shoppingList.getIsClosed()) {
            throw new ValidationException("Shopping-List is closed!");
        }
        Long newItemIngredientId = shoppingListIngredientAddDto.getExistingIngredientId();
        if (newItemIngredientId != null && !ingredientRepository.existsById(newItemIngredientId)) {
            throw new NotFoundException("Ingredient with id " + newItemIngredientId + " not found!");
        }

        Optional<ShoppingListItem> existingItemOptional = shoppingList.getItems().stream()
                .filter(item -> {
                    if (item.getIngredientId() != null) {
                        return item.getIngredientId().equals(newItemIngredientId) && item.getUnit().equals(IngredientUnit.valueOf(shoppingListIngredientAddDto.getUnit().getValue()));
                    } else {
                        return item.getCustomItemName().equals(shoppingListIngredientAddDto.getCustomIngredientName()) && item.getUnit().equals(IngredientUnit.valueOf(shoppingListIngredientAddDto.getUnit().getValue()));
                    }
                })
                .findFirst();

        if (existingItemOptional.isPresent()) {
            // If an item with the same ingredientId or custom name, and unit exists, sum the amounts
            ShoppingListItem existingItem = existingItemOptional.get();
            Double newAmount = existingItem.getAmount() + shoppingListIngredientAddDto.getAmount();

            existingItem.setAmount(newAmount);
            messagingTemplate.convertAndSend(
                    websocketProperties.getTopics().getShoppingListTopicPrefix() + "/" + id,
                    ShoppingListUpdateMessage.update(existingItem)
            );
        } else {
            ShoppingListItem shoppingListItem = ShoppingListItem.builder()
                    .shoppingList(shoppingList)
                    .amount(shoppingListIngredientAddDto.getAmount())
                    .unit(IngredientUnit.valueOf(shoppingListIngredientAddDto.getUnit().getValue()))
                    .ingredientId(newItemIngredientId)
                    .customItemName(shoppingListIngredientAddDto.getCustomIngredientName())
                    .isChecked(false)
                    .checkedByAccountUsername(null)
                    .build();

            shoppingList.addItem(shoppingListItem);
        }
        messagingTemplate.convertAndSend(
                websocketProperties.getTopics().getShoppingListTopicPrefix() + "/" + id,
                ShoppingListUpdateMessage.builder().updateType(ShoppingListUpdateType.RELOAD).build()
        );
        return shoppingListMapper.toShoppingListDto(shoppingList, mapIngredients(shoppingList, newItemIngredientId));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CloseShoppingListDto closeShoppingList(Long id) {
        ShoppingList shoppingList = shoppingListRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shopping list with id " + id + " not found!"));
        entityLockingRepository.lockEntity(ShoppingList.class, shoppingList.getId(), 50);
        Menu menu = menuRepository.findById(shoppingList.getMenuId()).orElseThrow();
        if (shoppingList.getIsClosed())
            return shoppingListMapper.toCloseShoppingListDto(menu.getStash().getId(), shoppingListMapper.toShoppingListDto(shoppingList, mapIngredients(shoppingList, null)));

        shoppingList.setIsClosed(true);
        List<IngredientUse> tickedIngredients;
        tickedIngredients = shoppingList.getItems().stream().filter(ShoppingListItem::getIsChecked).filter(item -> item.getIngredientId() != null)
                .map(shoppingListItem -> IngredientUse.fromShoppingListItem(shoppingListItem, ingredientId -> ingredientRepository.findById(ingredientId).orElseThrow())
                ).toList();
        stashService.addToStash(menu.getStash().getId(), tickedIngredients);
        messagingTemplate.convertAndSend(
            websocketProperties.getTopics().getShoppingListTopicPrefix() + "/" + id,
            ShoppingListUpdateMessage.builder().updateType(ShoppingListUpdateType.CLOSED).build()
        );
        return shoppingListMapper.toCloseShoppingListDto(menu.getStash().getId(), shoppingListMapper.toShoppingListDto(shoppingList, mapIngredients(shoppingList, null)));
    }

    private Map<Long, Ingredient> mapIngredients(ShoppingList shoppingList, Long newItemIngredientId) {
        Set<Long> ingredientIds = shoppingList.getItems()
                .stream()
                .map(ShoppingListItem::getIngredientId)
                .collect(Collectors.toSet());
        if (newItemIngredientId != null) {
            ingredientIds.add(newItemIngredientId);
        }
        return ingredientRepository.findAllById(ingredientIds)
                .stream().collect(Collectors.toMap(Ingredient::getId, Function.identity()));
    }
}
