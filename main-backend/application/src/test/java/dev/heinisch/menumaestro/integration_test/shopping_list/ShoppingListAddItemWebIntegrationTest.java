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
import dev.heinisch.menumaestro.test_support.DatabaseCleanerExtension;
import io.restassured.http.ContentType;
import io.restassured.http.Headers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openapitools.model.ErrorResponse;
import org.openapitools.model.IngredientCategory;
import org.openapitools.model.IngredientUnitDto;
import org.openapitools.model.ShoppingListDto;
import org.openapitools.model.ShoppingListIngredientAddDto;
import org.openapitools.model.ShoppingListIngredientDto;
import org.openapitools.model.ShoppingListStatus;
import org.springframework.http.HttpStatus;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static io.restassured.RestAssured.given;

@ExtendWith(DatabaseCleanerExtension.class)
@Slf4j
public class ShoppingListAddItemWebIntegrationTest extends BaseWebIntegrationTest {

    long organizationId;
    List<Ingredient> ingredients;
    ShoppingListItem item1, item2, item3, item4, item5, item6;
    ShoppingList shoppingListOnlyNotCustomIngredientsSomeUnchecked, shoppingListIncludingCustomIngredientsSomeUnchecked, shoppingListIngredientsAllChecked;
    Menu defaultMenu;

    @Override
    protected String getBasePath() {
        return "/shopping-lists";
    }

    @BeforeEach
    public void setup() {
        organizationId = organizationRepository.saveAndFlush(DefaultOrganizationTestData.defaultOrganization1()).getId();

        ingredients = ingredientRepository.saveAllAndFlush(DefaultIngredientTestData.getDefaultIngredients());

        accountRepository.saveAndFlush(DefaultAccountTestData.defaultAccount());
        defaultMenu = menuRepository.saveAndFlush(DefaultMenuTestData.defaultMenu1(organizationId));
        shoppingListOnlyNotCustomIngredientsSomeUnchecked = testShoppingList_OnlyNotCustomIngredientsSomeUnchecked();
        shoppingListIncludingCustomIngredientsSomeUnchecked = testShoppingList_IncludingCustomIngredientsSomeUnchecked();
        shoppingListIngredientsAllChecked = testShoppingList_IngredientsAllChecked();
    }

    ShoppingList testShoppingList_OnlyNotCustomIngredientsSomeUnchecked() {
        item1 = ShoppingListItem.builder()
                .ingredientId(ingredients.get(0).getId())
                .customItemName(null)
                .amount(1.)
                .unit(IngredientUnit.PIECE)
                .isChecked(false)
                .checkedByAccountUsername(null)
                .build();
        item2 = ShoppingListItem.builder()
                .ingredientId(ingredients.get(1).getId())
                .customItemName(null)
                .amount(2.)
                .unit(IngredientUnit.LITRES)
                .isChecked(true)
                .checkedByAccountUsername(DEFAULT_USERNAME)
                .build();

        ShoppingList shoppingList = ShoppingList.builder()
                .name("Day 1: Breakfast")
                .isClosed(false)
                .organizationId(organizationId)
                .menuId(defaultMenu.getId())
                .items(Set.of(item1, item2))
                .build();

        shoppingListRepository.saveAndFlush(shoppingList);
        return shoppingList;
    }

    ShoppingList testShoppingList_IncludingCustomIngredientsSomeUnchecked() {
        item3 = ShoppingListItem.builder()
                .ingredientId(ingredients.get(0).getId())
                .customItemName(null)
                .amount(1.)
                .unit(IngredientUnit.PIECE)
                .isChecked(false)
                .checkedByAccountUsername(null)
                .build();
        item4 = ShoppingListItem.builder()
                .ingredientId(null)
                .customItemName("Apple Juice")
                .amount(2.)
                .unit(IngredientUnit.LITRES)
                .isChecked(true)
                .checkedByAccountUsername(DEFAULT_USERNAME)
                .build();

        ShoppingList shoppingList = ShoppingList.builder()
                .name("Day 2: Breakfast")
                .isClosed(false)
                .organizationId(organizationId)
                .menuId(defaultMenu.getId())
                .items(Set.of(item3, item4))
                .build();

        shoppingListRepository.saveAndFlush(shoppingList);
        return shoppingList;
    }

