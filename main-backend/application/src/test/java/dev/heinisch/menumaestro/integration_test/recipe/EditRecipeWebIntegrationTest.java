package dev.heinisch.menumaestro.integration_test.recipe;

import dev.heinisch.menumaestro.domain.cooking_appliance.CookingAppliance;
import dev.heinisch.menumaestro.domain.ingredient.Ingredient;
import dev.heinisch.menumaestro.domain.recipe.Recipe;
import dev.heinisch.menumaestro.domain.recipe.RecipeCookingApplianceUse;
import dev.heinisch.menumaestro.domain.recipe.RecipeIngredientUse;
import dev.heinisch.menumaestro.domain.recipe.RecipeValue;
import dev.heinisch.menumaestro.domain.recipe.RecipeVisibility;
import dev.heinisch.menumaestro.integration_test.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.integration_test.utils.ErrorResponseAssert;
import io.restassured.RestAssured;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.CookingApplianceUseCreateEditDto;
import org.openapitools.model.CookingApplianceUseDto;
import org.openapitools.model.ErrorResponse;
import org.openapitools.model.IngredientUnitDto;
import org.openapitools.model.IngredientUseCreateEditDto;
import org.openapitools.model.IngredientUseDto;
import org.openapitools.model.RecipeCreateEditDto;
import org.openapitools.model.RecipeDto;
import org.springframework.http.HttpStatus;

import java.util.Comparator;
import java.util.List;

import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultCookingApplianceTestData.defaultCookingAppliance1;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultCookingApplianceTestData.defaultCookingAppliance2;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultCookingApplianceTestData.defaultCookingAppliance3;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultIngredientTestData.defaultIngredient1;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultIngredientTestData.defaultIngredient2;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultIngredientTestData.defaultIngredient3;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultRecipeTestData.defaultRecipeCreateEditDto1;

class EditRecipeWebIntegrationTest extends BaseWebIntegrationTest {

    private Ingredient ingredient1;
    private Ingredient ingredient2;
    private Ingredient ingredient3;
    private CookingAppliance appliance1;
    private CookingAppliance appliance2;
    private CookingAppliance appliance3;

    private RecipeDto recipeDto;

    @Override
    protected String getBasePath() {
        return "/recipes";
    }

    @BeforeEach
    public void setup() {
        ingredient1 = ingredientRepository.save(defaultIngredient1());
        ingredient2 = ingredientRepository.save(defaultIngredient2());
        ingredient3 = ingredientRepository.save(defaultIngredient3());
        appliance1 = cookingApplianceRepository.save(defaultCookingAppliance1());
        appliance2 = cookingApplianceRepository.save(defaultCookingAppliance2());
        appliance3 = cookingApplianceRepository.save(defaultCookingAppliance3());
        recipeDto = recipeService.createRecipe(defaultCreateEditRecipeDto());
    }

    @Test
    public void whenEditRecipe_withValidData_thenOK() {
        var editDto = defaultCreateEditRecipeDto()
                .author("Hello")
                .name("Blub")
                .description("I am a description")
                .addIngredientsItem(new IngredientUseCreateEditDto().id(ingredient3.getId()).unit(IngredientUnitDto.MILLILITRES).amount(3.2F))
                .addCookingAppliancesItem(new CookingApplianceUseCreateEditDto().id(appliance3.getId()).amount(2));

        var result = editRecipeSuccessful(recipeDto.getId(), editDto);

        assertRecipeEntityMatchesRequestDto(editDto, result.getId());
        assertRecipeDtoMatchesRequestDto(editDto, result);
    }

