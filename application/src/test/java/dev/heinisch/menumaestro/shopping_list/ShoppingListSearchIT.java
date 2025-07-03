package dev.heinisch.menumaestro.shopping_list;

import dev.heinisch.menumaestro.domain.account.Account;
import dev.heinisch.menumaestro.domain.ingredient.IngredientUnit;
import dev.heinisch.menumaestro.domain.menu.Menu;
import dev.heinisch.menumaestro.domain.organization.OrganizationAccountRelation;
import dev.heinisch.menumaestro.domain.organization.OrganizationRole;
import dev.heinisch.menumaestro.domain.shopping_list.ShoppingList;
import dev.heinisch.menumaestro.domain.shopping_list.ShoppingListItem;
import dev.heinisch.menumaestro.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.utils.TestPageableResponse;
import dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData;
import dev.heinisch.menumaestro.utils.test_constants.DefaultIngredientTestData;
import dev.heinisch.menumaestro.utils.test_constants.DefaultMenuTestData;
import dev.heinisch.menumaestro.utils.test_constants.DefaultOrganizationTestData;
import dev.heinisch.menumaestro.utils.DatabaseCleanerExtension;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.http.Headers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openapitools.model.ErrorResponse;
import org.openapitools.model.ShoppingListDto;
import org.openapitools.model.ShoppingListStatus;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;

import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.*;
import static io.restassured.RestAssured.given;

@ActiveProfiles("datagen-off")
@ExtendWith(DatabaseCleanerExtension.class)
public class ShoppingListSearchIT extends BaseWebIntegrationTest {

    long organizationId,organization2Id;
    ShoppingList shoppingList1, shoppingList2, shoppingList3, shoppingList4,shoppingList5;
    Account admin,defaultAccount,defaultAccount3;

    Menu defaultMenu1, defaultMenu2;
    @Override
    protected String getBasePath() {
        return "/shopping-lists";
    }

    @BeforeEach
    public void setup() {
        organizationId = organizationRepository.saveAndFlush(DefaultOrganizationTestData.defaultOrganization1()).getId();
        organization2Id = organizationRepository.saveAndFlush(DefaultOrganizationTestData.defaultOrganization2()).getId();
        defaultMenu1 = menuRepository.saveAndFlush(DefaultMenuTestData.defaultMenu1(organizationId));
        defaultMenu2 = menuRepository.saveAndFlush(DefaultMenuTestData.defaultMenu2(organizationId));
        var ingredients = ingredientRepository.saveAllAndFlush(DefaultIngredientTestData.getDefaultIngredients());

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
        admin=accountRepository.saveAndFlush(DefaultAccountTestData.defaultAccount2());
        admin.setIsGlobalAdmin(true);
        accountRepository.saveAndFlush(admin);

        defaultAccount=accountRepository.saveAndFlush(DefaultAccountTestData.defaultAccount());
        defaultAccount3=accountRepository.saveAndFlush(DefaultAccountTestData.defaultAccount3());
        ShoppingListItem item4 = ShoppingListItem.builder()
                .ingredientId(null)
                .customItemName("Apple Juice")
                .amount(1.)
                .unit(IngredientUnit.LITRES)
                .isChecked(true)
                .checkedByAccountUsername(DEFAULT_USERNAME)
                .build();

        ShoppingListItem item5 = ShoppingListItem.builder()
                .ingredientId(null)
                .customItemName("Apple Juice")
                .amount(1.)
                .unit(IngredientUnit.LITRES)
                .isChecked(true)
                .checkedByAccountUsername(DEFAULT_USERNAME)
                .build();
        ShoppingListItem item6 = ShoppingListItem.builder()
                .ingredientId(null)
                .customItemName("Orange Juice")
                .amount(1.)
                .unit(IngredientUnit.LITRES)
                .isChecked(false)
                .checkedByAccountUsername(null)
                .build();
        //NOTE: an item can be used in exactly one shopping list

        shoppingList1 = ShoppingList.builder()
                .name("Day 1: Breakfast")
                .isClosed(false)
                .organizationId(organizationId)
                .menuId(defaultMenu1.getId())
                .items(Set.of(item1))
                .build();

        shoppingList2 = ShoppingList.builder()
                .name("Day 1: Lunch")
                .isClosed(false)
                .organizationId(organizationId)
                .menuId(defaultMenu2.getId())
                .items(Set.of(item2))
                .build();

        shoppingList3 = ShoppingList.builder()
                .name("Day 2: Breakfast")
                .isClosed(false)
                .organizationId(organizationId)
                .menuId(defaultMenu1.getId())
                .items(Set.of(item3, item5))
                .build();

        shoppingList4 = ShoppingList.builder()
                .name("Day 3: Lunch")
                .isClosed(true)
                .organizationId(organizationId)
                .menuId(defaultMenu2.getId())
                .items(Set.of(item4))
                .build();

        shoppingList5 = ShoppingList.builder()
                .name("Day 4: Breakfast")
                .isClosed(false)
                .organizationId(organization2Id)
                .menuId(defaultMenu1.getId())
                .items(Set.of(item6))
                .build();

        shoppingListRepository.saveAllAndFlush(List.of(shoppingList1, shoppingList2, shoppingList3, shoppingList4,shoppingList5));
    }

