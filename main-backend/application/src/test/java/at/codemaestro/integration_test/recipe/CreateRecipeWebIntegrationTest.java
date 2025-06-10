package at.codemaestro.integration_test.recipe;

import at.codemaestro.domain.cooking_appliance.CookingAppliance;
import at.codemaestro.domain.ingredient.Ingredient;
import at.codemaestro.domain.recipe.Recipe;
import at.codemaestro.domain.recipe.RecipeCookingApplianceUse;
import at.codemaestro.domain.recipe.RecipeIngredientUse;
import at.codemaestro.domain.recipe.RecipeValue;
import at.codemaestro.domain.recipe.RecipeVisibility;
import at.codemaestro.integration_test.BaseWebIntegrationTest;
import at.codemaestro.integration_test.utils.ErrorResponseAssert;
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

import static at.codemaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static at.codemaestro.integration_test.utils.test_constants.DefaultCookingApplianceTestData.defaultCookingAppliance1;
import static at.codemaestro.integration_test.utils.test_constants.DefaultCookingApplianceTestData.defaultCookingAppliance2;
import static at.codemaestro.integration_test.utils.test_constants.DefaultIngredientTestData.defaultIngredient1;
import static at.codemaestro.integration_test.utils.test_constants.DefaultIngredientTestData.defaultIngredient2;
import static at.codemaestro.integration_test.utils.test_constants.DefaultRecipeTestData.defaultRecipeCreateEditDto1;


class CreateRecipeWebIntegrationTest extends BaseWebIntegrationTest {

    private Ingredient ingredient1;
    private Ingredient ingredient2;
    private CookingAppliance appliance1;
    private CookingAppliance appliance2;

    @Override
    protected String getBasePath() {
        return "/recipes";
    }

    @BeforeEach
    public void setup() {
        ingredient1 = ingredientRepository.save(defaultIngredient1());
        ingredient2 = ingredientRepository.save(defaultIngredient2());
        appliance1 = cookingApplianceRepository.save(defaultCookingAppliance1());
        appliance2 = cookingApplianceRepository.save(defaultCookingAppliance2());
    }

    @Test
    public void whenCreateRecipe_withValidData_thenCreated() {
        var createRecipeDto = defaultCreateEditRecipeDto();

        var recipeDto = createRecipeSuccessful(createRecipeDto);

        assertRecipeEntityMatchesRequestDto(createRecipeDto, recipeDto.getId());
        assertRecipeDtoMatchesRequestDto(createRecipeDto, recipeDto);
    }

