package at.codemaestro.unit_test.ingredientComputations;

import at.codemaestro.domain.ingredient.IngredientUnit;
import at.codemaestro.domain.menu.Menu;
import at.codemaestro.domain.menu.MenuComputationService;
import at.codemaestro.domain.menu.Snapshot;
import at.codemaestro.domain.stash.Stash;
import at.codemaestro.integration_test.utils.test_constants.DefaultOrganizationTestData;
import at.codemaestro.mapper.*;
import at.codemaestro.mapper.util.EnumsMapper;
import at.codemaestro.mapper.util.EnumsMapperImpl;
import at.codemaestro.persistence.EntityLockingRepository;
import at.codemaestro.persistence.IngredientRepository;
import at.codemaestro.persistence.MealRepository;
import at.codemaestro.persistence.MenuRepository;
import at.codemaestro.persistence.OrganizationRepository;
import at.codemaestro.persistence.RecipeRepository;
import at.codemaestro.persistence.ShoppingListRepository;
import at.codemaestro.service.JwtService;
import at.codemaestro.service.MenuService;
import at.codemaestro.service.ShoppingListService;
import at.codemaestro.service.StashService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.MealInMenuDto;
import org.openapitools.model.MealStatus;
import org.openapitools.model.SnapshotInMenuDto;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GetMenuStashComputeTest extends IngredientComputationTestBase {


    OrganizationRepository organizationRepository;
    MenuRepository menuRepository;
    RecipeRepository recipeRepository;
    MealRepository mealRepository;
    IngredientRepository ingredientRepository;
    IngredientMapper ingredientMapper;
    StashService stashService;
    EntityLockingRepository entityLocker;

    MenuComputationService menuComputationService;
    ShoppingListService shoppingListService;
    JwtService jwtService;

    MenuMapper menuMapper;
    OrganizationMapper organizationMapper;
    EnumsMapper enumsMapper;
    ShoppingListMapper shoppingListMapper;

    MenuService menuService;
    private ShoppingListRepository shoppingListRepository;


    @BeforeEach
    void setup() {
        organizationRepository = mock(OrganizationRepository.class);
        menuRepository = mock(MenuRepository.class);
        recipeRepository = mock(RecipeRepository.class);
        mealRepository = mock(MealRepository.class);
        ingredientRepository = mock(IngredientRepository.class);

        menuComputationService = new MenuComputationService(ingredientComputationService);

        menuMapper = new MenuMapperImpl();
        organizationMapper = new OrganizationMapperImpl();
        enumsMapper = new EnumsMapperImpl();
        shoppingListMapper = new ShoppingListMapperImpl();

        shoppingListRepository = mock(ShoppingListRepository.class);
        menuService = new MenuService(menuRepository, organizationRepository, recipeRepository, mealRepository, ingredientRepository, ingredientMapper, menuMapper, organizationMapper, enumsMapper, menuComputationService, shoppingListRepository, shoppingListService, stashService, entityLocker);
    }

    @Test
    void getMenu_stashConsidered_someMissingIngredients() {
        var stash = Stash.builder()
                .entries(List.of(
                        stashEntry(1L, IngredientUnit.GRAMS, 1000)
                ))
                .build();
        var menu = Menu.builder()
                .name("menu")
                .description("menu")
                .numberOfPeople(1)
                .organizationId(3L)
                .items(List.of(
                        mealScaledWithIngredients("meal 1", 1, 1, Set.of(
                                useIngredient(ingredient1, IngredientUnit.GRAMS, 1000),
                                useIngredient(ingredient2, IngredientUnit.LITRES, 1))),
                        Snapshot.builder().name("snapshot 1").build()

                )).build();
        menu.setStash(stash);
        when(menuRepository.findById(eq(2L))).thenReturn(Optional.of(menu));
        when(organizationRepository.findById(eq(3L))).thenReturn(Optional.of(DefaultOrganizationTestData.defaultOrganization1()));
        when(ingredientRepository.findById(any())).thenAnswer(i -> Optional.ofNullable(mockLoadIngredient(i.getArgument(0))));
        var result = Assertions.assertDoesNotThrow(() -> menuService.getMenuById(2L));
        Assertions.assertNotNull(result);
        Assertions.assertEquals("menu", result.getName());
        Assertions.assertEquals(1, result.getSnapshots().size());
        Assertions.assertEquals(1, result.getMeals().size());
        MealInMenuDto meal = result.getMeals().getFirst();
        SnapshotInMenuDto snapshot = result.getSnapshots().getFirst();
        Assertions.assertAll(
                () -> Assertions.assertEquals(MealStatus.SOME_INGREDIENTS_MISSING, meal.getStatus()),
                () -> Assertions.assertEquals(1, snapshot.getNumberOfMealsIncluded()),
                () -> Assertions.assertEquals(1, snapshot.getNumberOfAvailableIngredients()),
                () -> Assertions.assertEquals(2, snapshot.getNumberOfTotalIngredients())
        );
    }

    @Test
    void getMenu_multipleSnapshots_simpleComputation_valuesOk() {
        var stash = Stash.builder()
                .entries(List.of(
                        stashEntry(1L, IngredientUnit.GRAMS, 1000)
                ))
                .build();
        var menu = Menu.builder()
                .name("menu")
                .description("menu")
                .numberOfPeople(1)
                .organizationId(3L)
                .items(List.of(
                        mealScaledWithIngredients("meal 1", 1, 1, Set.of(
                                useIngredient(ingredient1, IngredientUnit.GRAMS, 1000),
                                useIngredient(ingredient2, IngredientUnit.LITRES, 1))),
                        Snapshot.builder().name("snapshot 1").build(),
                        mealScaledWithIngredients("meal 2", 1, 1, Set.of(
                                useIngredient(ingredient1, IngredientUnit.GRAMS, 1000))),
                        Snapshot.builder().name("snapshot 2").build()

                )).build();
        menu.setStash(stash);
        when(menuRepository.findById(eq(2L))).thenReturn(Optional.of(menu));
        when(organizationRepository.findById(eq(3L))).thenReturn(Optional.of(DefaultOrganizationTestData.defaultOrganization1()));
        when(ingredientRepository.findById(any())).thenAnswer(i -> Optional.ofNullable(mockLoadIngredient(i.getArgument(0))));
        var result = Assertions.assertDoesNotThrow(() -> menuService.getMenuById(2L));
        Assertions.assertNotNull(result);
        Assertions.assertEquals("menu", result.getName());
        Assertions.assertEquals(2, result.getSnapshots().size());
        Assertions.assertEquals(2, result.getMeals().size());
        MealInMenuDto meal1 = result.getMeals().getFirst();
        SnapshotInMenuDto snapshot1 = result.getSnapshots().getFirst();
        MealInMenuDto meal2 = result.getMeals().get(1);
        SnapshotInMenuDto snapshot2 = result.getSnapshots().get(1);
        Assertions.assertAll(
                () -> Assertions.assertEquals(MealStatus.SOME_INGREDIENTS_MISSING, meal1.getStatus()),
                () -> Assertions.assertEquals(1, snapshot1.getNumberOfMealsIncluded()),
                () -> Assertions.assertEquals(1, snapshot1.getNumberOfAvailableIngredients()),
                () -> Assertions.assertEquals(2, snapshot1.getNumberOfTotalIngredients())
        );
        Assertions.assertAll(
                () -> Assertions.assertEquals(MealStatus.ALL_INGREDIENTS_PRESENT, meal2.getStatus()),
                () -> Assertions.assertEquals(1, snapshot2.getNumberOfMealsIncluded()),
                () -> Assertions.assertEquals(1, snapshot2.getNumberOfAvailableIngredients()),
                () -> Assertions.assertEquals(1, snapshot2.getNumberOfTotalIngredients())
        );
    }
}
