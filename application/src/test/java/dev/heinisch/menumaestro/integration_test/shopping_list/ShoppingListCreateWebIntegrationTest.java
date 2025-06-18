package dev.heinisch.menumaestro.integration_test.shopping_list;

import dev.heinisch.menumaestro.domain.ingredient.Ingredient;
import dev.heinisch.menumaestro.domain.ingredient.IngredientUnit;
import dev.heinisch.menumaestro.domain.menu.Meal;
import dev.heinisch.menumaestro.domain.menu.Menu;
import dev.heinisch.menumaestro.domain.menu.MenuItem;
import dev.heinisch.menumaestro.domain.menu.Snapshot;
import dev.heinisch.menumaestro.domain.recipe.RecipeIngredientUse;
import dev.heinisch.menumaestro.domain.recipe.RecipeValue;
import dev.heinisch.menumaestro.integration_test.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultIngredientTestData;
import dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultMenuTestData;
import dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultOrganizationTestData;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.http.Headers;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openapitools.model.ErrorResponse;
import org.openapitools.model.IngredientUnitDto;
import org.openapitools.model.IngredientUseDto;
import org.openapitools.model.ShoppingListCreateDto;
import org.openapitools.model.ShoppingListDto;
import org.openapitools.model.ShoppingListIngredientDto;
import org.openapitools.model.ShoppingListStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultIngredientTestData.DEFAULT_INGREDIENT_CATEGORY_1;
import static io.restassured.RestAssured.given;

public class ShoppingListCreateWebIntegrationTest extends BaseWebIntegrationTest {

    private static final String PATH_CREATE = "";
    private static final String PATH_PREVIEW = "/used-ingredient-preview";

    private TransactionTemplate txTemplate;

    long organizationId;
    long defaultMenuId;

    @Override
    protected String getBasePath() {
        return "/shopping-lists";
    }

    @BeforeEach
    public void setup() {
        txTemplate = new TransactionTemplate(txManager);

        organizationId = organizationRepository.saveAndFlush(DefaultOrganizationTestData.defaultOrganization1()).getId();
        defaultMenuId = menuRepository.saveAndFlush(DefaultMenuTestData.defaultMenu1(organizationId)).getId();
    }

    @ParameterizedTest
    @ValueSource(strings = {PATH_CREATE, PATH_PREVIEW})
    void createShoppingList_requestBodyMissing_failsWithHttp400(String path) {
        given()
                .headers(new Headers(this.generateValidAuthorizationHeader("someone", List.of("ROLE_ADMIN"))))
                .contentType(ContentType.JSON)
                .post(URI + path)
                .then()
                .statusCode(400);
    }

    @ParameterizedTest
    @ValueSource(strings = {PATH_CREATE, PATH_PREVIEW})
    void createShoppingList_nameNull_fails(String path) {
        given().contentType(ContentType.JSON)
                .headers(new Headers(this.generateValidAuthorizationHeader("someone", List.of("ROLE_ADMIN"))))
                .body(new ShoppingListCreateDto()
                        .snapshotIds(Collections.emptyList())
                        .menuId(defaultMenuId)
                        .organizationId(organizationId)
                )
                .post(URI + path)
                .then()
                .statusCode(422);
    }

    @ParameterizedTest
    @ValueSource(strings = {PATH_CREATE, PATH_PREVIEW})
    void createShoppingList_orgIdNull_fails(String path) {
        given().contentType(ContentType.JSON)
                .headers(new Headers(this.generateValidAuthorizationHeader("someone", List.of("ROLE_ADMIN"))))
                .body(new ShoppingListCreateDto()
                        .menuId(defaultMenuId)
                        .snapshotIds(Collections.emptyList())
                        .name("something valid")
                )
                .post(URI + path)
                .then()
                .statusCode(422);
    }

    @ParameterizedTest
    @ValueSource(strings = {PATH_CREATE, PATH_PREVIEW})
    void createShoppingList_snapshotIdsNull_fails(String path) {
        given().contentType(ContentType.JSON)
                .headers(new Headers(this.generateValidAuthorizationHeader("someone", List.of("ROLE_ADMIN"))))
                .body(new ShoppingListCreateDto()
                        .snapshotIds(null)
                        .menuId(defaultMenuId)
                        .organizationId(organizationId)
                        .name("something valid")
                )
                .post(URI + path)
                .then()
                .statusCode(422);
    }

