package at.codemaestro.integration_test.meals;

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
import org.openapitools.model.MealEditDto;
import org.openapitools.model.RecipeCreateEditDto;
import org.openapitools.model.RecipeDto;
import org.openapitools.model.RecipeVisibility;
import org.springframework.http.HttpStatus;

import java.util.List;

import static at.codemaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static at.codemaestro.integration_test.utils.test_constants.DefaultAccountTestData.defaultAccount;
import static at.codemaestro.integration_test.utils.test_constants.DefaultCookingApplianceTestData.defaultCookingAppliance1;
import static at.codemaestro.integration_test.utils.test_constants.DefaultIngredientTestData.defaultIngredient1;
import static at.codemaestro.integration_test.utils.test_constants.DefaultIngredientTestData.defaultIngredient2;
import static at.codemaestro.integration_test.utils.test_constants.DefaultMealTestData.DEFAULT_NEW_MEAL_NAME_1;
import static at.codemaestro.integration_test.utils.test_constants.DefaultMealTestData.DEFAULT_NEW_NUMBER_OF_PEOPLE_1;
import static at.codemaestro.integration_test.utils.test_constants.DefaultMealTestData.defaultMealEditDto;
import static at.codemaestro.integration_test.utils.test_constants.DefaultMenuTestData.defaultMenu1;
import static at.codemaestro.integration_test.utils.test_constants.DefaultOrganizationTestData.defaultOrganization1;
import static at.codemaestro.integration_test.utils.test_constants.DefaultRecipeTestData.defaultRecipeCreateEditDto1;
import static org.assertj.core.api.Assertions.assertThat;

public class EditMealWebIntegrationTest extends BaseWebIntegrationTest {

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
    void whenEditMeal_withValidNewParameter_thenOK() {
        Long id = meal1.getId();
        var response = editMealSuccessful(id, defaultMealEditDto());
        assertThat(response.getName()).isEqualTo(DEFAULT_NEW_MEAL_NAME_1);
        assertThat(response.getNumberOfPeople()).isEqualTo(DEFAULT_NEW_NUMBER_OF_PEOPLE_1);
    }

    @Test
    void whenEditMeal_withValidRecipeValue_thenOK() {
        Long id = meal1.getId();
        var response = editMealSuccessful(id, defaultMealEditDto().recipe(new RecipeCreateEditDto()
                .name("bla bla")
                .author("testuser")
                .visibility(RecipeVisibility.PUBLIC)
                .description("description here")
                .addIngredientsItem(new IngredientUseCreateEditDto(ingredient1.getId(), IngredientUnitDto.LITRES, 42.3f))
                .servings(2)));
        assertThat(response.getName()).isEqualTo(DEFAULT_NEW_MEAL_NAME_1);
        assertThat(response.getNumberOfPeople()).isEqualTo(DEFAULT_NEW_NUMBER_OF_PEOPLE_1);
        assertThat(response.getRecipe().getDescription()).isEqualTo("description here");
        assertThat(response.getRecipe().getIngredients().size()).isEqualTo(1);
        assertThat(response.getRecipe().getIngredients().getFirst().getAmount()).isEqualTo(42.3f);
    }

    @Test
    void whenEditMeal_withNonExistentMeal_thenNonFound() {
        var errorResponse = editMealFails(-666L, defaultMealEditDto());
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.NOT_FOUND)
                .messageContains("not found");
    }

    @Test
    void whenEditMeal_withMissingAuthHeader_thenForbidden() {
        var errorResponse = editMealFails(-666L,
                defaultMealEditDto(),
                new Headers());
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("Access Denied");
    }

    @Test
    void whenEditMeal_withInvalidAuthHeader_thenUnauthorized() {
        var errorResponse = editMealFails(-666L,
                defaultMealEditDto(),
                new Headers(new Header("Authorization", "Bearer LOL")));
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNAUTHORIZED)
                .messageContains("Invalid JWT token");
    }

    @Test
    void whenEditMeal_withNotInOrganisation_thenForbidden() {
        var errorResponse = editMealFails(meal1.getId(),
                defaultMealEditDto(),
                new Headers(generateValidAuthorizationHeader("account_1", List.of("ROLE_USER"))));
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("not in the required organization");
    }

    @Test
    void whenEditMeal_withNotEnoughPermissions_thenForbidden() {
        organizationAccountRelationRepository.deleteAll();
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation.builder()
                .role(OrganizationRole.SHOPPER)
                .organization(organization1)
                .account(account1)
                .build()
        );

        var errorResponse = editMealFails(meal1.getId(), defaultMealEditDto());
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("not enough permissions")
                .messageContains("PLANNER");
    }

    @Test
    void whenEditMeal_withMenuClosed_thenValidationException() {
        menu1.setStatus(MenuStatus.CLOSED);
        menuRepository.saveAndFlush(menu1);

        var errorResponse = editMealFails(meal1.getId(), defaultMealEditDto());
        ErrorResponseAssert.assertThat(errorResponse)
            .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
            .messageContains("Meal cannot be changed, menu it belongs to was closed!");
    }

    @Test
    void whenEditMeal_withNotInOrganisationButAdmin_thenOK() {
        editMeal(
                meal1.getId(),
                defaultMealEditDto(),
                new Headers(generateValidAuthorizationHeader("someuser", List.of("ROLE_ADMIN")))
        )
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    private MealDto editMealSuccessful(Long id, MealEditDto dto) {
        return editMeal(id, dto)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(MealDto.class);
    }

    private ErrorResponse editMealFails(Long id, MealEditDto dto) {
        return editMealFails(
                id,
                dto,
                new Headers(List.of(this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER"))))
        );
    }

    private ErrorResponse editMealFails(Long id, MealEditDto dto, Headers headers) {
        return editMeal(id, dto, headers)
                .then()
                .extract()
                .as(ErrorResponse.class);
    }

    private Response editMeal(Long id, MealEditDto dto) {
        return editMeal(id, dto, new Headers(List.of(this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER")))));
    }

    private Response editMeal(Long id, MealEditDto dto, Headers headers) {
        return RestAssured
                .given()
                .contentType("application/json")
                .body(dto)
                .headers(headers)
                .when()
                .patch(URI + "/" + id);
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
