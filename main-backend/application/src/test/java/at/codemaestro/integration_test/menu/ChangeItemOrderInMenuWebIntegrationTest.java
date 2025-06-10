package at.codemaestro.integration_test.menu;

import at.codemaestro.domain.account.Account;
import at.codemaestro.domain.cooking_appliance.CookingAppliance;
import at.codemaestro.domain.ingredient.Ingredient;
import at.codemaestro.domain.menu.Menu;
import at.codemaestro.domain.menu.MenuStatus;
import at.codemaestro.domain.organization.Organization;
import at.codemaestro.domain.organization.OrganizationAccountRelation;
import at.codemaestro.domain.organization.OrganizationRole;
import at.codemaestro.integration_test.BaseWebIntegrationTest;
import at.codemaestro.integration_test.utils.ErrorResponseAssert;
import at.codemaestro.integration_test.utils.RestHelper;
import io.restassured.http.Headers;
import io.restassured.http.Method;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.AddMealToMenuRequest;
import org.openapitools.model.CookingApplianceUseCreateEditDto;
import org.openapitools.model.IngredientUnitDto;
import org.openapitools.model.IngredientUseCreateEditDto;
import org.openapitools.model.MealInMenuDto;
import org.openapitools.model.RecipeCreateEditDto;
import org.openapitools.model.RecipeDto;
import org.openapitools.model.SnapshotInMenuDto;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.stream.Collectors;

import static at.codemaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static at.codemaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME_2;
import static at.codemaestro.integration_test.utils.test_constants.DefaultAccountTestData.defaultAccount;
import static at.codemaestro.integration_test.utils.test_constants.DefaultCookingApplianceTestData.defaultCookingAppliance1;
import static at.codemaestro.integration_test.utils.test_constants.DefaultIngredientTestData.defaultIngredient1;
import static at.codemaestro.integration_test.utils.test_constants.DefaultIngredientTestData.defaultIngredient2;
import static at.codemaestro.integration_test.utils.test_constants.DefaultIngredientTestData.defaultIngredient3;
import static at.codemaestro.integration_test.utils.test_constants.DefaultMenuTestData.DEFAULT_SNAPSHOT_NAME_1;
import static at.codemaestro.integration_test.utils.test_constants.DefaultMenuTestData.DEFAULT_SNAPSHOT_NAME_2;
import static at.codemaestro.integration_test.utils.test_constants.DefaultMenuTestData.defaultMenu1;
import static at.codemaestro.integration_test.utils.test_constants.DefaultMenuTestData.defaultSnapshotCreateDto1;
import static at.codemaestro.integration_test.utils.test_constants.DefaultMenuTestData.defaultSnapshotCreateDto2;
import static at.codemaestro.integration_test.utils.test_constants.DefaultOrganizationTestData.defaultOrganization1;
import static at.codemaestro.integration_test.utils.test_constants.DefaultRecipeTestData.DEFAULT_RECIPE_NAME_1;
import static at.codemaestro.integration_test.utils.test_constants.DefaultRecipeTestData.DEFAULT_RECIPE_NAME_2;
import static at.codemaestro.integration_test.utils.test_constants.DefaultRecipeTestData.defaultRecipeCreateEditDto1;
import static at.codemaestro.integration_test.utils.test_constants.DefaultRecipeTestData.defaultRecipeCreateEditDto2;
import static org.assertj.core.api.Assertions.assertThat;

public class ChangeItemOrderInMenuWebIntegrationTest extends BaseWebIntegrationTest {


    private Organization organization1;
    private Ingredient ingredient1;
    private Ingredient ingredient2;
    private Ingredient ingredient3;
    private CookingAppliance appliance1;
    private Menu menu1;

    private RecipeDto recipeDto1;
    private RecipeDto recipeDto2;
    private Account account1;

    private Long mealId1, mealId2, snapshotId1, snapshotId2;


    private RestHelper.PathAndBodyWithoutReturnRestHelper<Long, List<Long>> rest;

