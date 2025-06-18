package dev.heinisch.menumaestro.integration_test.shopping_list;

import dev.heinisch.menumaestro.domain.ingredient.Ingredient;
import dev.heinisch.menumaestro.domain.ingredient.IngredientUnit;
import dev.heinisch.menumaestro.domain.menu.Menu;
import dev.heinisch.menumaestro.domain.shopping_list.ShoppingList;
import dev.heinisch.menumaestro.domain.shopping_list.ShoppingListItem;
import dev.heinisch.menumaestro.integration_test.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.integration_test.utils.ErrorResponseAssert;
import dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData;
import dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultIngredientTestData;
import dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultMenuTestData;
import dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultOrganizationTestData;
import dev.heinisch.menumaestro.service.ShoppingListService;
import io.restassured.http.ContentType;
import io.restassured.http.Headers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.ErrorResponse;
import org.openapitools.model.IngredientCategory;
import org.openapitools.model.ShoppingListDto;
import org.openapitools.model.ShoppingListIngredientDto;
import org.openapitools.model.ShoppingListStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static io.restassured.RestAssured.given;

public class ShoppingListGetByIdWebIntegrationTest extends BaseWebIntegrationTest {

    long organizationId;
    Menu defaultMenu;
    @Autowired
    protected ShoppingListService shoppingListService;

    @Override
    protected String getBasePath() {
        return "/shopping-lists";
    }

    @BeforeEach
    public void setup() {
        organizationId = organizationRepository.saveAndFlush(DefaultOrganizationTestData.defaultOrganization1()).getId();
        defaultMenu = menuRepository.saveAndFlush(DefaultMenuTestData.defaultMenu1(organizationId));
    }