    @ParameterizedTest
    @ValueSource(strings = {PATH_CREATE, PATH_PREVIEW})
    void createShoppingList_orgIdWrong_fails(String path) {
        given().contentType(ContentType.JSON)
                .headers(new Headers(this.generateValidAuthorizationHeader("someone", List.of("ROLE_ADMIN"))))
                .body(new ShoppingListCreateDto()
                        .name("something valid")
                        .menuId(defaultMenuId)
                        .organizationId(-1L)
                        .snapshotIds(Collections.emptyList())
                )
                .post(URI + path)
                .then()
                .statusCode(422);
    }

    @Test
    void createShoppingList_orgIdNotAccessible_fails() {
        // TODO @auth
    }

    @ParameterizedTest
    @ValueSource(strings = {PATH_CREATE, PATH_PREVIEW})
    void createShoppingList_menuIdNull_fails(String path) {
        ErrorResponse response = given().contentType(ContentType.JSON)
                .body(new ShoppingListCreateDto()
                        .name("something valid")
                        .organizationId(organizationId)
                        .snapshotIds(Collections.emptyList())
                )
                .headers(new Headers(this.generateValidAuthorizationHeader("someone", List.of("ROLE_ADMIN"))))
                .post(URI + path)
                .then()
                .statusCode(422)
                .extract().as(ErrorResponse.class);
        Assertions.assertNotNull(response.getDetails());
        Assertions.assertEquals(1, response.getDetails().size());
        Assertions.assertTrue(response.getDetails().getFirst().contains("menuId"));
        Assertions.assertTrue(response.getDetails().getFirst().contains("missing"));
    }

    @ParameterizedTest
    @ValueSource(strings = {PATH_CREATE, PATH_PREVIEW})
    void createShoppingList_menuIdWrong_fails(String path) {
        given().contentType(ContentType.JSON)
                .headers(new Headers(this.generateValidAuthorizationHeader("someone", List.of("ROLE_ADMIN"))))
                .body(new ShoppingListCreateDto()
                        .name("something valid")
                        .menuId(-1L)
                        .organizationId(organizationId)
                        .snapshotIds(Collections.emptyList())
                )
                .post(URI + path)
                .then()
                .statusCode(422);
    }

    @ParameterizedTest
    @ValueSource(strings = {PATH_CREATE, PATH_PREVIEW})
    void createShoppingList_menuNotFromOrganization_fails(String path) {
        var org2 = DefaultOrganizationTestData.defaultOrganization1();
        org2.setName("Organization 2");
        org2 = organizationRepository.saveAndFlush(org2);
        var menu2 = menuRepository.saveAndFlush(DefaultMenuTestData.defaultMenu1(org2.getId()));
        given().contentType(ContentType.JSON)
                .headers(new Headers(this.generateValidAuthorizationHeader("someone", List.of("ROLE_ADMIN"))))
                .body(new ShoppingListCreateDto()
                        .name("something valid")
                        .menuId(menu2.getId())
                        .organizationId(organizationId)
                        .snapshotIds(Collections.emptyList())
                )
                .post(URI + path)
                .then()
                .statusCode(422);
    }

    @ParameterizedTest
    @ValueSource(strings = {PATH_CREATE, PATH_PREVIEW})
    void createShoppingList_snapshotsNotFromSameMenu_fails(String path) {
        Snapshot snapshotToTest = snapshot("snapshot to test");
        menuRepository.saveAndFlush(menuWithItems(organizationId, List.of(snapshotToTest)));
        Assertions.assertNotNull(snapshotToTest.getId());
        given().contentType(ContentType.JSON)
                .headers(new Headers(this.generateValidAuthorizationHeader("someone", List.of("ROLE_ADMIN"))))
                .body(new ShoppingListCreateDto()
                        .name("something valid")
                        .menuId(defaultMenuId)
                        .organizationId(organizationId)
                        .snapshotIds(List.of(snapshotToTest.getId()))
                )
                .post(URI + path)
                .then()
                .statusCode(422);
    }

    @ParameterizedTest
    @ValueSource(strings = {PATH_CREATE, PATH_PREVIEW})
    void createShoppingList_someSnapshotNotExisting_fails(String path) {
        given().contentType(ContentType.JSON)
                .headers(new Headers(this.generateValidAuthorizationHeader("someone", List.of("ROLE_ADMIN"))))
                .body(new ShoppingListCreateDto()
                        .name("something valid")
                        .menuId(defaultMenuId)
                        .organizationId(organizationId)
                        .snapshotIds(List.of(-1L))
                )
                .post(URI + path)
                .then()
                .statusCode(422);
    }

