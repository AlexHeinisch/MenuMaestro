package dev.heinisch.menumaestro.ingredients;

import dev.heinisch.menumaestro.domain.ingredient.Ingredient;
import dev.heinisch.menumaestro.domain.ingredient.IngredientStatus;
import dev.heinisch.menumaestro.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.utils.TestPageableResponse;
import dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.openapitools.model.CreateIngredientDto;
import org.openapitools.model.IngredientCategory;
import org.openapitools.model.IngredientDto;
import org.openapitools.model.IngredientDtoWithCategory;
import org.openapitools.model.IngredientUnitDto;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.*;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultIngredientTestData.*;

@ActiveProfiles({"datagen-off", "test"})
public class CustomIngredientsIT extends BaseWebIntegrationTest{

    private Ingredient ingredient3;
    private Ingredient ingredient4;

    @BeforeEach
    public void setup() {
        accountRepository.saveAndFlush(DefaultAccountTestData.defaultAccount());
        accountRepository.saveAndFlush(DefaultAccountTestData.defaultAccount2());
        ingredient3 = defaultIngredient3();
        ingredient3.setStatus(IngredientStatus.REQUESTED);
        ingredient3.setUsername(DefaultAccountTestData.defaultAccount2().getUsername());
        ingredient3 = ingredientRepository.saveAndFlush(ingredient3);

        ingredient4 = defaultIngredient4();
        ingredient4.setStatus(IngredientStatus.REQUESTED);
        ingredient4.setUsername(DefaultAccountTestData.defaultAccount2().getUsername());
        ingredient4 = ingredientRepository.saveAndFlush(ingredient4);
    }

    @Test
    public void whenGetIngredientSuggestions_thenOk_then() {
        var result = getIngredientSuggestionsSuccessful("");
        Assertions.assertAll(
                () -> Assertions.assertNotNull(result),
                () -> Assertions.assertEquals(2, result.getTotalElements())
        );
        IngredientDtoWithCategory ingredientDto=result.getContent().getFirst();
        Assertions.assertAll(
                () -> Assertions.assertNotNull(ingredientDto),
                () -> Assertions.assertEquals(ingredient3.getId(), ingredientDto.getId()),
                () -> Assertions.assertEquals(ingredient3.getName(), ingredientDto.getName()),
                () -> Assertions.assertEquals(ingredient3.getDefaultUnit().name(), ingredientDto.getDefaultUnit().name()),
                () -> Assertions.assertEquals(ingredient3.getCategory().name(), ingredientDto.getCategory().name()),
                () -> Assertions.assertEquals(ingredient3.getUsername(), ingredientDto.getUsername())

        );

    }
    @Test
    public void whenGetIngredientSuggestions_thenOk() {
        var result = getIngredientSuggestionsSuccessful("");
        Assertions.assertAll(
                () -> Assertions.assertNotNull(result),
                () -> Assertions.assertEquals(2, result.getTotalElements())
        );
        IngredientDtoWithCategory ingredientDto=result.getContent().getFirst();
        Assertions.assertAll(
                () -> Assertions.assertNotNull(ingredientDto),
                () -> Assertions.assertEquals(ingredient3.getId(), ingredientDto.getId()),
                () -> Assertions.assertEquals(ingredient3.getName(), ingredientDto.getName()),
                () -> Assertions.assertEquals(ingredient3.getDefaultUnit().name(), ingredientDto.getDefaultUnit().name()),
                () -> Assertions.assertEquals(ingredient3.getCategory().name(), ingredientDto.getCategory().name()),
                () -> Assertions.assertEquals(ingredient3.getUsername(), ingredientDto.getUsername())

        );

    }