    @Test
    void whenCreateRecipe_withSomeValuesInvalid_thenUnprocessableEntity() {
        var createRecipeDto = defaultCreateEditRecipeDto()
                .description("") // allowed, optional description
                .author("") // 1st violation
                .servings(null); // 2nd violation

        var errorResponse = createRecipeFails(createRecipeDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error occurred")
                .detailsContainSubstring("servings is missing")
                .detailsContainSubstring("author cannot be blank")
                .detailsHaveSize(2);
    }

    @Test
    void whenCreateRecipe_withNegativeServings_thenUnprocessableEntity() {
        var createRecipeDto = defaultCreateEditRecipeDto()
                .servings(-5);

        var errorResponse = createRecipeFails(createRecipeDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error occurred")
                .detailsContainSubstring("servings is not positive")
                .detailsHaveSize(1);
    }

    @Test
    void whenCreateRecipe_withBlankName_thenUnprocessableEntity() {
        var createRecipeDto = defaultCreateEditRecipeDto()
                .name("");

        var errorResponse = createRecipeFails(createRecipeDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error occurred")
                .detailsContainSubstring("cannot be blank");
    }

    @Test
    void whenCreateRecipe_withTooLongName_thenUnprocessableEntity() {
        var createRecipeDto = defaultCreateEditRecipeDto()
                .name("Blub".repeat(40));

        var errorResponse = createRecipeFails(createRecipeDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error occurred")
                .detailsContainSubstring("is too long");
    }

    @Test
    void whenCreateRecipe_withBlankAuthor_thenUnprocessableEntity() {
        var createRecipeDto = defaultCreateEditRecipeDto()
                .author("");

        var errorResponse = createRecipeFails(createRecipeDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error occurred")
                .detailsContainSubstring("cannot be blank");
    }

    @Test
    void whenCreateRecipe_withTooLongAuthor_thenUnprocessableEntity() {
        var createRecipeDto = defaultCreateEditRecipeDto()
                .author("Blub".repeat(20));

        var errorResponse = createRecipeFails(createRecipeDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error occurred")
                .detailsContainSubstring("is too long");
    }

    @Test
    void whenCreateRecipe_withTooLongDescription_thenUnprocessableEntity() {
        var createRecipeDto = defaultCreateEditRecipeDto()
                .description("Blub".repeat(300));

        var errorResponse = createRecipeFails(createRecipeDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error occurred")
                .detailsContainSubstring("is too long");
    }

    @Test
    void whenCreateRecipe_withNullVisibility_thenUnprocessableEntity() {
        var createRecipeDto = defaultCreateEditRecipeDto()
                .visibility(null);

        var errorResponse = createRecipeFails(createRecipeDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error occurred")
                .detailsContainSubstring("is missing");
    }

    @Test
    void whenCreateRecipe_withNullIngredients_thenUnprocessableEntity() {
        var createRecipeDto = defaultCreateEditRecipeDto()
                .ingredients(null);

        var errorResponse = createRecipeFails(createRecipeDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error occurred")
                .detailsContainSubstring("is missing");
    }

    @Test
    void whenCreateRecipe_withNullCookingAppliances_thenUnprocessableEntity() {
        var createRecipeDto = defaultCreateEditRecipeDto()
                .cookingAppliances(null);

        var errorResponse = createRecipeFails(createRecipeDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error occurred")
                .detailsContainSubstring("is missing");
    }

    @Test
    void whenCreateRecipe_withEmptyIngredients_thenUnprocessableEntity() {
        var createRecipeDto = defaultCreateEditRecipeDto()
                .ingredients(List.of());

        var errorResponse = createRecipeFails(createRecipeDto, HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error occurred")
                .detailsContainSubstring("is empty");
    }

    @Test
    void whenCreateRecipe_withInvalidIngredients_thenUnprocessableEntity() {
        var createRecipeDto = defaultCreateEditRecipeDto()
                .addIngredientsItem(new IngredientUseCreateEditDto()
                        .id(null)
                        .amount(-10F)
                        .unit(null))
                .addIngredientsItem(new IngredientUseCreateEditDto()
                        .id(null)
                        .amount(null)
                        .unit(IngredientUnitDto.CUPS));

        var errorResponse = createRecipeFails(createRecipeDto, HttpStatus.UNPROCESSABLE_ENTITY);
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
    void whenCreateRecipe_withInvalidCookingAppliance_thenUnprocessableEntity() {
        var createRecipeDto = defaultCreateEditRecipeDto()
                .addCookingAppliancesItem(new CookingApplianceUseCreateEditDto()
                        .id(null)
                        .amount(-10))
                .addCookingAppliancesItem(new CookingApplianceUseCreateEditDto()
                        .id(null)
                        .amount(null));

        var errorResponse = createRecipeFails(createRecipeDto, HttpStatus.UNPROCESSABLE_ENTITY);
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
    void whenCreateRecipe_withSomeIngredientNotExists_thenConflict() {
        var createRecipeDto = defaultCreateEditRecipeDto()
                .addIngredientsItem(new IngredientUseCreateEditDto()
                        .unit(IngredientUnitDto.LITRES)
                        .amount(0.25F)
                        .id(666L));

        var errorResponse = createRecipeFails(createRecipeDto, HttpStatus.CONFLICT);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.CONFLICT)
                .messageContains("Conflict error")
                .detailsContainSubstring("not exist")
                .detailsHaveSize(1);
    }

    @Test
    void whenCreateRecipe_withDuplicateIngredients_thenConflict() {
        var createRecipeDto = defaultCreateEditRecipeDto()
                .addIngredientsItem(new IngredientUseCreateEditDto()
                        .unit(IngredientUnitDto.LITRES)
                        .amount(0.25F)
                        .id(ingredient1.getId()))
                .addIngredientsItem(new IngredientUseCreateEditDto()
                        .unit(IngredientUnitDto.LITRES)
                        .amount(0.5F)
                        .id(ingredient1.getId()));

        var errorResponse = createRecipeFails(createRecipeDto, HttpStatus.CONFLICT);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.CONFLICT)
                .messageContains("Conflict error")
                .detailsContainSubstring("duplicate")
                .detailsHaveSize(1);
    }

    @Test
    void whenCreateRecipe_withSomeCookingApplianceNotExists_thenConflict() {
        var createRecipeDto = defaultCreateEditRecipeDto()
                .addCookingAppliancesItem(new CookingApplianceUseCreateEditDto()
                        .amount(3)
                        .id(666L));

        var errorResponse = createRecipeFails(createRecipeDto, HttpStatus.CONFLICT);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.CONFLICT)
                .messageContains("Conflict error")
                .detailsContainSubstring("not exist")
                .detailsHaveSize(1);
    }

    @Test
    void whenCreateRecipe_withDuplicateCookingAppliances_thenConflict() {
        var createRecipeDto = defaultCreateEditRecipeDto()
                .addCookingAppliancesItem(new CookingApplianceUseCreateEditDto()
                        .amount(3)
                        .id(appliance1.getId())
                )
                .addCookingAppliancesItem(new CookingApplianceUseCreateEditDto()
                        .amount(5)
                        .id(appliance1.getId())
                );

        var errorResponse = createRecipeFails(createRecipeDto, HttpStatus.CONFLICT);
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

    private RecipeDto createRecipeSuccessful(RecipeCreateEditDto recipeCreateEditDto) {
        return createRecipe(recipeCreateEditDto)
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(RecipeDto.class);
    }

    private ErrorResponse createRecipeFails(RecipeCreateEditDto recipeCreateEditDto, HttpStatus status) {
        return createRecipeFails(
                recipeCreateEditDto,
                status,
                new Headers(List.of(this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER", "ROLE_ADMIN"))))
        );
    }

    private ErrorResponse createRecipeFails(RecipeCreateEditDto recipeCreateEditDto, HttpStatus status, Headers headers) {
        return createRecipe(recipeCreateEditDto, headers)
                .then()
                .statusCode(status.value())
                .extract()
                .as(ErrorResponse.class);
    }

    private Response createRecipe(RecipeCreateEditDto recipeCreateEditDto) {
        return createRecipe(recipeCreateEditDto, new Headers(List.of(this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER", "ROLE_ADMIN")))));
    }

    private Response createRecipe(RecipeCreateEditDto recipeCreateEditDto, Headers headers) {
        return RestAssured
                .given()
                .contentType("application/json")
                .headers(headers)
                .body(recipeCreateEditDto)
                .when()
                .post(URI);
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