    // so you can create a list with just custom items
    @ParameterizedTest
    @ValueSource(strings = {PATH_CREATE, PATH_PREVIEW})
    void createShoppingList_snapshotIdsEmpty_success(String path) {
        given().contentType(ContentType.JSON)
                .headers(new Headers(this.generateValidAuthorizationHeader("someone", List.of("ROLE_ADMIN"))))
                .body(new ShoppingListCreateDto()
                        .name("something valid")
                        .menuId(defaultMenuId)
                        .organizationId(organizationId)
                        .snapshotIds(Collections.emptyList())
                )
                .post(URI + path)
                .then()
                .statusCode(200);
    }

    @Test
    void createShoppingListOneSnapshot_success() {
        var ingredientsAndMenu = txTemplate.execute(status -> {
            var ingredients = ingredientRepository.saveAll(DefaultIngredientTestData.getDefaultIngredients());
            return Pair.of(ingredients, menuRepository.save(menuWithItems(organizationId,
                    List.of(mealWithIngredients(Set.of(
                                    recipeIngredientUse(ingredients.get(0), 0.5f, IngredientUnit.LITRES))),
                            snapshot("snapshot to test")))));
        });
        var ingredients = ingredientsAndMenu.getLeft();
        var menu = ingredientsAndMenu.getRight();
        MenuItem snapshot = menu.getItems().stream().filter(Snapshot.class::isInstance).findAny().orElseThrow();
        ShoppingListDto responseDto = given().contentType(ContentType.JSON)
                .body(new ShoppingListCreateDto()
                        .name("something valid")
                        .menuId(menu.getId())
                        .organizationId(organizationId)
                        .addSnapshotIdsItem(snapshot.getId())
                )
                .headers(new Headers(this.generateValidAuthorizationHeader("someone", List.of("ROLE_ADMIN"))))
                .post(URI)
                .then()
                .statusCode(200)
                .extract()
                .as(ShoppingListDto.class);
        Assertions.assertAll(
                () -> Assertions.assertEquals("something valid", responseDto.getName()),
                () -> Assertions.assertEquals(organizationId, (long) responseDto.getOrganizationId()),
                () -> Assertions.assertEquals(ShoppingListStatus.OPEN, responseDto.getStatus()),
                () -> Assertions.assertNotNull(responseDto.getIngredients()),
                () -> Assertions.assertEquals(1, responseDto.getIngredients().size())
        );
        var ingredient1 = responseDto.getIngredients().get(0);
        Assertions.assertAll(
                () -> Assertions.assertNotNull(ingredient1.getIngredient()),
                () -> Assertions.assertEquals(ingredients.get(0).getId(), ingredient1.getIngredient().getId()),
                () -> Assertions.assertEquals(ingredients.get(0).getName(), ingredient1.getIngredient().getName()),
                () -> Assertions.assertEquals(0.5f, ingredient1.getIngredient().getAmount()),
                () -> Assertions.assertEquals(IngredientUnitDto.LITRES, ingredient1.getIngredient().getUnit()),
                () -> Assertions.assertEquals(DEFAULT_INGREDIENT_CATEGORY_1.name(), ingredient1.getCategory().name())
        );
    }