    @Test
    void getAllShoppingLists_success() {
        TestPageableResponse<ShoppingListDto> fullResponseDto = given().contentType(ContentType.JSON)
            .headers(new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_ADMIN"))))
                .get(URI)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {});

        var responseDto = fullResponseDto.getContent();

        Assertions.assertEquals(5, fullResponseDto.getTotalElements());
        Assertions.assertAll(
                () -> Assertions.assertEquals(shoppingList1.getName(), responseDto.get(0).getName()),
                () -> Assertions.assertEquals(shoppingList1.getOrganizationId(), (long) responseDto.get(0).getOrganizationId()),
                () -> Assertions.assertEquals(ShoppingListStatus.OPEN, responseDto.get(0).getStatus()),
                () -> Assertions.assertNotNull(responseDto.get(0).getIngredients()),
                () -> Assertions.assertEquals(1, responseDto.get(0).getIngredients().size()),

                () -> Assertions.assertEquals(shoppingList2.getName(), responseDto.get(1).getName()),
                () -> Assertions.assertEquals(shoppingList2.getOrganizationId(), (long) responseDto.get(1).getOrganizationId()),
                () -> Assertions.assertEquals(ShoppingListStatus.OPEN, responseDto.get(1).getStatus()),
                () -> Assertions.assertNotNull(responseDto.get(1).getIngredients()),
                () -> Assertions.assertEquals(1, responseDto.get(1).getIngredients().size()),

                () -> Assertions.assertEquals(shoppingList3.getName(), responseDto.get(2).getName()),
                () -> Assertions.assertEquals(shoppingList3.getOrganizationId(), (long) responseDto.get(2).getOrganizationId()),
                () -> Assertions.assertEquals(ShoppingListStatus.OPEN, responseDto.get(2).getStatus()),
                () -> Assertions.assertNotNull(responseDto.get(2).getIngredients()),
                () -> Assertions.assertEquals(2, responseDto.get(2).getIngredients().size()),

                () -> Assertions.assertEquals(shoppingList5.getName(), responseDto.get(3).getName()),
                () -> Assertions.assertEquals(shoppingList5.getOrganizationId(), (long) responseDto.get(3).getOrganizationId()),
                () -> Assertions.assertEquals(ShoppingListStatus.OPEN, responseDto.get(3).getStatus()),
                () -> Assertions.assertNotNull(responseDto.get(3).getIngredients()),
                () -> Assertions.assertEquals(1, responseDto.get(3).getIngredients().size()),
                    // Order by isClosed ASC, name ASC

                () -> Assertions.assertEquals(shoppingList4.getName(), responseDto.get(4).getName()),
                () -> Assertions.assertEquals(shoppingList4.getOrganizationId(), (long) responseDto.get(4).getOrganizationId()),
                () -> Assertions.assertEquals(ShoppingListStatus.CLOSED, responseDto.get(4).getStatus()),
                () -> Assertions.assertNotNull(responseDto.get(4).getIngredients()),
                () -> Assertions.assertEquals(1, responseDto.get(4).getIngredients().size())
        );
    }

