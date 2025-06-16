package dev.heinisch.menumaestro.integration_test.ingredients;

import dev.heinisch.menumaestro.domain.ingredient.Ingredient;
import dev.heinisch.menumaestro.domain.ingredient.IngredientStatus;
import dev.heinisch.menumaestro.domain.ingredient.IngredientUnit;
import dev.heinisch.menumaestro.domain.menu.Meal;
import dev.heinisch.menumaestro.domain.menu.Menu;
import dev.heinisch.menumaestro.domain.organization.Organization;
import dev.heinisch.menumaestro.domain.recipe.RecipeIngredientUse;
import dev.heinisch.menumaestro.domain.recipe.RecipeValue;
import dev.heinisch.menumaestro.domain.shopping_list.ShoppingList;
import dev.heinisch.menumaestro.domain.shopping_list.ShoppingListItem;
import dev.heinisch.menumaestro.domain.stash.Stash;
import dev.heinisch.menumaestro.exceptions.VersionMatchFailedException;
import dev.heinisch.menumaestro.integration_test.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.integration_test.utils.TestPageableResponse;
import dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData;
import dev.heinisch.menumaestro.service.ShoppingListService;
import dev.heinisch.menumaestro.service.StashService;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.openapitools.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.*;

import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultIngredientTestData.*;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultMenuTestData.defaultMenu1;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultOrganizationTestData.defaultOrganization1;

@Disabled
public class DeleteAndReplaceIngredientWebIntegrationTest extends BaseWebIntegrationTest{

    private Ingredient ingredient1;
    private Ingredient ingredient2;
    private Ingredient ingredient3;
    private Ingredient ingredient4;
    private Stash stash;
    private Organization organization;
    private Menu menu;
    private IngredientUseCreateEditDto ingredientUseCreateEditDto1;
    private IngredientUseCreateEditDto ingredientUseCreateEditDto2;
    private Set<RecipeIngredientUse> ingredients;

    private ShoppingListItem shoppingListItem1;
    private ShoppingListItem shoppingListItem2;
    private ShoppingListItem shoppingListItem3;

    @Autowired
    private ShoppingListService shoppingListService;
    @Autowired
    private StashService stashService;

    private ShoppingList setupShoppingList(Set<ShoppingListItem> items) {
        return ShoppingList.builder()
                .name("Day 1 Shopping List")
                .isClosed(true)
                .menuId(menu.getId())
                .organizationId(organization.getId())
                .items(items)
                .build();
    }
    private Meal setupMeal(){
        return Meal.builder()
                .numberOfPeople(145)
                .id(1L)
                .name("mealName")
                .position(0)
                .menu(menu)
                .isDone(false)
                .recipe(RecipeValue.builder()
                        .name("Chicken with rice")
                        .author("Lisa")
                        .description("Default menu")
                        .ingredients(ingredients)
                        .cookingAppliances(Collections.emptySet())
                        .servings(21)
                        .build())
                .build();
    }
    @BeforeEach
    public void setup() {
        organization = organizationRepository.saveAndFlush(defaultOrganization1());
        stash= organization.getStash();

        accountRepository.saveAndFlush(DefaultAccountTestData.defaultAccount());
        accountRepository.saveAndFlush(DefaultAccountTestData.defaultAccount2());
        ingredient1 = ingredientRepository.saveAndFlush(defaultIngredient1());
        ingredient2 = ingredientRepository.saveAndFlush(defaultIngredient2());
        ingredient3 = defaultIngredient3();
        ingredient3.setStatus(IngredientStatus.REQUESTED);
        ingredient3.setUsername(DefaultAccountTestData.defaultAccount2().getUsername());
        ingredient3= ingredientRepository.saveAndFlush(ingredient3);
        ingredient4 = defaultIngredient4();
        ingredient4.setStatus(IngredientStatus.REQUESTED);
        ingredient4.setUsername(DefaultAccountTestData.defaultAccount2().getUsername());
        ingredient4 = ingredientRepository.saveAndFlush(ingredient4);

        ingredientUseCreateEditDto1= new IngredientUseCreateEditDto();
        ingredientUseCreateEditDto1.setId(ingredient3.getId());
        ingredientUseCreateEditDto1.setAmount(2f);
        ingredientUseCreateEditDto1.setUnit(IngredientUnitDto.GRAMS);
        ingredientUseCreateEditDto2= new IngredientUseCreateEditDto();
        ingredientUseCreateEditDto2.setId(ingredient2.getId());
        ingredientUseCreateEditDto2.setAmount(2f);
        ingredientUseCreateEditDto2.setUnit(IngredientUnitDto.GRAMS);

        shoppingListItem1 = ShoppingListItem.builder()
                .ingredientId(null)
                .customItemName("Apple Juice")
                .amount(1d)
                .unit(IngredientUnit.LITRES)
                .isChecked(true)
                .checkedByAccountUsername(DefaultAccountTestData.DEFAULT_USERNAME)
                .build();
        shoppingListItem2 = ShoppingListItem.builder()
                .ingredientId(ingredient2.getId())
                .amount(1d)
                .unit(IngredientUnit.LITRES)
                .isChecked(true)
                .checkedByAccountUsername(DefaultAccountTestData.DEFAULT_USERNAME)
                .build();
        shoppingListItem3 = ShoppingListItem.builder()
                .ingredientId(ingredient3.getId())
                .amount(1d)
                .unit(IngredientUnit.LITRES)
                .isChecked(true)
                .checkedByAccountUsername(DefaultAccountTestData.DEFAULT_USERNAME)
                .build();

        menu = menuRepository.saveAndFlush(defaultMenu1(organization.getId()));


        ingredients=new HashSet<>();


    }

