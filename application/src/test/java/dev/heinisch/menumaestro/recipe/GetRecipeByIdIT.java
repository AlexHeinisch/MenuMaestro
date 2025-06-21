package dev.heinisch.menumaestro.recipe;

import dev.heinisch.menumaestro.domain.cooking_appliance.CookingAppliance;
import dev.heinisch.menumaestro.domain.ingredient.Ingredient;
import dev.heinisch.menumaestro.domain.recipe.Recipe;
import dev.heinisch.menumaestro.domain.recipe.RecipeCookingApplianceUse;
import dev.heinisch.menumaestro.domain.recipe.RecipeIngredientUse;
import dev.heinisch.menumaestro.domain.recipe.RecipeValue;
import dev.heinisch.menumaestro.domain.recipe.RecipeVisibility;
import dev.heinisch.menumaestro.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.utils.ErrorResponseAssert;
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

import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultCookingApplianceTestData.defaultCookingAppliance1;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultCookingApplianceTestData.defaultCookingAppliance2;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultIngredientTestData.defaultIngredient1;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultIngredientTestData.defaultIngredient2;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultRecipeTestData.defaultRecipeCreateEditDto1;

class GetRecipeByIdIT extends BaseWebIntegrationTest {

    private Ingredient ingredient1;
    private Ingredient ingredient2;
    private CookingAppliance appliance1;
    private CookingAppliance appliance2;
    private RecipeDto recipeDto;

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
        recipeDto = recipeService.createRecipe(defaultCreateEditRecipeDto());
    }

    @Test
    void whenGetRecipeById_withValidId_thenOK() {
        var result = getRecipeByIdSuccessful(recipeDto.getId());

        assertRecipeEntityMatchesRequestDto(defaultCreateEditRecipeDto(), result.getId());
        assertRecipeDtoMatchesRequestDto(defaultCreateEditRecipeDto(), result);
    }

    @Test
    void whenGetRecipeById_withNonExistentId_thenNotFound() {
        var errorResponse = getRecipeByIdFails(666L, HttpStatus.NOT_FOUND);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.NOT_FOUND)
                .messageContains("not found");
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
                () -> Assertions.assertEquals(dto.getCookingAppliances().size(), recipeDto.getCookingAppliances().size()),
                () -> Assertions.assertEquals(dto.getImageId(), recipeDto.getImageId()),
                () -> Assertions.assertEquals(dto.getImageId() != null, recipeDto.getImageLink() != null, "image link is set IFF imageId is set")
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

    private RecipeDto getRecipeByIdSuccessful(Long id) {
        return getRecipeById(id)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(RecipeDto.class);
    }

    private ErrorResponse getRecipeByIdFails(Long id, HttpStatus status) {
        return getRecipeByIdFails(
                id,
                status,
                new Headers(List.of(this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER", "ROLE_ADMIN"))))
        );
    }

    private ErrorResponse getRecipeByIdFails(Long id, HttpStatus status, Headers headers) {
        return getRecipeById(id, headers)
                .then()
                .statusCode(status.value())
                .extract()
                .as(ErrorResponse.class);
    }

    private Response getRecipeById(Long id) {
        return getRecipeById(id, new Headers(List.of(this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER", "ROLE_ADMIN")))));
    }

    private Response getRecipeById(Long id, Headers headers) {
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
