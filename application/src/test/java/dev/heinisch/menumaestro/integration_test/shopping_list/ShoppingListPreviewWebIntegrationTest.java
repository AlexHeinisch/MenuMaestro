package dev.heinisch.menumaestro.integration_test.shopping_list;

import dev.heinisch.menumaestro.domain.ingredient.IngredientUnit;
import dev.heinisch.menumaestro.domain.menu.Meal;
import dev.heinisch.menumaestro.domain.menu.Menu;
import dev.heinisch.menumaestro.domain.menu.MenuItem;
import dev.heinisch.menumaestro.domain.menu.Snapshot;
import dev.heinisch.menumaestro.domain.recipe.RecipeIngredientUse;
import dev.heinisch.menumaestro.domain.recipe.RecipeValue;
import dev.heinisch.menumaestro.domain.stash.Stash;
import dev.heinisch.menumaestro.domain.stash.StashEntry;
import dev.heinisch.menumaestro.integration_test.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultIngredientTestData;
import dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultOrganizationTestData;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.http.Headers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openapitools.model.IngredientUnitDto;
import org.openapitools.model.ShoppingListCreateDto;
import org.openapitools.model.ShoppingListPreviewEntryDto;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static io.restassured.RestAssured.given;

/**
 * Note: all negative cases covered in {@link ShoppingListCreateWebIntegrationTest}
 */
public class ShoppingListPreviewWebIntegrationTest extends BaseWebIntegrationTest {

    private TransactionTemplate txTemplate;

    long ingredient1Id;
    long ingredient2Id;
    long organizationId;
    private Headers authHeader;

    @Override
    protected String getBasePath() {
        return "/shopping-lists/used-ingredient-preview";
    }

    public void commonSetup() {
        txTemplate = new TransactionTemplate(txManager);
        authHeader = new Headers(this.generateValidAuthorizationHeader("someone", List.of("ROLE_ADMIN")));

        ingredient1Id = ingredientRepository.saveAndFlush(DefaultIngredientTestData.defaultIngredient1()).getId();
        ingredient2Id = ingredientRepository.saveAndFlush(DefaultIngredientTestData.defaultIngredient2()).getId();

        organizationId = organizationRepository.saveAndFlush(DefaultOrganizationTestData.defaultOrganization1()).getId();
    }

    @Test
    void givenMenuWithMeal_usesMatchingStashIngredientAndLeavesOtherIngredient() {
        commonSetup();
        Stash stash = Stash.builder()
                .entries(List.of(
                        StashEntry.builder()
                                .ingredientId(ingredient1Id)
                                .unit(IngredientUnit.LITRES)
                                .amount(3.)
                                .build(),
                        StashEntry.builder()
                                .ingredientId(ingredient2Id)
                                .unit(IngredientUnit.GRAMS)
                                .amount(50.)
                                .build(),
                        StashEntry.builder()
                                .ingredientId(ingredient2Id)
                                .unit(IngredientUnit.PIECE)
                                .amount(1.)
                                .build()
                ))
                .build();
        Menu menu =
                txTemplate.execute(tx -> {
                    var menu1 = Menu.builder()
                            .name("")
                            .description("")
                            .organizationId(organizationId)
                            .numberOfPeople(1)
                            .items(List.of(Meal.builder()
                                            .isDone(false)
                                            .name("bla")
                                            .numberOfPeople(1)
                                            .recipe(RecipeValue.builder()
                                                    .servings(1)
                                                    .name("bla")
                                                    .description("")
                                                    .author("")
                                                    .cookingAppliances(Collections.emptySet())
                                                    .ingredients(Set.of(
                                                            RecipeIngredientUse.builder()
                                                                    .ingredient(ingredientRepository.findById(ingredient1Id).orElseThrow())
                                                                    .unit(IngredientUnit.LITRES).amount(2.5f).build(),
                                                            RecipeIngredientUse.builder()
                                                                    .ingredient(ingredientRepository.findById(ingredient2Id).orElseThrow())
                                                                    .unit(IngredientUnit.GRAMS).amount(70f).build()
                                                    ))
                                                    .build())
                                            .build(),
                                    Snapshot.builder()
                                            .name("test")
                                            .build()))
                            .build();
                    menu1.setStash(stash);
                    return menuRepository.saveAndFlush(menu1);
                });
        var menuItems = new ArrayList<>(menu.getItems());
        menuItems.sort(Comparator.comparing(MenuItem::getPosition));

        ShoppingListCreateDto body = new ShoppingListCreateDto()
                .name("something valid")
                .menuId(menu.getId())
                .organizationId(organizationId)
                .addSnapshotIdsItem(menuItems.get(1).getId());
        List<ShoppingListPreviewEntryDto> previewResponseDto = given().contentType(ContentType.JSON)
                .body(body)
                .headers(authHeader)
                .post(URI)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
        var preview = new ArrayList<>(previewResponseDto);
        preview.sort(Comparator.comparing(ShoppingListPreviewEntryDto::getAmount));
        Assertions.assertAll(
                () -> Assertions.assertEquals(2, preview.size()),
                () -> Assertions.assertEquals(DefaultIngredientTestData.DEFAULT_INGREDIENT_NAME_1, preview.get(0).getName()),
                () -> Assertions.assertEquals(2.5, (double) preview.get(0).getAmount()),
                () -> Assertions.assertEquals(2.5, (double) preview.get(0).getTotalAmount()),
                () -> Assertions.assertEquals(IngredientUnitDto.LITRES, preview.get(0).getUnit()),

                () -> Assertions.assertEquals(DefaultIngredientTestData.DEFAULT_INGREDIENT_NAME_2, preview.get(1).getName()),
                () -> Assertions.assertEquals(50., (double) preview.get(1).getAmount()),
                () -> Assertions.assertEquals(70., (double) preview.get(1).getTotalAmount()),
                () -> Assertions.assertEquals(IngredientUnitDto.GRAMS, preview.get(1).getUnit())
        );
    }
}