    @PostConstruct
    private void initRestHelper() {
        rest = new RestHelper.PathAndBodyWithoutReturnRestHelper<>(
                this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER")),
                Method.PUT,
                URI + "/{id}/items/order",
                HttpStatus.NO_CONTENT
        );
    }

    @Override
    protected String getBasePath() {
        return "/menus";
    }

    @BeforeEach
    public void setup() {
        organization1 = organizationRepository.saveAndFlush(defaultOrganization1());
        account1 = accountRepository.saveAndFlush(defaultAccount());
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation.builder()
                .role(OrganizationRole.OWNER)
                .organization(organization1)
                .account(account1)
                .build());
        ingredient1 = ingredientRepository.saveAndFlush(defaultIngredient1());
        ingredient2 = ingredientRepository.saveAndFlush(defaultIngredient2());
        ingredient3 = ingredientRepository.saveAndFlush(defaultIngredient3());
        appliance1 = cookingApplianceRepository.saveAndFlush(defaultCookingAppliance1());
        recipeDto1 = recipeService.createRecipe(defaultCreateEditRecipeDtoModified1());
        recipeDto2 = recipeService.createRecipe(defaultCreateEditRecipeDtoModified2());
        menu1 = menuRepository.saveAndFlush(defaultMenu1(organization1.getId()));
        menuService.addMealToMenu(menu1.getId(), new AddMealToMenuRequest(recipeDto1.getId()));
        menuService.addSnapshotToMenu(menu1.getId(), defaultSnapshotCreateDto1().position(1));
        menuService.addMealToMenu(menu1.getId(), new AddMealToMenuRequest(recipeDto2.getId()));
        menuService.addSnapshotToMenu(menu1.getId(), defaultSnapshotCreateDto2().position(3));
        var menu = menuService.getMenuById(menu1.getId());
        var meals = menu.getMeals().stream().collect(Collectors.toMap(MealInMenuDto::getName, MealInMenuDto::getId));
        var snapshots = menu.getSnapshots().stream().collect(Collectors.toMap(SnapshotInMenuDto::getName, SnapshotInMenuDto::getId));
        mealId1 = meals.get(DEFAULT_RECIPE_NAME_1);
        mealId2 = meals.get(DEFAULT_RECIPE_NAME_2);
        snapshotId1 = snapshots.get(DEFAULT_SNAPSHOT_NAME_1);
        snapshotId2 = snapshots.get(DEFAULT_SNAPSHOT_NAME_2);
        // due to heuristic moving the last snapshot down, need to force order
        rest.requestSuccessful(menu1.getId(), List.of(mealId1, snapshotId1, mealId2, snapshotId2));
    }

    @Test
    void whenChangeItemOrderInMenu_withValidData_thenNoContent() {
        validateInitialMenuOrder();
        rest.requestSuccessful(menu1.getId(), List.of(mealId1, mealId2, snapshotId1, snapshotId2));
        var menu = menuService.getMenuById(menu1.getId());
        assertThat(menu.getMeals()).hasSize(2)
                .map(MealInMenuDto::getPosition).containsExactly(0, 1);
        assertThat(menu.getSnapshots()).hasSize(2)
                .map(SnapshotInMenuDto::getPosition).containsExactly(2, 3);
    }

    @Test
    void whenChangeItemOrderInMenu_withTooManyIds_thenUnprocessableEntity() {
        validateInitialMenuOrder();
        ErrorResponseAssert.assertThat(rest.requestFails(menu1.getId(), List.of(mealId1, mealId2, snapshotId1, snapshotId2, mealId1), HttpStatus.UNPROCESSABLE_ENTITY))
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("not have the same amount");
    }

    @Test
    void whenChangeItemOrderInMenu_withTooLittleIds_thenUnprocessableEntity() {
        validateInitialMenuOrder();
        ErrorResponseAssert.assertThat(rest.requestFails(menu1.getId(), List.of(mealId1, mealId2, snapshotId1), HttpStatus.UNPROCESSABLE_ENTITY))
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("not have the same amount");
    }

    @Test
    void whenChangeItemOrderInMenu_withDuplicateIds_thenUnprocessableEntity() {
        validateInitialMenuOrder();
        ErrorResponseAssert.assertThat(rest.requestFails(menu1.getId(), List.of(mealId1, mealId1, snapshotId1, snapshotId1), HttpStatus.UNPROCESSABLE_ENTITY))
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("must contain exactly all items");
    }

    @Test
    void whenChangeItemOrderInMenu_MenuClosed_thenUnprocessableEntity() {
        menu1.setStatus(MenuStatus.CLOSED);
        menuRepository.saveAndFlush(menu1);

        ErrorResponseAssert.assertThat(rest.requestFails(menu1.getId(), List.of(mealId1, mealId2, snapshotId1, snapshotId2), HttpStatus.UNPROCESSABLE_ENTITY))
            .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
            .messageContains("Menu was closed, it cannot be changed!");
    }

    @Test
    void basicAuthTests() {
        rest.basicAuthTests(1L, List.of(1L, 2L));
    }

    @Test
    void whenChangeItemOrderInMenu_withNotInOrganisation_thenForbidden() {
        var errorResponse = rest.requestFails(menu1.getId(),
                List.of(1L, 2L),
                HttpStatus.FORBIDDEN,
                new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME_2, List.of("ROLE_USER"))));
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("not in the required organization");
    }

    @Test
    void whenChangeItemOrderInMenu_withNotEnoughPermissions_thenForbidden() {
        organizationAccountRelationRepository.deleteAll();
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation.builder()
                .role(OrganizationRole.SHOPPER)
                .organization(organization1)
                .account(account1)
                .build()
        );

        var errorResponse = rest.requestFails(menu1.getId(), List.of(1L, 2L), HttpStatus.FORBIDDEN);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("not enough permissions")
                .messageContains("PLANNER");
    }

    @Test
    void whenEditMeal_withNotInOrganisationButAdmin_thenOk() {
        validateInitialMenuOrder();
        rest.requestSuccessful(
                menu1.getId(),
                List.of(mealId1, mealId2, snapshotId1, snapshotId2),
                new Headers(generateValidAuthorizationHeader("someuser", List.of("ROLE_ADMIN")))
        );
    }

    private void validateInitialMenuOrder() {
        var menu = menuService.getMenuById(menu1.getId());
        assertThat(menu.getMeals()).hasSize(2)
                .map(MealInMenuDto::getPosition).containsExactly(0, 2);
        assertThat(menu.getSnapshots()).hasSize(2)
                .map(SnapshotInMenuDto::getPosition).containsExactly(1, 3);
    }

    private RecipeCreateEditDto defaultCreateEditRecipeDtoModified1() {
        return defaultRecipeCreateEditDto1()
                .author(DEFAULT_USERNAME)
                .addCookingAppliancesItem(new CookingApplianceUseCreateEditDto()
                        .amount(1)
                        .id(appliance1.getId()))
                .addIngredientsItem(new IngredientUseCreateEditDto().
                        unit(IngredientUnitDto.GRAMS)
                        .amount(250f)
                        .id(ingredient1.getId()))
                .addIngredientsItem(new IngredientUseCreateEditDto()
                        .unit(IngredientUnitDto.LITRES)
                        .amount(0.25f)
                        .id(ingredient2.getId()));
    }

    private RecipeCreateEditDto defaultCreateEditRecipeDtoModified2() {
        return defaultRecipeCreateEditDto2()
                .author(DEFAULT_USERNAME)
                .addCookingAppliancesItem(new CookingApplianceUseCreateEditDto()
                        .amount(2)
                        .id(appliance1.getId()))
                .addIngredientsItem(new IngredientUseCreateEditDto().
                        unit(IngredientUnitDto.GRAMS)
                        .amount(250f)
                        .id(ingredient3.getId()))
                .addIngredientsItem(new IngredientUseCreateEditDto()
                        .unit(IngredientUnitDto.LITRES)
                        .amount(0.25f)
                        .id(ingredient2.getId()));
    }
}
