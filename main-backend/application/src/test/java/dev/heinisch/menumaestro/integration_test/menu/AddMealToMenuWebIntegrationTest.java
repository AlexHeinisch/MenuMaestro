package dev.heinisch.menumaestro.integration_test.menu;

import dev.heinisch.menumaestro.domain.account.Account;
import dev.heinisch.menumaestro.domain.cooking_appliance.CookingAppliance;
import dev.heinisch.menumaestro.domain.ingredient.Ingredient;
import dev.heinisch.menumaestro.domain.menu.Menu;
import dev.heinisch.menumaestro.domain.menu.MenuStatus;
import dev.heinisch.menumaestro.domain.organization.Organization;
import dev.heinisch.menumaestro.domain.organization.OrganizationAccountRelation;
import dev.heinisch.menumaestro.domain.organization.OrganizationRole;
import dev.heinisch.menumaestro.integration_test.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.integration_test.utils.ErrorResponseAssert;
import dev.heinisch.menumaestro.integration_test.utils.RestHelper;
import io.restassured.http.Headers;
import io.restassured.http.Method;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.AddMealToMenuRequest;
import org.openapitools.model.CookingApplianceUseCreateEditDto;
import org.openapitools.model.IngredientUnitDto;
import org.openapitools.model.IngredientUseCreateEditDto;
import org.openapitools.model.RecipeCreateEditDto;
import org.openapitools.model.RecipeDto;
import org.openapitools.model.RecipeVisibility;
import org.springframework.http.HttpStatus;

import java.util.List;

import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME_2;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.defaultAccount;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.defaultAccount2;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultCookingApplianceTestData.defaultCookingAppliance1;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultIngredientTestData.defaultIngredient1;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultIngredientTestData.defaultIngredient2;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultMenuTestData.defaultMenu1;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultOrganizationTestData.defaultCreateOrganizationDto2;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultOrganizationTestData.defaultOrganization1;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultRecipeTestData.defaultRecipeCreateEditDto1;
import static org.assertj.core.api.Assertions.assertThat;

public class AddMealToMenuWebIntegrationTest extends BaseWebIntegrationTest {

    private Organization organization1;
    private Ingredient ingredient1;
    private Ingredient ingredient2;
    private CookingAppliance appliance1;
    private Menu menu1;

    private RecipeDto recipeDto;
    private Account account1;

    private RestHelper.PathAndBodyWithoutReturnRestHelper<Long, AddMealToMenuRequest> rest;

    @PostConstruct
    private void initRestHelper() {
        rest = new RestHelper.PathAndBodyWithoutReturnRestHelper<>(
                this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER")),
                Method.POST,
                URI + "/{id}/meals",
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
        appliance1 = cookingApplianceRepository.saveAndFlush(defaultCookingAppliance1());
        recipeDto = recipeService.createRecipe(defaultCreateEditRecipeDto());
        menu1 = menuRepository.saveAndFlush(defaultMenu1(organization1.getId()));
    }

    @Test
    void whenAddMealToMenu_withValidData_thenNoContent() {
        assertThat(menuService.getMenuById(menu1.getId()).getMeals()).hasSize(0);
        rest.requestSuccessful(menu1.getId(), defaultAddMealToMenuRequest());
        assertThat(menuService.getMenuById(menu1.getId()).getMeals()).hasSize(1);
    }

    @Test
    void whenAddMealToMenu_withNonExistentMenu_thenNotFound() {
        var errorResponse = rest.requestFails(-66L, defaultAddMealToMenuRequest(), HttpStatus.NOT_FOUND);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.NOT_FOUND)
                .messageContainsIgnoreCase("menu")
                .messageContainsIgnoreCase("not found");
    }