    @Test
    void getAllShoppingListsWithinPageSizeLimit_success() {
        TestPageableResponse<ShoppingListDto> fullResponseDto = given().contentType(ContentType.JSON)
                .headers(new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_ADMIN"))))
                .get(URI + "?page=1&size=2")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {});

        var responseDto = fullResponseDto.getContent();

        Assertions.assertEquals(2, fullResponseDto.getNumberOfElements());
        Assertions.assertAll(
                () -> Assertions.assertEquals(shoppingList3.getName(), responseDto.get(0).getName()),
                () -> Assertions.assertEquals(shoppingList3.getOrganizationId(), (long) responseDto.get(0).getOrganizationId()),
                () -> Assertions.assertEquals(ShoppingListStatus.OPEN, responseDto.get(0).getStatus()),
                () -> Assertions.assertNotNull(responseDto.get(0).getIngredients()),
                () -> Assertions.assertEquals(2, responseDto.get(0).getIngredients().size()),

                () -> Assertions.assertEquals(shoppingList5.getName(), responseDto.get(1).getName()),
                () -> Assertions.assertEquals(shoppingList5.getOrganizationId(), (long) responseDto.get(1).getOrganizationId()),
                () -> Assertions.assertEquals(ShoppingListStatus.OPEN, responseDto.get(1).getStatus()),
                () -> Assertions.assertNotNull(responseDto.get(1).getIngredients()),
                () -> Assertions.assertEquals(1, responseDto.get(1).getIngredients().size())
                // Order by isCompleted ASC, name ASC
        );
    }

    @Test
    void getShoppingListsByName_success() {
        TestPageableResponse<ShoppingListDto> fullResponseDto = given().contentType(ContentType.JSON)
            .headers(new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_ADMIN"))))
                .get(URI + "?name=Lunch")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {});

        var responseDto = fullResponseDto.getContent();

        Assertions.assertEquals(2, fullResponseDto.getTotalElements());
        Assertions.assertAll(
                () -> Assertions.assertEquals(shoppingList2.getName(), responseDto.get(0).getName()),
                () -> Assertions.assertEquals(shoppingList2.getOrganizationId(), (long) responseDto.get(0).getOrganizationId()),
                () -> Assertions.assertEquals(ShoppingListStatus.OPEN, responseDto.get(0).getStatus()),
                () -> Assertions.assertNotNull(responseDto.get(0).getIngredients()),
                () -> Assertions.assertEquals(1, responseDto.get(0).getIngredients().size()),

                () -> Assertions.assertEquals(shoppingList4.getName(), responseDto.get(1).getName()),
                () -> Assertions.assertEquals(shoppingList4.getOrganizationId(), (long) responseDto.get(1).getOrganizationId()),
                () -> Assertions.assertEquals(ShoppingListStatus.CLOSED, responseDto.get(1).getStatus()),
                () -> Assertions.assertNotNull(responseDto.get(1).getIngredients()),
                () -> Assertions.assertEquals(1, responseDto.get(1).getIngredients().size())
        );
    }

    @Test
    void getShoppingListsByNameAndStatusCompleted_success() {
        TestPageableResponse<ShoppingListDto> fullResponseDto = given().contentType(ContentType.JSON)
            .headers(new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_ADMIN"))))
                .get(URI + "?name=Lunch&status=CLOSED")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {});

        var responseDto = fullResponseDto.getContent();

        Assertions.assertEquals(1, fullResponseDto.getTotalElements());
        Assertions.assertAll(
                () -> Assertions.assertEquals(shoppingList4.getName(), responseDto.get(0).getName()),
                () -> Assertions.assertEquals(shoppingList4.getOrganizationId(), (long) responseDto.get(0).getOrganizationId()),
                () -> Assertions.assertEquals(ShoppingListStatus.CLOSED, responseDto.get(0).getStatus()),
                () -> Assertions.assertNotNull(responseDto.get(0).getIngredients()),
                () -> Assertions.assertEquals(1, responseDto.get(0).getIngredients().size())
        );
    }

    @Test
    void getShoppingListsByNameAndStatusOpen_success() {
        TestPageableResponse<ShoppingListDto> fullResponseDto = given().contentType(ContentType.JSON)
            .headers(new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_ADMIN"))))
                .get(URI + "?name=Day%1&status=OPEN")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {});

        var responseDto = fullResponseDto.getContent();

        Assertions.assertEquals(2, fullResponseDto.getTotalElements());
        Assertions.assertAll(
                () -> Assertions.assertEquals(shoppingList1.getName(), responseDto.get(0).getName()),
                () -> Assertions.assertEquals(shoppingList1.getOrganizationId(), (long) responseDto.get(0).getOrganizationId()),
                () -> Assertions.assertEquals(ShoppingListStatus.OPEN, responseDto.get(0).getStatus()),
                () -> Assertions.assertNotNull(responseDto.get(0).getIngredients()),
                () -> Assertions.assertEquals(1, responseDto.get(0).getIngredients().size()),

                () -> Assertions.assertEquals(shoppingList2.getName(), responseDto.get(1).getName()),
                () -> Assertions.assertEquals(shoppingList2.getOrganizationId(), (long) responseDto.get(1).getOrganizationId()),
                () -> Assertions.assertEquals(ShoppingListStatus.OPEN, responseDto.get(1).getStatus()),
                () -> Assertions.assertNotNull(responseDto.get(1).getIngredients()),
                () -> Assertions.assertEquals(1, responseDto.get(1).getIngredients().size())
        );
    }

