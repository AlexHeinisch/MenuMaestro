package at.codemaestro.integration_test.shopping_list;

import at.codemaestro.domain.account.Account;
import at.codemaestro.domain.ingredient.Ingredient;
import at.codemaestro.domain.ingredient.IngredientUnit;
import at.codemaestro.domain.menu.Menu;
import at.codemaestro.domain.organization.Organization;
import at.codemaestro.domain.organization.OrganizationAccountRelation;
import at.codemaestro.domain.organization.OrganizationRole;
import at.codemaestro.domain.shopping_list.ShoppingList;
import at.codemaestro.domain.shopping_list.ShoppingListItem;
import at.codemaestro.integration_test.BaseWebIntegrationTest;
import at.codemaestro.integration_test.utils.test_constants.DefaultAccountTestData;
import at.codemaestro.integration_test.utils.test_constants.DefaultIngredientTestData;
import at.codemaestro.integration_test.utils.test_constants.DefaultMenuTestData;
import at.codemaestro.integration_test.utils.test_constants.DefaultOrganizationTestData;
import at.codemaestro.persistence.AccountRepository;
import at.codemaestro.persistence.IngredientRepository;
import at.codemaestro.persistence.OrganizationAccountRelationRepository;
import at.codemaestro.persistence.OrganizationRepository;
import at.codemaestro.persistence.ShoppingListRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Headers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.ShoppingListEditDto;
import org.openapitools.model.ShoppingListIngredientEditDto;
import org.openapitools.model.ShoppingListStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Set;

import static at.codemaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static org.hamcrest.Matchers.equalTo;

@ActiveProfiles({"datagen-off", "test"})
public class ShoppingListEditWebIntegrationTest extends BaseWebIntegrationTest {

    @Override
    protected String getBasePath() {
        return "/shopping-lists";
    }

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ShoppingListRepository shoppingListRepository;

    @Autowired
    private OrganizationAccountRelationRepository organizationAccountRelationRepository;

    @Autowired
    private PlatformTransactionManager txManager;

    private TransactionTemplate txTemplate;

    Organization organization;

    Menu defaultMenu;

    ShoppingList shoppingList1;

    Account account, account2;

    ShoppingListItem item1, item2;

    List<Ingredient> ingredients;

    @BeforeEach
    public void setup() {
        txTemplate = new TransactionTemplate(txManager);

        organization = organizationRepository.saveAndFlush(DefaultOrganizationTestData.defaultOrganization1());
        defaultMenu = menuRepository.saveAndFlush(DefaultMenuTestData.defaultMenu1(organization.getId()));

        ingredients = ingredientRepository.saveAllAndFlush(DefaultIngredientTestData.getDefaultIngredients());

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
                .amount(1.8)
                .unit(IngredientUnit.LITRES)
                .isChecked(false)
                .checkedByAccountUsername(null)
                .build();

        account = accountRepository.saveAndFlush(DefaultAccountTestData.defaultAccount());

        account2 = DefaultAccountTestData.defaultAccount();
        account2.setUsername("Different");
        accountRepository.saveAndFlush(account2);

        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation
                .builder()
                .account(account)
                .organization(organization)
                .role(OrganizationRole.MEMBER)
                .build());

        shoppingList1 = ShoppingList.builder()
                .name("Day 1: Breakfast")
                .isClosed(false)
                .organizationId(organization.getId())
                .menuId(defaultMenu.getId())
                .items(Set.of(item1))
                .build();