    ShoppingList testShoppingList_IngredientsAllChecked() {
        item5 = ShoppingListItem.builder()
                .ingredientId(ingredients.get(0).getId())
                .customItemName(null)
                .amount(1.5)
                .unit(IngredientUnit.LITRES)
                .isChecked(true)
                .checkedByAccountUsername(DEFAULT_USERNAME)
                .build();
        item6 = ShoppingListItem.builder()
                .ingredientId(ingredients.get(1).getId())
                .customItemName("Apple Juice")
                .amount(3.)
                .unit(IngredientUnit.LITRES)
                .isChecked(true)
                .checkedByAccountUsername(DEFAULT_USERNAME)
                .build();

        ShoppingList shoppingList = ShoppingList.builder()
                .name("Day 1: Lunch")
                .isClosed(false)
                .menuId(defaultMenu.getId())
                .organizationId(organizationId)
                .items(Set.of(item5, item6))
                .build();

        shoppingListRepository.saveAndFlush(shoppingList);
        return shoppingList;
    }

    @Test
    void addItemToShoppingListWithIdNotExistingButValidBody_fails() {
        long shoppingListId = -1L;
        ErrorResponse errorResponse = given().contentType(ContentType.JSON)
                .headers(new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_ADMIN"))))
                .body(new ShoppingListIngredientAddDto()
                        .unit(IngredientUnitDto.GRAMS)
                        .amount(5.)
                        .customIngredientName("Orange Juice")
                )
                .post(URI + "/" + shoppingListId + "/items")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .extract().as(ErrorResponse.class);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.NOT_FOUND)
                .messageContains("not found");
    }

