package dev.heinisch.menumaestro.integration_test.ingredients;

import dev.heinisch.menumaestro.domain.account.Account;
import dev.heinisch.menumaestro.domain.ingredient.Ingredient;
import dev.heinisch.menumaestro.domain.ingredient.IngredientCategory;
import dev.heinisch.menumaestro.domain.ingredient.IngredientStatus;
import dev.heinisch.menumaestro.integration_test.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.integration_test.utils.ErrorResponseAssert;
import dev.heinisch.menumaestro.integration_test.utils.TestPageableResponse;
import dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.openapitools.model.ErrorResponse;
import org.openapitools.model.IngredientDto;
import org.springframework.http.HttpStatus;

import java.util.List;

import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME_2;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultIngredientTestData.*;
import static org.assertj.core.api.Assertions.assertThat;

class SearchIngredientsWebIntegrationTest extends BaseWebIntegrationTest {


    private IngredientDto ingredient1;
    private IngredientDto ingredient2;
    private IngredientDto ingredient3;
    private IngredientDto ingredient4;
    private IngredientDto ingredient5;
    private IngredientDto ingredient6;
    private Account account;
    @Override
    protected String getBasePath() {
        return "/ingredients";
    }

    @BeforeEach
    public void setup() {
        ingredient1 = ingredientMapper.toIngredientDto(ingredientRepository.save(defaultIngredient1()));
        ingredient2 = ingredientMapper.toIngredientDto(ingredientRepository.save(defaultIngredient2()));
        ingredient3 = ingredientMapper.toIngredientDto(ingredientRepository.save(defaultIngredient3()));
        ingredient4 = ingredientMapper.toIngredientDto(ingredientRepository.save(defaultIngredient4()));

        account=accountRepository.saveAndFlush(DefaultAccountTestData.defaultAccount2());
        var tmpIngredient = Ingredient.builder()
                .name("Südtiroler Speck")
                .defaultUnit(DEFAULT_INGREDIENT_UNIT_4)
                .category(IngredientCategory.RED_MEATS_AND_POULTRY)
                .status(IngredientStatus.REQUESTED)
                .username(account.getUsername())
                .build();
        ingredient5 = ingredientMapper.toIngredientDto(ingredientRepository.save(tmpIngredient));

        tmpIngredient = Ingredient.builder()
                .name("Toilet paper")
                .defaultUnit(DEFAULT_INGREDIENT_UNIT_4)
                .category(IngredientCategory.SNACKS)
                .status(IngredientStatus.REQUESTED)
                .username(account.getUsername())
                .build();
        ingredient6 = ingredientMapper.toIngredientDto(ingredientRepository.save(tmpIngredient));
    }

    public void whenGetCustomIngredients_thenOK(){
        var result = getIngredientsWithCustomSuccessful("");

        assertContainsIngredient(result, ingredient1);
        assertContainsIngredient(result, ingredient2);
        assertContainsIngredient(result, ingredient3);
        assertContainsIngredient(result, ingredient4);
        assertContainsIngredient(result, ingredient5);
        assertContainsIngredient(result, ingredient6);
        assertThat(result.getTotalElements()).isEqualTo(6);

    }

    @Test
    public void whenGetIngredients_withNoQuery_thenOK() {
        var result = getIngredientsSuccessful("");

        assertContainsIngredient(result, ingredient1);
        assertContainsIngredient(result, ingredient2);
        assertContainsIngredient(result, ingredient3);
        assertContainsIngredient(result, ingredient4);
        assertThat(result.getTotalElements()).isEqualTo(4);
    }

    @Test
    public void whenGetIngredients_withPageSizeOne_thenOK() {
        var result = getIngredientsSuccessful("size=1");

        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getNumberOfElements()).isEqualTo(1);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getFirst()).isEqualTo(true);
        assertThat(result.getLast()).isEqualTo(false);
    }

    @Test
    public void whenGetIngredients_withPageSizeOne_andPageNumberTwo_thenOK() {
        var result = getIngredientsSuccessful("size=1&page=1");

        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getNumberOfElements()).isEqualTo(1);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getFirst()).isEqualTo(false);
        assertThat(result.getLast()).isEqualTo(false);
    }

    @Test
    public void whenGetIngredients_withPageNumberTwo_thenOK() {
        var result = getIngredientsSuccessful("page=1");

        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getNumberOfElements()).isEqualTo(0);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getFirst()).isEqualTo(false);
        assertThat(result.getLast()).isEqualTo(true);
    }

    @Test
    public void whenGetIngredients_withQueryForName_thenOK() {
        var result = getIngredientsSuccessful("name=" + ingredient1.getName());

        assertContainsIngredient(result, ingredient1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    public void whenGetIngredients_withMissingHeader_thenForbidden() {
        var errorResponse = getIngredients(
                "name=" + ingredient1.getName(),
                new Headers()
        )
                .then()
                .extract()
                .as(ErrorResponse.class);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("Access Denied");
    }

    @Test
    public void whenGetIngredients_withInvalidHeader_thenUnauthorized() {
        var errorResponse = getIngredients(
                "name=" + ingredient1.getName(),
                new Headers(new Header("Authorization", "Bearer LOL"))
        )
                .then()
                .extract()
                .as(ErrorResponse.class);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNAUTHORIZED)
                .messageContains("Invalid JWT token");
    }

    void assertContainsIngredient(TestPageableResponse<IngredientDto> listDto, IngredientDto ingredientDto) {
        assertThat(listDto.getContent()).contains(ingredientDto);
    }
    private TestPageableResponse<IngredientDto> getIngredientsWithCustomSuccessful(String query) {
        return getIngredientsWithCustom(query)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<TestPageableResponse<IngredientDto>>() {});
    }

    private Response getIngredientsWithCustom(String query) {
        return getIngredients(
                query,
                new Headers(this.generateValidAuthorizationHeader(DEFAULT_USERNAME_2, List.of("ROLE_USER", "ROLE_ADMIN")))
        );
    }



    private TestPageableResponse<IngredientDto> getIngredientsSuccessful(String query) {
        return getIngredients(query)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<TestPageableResponse<IngredientDto>>() {
                });
    }

    private Response getIngredients(String query) {
        return getIngredients(
                query,
                new Headers(this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER", "ROLE_ADMIN")))
        );
    }

    private Response getIngredients(String query, Headers headers) {
        return RestAssured
                .given()
                .headers(headers)
                .when()
                .get(URI + (StringUtils.isBlank(query) ? "" : "?" + query));
    }

}