    @Test
    void getShoppingListById_shoppingListWithIdNotExisting_fails() {
        long shoppingListId = -1L;
        ErrorResponse errorResponse = given().contentType(ContentType.JSON)
                .headers(new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_ADMIN"))))
                .get(URI + "/" + shoppingListId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .extract().as(ErrorResponse.class);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.NOT_FOUND)
                .messageContains("not found");
    }

    @Test
    void getShoppingListOpenOneItem_success() {
        var ingredients = ingredientRepository.saveAllAndFlush(DefaultIngredientTestData.getDefaultIngredients());

        ShoppingListItem item1 = ShoppingListItem.builder()
                .ingredientId(ingredients.get(0).getId())
                .customItemName(null)
                .amount(0.5)
                .unit(IngredientUnit.LITRES)
                .isChecked(false)
                .checkedByAccountUsername(null)
                .build();

        ShoppingList shoppingList = ShoppingList.builder()
                .name("Day 1 Shopping List")
                .isClosed(false)
                .menuId(defaultMenu.getId())
                .organizationId(organizationId)
                .items(Set.of(item1))
                .build();

        shoppingList = shoppingListRepository.saveAndFlush(shoppingList);

        ShoppingListDto responseDto = given().contentType(ContentType.JSON)
                .headers(new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_ADMIN"))))
                .get(URI + '/' + shoppingList.getId())
                .then()
                .statusCode(200)
                .extract()
                .as(ShoppingListDto.class);

        Assertions.assertAll(
                () -> Assertions.assertEquals("Day 1 Shopping List", responseDto.getName()),
                () -> Assertions.assertEquals(organizationId, (long) responseDto.getOrganizationId()),
                () -> Assertions.assertEquals(ShoppingListStatus.OPEN, responseDto.getStatus()),
                () -> Assertions.assertNotNull(responseDto.getIngredients()),
                () -> Assertions.assertEquals(1, responseDto.getIngredients().size())
        );
        var ingredient1 = responseDto.getIngredients().get(0);
        Assertions.assertNotNull(ingredient1.getIngredient());
        Assertions.assertAll(
                () -> Assertions.assertEquals(ingredients.get(0).getId(), ingredient1.getIngredient().getId()),
                () -> Assertions.assertEquals(ingredients.get(0).getName(), ingredient1.getIngredient().getName()),
                () -> Assertions.assertEquals(item1.getAmount(), (double) ingredient1.getIngredient().getAmount()),
                () -> Assertions.assertEquals(item1.getUnit().toString(), ingredient1.getIngredient().getUnit().toString()),
                () -> Assertions.assertEquals(ingredients.get(0).getCategory().toString(), ingredient1.getCategory().toString()),
                () -> Assertions.assertEquals(item1.getCheckedByAccountUsername(), ingredient1.getCheckedBy())
        );
    }

    @Test
    void getShoppingListOpenMultipleItems_success() {
        List<Ingredient> defaultIngredients = DefaultIngredientTestData.getDefaultIngredients();
        var ingredients = ingredientRepository.saveAllAndFlush(defaultIngredients);

        ShoppingListItem item1 = ShoppingListItem.builder()
                .ingredientId(ingredients.get(0).getId())
                .customItemName(null)
                .amount(1.)
                .unit(IngredientUnit.PIECE)
                .isChecked(false)
                .checkedByAccountUsername(null)
                .build();

        ShoppingListItem item2 = ShoppingListItem.builder()
                .ingredientId(ingredients.get(1).getId())
                .customItemName(null)
                .amount(1.8)
                .unit(IngredientUnit.LITRES)
                .isChecked(false)
                .checkedByAccountUsername(null)
                .build();

        ShoppingListItem item3 = ShoppingListItem.builder()
                .ingredientId(ingredients.get(2).getId())
                .customItemName(null)
                .amount(2.8)
                .unit(IngredientUnit.GRAMS)
                .isChecked(false)
                .checkedByAccountUsername(null)
                .build();

        ShoppingList shoppingList = ShoppingList.builder()
                .name("Day 1 Shopping List")
                .menuId(defaultMenu.getId())
                .isClosed(false)
                .organizationId(organizationId)
                .items(Set.of(item1, item2, item3))
                .build();

        shoppingList = shoppingListRepository.saveAndFlush(shoppingList);

        ShoppingListDto responseDto = given().contentType(ContentType.JSON)
                .headers(new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_ADMIN"))))
                .get(URI + '/' + shoppingList.getId())
                .then()
                .statusCode(200)
                .extract()
                .as(ShoppingListDto.class);

        List<ShoppingListIngredientDto> shoppingIngredients = responseDto.getIngredients();
        Assertions.assertAll(
                () -> Assertions.assertEquals("Day 1 Shopping List", responseDto.getName()),
                () -> Assertions.assertEquals(organizationId, (long) responseDto.getOrganizationId()),
                () -> Assertions.assertEquals(ShoppingListStatus.OPEN, responseDto.getStatus()),
                () -> Assertions.assertNotNull(shoppingIngredients),
                () -> Assertions.assertEquals(3, shoppingIngredients.size())
        );

        // for easier comparison
        shoppingIngredients.sort(Comparator.comparing(i -> i.getIngredient().getAmount()));

        var ingredient1 = shoppingIngredients.get(0);
        var ingredient2 = shoppingIngredients.get(1);
        var ingredient3 = shoppingIngredients.get(2);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(ingredient1.getIngredient()),
                () -> Assertions.assertNotNull(ingredient2.getIngredient()),
                () -> Assertions.assertNotNull(ingredient3.getIngredient())
        );
        Assertions.assertAll(
                () -> Assertions.assertEquals(ingredients.get(0).getId(), ingredient1.getIngredient().getId()),
                () -> Assertions.assertEquals(ingredients.get(0).getName(), ingredient1.getIngredient().getName()),
                () -> Assertions.assertEquals(item1.getAmount(), (double) ingredient1.getIngredient().getAmount()),
                () -> Assertions.assertEquals(item1.getUnit().toString(), ingredient1.getIngredient().getUnit().toString()),
                () -> Assertions.assertEquals(ingredients.get(0).getCategory().toString(), ingredient1.getCategory().toString()),
                () -> Assertions.assertEquals(item1.getCheckedByAccountUsername(), ingredient1.getCheckedBy()),

                () -> Assertions.assertEquals(ingredients.get(1).getId(), ingredient2.getIngredient().getId()),
                () -> Assertions.assertEquals(ingredients.get(1).getName(), ingredient2.getIngredient().getName()),
                () -> Assertions.assertEquals(item2.getAmount(), (double) ingredient2.getIngredient().getAmount(), 0.000001),
                () -> Assertions.assertEquals(item2.getUnit().toString(), ingredient2.getIngredient().getUnit().toString()),
                () -> Assertions.assertEquals(ingredients.get(1).getCategory().toString(), ingredient2.getCategory().toString()),
                () -> Assertions.assertEquals(item2.getCheckedByAccountUsername(), ingredient2.getCheckedBy()),

                () -> Assertions.assertEquals(ingredients.get(2).getId(), ingredient3.getIngredient().getId()),
                () -> Assertions.assertEquals(ingredients.get(2).getName(), ingredient3.getIngredient().getName()),
                () -> Assertions.assertEquals(item3.getAmount(), (double) ingredient3.getIngredient().getAmount(), 0.000001),
                () -> Assertions.assertEquals(item3.getUnit().toString(), ingredient3.getIngredient().getUnit().toString()),
                () -> Assertions.assertEquals(ingredients.get(2).getCategory().toString(), ingredient3.getCategory().toString()),
                () -> Assertions.assertEquals(item3.getCheckedByAccountUsername(), ingredient3.getCheckedBy())
        );
    }

    @Test
    void getShoppingListCompletedOneCustomItem_success() {
        accountRepository.saveAndFlush(DefaultAccountTestData.defaultAccount());
        ShoppingListItem item1 = ShoppingListItem.builder()
                .ingredientId(null)
                .customItemName("Apple Juice")
                .amount(1.)
                .unit(IngredientUnit.LITRES)
                .isChecked(true)
                .checkedByAccountUsername(DefaultAccountTestData.DEFAULT_USERNAME)
                .build();

        ShoppingList shoppingList = ShoppingList.builder()
                .name("Day 1 Shopping List")
                .isClosed(true)
                .menuId(defaultMenu.getId())
                .organizationId(organizationId)
                .items(Set.of(item1))
                .build();

        shoppingList = shoppingListRepository.saveAndFlush(shoppingList);

        ShoppingListDto responseDto = given().contentType(ContentType.JSON)
                .headers(new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_ADMIN"))))
                .get(URI + '/' + shoppingList.getId())
                .then()
                .statusCode(200)
                .extract()
                .as(ShoppingListDto.class);

        Assertions.assertAll(
                () -> Assertions.assertEquals("Day 1 Shopping List", responseDto.getName()),
                () -> Assertions.assertEquals(organizationId, (long) responseDto.getOrganizationId()),
                () -> Assertions.assertEquals(ShoppingListStatus.CLOSED, responseDto.getStatus()),
                () -> Assertions.assertNotNull(responseDto.getIngredients()),
                () -> Assertions.assertEquals(1, responseDto.getIngredients().size())
        );
        var ingredient1 = responseDto.getIngredients().get(0);
        Assertions.assertNotNull(ingredient1.getIngredient());
        Assertions.assertAll(
                () -> Assertions.assertNull(ingredient1.getIngredient().getId()),
                () -> Assertions.assertEquals(item1.getCustomItemName(), ingredient1.getIngredient().getName()),
                () -> Assertions.assertEquals(item1.getAmount(), (double) ingredient1.getIngredient().getAmount()),
                () -> Assertions.assertEquals(item1.getUnit().toString(), ingredient1.getIngredient().getUnit().toString()),
                () -> Assertions.assertEquals(IngredientCategory.OTHER, ingredient1.getCategory()),
                () -> Assertions.assertEquals(item1.getCheckedByAccountUsername(), ingredient1.getCheckedBy())
        );
    }
}