    @Test
    public void whenApproveIngredient_thenOk() {
        var approved3 = approveIngredientSuggestionsSuccessful(ingredient3.getId());
        var approved4 = approveIngredientSuggestionsSuccessful(ingredient4.getId());

        var result = getIngredientSuggestionsSuccessful("");
        Assertions.assertAll(
                () -> Assertions.assertNotNull(result),
                () -> Assertions.assertEquals(0, result.getTotalElements())
        );
        Assertions.assertAll(
                () -> Assertions.assertNotNull(approved3),
                () -> Assertions.assertEquals(ingredient3.getId(), approved3.getId()),
                () -> Assertions.assertEquals(ingredient3.getName(), approved3.getName()),
                () -> Assertions.assertEquals(ingredient3.getDefaultUnit().name(), approved3.getDefaultUnit().name()),
                () -> Assertions.assertNotNull(approved4),
                () -> Assertions.assertEquals(ingredient4.getId(), approved4.getId()),
                () -> Assertions.assertEquals(ingredient4.getName(), approved4.getName()),
                () -> Assertions.assertEquals(ingredient4.getDefaultUnit().name(), approved4.getDefaultUnit().name())

        );

    }
    @Test
    public void whenSuggestIngredientAsAdmin_thenOkAndOthersSee() {
        CreateIngredientDto createIngredientDto= new CreateIngredientDto();
        createIngredientDto.setName("Südtiroler Speck");
        createIngredientDto.setCategory(IngredientCategory.OTHER);
        createIngredientDto.setDefaultUnit(IngredientUnitDto.CUPS);
        IngredientDto ingredientDto = suggestIngredientsAsAdminSuccessful(createIngredientDto);
        TestPageableResponse<IngredientDtoWithCategory> result = getIngredientSuggestionsSuccessful("");

        Assertions.assertAll(
                () -> Assertions.assertNotNull(result),
                () -> Assertions.assertEquals(2, result.getTotalElements())
        );
    }



    @Test
    public void whenSuggestIngredient_thenOkAndOthersDontSee() {
        CreateIngredientDto createIngredientDto= new CreateIngredientDto();
        createIngredientDto.setName("Südtiroler Speck");
        createIngredientDto.setCategory(IngredientCategory.OTHER);
        createIngredientDto.setDefaultUnit(IngredientUnitDto.CUPS);
        IngredientDto ingredientDto = suggestIngredientsSuccessful(createIngredientDto);
        TestPageableResponse<IngredientDtoWithCategory> result = getIngredientSuggestionsSuccessful("");
        IngredientDtoWithCategory element=result.getContent().get(2);
        Assertions.assertAll(
                () -> Assertions.assertNotNull(result),
                () -> Assertions.assertEquals(3, result.getTotalElements())
        );
        Assertions.assertAll(
                () -> Assertions.assertNotNull(ingredientDto),
                () -> Assertions.assertEquals(ingredientDto.getId(), element.getId()),
                () -> Assertions.assertEquals(ingredientDto.getName(), createIngredientDto.getName())
        );
    }

    @Test
    public void whenSuggestToManyIngredient_thenError429() {
        CreateIngredientDto createIngredientDto= new CreateIngredientDto();
        createIngredientDto.setName("Südtiroler Speck");
        createIngredientDto.setCategory(IngredientCategory.OTHER);
        createIngredientDto.setDefaultUnit(IngredientUnitDto.CUPS);
        suggestIngredientsSuccessful(createIngredientDto);
        suggestIngredientsSuccessful(createIngredientDto);
        suggestIngredientsSuccessful(createIngredientDto);
        suggestIngredientsSuccessful(createIngredientDto);
        suggestIngredientsSuccessful(createIngredientDto);
        suggestIngredientsSuccessful(createIngredientDto);
        suggestIngredientsSuccessful(createIngredientDto);
        suggestIngredientsSuccessful(createIngredientDto);
        suggestIngredientsSuccessful(createIngredientDto);
        suggestIngredientsSuccessful(createIngredientDto);
        suggestIngredientsError(createIngredientDto);

    }
    @Test
    public void whenGetSuggestions_withPageSizeOne_thenOK() {
        var result = getIngredientSuggestionsSuccessful("?size=1");
        Assertions.assertAll(
                () -> Assertions.assertNotNull(result),
                () -> Assertions.assertEquals(2, result.getTotalElements())
        );
        IngredientDtoWithCategory ingredientDto=result.getContent().getFirst();
        Assertions.assertAll(
                () -> Assertions.assertNotNull(ingredientDto),
                () -> Assertions.assertEquals(ingredient3.getId(), ingredientDto.getId()),
                () -> Assertions.assertEquals(ingredient3.getName(), ingredientDto.getName()),
                () -> Assertions.assertEquals(ingredient3.getDefaultUnit().name(), ingredientDto.getDefaultUnit().name()),
                () -> Assertions.assertEquals(ingredient3.getCategory().name(), ingredientDto.getCategory().name()),
                () -> Assertions.assertEquals(ingredient3.getUsername(), ingredientDto.getUsername())
        );

    }