    @Test
    void getShoppingListsByNameAndStatusOpenAndMenuId_success() {
        TestPageableResponse<ShoppingListDto> fullResponseDto = given().contentType(ContentType.JSON)
                .headers(new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_ADMIN"))))
                .get(URI + "?name=Day%1&status=OPEN&menuId=" + defaultMenu1.getId())
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {});

        var responseDto = fullResponseDto.getContent();

        Assertions.assertEquals(1, fullResponseDto.getTotalElements());
        Assertions.assertAll(
                () -> Assertions.assertEquals(shoppingList1.getName(), responseDto.get(0).getName()),
                () -> Assertions.assertEquals(shoppingList1.getOrganizationId(), (long) responseDto.get(0).getOrganizationId()),
                () -> Assertions.assertEquals(ShoppingListStatus.OPEN, responseDto.get(0).getStatus()),
                () -> Assertions.assertEquals(defaultMenu1.getId(), responseDto.get(0).getMenuId()),
                () -> Assertions.assertNotNull(responseDto.get(0).getIngredients()),
                () -> Assertions.assertEquals(1, responseDto.get(0).getIngredients().size())
        );
    }

    @Test
    void getShoppingListsByNameAndStatusCompleted_noMatch_success() {
        TestPageableResponse<ShoppingListDto> responseDto = given().contentType(ContentType.JSON)
            .headers(new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_ADMIN"))))
                .get(URI + "?name=Breakfast&status=CLOSED")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {});

        Assertions.assertEquals(0, responseDto.getTotalElements());
    }

    @Test
    void getShoppingListsStatusCompleted_success() {
        TestPageableResponse<ShoppingListDto> fullResponseDto = given().contentType(ContentType.JSON)
            .headers(new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_ADMIN"))))
                .get(URI + "?status=CLOSED")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {});

        var responseDto = fullResponseDto.getContent();

        Assertions.assertEquals(1, fullResponseDto.getTotalElements());
        Assertions.assertAll(
                () -> Assertions.assertEquals(shoppingList4.getName(), responseDto.get(0).getName()),
                () -> Assertions.assertEquals(shoppingList4.getOrganizationId(), (long) responseDto.get(0).getOrganizationId()),
                () -> Assertions.assertEquals(ShoppingListStatus.CLOSED, responseDto.get(0).getStatus()),
                () -> Assertions.assertNotNull(responseDto.get(0).getIngredients()),
                () -> Assertions.assertEquals(1, responseDto.get(0).getIngredients().size())
        );
    }

    @Test
    void getShoppingListsStatusOpen_success() {
        TestPageableResponse<ShoppingListDto> fullResponseDto = given().contentType(ContentType.JSON)
            .headers(new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_ADMIN"))))
                .get(URI + "?status=OPEN")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {});

        var responseDto = fullResponseDto.getContent();

        Assertions.assertEquals(4, fullResponseDto.getTotalElements());
        Assertions.assertAll(
                () -> Assertions.assertEquals(shoppingList1.getName(), responseDto.get(0).getName()),
                () -> Assertions.assertEquals(shoppingList1.getOrganizationId(), (long) responseDto.get(0).getOrganizationId()),
                () -> Assertions.assertEquals(ShoppingListStatus.OPEN, responseDto.get(0).getStatus()),
                () -> Assertions.assertNotNull(responseDto.get(0).getIngredients()),
                () -> Assertions.assertEquals(1, responseDto.get(0).getIngredients().size()),

                () -> Assertions.assertEquals(shoppingList2.getName(), responseDto.get(1).getName()),
                () -> Assertions.assertEquals(shoppingList2.getOrganizationId(), (long) responseDto.get(1).getOrganizationId()),
                () -> Assertions.assertEquals(ShoppingListStatus.OPEN, responseDto.get(1).getStatus()),
                () -> Assertions.assertNotNull(responseDto.get(1).getIngredients()),
                () -> Assertions.assertEquals(1, responseDto.get(1).getIngredients().size()),

                () -> Assertions.assertEquals(shoppingList3.getName(), responseDto.get(2).getName()),
                () -> Assertions.assertEquals(shoppingList3.getOrganizationId(), (long) responseDto.get(2).getOrganizationId()),
                () -> Assertions.assertEquals(ShoppingListStatus.OPEN, responseDto.get(2).getStatus()),
                () -> Assertions.assertNotNull(responseDto.get(2).getIngredients()),
                () -> Assertions.assertEquals(2, responseDto.get(2).getIngredients().size()),

                () -> Assertions.assertEquals(shoppingList5.getName(), responseDto.get(3).getName()),
                () -> Assertions.assertEquals(shoppingList5.getOrganizationId(), (long) responseDto.get(3).getOrganizationId()),
                () -> Assertions.assertEquals(ShoppingListStatus.OPEN, responseDto.get(3).getStatus()),
                () -> Assertions.assertNotNull(responseDto.get(3).getIngredients()),
                () -> Assertions.assertEquals(1, responseDto.get(3).getIngredients().size())

        );
    }

