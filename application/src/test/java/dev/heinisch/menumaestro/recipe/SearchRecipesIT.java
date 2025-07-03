package dev.heinisch.menumaestro.recipe;

import dev.heinisch.menumaestro.domain.cooking_appliance.CookingAppliance;
import dev.heinisch.menumaestro.domain.ingredient.Ingredient;
import dev.heinisch.menumaestro.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.utils.TestPageableResponse;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.openapitools.model.CookingApplianceUseCreateEditDto;
import org.openapitools.model.IngredientUnitDto;
import org.openapitools.model.IngredientUseCreateEditDto;
import org.openapitools.model.RecipeCreateEditDto;
import org.openapitools.model.RecipeDto;
import org.springframework.http.HttpStatus;

import java.util.List;

import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultCookingApplianceTestData.defaultCookingAppliance1;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultCookingApplianceTestData.defaultCookingAppliance2;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultCookingApplianceTestData.defaultCookingAppliance3;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultCookingApplianceTestData.defaultCookingAppliance4;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultIngredientTestData.defaultIngredient1;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultIngredientTestData.defaultIngredient2;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultIngredientTestData.defaultIngredient3;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultIngredientTestData.defaultIngredient4;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultRecipeTestData.defaultRecipeCreateEditDto1;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultRecipeTestData.defaultRecipeCreateEditDto2;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultRecipeTestData.defaultRecipeCreateEditDto3;
import static org.assertj.core.api.Assertions.assertThat;

class SearchRecipesIT extends BaseWebIntegrationTest {

    private Ingredient ingredient1;
    private Ingredient ingredient2;
    private Ingredient ingredient3;
    private Ingredient ingredient4;
    private CookingAppliance appliance1;
    private CookingAppliance appliance2;
    private CookingAppliance appliance3;
    private CookingAppliance appliance4;

    private RecipeDto recipe1;
    private RecipeDto recipe2;
    private RecipeDto recipe3;

    @Override
    protected String getBasePath() {
        return "/recipes";
    }

    @BeforeEach
    public void setup() {
        ingredient1 = ingredientRepository.save(defaultIngredient1());
        ingredient2 = ingredientRepository.save(defaultIngredient2());
        ingredient3 = ingredientRepository.save(defaultIngredient3());
        ingredient4 = ingredientRepository.save(defaultIngredient4());
        appliance1 = cookingApplianceRepository.save(defaultCookingAppliance1());
        appliance2 = cookingApplianceRepository.save(defaultCookingAppliance2());
        appliance3 = cookingApplianceRepository.save(defaultCookingAppliance3());
        appliance4 = cookingApplianceRepository.save(defaultCookingAppliance4());
        recipe1 = recipeService.createRecipe(recipeCreateEditDto1());
        recipe2 = recipeService.createRecipe(recipeCreateEditDto2());
        recipe3 = recipeService.createRecipe(recipeCreateEditDto3());
    }