    @Test
    public void whenGetIngredients_withPageSizeOne_andPageNumberTwo_thenOK() {
        var result = getIngredientSuggestionsSuccessful("?size=1&page=1");
        Assertions.assertAll(
                () -> Assertions.assertNotNull(result),
                () -> Assertions.assertEquals(2, result.getTotalElements())
        );
    }

    private IngredientDto suggestIngredientsAsAdminSuccessful(CreateIngredientDto createIngredientDto) {
        return suggestIngredientsAsAdmin(createIngredientDto)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<IngredientDto>() {});
    }
    private void suggestIngredientsError(CreateIngredientDto createIngredientDto) {
        suggestIngredients(createIngredientDto)
                .then()
                .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .extract();
    }

    private IngredientDto suggestIngredientsSuccessful(CreateIngredientDto createIngredientDto) {
        return suggestIngredients(createIngredientDto)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<IngredientDto>() {});
    }
    private Response suggestIngredientsAsAdmin(CreateIngredientDto createIngredientDto) {
        return suggestIngredients(createIngredientDto,
                new Headers(this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_ADMIN")))
        );
    }
    private Response suggestIngredients(CreateIngredientDto createIngredientDto) {
        return suggestIngredients(createIngredientDto,
                new Headers(this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER")))
        );
    }

    private Response suggestIngredients(CreateIngredientDto createIngredientDto,Headers headers) {
        return RestAssured
                .given()
                .headers(headers)
                .when()
                .contentType("application/json")
                .body(createIngredientDto)
                .post(URI + "/ingredients");
    }

    private IngredientDto approveIngredientSuggestionsSuccessful(Long ingredientId) {
        return approveIngredientSuggestions(ingredientId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<IngredientDto>() {});
    }

    private Response approveIngredientSuggestions(Long ingredientId) {
        return approveIngredientSuggestions(ingredientId,
                new Headers(this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER", "ROLE_ADMIN")))
        );
    }

    private Response approveIngredientSuggestions(Long ingredientId,Headers headers) {
        return RestAssured
                .given()
                .headers(headers)
                .when()
                .post(URI + "/ingredients/"+ingredientId);
    }

    private TestPageableResponse<IngredientDtoWithCategory> getIngredientSuggestionsSuccessful(String query) {
        return getIngredientSuggestions(query)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<TestPageableResponse<IngredientDtoWithCategory>>() {});
    }

    private Response getIngredientSuggestions(String query) {
        return getIngredientSuggestions(query,
                new Headers(this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER", "ROLE_ADMIN")))
        );
    }

    private Response getIngredientSuggestions(String query,Headers headers) {
        return RestAssured
                .given()
                .headers(headers)
                .when()
                .get(URI + "/ingredientSuggestions"+(StringUtils.isBlank(query) ? "" : "?" + query));
    }

    private TestPageableResponse<IngredientDto> getIngredientsSuccessful(String query) {
        return getIngredients(query)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<TestPageableResponse<IngredientDto>>() {});
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
                .get(URI+"/ingredients" + (StringUtils.isBlank(query) ? "" : "?" + query));
    }

}