    @Test
    public void replaceIngredients_BigTest() {
        ShoppingList shoppingList=setupShoppingList(Set.of(shoppingListItem1,shoppingListItem3));
        shoppingListRepository.saveAndFlush(shoppingList);

        try {
            stashService.updateStash(stash.getId(),List.of(ingredientUseCreateEditDto1), stash.getVersionNumber());

        }catch (VersionMatchFailedException e){
            Assertions.fail(e.getMessage());
        }
        ingredients.add(RecipeIngredientUse.builder().ingredient(ingredient1).amount(5f).unit(IngredientUnit.GRAMS).build());
        ingredients.add(RecipeIngredientUse.builder().ingredient(ingredient3).amount(50f).unit(IngredientUnit.GRAMS).build());

        Meal meal=setupMeal();
        meal= mealRepository.saveAndFlush(meal);


        meal=mealRepository.findById(meal.getId()).orElseThrow();
        List<Ingredient> mealIngredients2= meal.getRecipe().getIngredients().stream().map(RecipeIngredientUse::getIngredient).toList();
        IngredientDto response= replaceIngredientsSuccessful(ingredient3.getId(), ingredient2.getId());
        List<IngredientDto> result = getIngredientsSuccessful("").getContent();
        ShoppingListDto shoppingListDto =shoppingListService.getShoppingListById(shoppingList.getId());
        meal=mealRepository.findById(meal.getId()).orElseThrow();
        StashResponseDto stashResponseDto=stashService.getStash(stash.getId());
        List<Long> mealIngredients= meal.getRecipe().getIngredients().stream().map(RecipeIngredientUse::getIngredientId).toList();

        List<Long> stashIngredientIds= stashResponseDto.getIngredients().stream().map(IngredientUseDto::getId).toList();
        List<Long> shoppingListIngredientIds= shoppingListDto.getIngredients().stream().map(i -> i.getIngredient().getId()).toList();
        Assertions.assertAll(
                () -> Assertions.assertTrue(result.contains(ingredientMapper.toIngredientDto(ingredient2))),
                () ->    Assertions.assertFalse(result.contains(ingredientMapper.toIngredientDto(ingredient3)))
        );



        Assertions.assertAll(
                () -> Assertions.assertEquals(2, shoppingListDto.getIngredients().size()),
                () -> Assertions.assertFalse( shoppingListIngredientIds.contains(ingredient3.getId())),
                () -> Assertions.assertTrue( shoppingListIngredientIds.contains(ingredient2.getId()))
        );
        Assertions.assertAll(
                () -> Assertions.assertEquals(2, mealIngredients.size()),
                () -> Assertions.assertFalse( mealIngredients.contains(ingredient3.getId())),
                () -> Assertions.assertTrue( mealIngredients.contains(ingredient2.getId()))
        );
        Assertions.assertAll(
                () -> Assertions.assertEquals(1, stashIngredientIds.size()),
                () -> Assertions.assertFalse(stashIngredientIds.contains(ingredient3.getId())),
                () -> Assertions.assertTrue(stashIngredientIds.contains(ingredient2.getId()))
        );
    }