    @Test
    public void whenGetRecipes_withNoQuery_thenOK() {
        var result = getRecipesSuccessful("");

        assertContainsRecipe(result, recipe1);
        assertContainsRecipe(result, recipe2);
        assertContainsRecipe(result, recipe3);
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    public void whenGetRecipes_withPageSizeOne_thenOK() {
        var result = getRecipesSuccessful("size=1");

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getNumberOfElements()).isEqualTo(1);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getFirst()).isEqualTo(true);
        assertThat(result.getLast()).isEqualTo(false);
    }

    @Test
    public void whenGetRecipes_withPageSizeOne_andPageNumberTwo_thenOK() {
        var result = getRecipesSuccessful("size=1&page=1");

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getNumberOfElements()).isEqualTo(1);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getFirst()).isEqualTo(false);
        assertThat(result.getLast()).isEqualTo(false);
    }

    @Test
    public void whenGetRecipes_withPageNumberTwo_thenOK() {
        var result = getRecipesSuccessful("page=1");

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getNumberOfElements()).isEqualTo(0);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getFirst()).isEqualTo(false);
        assertThat(result.getLast()).isEqualTo(true);
    }

    @Test
    public void whenGetRecipes_withQueryForName_thenOK() {
        var result = getRecipesSuccessful("name=" + recipe1.getName());

        assertContainsRecipe(result, recipe1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    public void whenGetRecipes_withQueryForAuthor_thenOK() {
        var result = getRecipesSuccessful("author=" + recipe1.getAuthor());

        assertContainsRecipe(result, recipe1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    public void whenGetRecipes_withQueryForDescription_thenOK() {
        var result = getRecipesSuccessful("description=" + recipe1.getDescription());

        assertContainsRecipe(result, recipe1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    public void whenGetRecipes_withQueryForIngredients_thenOK() {
        var result = getRecipesSuccessful("ingredients=" + ingredient1.getId() + "," + ingredient4.getId());

        assertContainsRecipe(result, recipe1);
        assertContainsRecipe(result, recipe2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    public void whenGetRecipes_withQueryForCookingAppliances_thenOK() {
        var result = getRecipesSuccessful("required_cooking_appliances=" + appliance4.getId() + "," + appliance3.getId());

        assertContainsRecipe(result, recipe3);
        assertContainsRecipe(result, recipe2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    public void whenGetRecipes_withQueryForCookingAppliances_andName_andAuthor_thenOK() {
        var result = getRecipesSuccessful("required_cooking_appliances=" + appliance4.getId() + ","
                + appliance3.getId() + "&name=" + recipe2.getName().substring(3, 7) + "&author=" + recipe2.getAuthor());

        assertContainsRecipe(result, recipe2);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    public void whenGetRecipes_withQueryForAuthorAndName_thenOK() {
        var result = getRecipesSuccessful("author=" + recipe1.getAuthor() + "&name=" + recipe2.getName());

        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getEmpty()).isTrue();
    }

    void assertContainsRecipe(TestPageableResponse<RecipeDto> listDto, RecipeDto recipeDto) {
        assertThat(listDto.getContent()).contains(recipeDto);
    }

    private TestPageableResponse<RecipeDto> getRecipesSuccessful(String query) {
        return getRecipes(query)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<TestPageableResponse<RecipeDto>>() {
                });
    }

    private Response getRecipes(String query) {
        return getRecipes(
                query,
                new Headers(this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER", "ROLE_ADMIN")))
        );
    }

    private Response getRecipes(String query, Headers headers) {
        return RestAssured
                .given()
                .headers(headers)
                .when()
                .get(URI + (StringUtils.isBlank(query) ? "" : "?" + query));
    }

    private RecipeCreateEditDto recipeCreateEditDto1() {
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

    private RecipeCreateEditDto recipeCreateEditDto2() {
        return defaultRecipeCreateEditDto2()
                .addCookingAppliancesItem(new CookingApplianceUseCreateEditDto()
                        .amount(2)
                        .id(appliance4.getId()))
                .addIngredientsItem(new IngredientUseCreateEditDto().
                        unit(IngredientUnitDto.OUNCES)
                        .amount(250F)
                        .id(ingredient3.getId()))
                .addIngredientsItem(new IngredientUseCreateEditDto()
                        .unit(IngredientUnitDto.CUPS)
                        .amount(0.25F)
                        .id(ingredient4.getId()))
                .addIngredientsItem(new IngredientUseCreateEditDto()
                        .unit(IngredientUnitDto.TEASPOONS)
                        .amount(4.25F)
                        .id(ingredient1.getId()));
    }

    private RecipeCreateEditDto recipeCreateEditDto3() {
        return defaultRecipeCreateEditDto3()
                .addCookingAppliancesItem(new CookingApplianceUseCreateEditDto()
                        .amount(5)
                        .id(appliance3.getId()))
                .addIngredientsItem(new IngredientUseCreateEditDto()
                        .unit(IngredientUnitDto.LITRES)
                        .amount(20F)
                        .id(ingredient3.getId()));
    }
}
