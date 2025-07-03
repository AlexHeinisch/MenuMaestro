package dev.heinisch.menumaestro.meals;

import dev.heinisch.menumaestro.domain.account.Account;
import dev.heinisch.menumaestro.domain.cooking_appliance.CookingAppliance;
import dev.heinisch.menumaestro.domain.ingredient.Ingredient;
import dev.heinisch.menumaestro.domain.menu.Menu;
import dev.heinisch.menumaestro.domain.organization.Organization;
import dev.heinisch.menumaestro.domain.organization.OrganizationAccountRelation;
import dev.heinisch.menumaestro.domain.organization.OrganizationRole;
import dev.heinisch.menumaestro.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.utils.ErrorResponseAssert;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.AddMealToMenuRequest;
import org.openapitools.model.CookingApplianceUseCreateEditDto;
import org.openapitools.model.ErrorResponse;
import org.openapitools.model.IngredientUnitDto;
import org.openapitools.model.IngredientUseCreateEditDto;
import org.openapitools.model.MealDto;
import org.openapitools.model.RecipeCreateEditDto;
import org.openapitools.model.RecipeDto;
import org.springframework.http.HttpStatus;

import java.util.List;

import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.defaultAccount;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultCookingApplianceTestData.defaultCookingAppliance1;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultIngredientTestData.defaultIngredient1;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultIngredientTestData.defaultIngredient2;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultMenuTestData.defaultMenu1;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultOrganizationTestData.defaultOrganization1;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultRecipeTestData.DEFAULT_RECIPE_NAME_1;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultRecipeTestData.defaultRecipeCreateEditDto1;
import static org.assertj.core.api.Assertions.assertThat;

public class GetMealIT extends BaseWebIntegrationTest {

    private Organization organization1;
    private Ingredient ingredient1;
    private Ingredient ingredient2;
    private CookingAppliance appliance1;
    private Menu menu1;

    private RecipeDto recipeDto;
    private Account account1;
    private MealDto meal1;

    @Override
    protected String getBasePath() {
        return "/meals";
    }

    @BeforeEach
    public void setup() {
        organization1 = organizationRepository.saveAndFlush(defaultOrganization1());
        account1 = accountRepository.saveAndFlush(defaultAccount());
        organizationAccountRelationRepository.save(OrganizationAccountRelation.builder()
                .role(OrganizationRole.OWNER)
                .organization(organization1)
                .account(account1)
                .build());
        ingredient1 = ingredientRepository.save(defaultIngredient1());
        ingredient2 = ingredientRepository.save(defaultIngredient2());
        appliance1 = cookingApplianceRepository.save(defaultCookingAppliance1());
        recipeDto = recipeService.createRecipe(defaultCreateEditRecipeDto());
        menu1 = menuRepository.save(defaultMenu1(organization1.getId()));
        menuService.addMealToMenu(menu1.getId(), new AddMealToMenuRequest().recipeId(recipeDto.getId()));
        meal1 = mealService.getMealById(menuService.getMenuById(menu1.getId()).getMeals().get(0).getId());
    }

    @Test
    void whenGetMealById_withValidId_thenOK() {
        var response = getMealByIdSuccessful(meal1.getId());
        assertThat(response.getName()).isEqualTo(DEFAULT_RECIPE_NAME_1);
        assertThat(response.getRecipe().getName()).isEqualTo(DEFAULT_RECIPE_NAME_1);
    }

    @Test
    void whenGetMealById_withNonExistentMeal_thenNonFound() {
        var errorResponse = getMealByIdFails(-666L);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.NOT_FOUND)
                .messageContains("not found");
    }

    @Test
    void whenGetMealById_withMissingAuthHeader_thenForbidden() {
        var errorResponse = getMealByIdFails(-666L,
                new Headers());
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("Access Denied");
    }

    @Test
    void whenGetMealById_withInvalidAuthHeader_thenUnauthorized() {
        var errorResponse = getMealByIdFails(-666L,
                new Headers(new Header("Authorization", "Bearer LOL")));
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNAUTHORIZED)
                .messageContains("Invalid JWT token");
    }

    @Test
    void whenGetMealById_withNotInOrganisation_thenForbidden() {
        var errorResponse = getMealByIdFails(meal1.getId(),
                new Headers(generateValidAuthorizationHeader("account_1", List.of("ROLE_USER"))));
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("not in the required organization");
    }

    @Test
    void whenGetMealById_withNotEnoughPermissions_thenForbidden() {
        organizationAccountRelationRepository.deleteAll();
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation.builder()
                .role(OrganizationRole.INVITED)
                .organization(organization1)
                .account(account1)
                .build()
        );

        var errorResponse = getMealByIdFails(meal1.getId());
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("not enough permissions")
                .messageContains("MEMBER");
    }

    @Test
    void whenGetMealById_withNotInOrganisationButAdmin_thenOK() {
        getMealById(
                meal1.getId(),
                new Headers(generateValidAuthorizationHeader("someuser", List.of("ROLE_ADMIN")))
        )
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    private MealDto getMealByIdSuccessful(Long id) {
        return getMealById(id)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(MealDto.class);
    }

    private ErrorResponse getMealByIdFails(Long id) {
        return getMealByIdFails(
                id,
                new Headers(List.of(this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER"))))
        );
    }

    private ErrorResponse getMealByIdFails(Long id, Headers headers) {
        return getMealById(id, headers)
                .then()
                .extract()
                .as(ErrorResponse.class);
    }

    private Response getMealById(Long id) {
        return getMealById(id, new Headers(List.of(this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER")))));
    }

    private Response getMealById(Long id, Headers headers) {
        return RestAssured
                .given()
                .headers(headers)
                .when()
                .get(URI + "/" + id);
    }

    private RecipeCreateEditDto defaultCreateEditRecipeDto() {
        return defaultRecipeCreateEditDto1()
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

}