    @Test
    void getShoppingListsStatusNotValid_fails() {
        ErrorResponse responseDto = given().contentType(ContentType.JSON)
            .headers(new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_ADMIN"))))
                .get(URI + "?status=VeryNoise")
                .then()
                .statusCode(400)
                .extract()
                .as(new TypeRef<>() {});
        Assertions.assertAll(
                () -> Assertions.assertEquals(400, responseDto.getStatus()),
                () -> Assertions.assertEquals("Parameter 'status' is not valid for type 'ShoppingListStatus'", responseDto.getMessage())
        );
    }

    @Test
    void whenSearchShoppingLists_asAdmin_seesAllLists() {
        TestPageableResponse<ShoppingListDto> responseDto = given().contentType(ContentType.JSON)
            .headers(new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME_2, List.of("ROLE_ADMIN"))))
                .get(URI)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {});

        Assertions.assertEquals(5, responseDto.getTotalElements());
    }

    @Test
    void whenSearchShoppingLists_asAdmin_seesAllListsWithinPageSizeLimit() {
        TestPageableResponse<ShoppingListDto> responseDto = given().contentType(ContentType.JSON)
                .headers(new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME_2, List.of("ROLE_ADMIN"))))
                .get(URI + "?page=0&size=3")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {});

        Assertions.assertEquals(3, responseDto.getNumberOfElements());
    }

    @Test
    void whenSearchShoppingLists_asUserInOrg_seesOnlyOrgLists() {
        organizationAccountRelationRepository.saveAndFlush(
            OrganizationAccountRelation.builder()
                .account(defaultAccount)
                .organization(organizationRepository.findById(organizationId).get())
                .role(OrganizationRole.MEMBER)
                .build()
        );

        TestPageableResponse<ShoppingListDto> fullResponseDto = given().contentType(ContentType.JSON)
            .headers(new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER"))))
                .get(URI)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {});

        Assertions.assertEquals(4, fullResponseDto.getTotalElements());
        var responseDto = fullResponseDto.getContent();
        responseDto.forEach(list -> Assertions.assertEquals(organizationId, (long) list.getOrganizationId()));
    }

    @Test
    void whenSearchShoppingLists_asUserNotInOrg_seesEmptyList() {

        TestPageableResponse<ShoppingListDto> responseDto = given().contentType(ContentType.JSON)
            .headers(new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME_3, List.of("ROLE_USER"))))
                .get(URI)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {});

        Assertions.assertEquals(0, responseDto.getTotalElements());
    }

    @Test
    void whenSearchShoppingLists_asUserInMultipleOrgs_seesAllOrgLists() {

        organizationAccountRelationRepository.saveAndFlush(
            OrganizationAccountRelation.builder()
                .account(defaultAccount)
                .organization(organizationRepository.findById(organizationId).get())
                .role(OrganizationRole.MEMBER)
                .build()
        );
        organizationAccountRelationRepository.saveAndFlush(
            OrganizationAccountRelation.builder()
                .account(defaultAccount)
                .organization(organizationRepository.findById(organization2Id).get())
                .role(OrganizationRole.MEMBER)
                .build()
        );

        TestPageableResponse<ShoppingListDto> responseDto = given().contentType(ContentType.JSON)
            .headers(new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER"))))
                .get(URI)
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {});

        Assertions.assertEquals(5, responseDto.getTotalElements());
    }
}
