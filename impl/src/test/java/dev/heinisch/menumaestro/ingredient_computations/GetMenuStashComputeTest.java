package dev.heinisch.menumaestro.ingredient_computations;

import dev.heinisch.menumaestro.domain.ingredient.IngredientUnit;
import dev.heinisch.menumaestro.domain.menu.Menu;
import dev.heinisch.menumaestro.domain.menu.MenuComputationService;
import dev.heinisch.menumaestro.domain.menu.Snapshot;
import dev.heinisch.menumaestro.domain.organization.Organization;
import dev.heinisch.menumaestro.domain.stash.Stash;
import dev.heinisch.menumaestro.mapper.IngredientMapper;
import dev.heinisch.menumaestro.mapper.MenuMapper;
import dev.heinisch.menumaestro.mapper.MenuMapperImpl;
import dev.heinisch.menumaestro.mapper.OrganizationMapper;
import dev.heinisch.menumaestro.mapper.OrganizationMapperImpl;
import dev.heinisch.menumaestro.mapper.ShoppingListMapper;
import dev.heinisch.menumaestro.mapper.ShoppingListMapperImpl;
import dev.heinisch.menumaestro.mapper.util.EnumsMapper;
import dev.heinisch.menumaestro.mapper.util.EnumsMapperImpl;
import dev.heinisch.menumaestro.persistence.EntityLockingRepository;
import dev.heinisch.menumaestro.persistence.IngredientRepository;
import dev.heinisch.menumaestro.persistence.MealRepository;
import dev.heinisch.menumaestro.persistence.MenuRepository;
import dev.heinisch.menumaestro.persistence.OrganizationRepository;
import dev.heinisch.menumaestro.persistence.RecipeRepository;
import dev.heinisch.menumaestro.persistence.ShoppingListRepository;
import dev.heinisch.menumaestro.service.JwtService;
import dev.heinisch.menumaestro.service.MarkdownSanitizerService;
import dev.heinisch.menumaestro.service.MenuService;
import dev.heinisch.menumaestro.service.ShoppingListService;
import dev.heinisch.menumaestro.service.StashService;
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
    MarkdownSanitizerService markdownSanitizerService;

    MenuComputationService menuComputationService;
    ShoppingListService shoppingListService;
    JwtService jwtService;

    MenuMapper menuMapper;
    OrganizationMapper organizationMapper;
    EnumsMapper enumsMapper;
    ShoppingListMapper shoppingListMapper;

    MenuService menuService;
    private ShoppingListRepository shoppingListRepository;

    public static Organization defaultOrganization1() {
        return Organization.builder()
                .name("Test Org")
                .description("Hello this is a test org")
                .build();
    }

    @BeforeEach
    void setup() {
        organizationRepository = mock(OrganizationRepository.class);
        menuRepository = mock(MenuRepository.class);
        recipeRepository = mock(RecipeRepository.class);
        mealRepository = mock(MealRepository.class);
        ingredientRepository = mock(IngredientRepository.class);
        markdownSanitizerService = mock(MarkdownSanitizerService.class);

        menuComputationService = new MenuComputationService(ingredientComputationService);

        menuMapper = new MenuMapperImpl();
        organizationMapper = new OrganizationMapperImpl();
        enumsMapper = new EnumsMapperImpl();
        shoppingListMapper = new ShoppingListMapperImpl();

        shoppingListRepository = mock(ShoppingListRepository.class);
        menuService = new MenuService(menuRepository, organizationRepository, recipeRepository, mealRepository, ingredientRepository, ingredientMapper, menuMapper, organizationMapper, enumsMapper, menuComputationService, shoppingListRepository, shoppingListService, stashService, entityLocker, markdownSanitizerService);
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
        when(organizationRepository.findById(eq(3L))).thenReturn(Optional.of(defaultOrganization1()));
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
        when(organizationRepository.findById(eq(3L))).thenReturn(Optional.of(defaultOrganization1()));
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