    @Test
    public void whenEditRecipe_withNonExistentRecipe_thenNotFound() {
        var editDto = defaultCreateEditRecipeDto()
                .author("Hello")
                .name("Blub")
                .description("I am a description")
                .addIngredientsItem(new IngredientUseCreateEditDto().id(ingredient3.getId()).unit(IngredientUnitDto.MILLILITRES).amount(3.2F))
                .addCookingAppliancesItem(new CookingApplianceUseCreateEditDto().id(appliance3.getId()).amount(2));

        var errorResponse = editRecipeFails(666L, editDto, HttpStatus.NOT_FOUND);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.NOT_FOUND)
                .messageContains("not found");
    }

    @Test
    void whenEditRecipe_withSomeValuesInvalid_thenUnprocessableEntity() {
        var editDto = defaultCreateEditRecipeDto()
                .description("") // allowed, optional description
                .author("") // 1st violation
                .servings(null); // 2nd violation

        var errorResponse = editRecipeFails(recipeDto.getId(), editDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error occurred")
                .detailsContainSubstring("servings is missing")
                .detailsContainSubstring("author cannot be blank")
                .detailsHaveSize(2);
    }

    @Test
    void whenEditRecipe_withNegativeServings_thenUnprocessableEntity() {
        var editDto = defaultCreateEditRecipeDto()
                .servings(-5);

        var errorResponse = editRecipeFails(recipeDto.getId(), editDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error occurred")
                .detailsContainSubstring("servings is not positive")
                .detailsHaveSize(1);
    }

    @Test
    void whenEditRecipe_withBlankName_thenUnprocessableEntity() {
        var editDto = defaultCreateEditRecipeDto()
                .name("");

        var errorResponse = editRecipeFails(recipeDto.getId(), editDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error occurred")
                .detailsContainSubstring("cannot be blank");
    }

    @Test
    void whenEditRecipe_withTooLongName_thenUnprocessableEntity() {
        var editDto = defaultCreateEditRecipeDto()
                .name("Blub".repeat(40));

        var errorResponse = editRecipeFails(recipeDto.getId(), editDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error occurred")
                .detailsContainSubstring("is too long");
    }

    @Test
    void whenEditRecipe_withBlankAuthor_thenUnprocessableEntity() {
        var editDto = defaultCreateEditRecipeDto()
                .author("");

        var errorResponse = editRecipeFails(recipeDto.getId(), editDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error occurred")
                .detailsContainSubstring("cannot be blank");
    }

    @Test
    void whenEditRecipe_withTooLongAuthor_thenUnprocessableEntity() {
        var editDto = defaultCreateEditRecipeDto()
                .author("Blub".repeat(20));

        var errorResponse = editRecipeFails(recipeDto.getId(), editDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error occurred")
                .detailsContainSubstring("is too long");
    }

    @Test
    void whenEditRecipe_withTooLongDescription_thenUnprocessableEntity() {
        var editDto = defaultCreateEditRecipeDto()
                .description("Blub".repeat(300));

        var errorResponse = editRecipeFails(recipeDto.getId(), editDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error occurred")
                .detailsContainSubstring("is too long");
    }

    @Test
    void whenEditRecipe_withNullVisibility_thenUnprocessableEntity() {
        var editDto = defaultCreateEditRecipeDto()
                .visibility(null);

        var errorResponse = editRecipeFails(recipeDto.getId(), editDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error occurred")
                .detailsContainSubstring("is missing");
    }

    @Test
    void whenEditRecipe_withNullIngredients_thenUnprocessableEntity() {
        var editDto = defaultCreateEditRecipeDto()
                .ingredients(null);

        var errorResponse = editRecipeFails(recipeDto.getId(), editDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error occurred")
                .detailsContainSubstring("is missing");
    }

    @Test
    void whenEditRecipe_withNullCookingAppliances_thenUnprocessableEntity() {
        var editDto = defaultCreateEditRecipeDto()
                .cookingAppliances(null);

        var errorResponse = editRecipeFails(recipeDto.getId(), editDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error occurred")
                .detailsContainSubstring("is missing");
    }

    @Test
    void whenEditRecipe_withEmptyIngredients_thenUnprocessableEntity() {
        var editDto = defaultCreateEditRecipeDto()
                .ingredients(List.of());

        var errorResponse = editRecipeFails(recipeDto.getId(), editDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error occurred")
                .detailsContainSubstring("is empty");
    }

    @Test
    void whenEditRecipe_withInvalidIngredients_thenUnprocessableEntity() {
        var editDto = defaultCreateEditRecipeDto()
                .addIngredientsItem(new IngredientUseCreateEditDto()
                        .id(null)
                        .amount(-10F)
                        .unit(null))
                .addIngredientsItem(new IngredientUseCreateEditDto()
                        .id(null)
                        .amount(null)
                        .unit(IngredientUnitDto.CUPS));

        var errorResponse = editRecipeFails(recipeDto.getId(), editDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error occurred")
                .detailsContainSubstring("id in ingredient list is missing")
                .detailsContainSubstring("amount in ingredient list is not positive")
                .detailsContainSubstring("unit in ingredient list is missing")
                .detailsContainSubstring("amount in ingredient list is missing")
                .detailsContainSubstring("id in ingredient list is missing")
                .detailsHaveSize(5);
    }

    @Test
    void whenEditRecipe_withInvalidCookingAppliance_thenUnprocessableEntity() {
        var editDto = defaultCreateEditRecipeDto()
                .addCookingAppliancesItem(new CookingApplianceUseCreateEditDto()
                        .id(null)
                        .amount(-10))
                .addCookingAppliancesItem(new CookingApplianceUseCreateEditDto()
                        .id(null)
                        .amount(null));

        var errorResponse = editRecipeFails(recipeDto.getId(), editDto, HttpStatus.UNPROCESSABLE_ENTITY);
        System.out.println(errorResponse);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error occurred")
                .detailsContainSubstring("id in cooking appliance list is missing")
                .detailsContainSubstring("amount in cooking appliance list is not positive")
                .detailsContainSubstring("id in cooking appliance list is missing")
                .detailsContainSubstring("amount in cooking appliance list is missing")
                .detailsHaveSize(4);
    }

    @Test
    void whenEditRecipe_withSomeIngredientNotExists_thenConflict() {
        var editDto = defaultCreateEditRecipeDto()
                .addIngredientsItem(new IngredientUseCreateEditDto()
                        .unit(IngredientUnitDto.LITRES)
                        .amount(0.25F)
                        .id(666L));

        var errorResponse = editRecipeFails(recipeDto.getId(), editDto, HttpStatus.CONFLICT);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.CONFLICT)
                .messageContains("Conflict error")
                .detailsContainSubstring("not exist")
                .detailsHaveSize(1);
    }

    @Test
    void whenEditRecipe_withDuplicateIngredients_thenConflict() {
        var editDto = defaultCreateEditRecipeDto()
                .addIngredientsItem(new IngredientUseCreateEditDto()
                        .unit(IngredientUnitDto.LITRES)
                        .amount(0.25F)
                        .id(ingredient1.getId()))
                .addIngredientsItem(new IngredientUseCreateEditDto()
                        .unit(IngredientUnitDto.LITRES)
                        .amount(0.5F)
                        .id(ingredient1.getId()));

        var errorResponse = editRecipeFails(recipeDto.getId(), editDto, HttpStatus.CONFLICT);
        System.out.println(errorResponse);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.CONFLICT)
                .messageContains("Conflict error")
                .detailsContainSubstring("duplicate")
                .detailsHaveSize(1);
    }

    @Test
    void whenEditRecipe_withSomeCookingApplianceNotExists_thenConflict() {
        var editDto = defaultCreateEditRecipeDto()
                .addCookingAppliancesItem(new CookingApplianceUseCreateEditDto()
                        .amount(3)
                        .id(666L));

        var errorResponse = editRecipeFails(recipeDto.getId(), editDto, HttpStatus.CONFLICT);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.CONFLICT)
                .messageContains("Conflict error")
                .detailsContainSubstring("not exist")
                .detailsHaveSize(1);
    }

    @Test
    void whenEditRecipe_withDuplicateCookingAppliances_thenConflict() {
        var editDto = defaultCreateEditRecipeDto()
                .addCookingAppliancesItem(new CookingApplianceUseCreateEditDto()
                        .amount(3)
                        .id(appliance1.getId())
                )
                .addCookingAppliancesItem(new CookingApplianceUseCreateEditDto()
                        .amount(5)
                        .id(appliance1.getId())
                );

        var errorResponse = editRecipeFails(recipeDto.getId(), editDto, HttpStatus.CONFLICT);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.CONFLICT)
                .messageContains("Conflict error")
                .detailsContainSubstring("duplicate")
                .detailsHaveSize(1);
    }


    private void assertRecipeEntityMatchesRequestDto(RecipeCreateEditDto dto, Long recipeId) {
        Recipe entity = recipeRepository.findById(recipeId).orElseThrow();
        RecipeValue value = entity.getRecipeValue();
        Assertions.assertAll(
                () -> Assertions.assertNotNull(entity.getId()),
                () -> Assertions.assertEquals(dto.getName(), value.getName()),
                () -> Assertions.assertEquals(dto.getServings(), value.getServings()),
                () -> Assertions.assertEquals(dto.getAuthor(), value.getAuthor()),
                () -> Assertions.assertEquals(RecipeVisibility.PUBLIC, entity.getVisibility()),
                () -> Assertions.assertEquals(dto.getIngredients().size(), value.getIngredients().size()),
                () -> Assertions.assertEquals(dto.getCookingAppliances().size(), value.getCookingAppliances().size())
        );

        Assertions.assertIterableEquals(
                dto.getIngredients()
                        .stream()
                        .sorted(Comparator.comparingLong(IngredientUseCreateEditDto::getId))
                        .toList(),
                value.getIngredients()
                        .stream()
                        .sorted(Comparator.comparingLong(RecipeIngredientUse::getIngredientId))
                        .map((i) -> new IngredientUseCreateEditDto()
                                .id(i.getIngredientId())
                                .amount(i.getAmount())
                                .unit(IngredientUnitDto.fromValue(i.getUnit().name())))
                        .toList()
        );
        Assertions.assertIterableEquals(
                dto.getCookingAppliances()
                        .stream()
                        .sorted(Comparator.comparingLong(CookingApplianceUseCreateEditDto::getId))
                        .toList(),
                value.getCookingAppliances()
                        .stream()
                        .sorted(Comparator.comparingLong(RecipeCookingApplianceUse::getCookingApplianceId))
                        .map((c) -> new CookingApplianceUseCreateEditDto()
                                .id(c.getCookingApplianceId())
                                .amount(c.getAmount()))
                        .toList()
        );
    }

    private void assertRecipeDtoMatchesRequestDto(RecipeCreateEditDto dto, RecipeDto recipeDto) {
        Assertions.assertAll(
                () -> Assertions.assertNotNull(recipeDto.getId()),
                () -> Assertions.assertEquals(dto.getName(), recipeDto.getName()),
                () -> Assertions.assertEquals(dto.getServings(), recipeDto.getServings()),
                () -> Assertions.assertEquals(dto.getAuthor(), recipeDto.getAuthor()),
                () -> Assertions.assertEquals(org.openapitools.model.RecipeVisibility.PUBLIC, recipeDto.getVisibility()),
                () -> Assertions.assertEquals(dto.getIngredients().size(), recipeDto.getIngredients().size()),
                () -> Assertions.assertEquals(dto.getCookingAppliances().size(), recipeDto.getCookingAppliances().size())
        );

        Assertions.assertIterableEquals(
                dto.getIngredients()
                        .stream()
                        .sorted(Comparator.comparingLong(IngredientUseCreateEditDto::getId))
                        .toList(),
                recipeDto.getIngredients()
                        .stream()
                        .sorted(Comparator.comparingLong(IngredientUseDto::getId))
                        .map((i) -> new IngredientUseCreateEditDto()
                                .id(i.getId())
                                .amount(i.getAmount())
                                .unit(IngredientUnitDto.fromValue(i.getUnit().name())))
                        .toList()
        );
        Assertions.assertIterableEquals(
                dto.getCookingAppliances()
                        .stream()
                        .sorted(Comparator.comparingLong(CookingApplianceUseCreateEditDto::getId))
                        .toList(),
                recipeDto.getCookingAppliances()
                        .stream()
                        .sorted(Comparator.comparingLong(CookingApplianceUseDto::getId))
                        .map((c) -> new CookingApplianceUseCreateEditDto()
                                .id(c.getId())
                                .amount(c.getAmount()))
                        .toList()
        );
    }

    private RecipeDto editRecipeSuccessful(Long id, RecipeCreateEditDto recipeCreateEditDto) {
        return editRecipe(id, recipeCreateEditDto)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(RecipeDto.class);
    }

    private ErrorResponse editRecipeFails(Long id, RecipeCreateEditDto recipeCreateEditDto, HttpStatus status) {
        return editRecipeFails(
                id,
                recipeCreateEditDto,
                status,
                new Headers(List.of(this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER", "ROLE_ADMIN"))))
        );
    }

    private ErrorResponse editRecipeFails(Long id, RecipeCreateEditDto recipeCreateEditDto, HttpStatus status, Headers headers) {
        return editRecipe(id, recipeCreateEditDto, headers)
                .then()
                .statusCode(status.value())
                .log().all()
                .extract()
                .as(ErrorResponse.class);
    }

    private Response editRecipe(Long id, RecipeCreateEditDto recipeCreateEditDto) {
        return editRecipe(id, recipeCreateEditDto, new Headers(List.of(this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER", "ROLE_ADMIN")))));
    }

    private Response editRecipe(Long id, RecipeCreateEditDto recipeCreateEditDto, Headers headers) {
        return RestAssured
                .given()
                .contentType("application/json")
                .headers(headers)
                .body(recipeCreateEditDto)
                .when()
                .put(URI + "/" + id);
    }

    private RecipeCreateEditDto defaultCreateEditRecipeDto() {
        return defaultRecipeCreateEditDto1()
                .addCookingAppliancesItem(new CookingApplianceUseCreateEditDto()
                        .amount(1)
                        .id(appliance1.getId()))
                .addCookingAppliancesItem(new CookingApplianceUseCreateEditDto()
                        .amount(1)
                        .id(appliance2.getId()))
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
