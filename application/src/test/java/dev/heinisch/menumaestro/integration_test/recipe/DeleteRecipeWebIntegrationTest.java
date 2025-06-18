package dev.heinisch.menumaestro.integration_test.recipe;

import dev.heinisch.menumaestro.domain.cooking_appliance.CookingAppliance;
import dev.heinisch.menumaestro.domain.ingredient.Ingredient;
import dev.heinisch.menumaestro.integration_test.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.integration_test.utils.ErrorResponseAssert;
import io.restassured.RestAssured;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.CookingApplianceUseCreateEditDto;
import org.openapitools.model.ErrorResponse;
import org.openapitools.model.IngredientUnitDto;
import org.openapitools.model.IngredientUseCreateEditDto;
import org.openapitools.model.RecipeDto;
import org.springframework.http.HttpStatus;

import java.util.List;

import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultCookingApplianceTestData.defaultCookingAppliance1;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultIngredientTestData.defaultIngredient1;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultRecipeTestData.defaultRecipeCreateEditDto1;

class DeleteRecipeWebIntegrationTest extends BaseWebIntegrationTest {

    private RecipeDto recipe;

    @Override
    protected String getBasePath() {
        return "/recipes";
    }

    @BeforeEach
    public void setup() {
        Ingredient ingredient1 = ingredientRepository.save(defaultIngredient1());
        CookingAppliance appliance1 = cookingApplianceRepository.save(defaultCookingAppliance1());

        recipe = recipeService.createRecipe(defaultRecipeCreateEditDto1()
                .addIngredientsItem(new IngredientUseCreateEditDto().id(ingredient1.getId()).unit(IngredientUnitDto.CUPS).amount(1.2F))
                .addCookingAppliancesItem(new CookingApplianceUseCreateEditDto().id(appliance1.getId()).amount(2))
        );
    }

    @Test
    void whenDeleteRecipe_withNonExistingRecipe_thenNotFound() {
        var errorResponse = deleteRecipeFails(-666L, HttpStatus.NOT_FOUND);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.NOT_FOUND)
                .messageContains(String.format("Recipe with id '%d' not found", -666L));
    }

    @Test
    void whenDeleteRecipe_withValidId_thenNoContent() {
        Long recipeId = recipe.getId();
        deleteRecipeSuccess(recipeId);
        Assertions.assertTrue(recipeRepository.findById(recipeId).isEmpty());
    }


    private void deleteRecipeSuccess(Long id) {
        deleteRecipe(id)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    private void deleteRecipeSuccess(Long id, Headers headers) {
        deleteRecipe(id, headers)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    private ErrorResponse deleteRecipeFails(Long id, HttpStatus status) {
        return deleteRecipe(id)
                .then()
                .statusCode(status.value())
                .extract()
                .as(ErrorResponse.class);
    }

    private ErrorResponse deleteRecipeFails(Long id, HttpStatus status, Headers headers) {
        return deleteRecipe(id, headers)
                .then()
                .statusCode(status.value())
                .extract()
                .as(ErrorResponse.class);
    }

    private Response deleteRecipe(Long id) {
        return deleteRecipe(id, new Headers(List.of(this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER", "ROLE_ADMIN")))));
    }

    private Response deleteRecipe(Long id, Headers headers) {
        return RestAssured.given().headers(headers).delete(URI + "/{id}", id);
    }

}