    /**
     * Menu:
     * <ol>
     *     <li>Meal 1</li>
     *     <li>Snapshot 1</li>
     *     <li>Meal 2</li>
     *     <li>Meal 3</li>
     *     <li>Snapshot 2</li>
     *     <li>Meal 4</li>
     * </ol>
     * snapshot 1+2 -> ingredients from meals 1,2,3 but not 4
     */
    @Test
    void createShoppingListTwoSnapshots_success() {
        List<Ingredient> defaultIngredients = DefaultIngredientTestData.getDefaultIngredients();
        var menu = txTemplate.execute(status -> {
            var ingredients = ingredientRepository.saveAll(defaultIngredients);
            var menu1 = menuWithItems(organizationId, List.of(
                    mealWithIngredients(Set.of(recipeIngredientUse(ingredients.get(0), 1f, IngredientUnit.PIECE))),
                    snapshot("snapshot1"),
                    mealWithIngredients(Set.of(recipeIngredientUse(ingredients.get(1), 0.5f, IngredientUnit.LITRES),
                            recipeIngredientUse(ingredients.get(2), 22f, IngredientUnit.GRAMS))),
                    mealWithIngredients(Set.of(recipeIngredientUse(ingredients.get(1), 1.3f, IngredientUnit.LITRES))),
                    snapshot("snapshot2")
            ));
            return menuRepository.save(menu1);
        });
        var menuItems = new ArrayList<>(menu.getItems());
        menuItems.sort(Comparator.comparing(MenuItem::getPosition));
        ShoppingListCreateDto body = new ShoppingListCreateDto()
                .name("something valid")
                .menuId(menu.getId())
                .organizationId(organizationId)
                .addSnapshotIdsItem(menuItems.get(1).getId())
                .addSnapshotIdsItem(menuItems.get(4).getId());
        List<IngredientUseDto> previewResponseDto = given().contentType(ContentType.JSON)
                .body(body)
                .headers(new Headers(this.generateValidAuthorizationHeader("someone", List.of("ROLE_ADMIN"))))
                .post(URI + PATH_PREVIEW)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
        Assertions.assertTrue(previewResponseDto.isEmpty());
        ShoppingListDto responseDto = given().contentType(ContentType.JSON)
                .body(body)
                .headers(new Headers(this.generateValidAuthorizationHeader("someone", List.of("ROLE_ADMIN"))))
                .post(URI)
                .then()
                .statusCode(200)
                .extract()
                .as(ShoppingListDto.class);
        List<ShoppingListIngredientDto> shoppingIngredients = responseDto.getIngredients();
        Assertions.assertAll(
                () -> Assertions.assertEquals("something valid", responseDto.getName()),
                () -> Assertions.assertEquals(organizationId, (long) responseDto.getOrganizationId()),
                () -> Assertions.assertEquals(ShoppingListStatus.OPEN, responseDto.getStatus()),
                () -> Assertions.assertNotNull(shoppingIngredients),
                () -> Assertions.assertEquals(3, shoppingIngredients.size())
        );
        // for easier comparison
        shoppingIngredients.sort(Comparator.comparing(i -> i.getIngredient().getAmount()));
        Assertions.assertAll(
                () -> Assertions.assertEquals(1f, shoppingIngredients.get(0).getIngredient().getAmount()),
                () -> Assertions.assertEquals(defaultIngredients.get(0).getName(), shoppingIngredients.get(0).getIngredient().getName()),
                () -> Assertions.assertEquals(IngredientUnitDto.PIECE, shoppingIngredients.get(0).getIngredient().getUnit()),

                () -> Assertions.assertEquals(1.8f, shoppingIngredients.get(1).getIngredient().getAmount()),
                () -> Assertions.assertEquals(defaultIngredients.get(1).getName(), shoppingIngredients.get(1).getIngredient().getName()),
                () -> Assertions.assertEquals(IngredientUnitDto.LITRES, shoppingIngredients.get(1).getIngredient().getUnit()),

                () -> Assertions.assertEquals(22f, shoppingIngredients.get(2).getIngredient().getAmount()),
                () -> Assertions.assertEquals(defaultIngredients.get(2).getName(), shoppingIngredients.get(2).getIngredient().getName()),
                () -> Assertions.assertEquals(IngredientUnitDto.GRAMS, shoppingIngredients.get(2).getIngredient().getUnit())
        );
    }

    private static Snapshot snapshot(String name) {
        return Snapshot.builder()
                .name(name)
                .build();
    }

    private static RecipeIngredientUse recipeIngredientUse(Ingredient ingredient, float amount, IngredientUnit unit) {
        return RecipeIngredientUse.builder()
                .ingredient(ingredient)
                .amount(amount)
                .unit(unit)
                .build();
    }

    private static Meal mealWithIngredients(Set<RecipeIngredientUse> ingredientUses) {
        return Meal.builder()
                .name("meal 1")
                .isDone(false)
                .numberOfPeople(1)
                .recipe(RecipeValue.builder()
                        .author("author")
                        .name("meal 1 recipe")
                        .description("description")
                        .cookingAppliances(Collections.emptySet())
                        .ingredients(ingredientUses)
                        .servings(1)
                        .build())
                .build();
    }

    private static Menu menuWithItems(Long organizationId, List<MenuItem> items) {
        return Menu.builder()
                .name("something valid")
                .description("bla")
                .numberOfPeople(1)
                .organizationId(organizationId)
                .items(items)
                .build();
    }
}