    @Test
    public void whenReplaceIngredientWithExistingIngredientInShoppingList_thenOk() {

        ShoppingList shoppingList=setupShoppingList(Set.of(shoppingListItem2,shoppingListItem3));
        shoppingListRepository.saveAndFlush(shoppingList);

        IngredientDto response = replaceIngredientsSuccessful(ingredient3.getId(), ingredient2.getId());
        ShoppingListDto shoppingListDto = shoppingListService.getShoppingListById(shoppingList.getId());
        List<Long> shoppingListIngredientIds = shoppingListDto.getIngredients().stream().map(i -> i.getIngredient().getId()).toList();

        Assertions.assertAll(
                () -> Assertions.assertEquals(1, shoppingListDto.getIngredients().size()),
                () -> Assertions.assertFalse(shoppingListIngredientIds.contains(ingredient3.getId())),
                () -> Assertions.assertFalse(shoppingListIngredientIds.contains(ingredient1.getId())),
                () -> Assertions.assertTrue(shoppingListIngredientIds.contains(ingredient2.getId()))
        );
    }

    @Test
    public void whenReplaceIngredientWithNonExistingInShoppingList_thenOk() {

        ShoppingList shoppingList=setupShoppingList(Set.of(shoppingListItem1,shoppingListItem3));
        shoppingListRepository.saveAndFlush(shoppingList);

        IngredientDto response = replaceIngredientsSuccessful(ingredient3.getId(), ingredient1.getId());
        ShoppingListDto shoppingListDto = shoppingListService.getShoppingListById(shoppingList.getId());
        List<Long> shoppingListIngredientIds = shoppingListDto.getIngredients().stream().map(i -> i.getIngredient().getId()).toList();

        Assertions.assertAll(
                () -> Assertions.assertEquals(2, shoppingListDto.getIngredients().size()),
                () -> Assertions.assertFalse(shoppingListIngredientIds.contains(ingredient3.getId())),
                () -> Assertions.assertFalse(shoppingListIngredientIds.contains(ingredient2.getId())),
                () -> Assertions.assertTrue(shoppingListIngredientIds.contains(ingredient1.getId()))
        );
    }

    @Test
    public void whenReplaceNonExistingIngredient_thenNotFound() {

        Response response = replaceIngredients(ingredient3.getId()*5,ingredient3.getId()*5);
        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
    }