        shoppingListRepository.saveAllAndFlush(List.of(shoppingList1));
    }

    @Test
    void checkOffShoppingListItem_ShoppingListDoesNotExist_fails() {
        ShoppingListEditDto shoppingListEditDto = new ShoppingListEditDto();

        var response = RestAssured
                .given()
                .headers(new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_ADMIN"))))
                .contentType(ContentType.JSON)
                .body(shoppingListEditDto)
                .patch(URI + "/{id}", -1);

        response
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("message", equalTo("Shopping list with id -1 not found!"));
    }

    @Test
    void checkOffShoppingListItem_ShoppingListItemIsNotPartOfThisShoppingList_fails() {
        ShoppingListEditDto shoppingListEditDto = new ShoppingListEditDto()
                .status(ShoppingListStatus.OPEN)
                .ingredients(List.of(
                        new ShoppingListIngredientEditDto()
                                .id(item2.getId())
                                .checked(true))
                );

        var response = RestAssured
                .given()
                .headers(new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_ADMIN"))))
                .contentType(ContentType.JSON)
                .body(shoppingListEditDto)
                .patch(URI + "/{id}", shoppingList1.getId());

        response
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("message", equalTo("Shopping list item with id " + item2.getId() +
                        " not found in shopping list with id " + shoppingList1.getId() + "!"));
    }

    @Test
    void checkOffShoppingListItem_AlreadyCheckedByAnotherUser_fails() {
        var item = shoppingList1.getItems().stream().findFirst().get();
        item.setCheckedByAccountUsername(account2.getUsername());
        item.setIsChecked(true);
        shoppingListRepository.saveAndFlush(shoppingList1);

        ShoppingListEditDto shoppingListEditDto = new ShoppingListEditDto()
                .status(ShoppingListStatus.OPEN)
                .ingredients(List.of(
                        new ShoppingListIngredientEditDto()
                                .id(item1.getId())
                                .checked(true))
                );

        var response = RestAssured
                .given()
                .headers(new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_ADMIN"))))
                .contentType(ContentType.JSON)
                .body(shoppingListEditDto)
                .patch(URI + "/{id}", shoppingList1.getId());

        response
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body("message", equalTo("Item was already checked off!"));
    }

    @Test
    void checkOffShoppingListItem_success() {
        ShoppingListEditDto shoppingListEditDto = new ShoppingListEditDto()
                .status(ShoppingListStatus.OPEN)
                .ingredients(List.of(
                        new ShoppingListIngredientEditDto()
                                .id(item1.getId())
                                .checked(true))
                );

        var response = RestAssured
                .given()
                .headers(new Headers(List.of(this.generateValidAuthorizationHeader(account.getUsername()
                        , List.of("ROLE_ADMIN", "ROLE_USER")))))
                .contentType(ContentType.JSON)
                .body(shoppingListEditDto)
                .patch(URI + "/{id}", shoppingList1.getId());

        response
                .then()
                .statusCode(HttpStatus.OK.value());

        txTemplate.executeWithoutResult(tx -> {
            ShoppingList shoppingList = shoppingListRepository.findById(shoppingList1.getId()).get();
            Assertions.assertEquals(account.getUsername(), shoppingList.getItems().stream().findFirst().get().getCheckedByAccountUsername());
        });
    }

    @Test
    void uncheckShoppingListItem_success() {
        shoppingList1.getItems().stream().findFirst().get().setCheckedByAccountUsername(account2.getUsername());
        shoppingListRepository.saveAndFlush(shoppingList1);

        ShoppingListEditDto shoppingListEditDto = new ShoppingListEditDto()
                .status(ShoppingListStatus.OPEN)
                .ingredients(List.of(
                        new ShoppingListIngredientEditDto()
                                .id(item1.getId())
                                .checked(false))
                );

        var response = RestAssured
                .given()
                .headers(new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_ADMIN"))))
                .contentType(ContentType.JSON)
                .body(shoppingListEditDto)
                .patch(URI + "/{id}", shoppingList1.getId());

        response
                .then()
                .statusCode(HttpStatus.OK.value());

        txTemplate.executeWithoutResult(tx -> {
            ShoppingList shoppingList = shoppingListRepository.findById(shoppingList1.getId()).get();
            Assertions.assertNull(shoppingList.getItems().stream().findFirst().get().getCheckedByAccountUsername());
        });
    }

    @Test
    void markShoppingListAsClosed_success() {

        var response = RestAssured
            .given()
            .headers(new Headers(List.of(this.generateValidAuthorizationHeader(account.getUsername()
                , List.of("ROLE_ADMIN", "ROLE_USER")))))
            .contentType(ContentType.JSON)
            .patch(URI + "/{id}/close", shoppingList1.getId());
        response
                .then()
                .statusCode(HttpStatus.OK.value());

        txTemplate.executeWithoutResult(tx -> {
            ShoppingList shoppingList = shoppingListRepository.findById(shoppingList1.getId()).get();
            Assertions.assertTrue(shoppingList.getIsClosed());
        });
    }
    @Test
    void markShoppingListAsClosedWithCustomIngredients_success() {
        ShoppingListItem item3 = ShoppingListItem.builder()
                .ingredientId(ingredients.get(0).getId())
                .customItemName(null)
                .amount(1.)
                .unit(IngredientUnit.PIECE)
                .isChecked(false)
                .checkedByAccountUsername(null)
                .build();
        ShoppingListItem item4 = ShoppingListItem.builder()
                .ingredientId(null)
                .customItemName("Apple Juice")
                .amount(2.)
                .unit(IngredientUnit.LITRES)
                .isChecked(true)
                .checkedByAccountUsername(DEFAULT_USERNAME)
                .build();

        ShoppingList shoppingList2 = ShoppingList.builder()
                .name("Day 2: Breakfast")
                .isClosed(false)
                .organizationId(organization.getId())
                .menuId(defaultMenu.getId())
                .items(Set.of(item3, item4))
                .build();
        shoppingListRepository.saveAllAndFlush(List.of(shoppingList2));

        var response = RestAssured
                .given()
                .headers(new Headers(List.of(this.generateValidAuthorizationHeader(account.getUsername()
                        , List.of("ROLE_ADMIN", "ROLE_USER")))))
                .contentType(ContentType.JSON)
                .patch(URI + "/{id}/close", shoppingList2.getId());
        response
                .then()
                .statusCode(HttpStatus.OK.value());

        txTemplate.executeWithoutResult(tx -> {
            ShoppingList shoppingList = shoppingListRepository.findById(shoppingList2.getId()).get();
            Assertions.assertTrue(shoppingList.getIsClosed());
        });
    }

}