    @Test
    void addItemToShoppingListWithMissingRequiredFields_fails() {
        long shoppingListId = shoppingListIncludingCustomIngredientsSomeUnchecked.getId();
        ErrorResponse errorResponse = given().contentType(ContentType.JSON)
                .headers(new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_ADMIN"))))
                .body(new ShoppingListIngredientAddDto()
                        .unit(null)
                        .amount(null))
                .post(URI + "/" + shoppingListId + "/items")
                .then()
                .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .extract().as(ErrorResponse.class);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error occurred")
                .detailsContainSubstring("unit is missing")
                .detailsContainSubstring("amount is missing")
                .detailsHaveSize(2);
    }

    @Test
    void addItemToShoppingListWithIdOrNameMissing_fails() {
        long shoppingListId = shoppingListIncludingCustomIngredientsSomeUnchecked.getId();
        ErrorResponse errorResponse = given().contentType(ContentType.JSON)
                .headers(new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_ADMIN"))))
                .body(new ShoppingListIngredientAddDto()
                        .unit(IngredientUnitDto.GRAMS)
                        .amount(5.)
                )
                .post(URI + "/" + shoppingListId + "/items")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .extract().as(ErrorResponse.class);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.CONFLICT)
                .messageEquals("Either an existing ingredient ID or a custom ingredient name must be provided, but not both!");
    }

    @Test
    void addItemToShoppingListWithBothIdAndName_fails() {
        long shoppingListId = shoppingListIncludingCustomIngredientsSomeUnchecked.getId();
        ErrorResponse errorResponse = given().contentType(ContentType.JSON)
                .headers(new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_ADMIN"))))
                .body(new ShoppingListIngredientAddDto()
                        .unit(IngredientUnitDto.GRAMS)
                        .amount(5.)
                        .customIngredientName("Apple Juice")
                        .existingIngredientId(ingredients.get(0).getId())
                )
                .post(URI + "/" + shoppingListId + "/items")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .extract().as(ErrorResponse.class);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.CONFLICT)
                .messageEquals("Either an existing ingredient ID or a custom ingredient name must be provided, but not both!");
    }

    @Test
    void addNotCustomItemToShoppingList_WithOnlyNotCustomIngredientsSomeUnchecked_success() {
        long shoppingListId = shoppingListOnlyNotCustomIngredientsSomeUnchecked.getId();
        //contains item1 (not custom), item2 (not custom)

        ShoppingListIngredientAddDto shoppingListIngredientAddDto = new ShoppingListIngredientAddDto()
                .unit(IngredientUnitDto.GRAMS)
                .amount(5.)
                .existingIngredientId(ingredients.get(2).getId());
        ShoppingListDto responseDto = given().contentType(ContentType.JSON)
                .headers(new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_ADMIN"))))
                .body(shoppingListIngredientAddDto)
                .post(URI + "/" + shoppingListId + "/items")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(ShoppingListDto.class);

        List<ShoppingListIngredientDto> shoppingIngredients = responseDto.getIngredients();
        Assertions.assertAll(
                () -> Assertions.assertEquals(shoppingListOnlyNotCustomIngredientsSomeUnchecked.getName(), responseDto.getName()),
                () -> Assertions.assertEquals(organizationId, (long) responseDto.getOrganizationId()),
                () -> Assertions.assertEquals(ShoppingListStatus.OPEN, responseDto.getStatus()),
                () -> Assertions.assertNotNull(shoppingIngredients),
                () -> Assertions.assertEquals(shoppingListOnlyNotCustomIngredientsSomeUnchecked.getItems().size() + 1, shoppingIngredients.size())
        );

        // for easier comparison
        shoppingIngredients.sort(Comparator.comparing(i -> i.getIngredient().getAmount()));

        var ingredient1 = shoppingIngredients.get(0);
        var ingredient2 = shoppingIngredients.get(1);
        var ingredient3 = shoppingIngredients.get(2);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(ingredient1.getIngredient()),
                () -> Assertions.assertNotNull(ingredient2.getIngredient()),
                () -> Assertions.assertNotNull(ingredient3.getIngredient()),
                () -> Assertions.assertEquals(ShoppingListStatus.OPEN, responseDto.getStatus())
        );
        Assertions.assertAll(
                // all other items unchanged
                () -> Assertions.assertEquals(ingredients.get(0).getId(), ingredient1.getIngredient().getId()),
                () -> Assertions.assertEquals(ingredients.get(0).getName(), ingredient1.getIngredient().getName()),
                () -> Assertions.assertEquals(item1.getAmount(), (double) ingredient1.getIngredient().getAmount()),
                () -> Assertions.assertEquals(item1.getUnit().toString(), ingredient1.getIngredient().getUnit().toString()),
                () -> Assertions.assertEquals(ingredients.get(0).getCategory().toString(), ingredient1.getCategory().toString()),
                () -> Assertions.assertEquals(item1.getCheckedByAccountUsername(), ingredient1.getCheckedBy()),

                () -> Assertions.assertEquals(ingredients.get(1).getId(), ingredient2.getIngredient().getId()),
                () -> Assertions.assertEquals(ingredients.get(1).getName(), ingredient2.getIngredient().getName()),
                () -> Assertions.assertEquals(item2.getAmount(), (double) ingredient2.getIngredient().getAmount()),
                () -> Assertions.assertEquals(item2.getUnit().toString(), ingredient2.getIngredient().getUnit().toString()),
                () -> Assertions.assertEquals(ingredients.get(1).getCategory().toString(), ingredient2.getCategory().toString()),
                () -> Assertions.assertEquals(item2.getCheckedByAccountUsername(), ingredient2.getCheckedBy()),

                // added item with reference to ingredient of position 2
                () -> Assertions.assertEquals(ingredients.get(2).getId(), ingredient3.getIngredient().getId()),
                () -> Assertions.assertEquals(ingredients.get(2).getName(), ingredient3.getIngredient().getName()),
                () -> Assertions.assertEquals(shoppingListIngredientAddDto.getAmount(), (double) ingredient3.getIngredient().getAmount()),
                () -> Assertions.assertEquals(shoppingListIngredientAddDto.getUnit().toString(), ingredient3.getIngredient().getUnit().toString()),
                () -> Assertions.assertEquals(ingredients.get(2).getCategory().toString(), ingredient3.getCategory().toString()),
                () -> Assertions.assertNull(ingredient3.getCheckedBy())
        );
    }

    @Test
    void addNotCustomItemToShoppingList_IncludingCustomIngredientsSomeUnchecked_success() {
        long shoppingListId = shoppingListIncludingCustomIngredientsSomeUnchecked.getId();
        //contains item3 (not custom), item4 (custom)

        ShoppingListIngredientAddDto shoppingListIngredientAddDto = new ShoppingListIngredientAddDto()
                .unit(IngredientUnitDto.GRAMS)
                .amount(5.5)
                .existingIngredientId(ingredients.get(2).getId());
        ShoppingListDto responseDto = given().contentType(ContentType.JSON)
                .headers(new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_ADMIN"))))
                .body(shoppingListIngredientAddDto)
                .post(URI + "/" + shoppingListId + "/items")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(ShoppingListDto.class);

        List<ShoppingListIngredientDto> shoppingIngredients = responseDto.getIngredients();
        Assertions.assertAll(
                () -> Assertions.assertEquals(shoppingListIncludingCustomIngredientsSomeUnchecked.getName(), responseDto.getName()),
                () -> Assertions.assertEquals(organizationId, (long) responseDto.getOrganizationId()),
                () -> Assertions.assertEquals(ShoppingListStatus.OPEN, responseDto.getStatus()),
                () -> Assertions.assertNotNull(shoppingIngredients),
                () -> Assertions.assertEquals(shoppingListIncludingCustomIngredientsSomeUnchecked.getItems().size() + 1, shoppingIngredients.size())
        );

        // for easier comparison
        shoppingIngredients.sort(Comparator.comparing(i -> i.getIngredient().getAmount()));

        var ingredient1 = shoppingIngredients.get(0);
        var ingredient2 = shoppingIngredients.get(1);
        var ingredient3 = shoppingIngredients.get(2);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(ingredient1.getIngredient()),
                () -> Assertions.assertNotNull(ingredient3.getIngredient()),
                () -> Assertions.assertEquals(ShoppingListStatus.OPEN, responseDto.getStatus())
        );
        Assertions.assertAll(
                // all other items unchanged
                () -> Assertions.assertEquals(ingredients.get(0).getId(), ingredient1.getIngredient().getId()),
                () -> Assertions.assertEquals(ingredients.get(0).getName(), ingredient1.getIngredient().getName()),
                () -> Assertions.assertEquals(item3.getAmount(), (double) ingredient1.getIngredient().getAmount()),
                () -> Assertions.assertEquals(item3.getUnit().toString(), ingredient1.getIngredient().getUnit().toString()),
                () -> Assertions.assertEquals(ingredients.get(0).getCategory().toString(), ingredient1.getCategory().toString()),
                () -> Assertions.assertEquals(item3.getCheckedByAccountUsername(), ingredient1.getCheckedBy()),

                () -> Assertions.assertNull(ingredient2.getIngredient().getId()),
                () -> Assertions.assertEquals(item4.getCustomItemName(), ingredient2.getIngredient().getName()),
                () -> Assertions.assertEquals(item4.getAmount(), (double) ingredient2.getIngredient().getAmount()),
                () -> Assertions.assertEquals(item4.getUnit().toString(), ingredient2.getIngredient().getUnit().toString()),
                () -> Assertions.assertEquals(IngredientCategory.OTHER, ingredient2.getCategory()),
                () -> Assertions.assertEquals(item4.getCheckedByAccountUsername(), ingredient2.getCheckedBy()),

                // added item with reference to ingredient of position 2
                () -> Assertions.assertEquals(ingredients.get(2).getId(), ingredient3.getIngredient().getId()),
                () -> Assertions.assertEquals(ingredients.get(2).getName(), ingredient3.getIngredient().getName()),
                () -> Assertions.assertEquals(shoppingListIngredientAddDto.getAmount(), (double) ingredient3.getIngredient().getAmount()),
                () -> Assertions.assertEquals(shoppingListIngredientAddDto.getUnit().toString(), ingredient3.getIngredient().getUnit().toString()),
                () -> Assertions.assertEquals(ingredients.get(2).getCategory().toString(), ingredient3.getCategory().toString()),
                () -> Assertions.assertNull(ingredient3.getCheckedBy())
        );
    }

    @Test
    void addCustomItemToShoppingList_AllItemsChecked_success() {
        long shoppingListId = shoppingListIngredientsAllChecked.getId();
        //contains item5 (not custom), item6 (custom)

        ShoppingListIngredientAddDto shoppingListIngredientAddDto = new ShoppingListIngredientAddDto()
                .unit(IngredientUnitDto.GRAMS)
                .amount(5.5)
                .customIngredientName("Orange Juice");
        ShoppingListDto responseDto = given().contentType(ContentType.JSON)
                .headers(new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_ADMIN"))))
                .body(shoppingListIngredientAddDto)
                .post(URI + "/" + shoppingListId + "/items")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(ShoppingListDto.class);

        List<ShoppingListIngredientDto> shoppingIngredients = responseDto.getIngredients();
        Assertions.assertAll(
                () -> Assertions.assertEquals(shoppingListIngredientsAllChecked.getName(), responseDto.getName()),
                () -> Assertions.assertEquals(organizationId, (long) responseDto.getOrganizationId()),
                () -> Assertions.assertEquals(ShoppingListStatus.OPEN, responseDto.getStatus()), // before adding it was COMPLETE
                () -> Assertions.assertNotNull(shoppingIngredients),
                () -> Assertions.assertEquals(shoppingListIngredientsAllChecked.getItems().size() + 1, shoppingIngredients.size())
        );

        // for easier comparison
        shoppingIngredients.sort(Comparator.comparing(i -> i.getIngredient().getAmount()));

        var ingredient1 = shoppingIngredients.get(0);
        var ingredient2 = shoppingIngredients.get(1);
        var ingredient3 = shoppingIngredients.get(2);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(ingredient1.getIngredient()),
                () -> Assertions.assertEquals(ShoppingListStatus.OPEN, responseDto.getStatus())
        );
        Assertions.assertAll(
                // all other items unchanged
                () -> Assertions.assertEquals(ingredients.get(0).getId(), ingredient1.getIngredient().getId()),
                () -> Assertions.assertEquals(ingredients.get(0).getName(), ingredient1.getIngredient().getName()),
                () -> Assertions.assertEquals(item5.getAmount(), (double) ingredient1.getIngredient().getAmount()),
                () -> Assertions.assertEquals(item5.getUnit().toString(), ingredient1.getIngredient().getUnit().toString()),
                () -> Assertions.assertEquals(ingredients.get(0).getCategory().toString(), ingredient1.getCategory().toString()),
                () -> Assertions.assertEquals(item5.getCheckedByAccountUsername(), ingredient1.getCheckedBy()),

                () -> Assertions.assertNull(ingredient2.getIngredient().getId()),
                () -> Assertions.assertEquals(item6.getCustomItemName(), ingredient2.getIngredient().getName()),
                () -> Assertions.assertEquals(item6.getAmount(), (double) ingredient2.getIngredient().getAmount()),
                () -> Assertions.assertEquals(item6.getUnit().toString(), ingredient2.getIngredient().getUnit().toString()),
                () -> Assertions.assertEquals(IngredientCategory.OTHER, ingredient2.getCategory()),
                () -> Assertions.assertEquals(item6.getCheckedByAccountUsername(), ingredient2.getCheckedBy()),

                // added custom item (with no reference to an ingredient id)
                () -> Assertions.assertNull(ingredient3.getIngredient().getId()),
                () -> Assertions.assertEquals(shoppingListIngredientAddDto.getCustomIngredientName(), ingredient3.getIngredient().getName()),
                () -> Assertions.assertEquals(shoppingListIngredientAddDto.getAmount(), (double) ingredient3.getIngredient().getAmount()),
                () -> Assertions.assertEquals(shoppingListIngredientAddDto.getUnit().toString(), ingredient3.getIngredient().getUnit().toString()),
                () -> Assertions.assertEquals(IngredientCategory.OTHER, ingredient3.getCategory()),
                () -> Assertions.assertNull(ingredient3.getCheckedBy())
        );
    }

    @Test
    void addNotCustomItemToShoppingList_AlreadyExistingIngredient_success() {
        long shoppingListId = shoppingListIncludingCustomIngredientsSomeUnchecked.getId();
        //contains item3 (not custom), item4 (custom)

        ShoppingListIngredientAddDto shoppingListIngredientAddDto = new ShoppingListIngredientAddDto()
                .unit(IngredientUnitDto.PIECE)
                .amount(6.)
                .existingIngredientId(ingredients.get(0).getId());
        ShoppingListDto responseDto = given().contentType(ContentType.JSON)
                .headers(new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_ADMIN"))))
                .body(shoppingListIngredientAddDto)
                .post(URI + "/" + shoppingListId + "/items")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(ShoppingListDto.class);

        List<ShoppingListIngredientDto> shoppingIngredients = responseDto.getIngredients();
        Assertions.assertAll(
                () -> Assertions.assertEquals(shoppingListIncludingCustomIngredientsSomeUnchecked.getName(), responseDto.getName()),
                () -> Assertions.assertEquals(organizationId, (long) responseDto.getOrganizationId()),
                () -> Assertions.assertEquals(ShoppingListStatus.OPEN, responseDto.getStatus()),
                () -> Assertions.assertNotNull(shoppingIngredients),
                () -> Assertions.assertEquals(shoppingListIncludingCustomIngredientsSomeUnchecked.getItems().size(), shoppingIngredients.size())
        );

        // for easier comparison
        shoppingIngredients.sort(Comparator.comparing(i -> i.getIngredient().getAmount()));

        var ingredient1 = shoppingIngredients.get(0);
        var ingredient2 = shoppingIngredients.get(1);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(ingredient2.getIngredient()),
                () -> Assertions.assertEquals(ShoppingListStatus.OPEN, responseDto.getStatus())
        );
        Assertions.assertAll(
                // changed amount of not non-custom item because of addition
                () -> Assertions.assertEquals(ingredients.get(0).getId(), ingredient2.getIngredient().getId()),
                () -> Assertions.assertEquals(ingredients.get(0).getName(), ingredient2.getIngredient().getName()),
                () -> Assertions.assertEquals(item3.getAmount() + shoppingListIngredientAddDto.getAmount(), (double) ingredient2.getIngredient().getAmount()),
                () -> Assertions.assertEquals(item3.getUnit().toString(), ingredient2.getIngredient().getUnit().toString()),
                () -> Assertions.assertEquals(ingredients.get(0).getCategory().toString(), ingredient2.getCategory().toString()),
                () -> Assertions.assertEquals(item3.getCheckedByAccountUsername(), ingredient2.getCheckedBy()),

                // custom item unchanged
                () -> Assertions.assertNull(ingredient1.getIngredient().getId()),
                () -> Assertions.assertEquals(item4.getCustomItemName(), ingredient1.getIngredient().getName()),
                () -> Assertions.assertEquals(item4.getAmount(), (double) ingredient1.getIngredient().getAmount()),
                () -> Assertions.assertEquals(item4.getUnit().toString(), ingredient1.getIngredient().getUnit().toString()),
                () -> Assertions.assertEquals(IngredientCategory.OTHER, ingredient1.getCategory()),
                () -> Assertions.assertEquals(item4.getCheckedByAccountUsername(), ingredient1.getCheckedBy())
        );
    }

    @Test
    void addCustomItemToShoppingList_AlreadyExistingName_success() {
        long shoppingListId = shoppingListIncludingCustomIngredientsSomeUnchecked.getId();
        //contains item3 (not custom), item4 (custom)

        ShoppingListIngredientAddDto shoppingListIngredientAddDto = new ShoppingListIngredientAddDto()
                .unit(IngredientUnitDto.LITRES)
                .amount(6.)
                .customIngredientName("Apple Juice");
        ShoppingListDto responseDto = given().contentType(ContentType.JSON)
                .headers(new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_ADMIN"))))
                .body(shoppingListIngredientAddDto)
                .post(URI + "/" + shoppingListId + "/items")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(ShoppingListDto.class);

        List<ShoppingListIngredientDto> shoppingIngredients = responseDto.getIngredients();
        Assertions.assertAll(
                () -> Assertions.assertEquals(shoppingListIncludingCustomIngredientsSomeUnchecked.getName(), responseDto.getName()),
                () -> Assertions.assertEquals(organizationId, (long) responseDto.getOrganizationId()),
                () -> Assertions.assertEquals(ShoppingListStatus.OPEN, responseDto.getStatus()),
                () -> Assertions.assertNotNull(shoppingIngredients),
                () -> Assertions.assertEquals(shoppingListIncludingCustomIngredientsSomeUnchecked.getItems().size(), shoppingIngredients.size())
        );

        // for easier comparison
        shoppingIngredients.sort(Comparator.comparing(i -> i.getIngredient().getAmount()));

        var ingredient1 = shoppingIngredients.get(0);
        var ingredient2 = shoppingIngredients.get(1);

        Assertions.assertAll(
                () -> Assertions.assertNotNull(ingredient1.getIngredient()),
                () -> Assertions.assertEquals(ShoppingListStatus.OPEN, responseDto.getStatus())
        );
        Assertions.assertAll(
                // non-custom item because of addition
                () -> Assertions.assertEquals(ingredients.get(0).getId(), ingredient1.getIngredient().getId()),
                () -> Assertions.assertEquals(ingredients.get(0).getName(), ingredient1.getIngredient().getName()),
                () -> Assertions.assertEquals(item3.getAmount(), (double) ingredient1.getIngredient().getAmount()),
                () -> Assertions.assertEquals(item3.getUnit().toString(), ingredient1.getIngredient().getUnit().toString()),
                () -> Assertions.assertEquals(ingredients.get(0).getCategory().toString(), ingredient1.getCategory().toString()),
                () -> Assertions.assertEquals(item3.getCheckedByAccountUsername(), ingredient1.getCheckedBy()),

                // changed amount of not custom item because of addition
                () -> Assertions.assertNull(ingredient2.getIngredient().getId()),
                () -> Assertions.assertEquals(item4.getCustomItemName(), ingredient2.getIngredient().getName()),
                () -> Assertions.assertEquals(item4.getAmount() + shoppingListIngredientAddDto.getAmount(), (double) ingredient2.getIngredient().getAmount()),
                () -> Assertions.assertEquals(item4.getUnit().toString(), ingredient2.getIngredient().getUnit().toString()),
                () -> Assertions.assertEquals(IngredientCategory.OTHER, ingredient2.getCategory()),
                () -> Assertions.assertEquals(item4.getCheckedByAccountUsername(), ingredient2.getCheckedBy())
        );
    }
}
