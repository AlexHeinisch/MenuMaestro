package dev.heinisch.menumaestro.integration_test.menu;

import dev.heinisch.menumaestro.domain.account.Account;
import dev.heinisch.menumaestro.domain.ingredient.Ingredient;
import dev.heinisch.menumaestro.domain.ingredient.IngredientUnit;
import dev.heinisch.menumaestro.domain.menu.Menu;
import dev.heinisch.menumaestro.domain.menu.MenuStatus;
import dev.heinisch.menumaestro.domain.organization.Organization;
import dev.heinisch.menumaestro.domain.organization.OrganizationAccountRelation;
import dev.heinisch.menumaestro.domain.organization.OrganizationRole;
import dev.heinisch.menumaestro.domain.stash.Stash;
import dev.heinisch.menumaestro.domain.stash.StashEntry;
import dev.heinisch.menumaestro.integration_test.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData;
import dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultIngredientTestData;
import dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultMenuTestData;
import dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultOrganizationTestData;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static org.junit.jupiter.api.Assertions.assertAll;


public class CloseMenuWebIntegrationTest extends BaseWebIntegrationTest {

    private Header authHeader;
    private TransactionTemplate txTemplate;

    @Override
    protected String getBasePath() {
        return "/menus";
    }

    Account account;
    Organization org;
    Menu menu;

    long orgStashId;
    long menuStashId;

    Ingredient ingredient1;
    Ingredient ingredient2;

    @BeforeEach
    public void setup() {
        txTemplate = new TransactionTemplate(txManager);

        authHeader = this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER"));
        account = accountRepository.saveAndFlush(DefaultAccountTestData.defaultAccount());
        org = organizationRepository.saveAndFlush(DefaultOrganizationTestData.defaultOrganization1());
        menu = menuRepository.saveAndFlush(DefaultMenuTestData.defaultMenu1(org.getId()));

        orgStashId = org.getStash().getId();
        menuStashId = menu.getStash().getId();

        ingredient1 = ingredientRepository.saveAndFlush(DefaultIngredientTestData.defaultIngredient1());
        ingredient2 = ingredientRepository.saveAndFlush(DefaultIngredientTestData.defaultIngredient2());
    }

    @Test
    void closeMenu_withNonExistingMenuId_thenThrowsNotFoundException() {
        RestAssured.given()
            .headers(new Headers(authHeader))
            .contentType("application/json")
            .post(URI + "/" + 666)
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void closeMenu_withMissingRoleInOrganization_thenForbiddenException() {
        RestAssured.given()
            .headers(new Headers(authHeader))
            .contentType("application/json")
            .body(Collections.emptyList())
            .post(URI + "/" + menu.getId())
            .then()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void closeMenu_withExistingMenuAndEmptyStash_thenMenuClosedAndOrgWithEmptyStash() {
        createAccountRelation(org, account, OrganizationRole.ADMIN);

        RestAssured.given()
            .headers(new Headers(authHeader))
            .contentType("application/json")
            .post(URI + "/" + menu.getId())
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value());

        Assertions.assertEquals(MenuStatus.CLOSED, menuRepository.findById(menu.getId()).get().getStatus());
    }

    @Test
    void closeMenu_withExistingMenuAndOrgAndStash_thenMenuClosedAndOrgStash() {
        createAccountRelation(org, account, OrganizationRole.ADMIN);

        txTemplate.executeWithoutResult(tx -> {
            var stash = stashRepository.findById(menu.getStash().getId()).orElseThrow();
            stash.getEntries().add(stashEntry(stash, ingredient1.getId(), 1500, IngredientUnit.GRAMS));
            stash.getEntries().add(stashEntry(stash, ingredient2.getId(), 1, IngredientUnit.LITRES));
        });

        RestAssured.given()
            .headers(new Headers(authHeader))
            .contentType("application/json")
            .post(URI + "/" + menu.getId())
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value());

        Stash stashMenu = stashRepository.findByIdFetchAggregate(menu.getStash().getId()).orElseThrow();
        var entriesMenu = new ArrayList<>(stashMenu.getEntries());
        Stash stashOrg = stashRepository.findByIdFetchAggregate(org.getStash().getId()).orElseThrow();
        var entriesOrg = new ArrayList<>(stashOrg.getEntries());
        entriesOrg.sort(Comparator.comparing(StashEntry::getAmount));
        Menu updatedMenu = menuRepository.findById(menu.getId()).get();

        assertAll(
            () -> Assertions.assertEquals(MenuStatus.CLOSED, updatedMenu.getStatus()),
            () -> Assertions.assertTrue(entriesMenu.isEmpty()),
            () -> Assertions.assertEquals(2, entriesOrg.size()),

            () -> Assertions.assertEquals(ingredient2.getId(),
                entriesOrg.get(0).getIngredientId()),
            () -> Assertions.assertEquals(1,
                entriesOrg.get(0).getAmount()),
            () -> Assertions.assertEquals(IngredientUnit.LITRES,
                entriesOrg.get(0).getUnit()),

            () -> Assertions.assertEquals(ingredient1.getId(),
                entriesOrg.get(1).getIngredientId()),
            () -> Assertions.assertEquals(1.5,
                entriesOrg.get(1).getAmount()),
            () -> Assertions.assertEquals(IngredientUnit.KILOGRAMS,
                entriesOrg.get(1).getUnit())
        );
    }

    void createAccountRelation(Organization organization, Account account, OrganizationRole role) {
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation.builder()
            .organization(organization)
            .account(account)
            .role(role)
            .build());
    }

    StashEntry stashEntry(Stash stash, long ingredientId, double amount, IngredientUnit unit) {
        return StashEntry.builder()
            .stash(stash)
            .ingredientId(ingredientId)
            .amount(amount)
            .unit(unit)
            .build();
    }

}