    @Test
    void whenAddMealToMenu_withNonExistentRecipe_thenNotFound() {
        var errorResponse = rest.requestFails(menu1.getId(), new AddMealToMenuRequest(-66L), HttpStatus.NOT_FOUND);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.NOT_FOUND)
                .messageContainsIgnoreCase("recipe")
                .messageContainsIgnoreCase("not found");
    }

    @Test
    void whenAddMealToMenu_withMenuClosed_thenValidationException() {
        menu1.setStatus(MenuStatus.CLOSED);
        menuRepository.saveAndFlush(menu1);

        var errorResponse = rest.requestFails(menu1.getId(), defaultAddMealToMenuRequest(), HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
            .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
            .messageContains("Menu was closed, it cannot be changed!");
    }

    @Test
    void whenAddMealToMenu_withUserDoesntOwnRecipe_thenForbidden() {
        accountRepository.saveAndFlush(defaultAccount2());
        var recipeDto2 = recipeService.createRecipe(defaultRecipeCreateEditDto1().name("Test").author(DEFAULT_USERNAME_2).visibility(RecipeVisibility.PRIVATE));

        var errorResponse = rest.requestFails(menu1.getId(), new AddMealToMenuRequest(recipeDto2.getId()), HttpStatus.FORBIDDEN);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContainsIgnoreCase("private recipe")
                .messageContainsIgnoreCase("not the author");
    }

    @Test
    void whenAddMealToMenu_withUserNotInTheSameOrganization_thenForbidden() {
        // arrange
        accountRepository.saveAndFlush(defaultAccount2());
        organizationService.createOrganization(defaultCreateOrganizationDto2(), DEFAULT_USERNAME_2);
        var recipeDto2 = recipeService.createRecipe(defaultRecipeCreateEditDto1().name("Test").author(DEFAULT_USERNAME_2).visibility(RecipeVisibility.ORGANIZATION));

        // act
        var errorResponse = rest.requestFails(menu1.getId(), new AddMealToMenuRequest(recipeDto2.getId()), HttpStatus.FORBIDDEN);

        // assert
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContainsIgnoreCase("does not share an organization");
    }

    @Test
    void whenAddMealToMenu_withUserDoesntOwnRecipe_butAdmin_thenNoContent() {
        var recipeDto2 = recipeService.createRecipe(defaultRecipeCreateEditDto1().name("Test").author(DEFAULT_USERNAME + "_NEW"));
        rest.requestSuccessful(menu1.getId(), new AddMealToMenuRequest(recipeDto2.getId()), new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_ADMIN"))));
    }

    @Test
    void whenAddMealToMenu_withUserIsNotInOrganization_thenForbidden() {
        organizationAccountRelationRepository.deleteAll();
        var errorResponse = rest.requestFails(menu1.getId(), defaultAddMealToMenuRequest(), HttpStatus.FORBIDDEN);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContainsIgnoreCase("not in the required organization");
    }

    @Test
    void whenAddMealToMenu_withUserIsHasNotEnoughPermissions_thenForbidden() {
        organizationAccountRelationRepository.deleteAll();
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation.builder()
                .role(OrganizationRole.INVITED)
                .organization(organization1)
                .account(account1)
                .build()
        );
        var errorResponse = rest.requestFails(menu1.getId(), defaultAddMealToMenuRequest(), HttpStatus.FORBIDDEN);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContainsIgnoreCase("not enough permissions");
    }

    @Test
    void whenAddMealToMenu_withUserIsNotInOrganization_butAdmin_thenNoContent() {
        organizationAccountRelationRepository.deleteAll();
        rest.requestSuccessful(menu1.getId(), defaultAddMealToMenuRequest(), new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_ADMIN"))));
    }

    @Test
    void whenAddMealToMenu_withUserNotEnoughPermissions_thenForbidden() {
        organizationAccountRelationRepository.deleteAll();
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation.builder()
                .role(OrganizationRole.SHOPPER)
                .organization(organization1)
                .account(account1)
                .build()
        );
        var errorResponse = rest.requestFails(menu1.getId(), defaultAddMealToMenuRequest(), HttpStatus.FORBIDDEN);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContainsIgnoreCase("not enough permissions");
    }

    @Test
    void basicAuthTests() {
        rest.basicAuthTests(1L, new AddMealToMenuRequest().recipeId(1L));
    }

    private RecipeCreateEditDto defaultCreateEditRecipeDto() {
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

    private AddMealToMenuRequest defaultAddMealToMenuRequest() {
        return new AddMealToMenuRequest(recipeDto.getId());
    }

}