    @Test
    public void whenReplaceNonPublicIngredient_thenBadRequest() {

        Response response = replaceIngredients(ingredient2.getId(),ingredient1.getId());
        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.getStatusCode());
    }

    @Test
    public void whenReplaceIngredientWithExistingInStash_thenOk() {
        try {
            stashService.updateStash(stash.getId(),List.of(ingredientUseCreateEditDto1,ingredientUseCreateEditDto2), stash.getVersionNumber());

        }catch (VersionMatchFailedException e){
            Assertions.fail(e.getMessage());
        }

        IngredientDto response = replaceIngredientsSuccessful(ingredient3.getId(), ingredient2.getId());
        StashResponseDto stashResponseDto = stashService.getStash(stash.getId());
        List<Long> stashIngredientIds = stashResponseDto.getIngredients().stream().map(IngredientUseDto::getId).toList();

        Assertions.assertAll(
                () -> Assertions.assertEquals(1, stashIngredientIds.size()),
                () -> Assertions.assertFalse(stashIngredientIds.contains(ingredient3.getId())),
                () -> Assertions.assertTrue(stashIngredientIds.contains(ingredient2.getId()))
        );
    }

    @Test
    public void whenReplaceIngredientWithNonExistingInStash_thenOk() {
        try {
            stashService.updateStash(stash.getId(),List.of(ingredientUseCreateEditDto1,ingredientUseCreateEditDto2), stash.getVersionNumber());

        }catch (VersionMatchFailedException e){
            Assertions.fail(e.getMessage());
        }

        IngredientDto response = replaceIngredientsSuccessful(ingredient3.getId(), ingredient1.getId());
        StashResponseDto stashResponseDto = stashService.getStash(stash.getId());
        List<Long> stashIngredientIds = stashResponseDto.getIngredients().stream().map(IngredientUseDto::getId).toList();

        Assertions.assertAll(
                () -> Assertions.assertEquals(2, stashIngredientIds.size()),
                () -> Assertions.assertFalse(stashIngredientIds.contains(ingredient3.getId())),
                () -> Assertions.assertTrue(stashIngredientIds.contains(ingredient1.getId())),
                () -> Assertions.assertTrue(stashIngredientIds.contains(ingredient2.getId()))

        );
    }

    @Test
    public void whenReplaceIngredientWithExistingInRecipe_thenOk() {
        ingredients.add(RecipeIngredientUse.builder().ingredient(ingredient2).amount(5f).unit(IngredientUnit.GRAMS).build());
        ingredients.add(RecipeIngredientUse.builder().ingredient(ingredient3).amount(50f).unit(IngredientUnit.GRAMS).build());

        Meal meal = setupMeal();
        meal= mealRepository.saveAndFlush(meal);

        IngredientDto response = replaceIngredientsSuccessful(ingredient3.getId(), ingredient2.getId());
        meal = mealRepository.findById(meal.getId()).orElseThrow();
        List<Long> mealIngredients = meal.getRecipe().getIngredients().stream().map(RecipeIngredientUse::getIngredientId).toList();

        Assertions.assertAll(
                () -> Assertions.assertEquals(1, mealIngredients.size()),
                () -> Assertions.assertFalse(mealIngredients.contains(ingredient3.getId())),
                () -> Assertions.assertTrue(mealIngredients.contains(ingredient2.getId()))
        );//When we have a conflict we remove and not replace
    }

    @Test
    public void whenReplaceIngredientWithNonExistingInRecipe_thenOk() {
        ingredients.add(RecipeIngredientUse.builder().ingredient(ingredient1).amount(5f).unit(IngredientUnit.GRAMS).build());
        ingredients.add(RecipeIngredientUse.builder().ingredient(ingredient3).amount(50f).unit(IngredientUnit.GRAMS).build());

        Meal meal = setupMeal();
        meal= mealRepository.saveAndFlush(meal);

        IngredientDto response = replaceIngredientsSuccessful(ingredient3.getId(), ingredient2.getId());
        meal = mealRepository.findById(meal.getId()).orElseThrow();
        List<Long> mealIngredients = meal.getRecipe().getIngredients().stream().map(RecipeIngredientUse::getIngredientId).toList();

        Assertions.assertAll(
                () -> Assertions.assertEquals(2, mealIngredients.size()),
                () -> Assertions.assertFalse(mealIngredients.contains(ingredient3.getId())),
                () -> Assertions.assertTrue(mealIngredients.contains(ingredient2.getId())),
                () -> Assertions.assertTrue(mealIngredients.contains(ingredient1.getId()))
        );
    }

    @Test
    public void whenDeleteExistingIngredient_bigTest() {
        try {
            stashService.updateStash(stash.getId(),List.of(ingredientUseCreateEditDto1,ingredientUseCreateEditDto2), stash.getVersionNumber());

        }catch (VersionMatchFailedException e){
            Assertions.fail(e.getMessage());
        }
        ingredients.add(RecipeIngredientUse.builder().ingredient(ingredient1).amount(5f).unit(IngredientUnit.GRAMS).build());
        ingredients.add(RecipeIngredientUse.builder().ingredient(ingredient3).amount(50f).unit(IngredientUnit.GRAMS).build());

        Meal meal = setupMeal();
        meal= mealRepository.saveAndFlush(meal);

        ShoppingList shoppingList=setupShoppingList(Set.of(shoppingListItem1,shoppingListItem3));
        shoppingListRepository.saveAndFlush(shoppingList);

        deleteIngredientSuggestionsSuccessful(ingredient3.getId());
        TestPageableResponse<IngredientDtoWithCategory> result = getIngredientSuggestionsSuccessful();
        Assertions.assertAll(
                () -> Assertions.assertNotNull(result),
                () -> Assertions.assertEquals(1, result.getTotalElements())
        );
        ShoppingListDto shoppingListDto = shoppingListService.getShoppingListById(shoppingList.getId());
        meal = mealRepository.findById(meal.getId()).orElseThrow();
        StashResponseDto stashResponseDto = stashService.getStash(stash.getId());
        List<Ingredient> mealIngredients = meal.getRecipe().getIngredients().stream().map(RecipeIngredientUse::getIngredient).toList();
        List<Long> shoppingListIngredientIds = shoppingListDto.getIngredients().stream().map(shoppingListIngredientDto -> shoppingListIngredientDto.getIngredient().getId()).toList();
        List<Long> stashIngredientIds = stashResponseDto.getIngredients().stream().map(IngredientUseDto::getId).toList();

        Assertions.assertAll(
                () -> Assertions.assertEquals(1, shoppingListDto.getIngredients().size()),
                () -> Assertions.assertEquals(1, mealIngredients.size()),
                () -> Assertions.assertFalse(mealIngredients.contains(ingredient3)),
                () -> Assertions.assertFalse(shoppingListIngredientIds.contains(ingredient3.getId())),
                () -> Assertions.assertFalse(stashIngredientIds.contains(ingredient3.getId()))
        );
    }

    @Test
    public void whenDeleteNonExistingIngredient_thenNotFound() {
        Response response = deleteIngredientSuggestions(ingredient1.getId()*50);
        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
    }
    private IngredientDto replaceIngredientsSuccessful(long willBeReplaced,long replaces) {
        return replaceIngredients(willBeReplaced, replaces)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<IngredientDto>() {});
    }


    private Response replaceIngredients(long willBeReplaced,long replaces) {
        return replaceIngredients(
                willBeReplaced,replaces,
                new Headers(this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER", "ROLE_ADMIN")))
        );
    }

    private Response replaceIngredients(long willBeReplaced,long replaces, Headers headers) {
        return RestAssured
                .given()
                .headers(headers)
                .when()
                .body(replaces)
                .contentType(ContentType.JSON)
                .patch(URI + "/ingredients/"+willBeReplaced);
    }

    private void deleteIngredientSuggestionsSuccessful(Long ingredientId) {
        deleteIngredientSuggestions(ingredientId)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value())
                .extract();
    }

    private Response deleteIngredientSuggestions(Long ingredientId) {
        return deleteIngredientSuggestions(ingredientId,
                new Headers(this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER", "ROLE_ADMIN")))
        );
    }

    private Response deleteIngredientSuggestions(Long ingredientId,Headers headers) {
        return RestAssured
                .given()
                .headers(headers)
                .when()
                .delete(URI + "/ingredients/"+ingredientId);
    }

    private TestPageableResponse<IngredientDtoWithCategory> getIngredientSuggestionsSuccessful() {
        return getIngredientSuggestions()
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<TestPageableResponse<IngredientDtoWithCategory>>() {});
    }

    private Response getIngredientSuggestions() {
        return getIngredientSuggestions(
                new Headers(this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER", "ROLE_ADMIN")))
        );
    }

    private Response getIngredientSuggestions(Headers headers) {
        return RestAssured
                .given()
                .headers(headers)
                .when()
                .get(URI + "/ingredientSuggestions");
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
                .get(URI + "/ingredients" + (StringUtils.isBlank(query) ? "" : "?" + query));
    }

}
