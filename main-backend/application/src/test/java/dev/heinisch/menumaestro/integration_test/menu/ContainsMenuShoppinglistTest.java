package dev.heinisch.menumaestro.integration_test.menu;

import dev.heinisch.menumaestro.domain.account.Account;
import dev.heinisch.menumaestro.domain.cooking_appliance.CookingAppliance;
import dev.heinisch.menumaestro.domain.ingredient.Ingredient;
import dev.heinisch.menumaestro.domain.ingredient.IngredientUnit;
import dev.heinisch.menumaestro.domain.menu.Menu;
import dev.heinisch.menumaestro.domain.organization.Organization;
import dev.heinisch.menumaestro.domain.organization.OrganizationAccountRelation;
import dev.heinisch.menumaestro.domain.organization.OrganizationRole;
import dev.heinisch.menumaestro.domain.shopping_list.ShoppingList;
import dev.heinisch.menumaestro.domain.shopping_list.ShoppingListItem;
import dev.heinisch.menumaestro.integration_test.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.integration_test.utils.RestHelper;
import io.restassured.http.Method;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.AddMealToMenuRequest;
import org.openapitools.model.CookingApplianceUseCreateEditDto;
import org.openapitools.model.IngredientUnitDto;
import org.openapitools.model.IngredientUseCreateEditDto;
import org.openapitools.model.RecipeCreateEditDto;
import org.openapitools.model.RecipeDto;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Set;

import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.defaultAccount;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultCookingApplianceTestData.defaultCookingAppliance1;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultIngredientTestData.defaultIngredient1;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultIngredientTestData.defaultIngredient2;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultMenuTestData.defaultMenu1;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultOrganizationTestData.defaultOrganization1;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultRecipeTestData.defaultRecipeCreateEditDto1;

public class ContainsMenuShoppinglistTest extends BaseWebIntegrationTest {

    private RestHelper.PathRestHelper<Boolean, Long> rest;
    private Menu menu;
    private Account defaultAccount;
    private Organization organization;
    private Ingredient ingredient1;
    private Ingredient ingredient2;
    private CookingAppliance appliance1;
    private RecipeDto recipeDto;

    @Override
    protected String getBasePath() {
        return "/menus";
    }

    @PostConstruct
    private void initRestHelper() {
        rest = new RestHelper.PathRestHelper<>(
                Boolean.class,
                this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER")),
                Method.GET,
                URI + "/{id}/existsShoppingListForMenu",
                HttpStatus.OK
        );
    }


    @BeforeEach
    public void setup() {
        defaultAccount = accountRepository.saveAndFlush(defaultAccount());

        organization = organizationRepository.saveAndFlush(defaultOrganization1());
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation.builder()
                .role(OrganizationRole.OWNER)
                .organization(organization)
                .account(defaultAccount)
                .build());

        menu = menuRepository.saveAndFlush(defaultMenu1(organization.getId()));
        ingredient1 = ingredientRepository.saveAndFlush(defaultIngredient1());
        ingredient2 = ingredientRepository.saveAndFlush(defaultIngredient2());
        appliance1 = cookingApplianceRepository.saveAndFlush(defaultCookingAppliance1());
        recipeDto = recipeService.createRecipe(defaultCreateEditRecipeDto());
        menuService.addMealToMenu(menu.getId(), new AddMealToMenuRequest().recipeId(recipeDto.getId()));

    }

    @Test
    void isShoppingListAddedToMenu() {

        boolean doesShoppingListExistsForMenu = rest.requestSuccessful(menu.getId());
        Assertions.assertFalse(doesShoppingListExistsForMenu);


        ShoppingListItem item1 = ShoppingListItem.builder()
                .ingredientId(ingredient1.getId())
                .customItemName(null)
                .amount(0.5)
                .unit(IngredientUnit.LITRES)
                .isChecked(false)
                .checkedByAccountUsername(null)
                .build();

        ShoppingList shoppingList = ShoppingList.builder()
                .name("Day 1 Shopping List")
                .isClosed(false)
                .menuId(menu.getId())
                .organizationId(organization.getId())
                .items(Set.of(item1))
                .build();

        shoppingList = shoppingListRepository.saveAndFlush(shoppingList);
        doesShoppingListExistsForMenu = rest.requestSuccessful(menu.getId());

        Assertions.assertTrue(doesShoppingListExistsForMenu);
        ShoppingListItem item2 = ShoppingListItem.builder()
                .ingredientId(ingredient2.getId())
                .customItemName(null)
                .amount(0.5)
                .unit(IngredientUnit.LITRES)
                .isChecked(false)
                .checkedByAccountUsername(null)
                .build();

        ShoppingList shoppingList2 = ShoppingList.builder()
                .name("Day 2 Shopping List")
                .isClosed(false)
                .menuId(menu.getId())
                .organizationId(organization.getId())
                .items(Set.of(item2))
                .build();
        shoppingList2 = shoppingListRepository.saveAndFlush(shoppingList2);
        doesShoppingListExistsForMenu = rest.requestSuccessful(menu.getId());
        Assertions.assertTrue(doesShoppingListExistsForMenu);

        menuService.deleteMenuById(menu.getId());

        Assertions.assertFalse(shoppingListRepository.findById(shoppingList.getId()).isPresent());
        Assertions.assertFalse(shoppingListRepository.findById(shoppingList2.getId()).isPresent());
    }

    @Test
    void closedShoppingListAddedToMenu() {

        boolean doesShoppingListExistsForMenu = rest.requestSuccessful(menu.getId());
        Assertions.assertFalse(doesShoppingListExistsForMenu);


        ShoppingListItem item1 = ShoppingListItem.builder()
                .ingredientId(ingredient1.getId())
                .customItemName(null)
                .amount(0.5)
                .unit(IngredientUnit.LITRES)
                .isChecked(false)
                .checkedByAccountUsername(null)
                .build();

        ShoppingList shoppingList = ShoppingList.builder()
                .name("Day 1 Shopping List")
                .isClosed(true)
                .menuId(menu.getId())
                .organizationId(organization.getId())
                .items(Set.of(item1))
                .build();

        shoppingList = shoppingListRepository.saveAndFlush(shoppingList);
        doesShoppingListExistsForMenu = rest.requestSuccessful(menu.getId());

        Assertions.assertFalse(doesShoppingListExistsForMenu);
        ShoppingListItem item2 = ShoppingListItem.builder()
                .ingredientId(ingredient2.getId())
                .customItemName(null)
                .amount(0.5)
                .unit(IngredientUnit.LITRES)
                .isChecked(false)
                .checkedByAccountUsername(null)
                .build();

        ShoppingList shoppingList2 = ShoppingList.builder()
                .name("Day 2 Shopping List")
                .isClosed(false)
                .menuId(menu.getId())
                .organizationId(organization.getId())
                .items(Set.of(item2))
                .build();
        shoppingList2 = shoppingListRepository.saveAndFlush(shoppingList2);
        doesShoppingListExistsForMenu = rest.requestSuccessful(menu.getId());
        Assertions.assertTrue(doesShoppingListExistsForMenu);
        shoppingList2.setIsClosed(true);
        shoppingList2 = shoppingListRepository.saveAndFlush(shoppingList2);
        doesShoppingListExistsForMenu = rest.requestSuccessful(menu.getId());
        Assertions.assertFalse(doesShoppingListExistsForMenu);

    }


    private RecipeCreateEditDto defaultCreateEditRecipeDto() {
        return defaultRecipeCreateEditDto1()
                .addCookingAppliancesItem(new CookingApplianceUseCreateEditDto()
                        .amount(1)
                        .id(appliance1.getId()))
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
