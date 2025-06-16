package dev.heinisch.menumaestro.integration_test.stash;


import dev.heinisch.menumaestro.domain.account.Account;
import dev.heinisch.menumaestro.domain.ingredient.Ingredient;
import dev.heinisch.menumaestro.domain.ingredient.IngredientUnit;
import dev.heinisch.menumaestro.domain.menu.Menu;
import dev.heinisch.menumaestro.domain.organization.Organization;
import dev.heinisch.menumaestro.domain.organization.OrganizationAccountRelation;
import dev.heinisch.menumaestro.domain.organization.OrganizationRole;
import dev.heinisch.menumaestro.domain.stash.Stash;
import dev.heinisch.menumaestro.domain.stash.StashEntry;
import dev.heinisch.menumaestro.integration_test.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.integration_test.utils.ErrorResponseAssert;
import dev.heinisch.menumaestro.integration_test.utils.RestHelper;
import dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData;
import dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultIngredientTestData;
import dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultMenuTestData;
import dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultOrganizationTestData;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.http.Method;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openapitools.model.ErrorResponse;
import org.openapitools.model.IngredientUnitDto;
import org.openapitools.model.IngredientUseCreateEditDto;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;

public class MoveStashIngredientsWebIntegrationTest extends BaseWebIntegrationTest {

    private Header authHeader;
    private TransactionTemplate txTemplate;

    @Override
    protected String getBasePath() {
        return "/stash/%s/move-ingredients?otherStashId=%s";
    }

    private String moveEndpointUrl(long fromStash, long toStash) {
        return URI.formatted(fromStash, toStash);
    }


    Account account;
    Organization org1;
    Organization org2;

    long org1StashId;
    long org2StashId;
    Stash menu1Stash;
    long menu1StashId;

    Ingredient ingredient1;
    Ingredient ingredient2;

    @BeforeEach
    public void setup() {
        txTemplate = new TransactionTemplate(txManager);

        authHeader = this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER"));
        account = accountRepository.saveAndFlush(DefaultAccountTestData.defaultAccount());
        org1 = organizationRepository.saveAndFlush(DefaultOrganizationTestData.defaultOrganization1());
        org2 = organizationRepository.saveAndFlush(DefaultOrganizationTestData.defaultOrganization2());
        Menu menu = menuRepository.saveAndFlush(DefaultMenuTestData.defaultMenu1(org1.getId()));

        org1StashId = org1.getStash().getId();
        org2StashId = org2.getStash().getId();
        menu1Stash = menu.getStash();
        menu1StashId = menu1Stash.getId();

        ingredient1 = ingredientRepository.saveAndFlush(DefaultIngredientTestData.defaultIngredient1());
        ingredient2 = ingredientRepository.saveAndFlush(DefaultIngredientTestData.defaultIngredient2());
    }

    @Test
    void basicAuthTests() {
        RestHelper.basicAuthTests(spec -> spec.contentType("application/json")
                .body(Collections.emptyList()).request(Method.POST, moveEndpointUrl(org1StashId, menu1StashId)));
    }

    @Test
    void missingRoleInOrganization_fails() {
        RestAssured.given()
                .headers(new Headers(authHeader))
                .contentType("application/json")
                .body(Collections.emptyList())
                .request(Method.POST, moveEndpointUrl(org1StashId, org2StashId))
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void hasSufficientPermission_emptyTransfer_success() {
        createAccountRelation(org1, account, OrganizationRole.PLANNER);
        RestAssured.given()
                .headers(new Headers(authHeader))
                .contentType("application/json")
                .body(Collections.emptyList())
                .request(Method.POST, moveEndpointUrl(org1StashId, menu1StashId))
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value())
                .header("ETag", "1");
    }

    @Test
    void stashLocked_emptyTransfer_fails() {
        menu1Stash.setLocked(true);
        stashRepository.saveAndFlush(menu1Stash);

        createAccountRelation(org1, account, OrganizationRole.PLANNER);
        RestAssured.given()
            .headers(new Headers(authHeader))
            .contentType("application/json")
            .body(Collections.emptyList())
            .request(Method.POST, moveEndpointUrl(org1StashId, menu1StashId))
            .then()
            .statusCode(HttpStatus.CONFLICT.value());
    }

    static Stream<Arguments> testParameters() {
        return Stream.of(Arguments.of(OrganizationRole.PLANNER, OrganizationRole.SHOPPER),
                Arguments.of(OrganizationRole.SHOPPER, OrganizationRole.PLANNER));
    }

    @ParameterizedTest
    @MethodSource("testParameters")
    void crossOrgTransfer_notEnoughPermissionInOrg_fails(OrganizationRole org1Role, OrganizationRole org2Role) {
        createAccountRelation(org1, account, org1Role);
        createAccountRelation(org2, account, org2Role);
        RestAssured.given()
                .headers(new Headers(authHeader))
                .contentType("application/json")
                .body(Collections.emptyList())
                .request(Method.POST, moveEndpointUrl(org1StashId, org2StashId))
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @ValueSource(booleans = {true, false})
    @ParameterizedTest
    void transfer_ingredientNotThereTooSmall_fails(boolean withIngredient) {
        createAccountRelation(org1, account, OrganizationRole.PLANNER);
        if (withIngredient) {
            txTemplate.executeWithoutResult(tx -> {
                var stash = stashRepository.findById(org1StashId).orElseThrow();
                stash.getEntries().add(stashEntry(stash, ingredient1.getId(), 0.5, IngredientUnit.GRAMS));
            });
        }
        var response = RestAssured.given()
                .headers(new Headers(authHeader))
                .contentType("application/json")
                .body(List.of(new IngredientUseCreateEditDto().id(ingredient1.getId()).unit(IngredientUnitDto.GRAMS).amount(0.6f)))
                .request(Method.POST, moveEndpointUrl(org1StashId, menu1StashId))
                .then()
                .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .log().all()
                .extract().as(ErrorResponse.class);
        ErrorResponseAssert.assertThat(response)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Insufficient amount");
    }

    /**
     * Merged test for the following cases:
     * - source ingredient amount becomes 0 -> deletes entry
     * - source ingredient amount nonzero -> amount subtracted
     * - target ingredient existed -> amount added
     * - target ingredient was in stash but with different unit -> stash contains both units
     */
    @Test
    void moveIngredients_updateAppliedCorrectly() {
        createAccountRelation(org1, account, OrganizationRole.PLANNER);
        createAccountRelation(org2, account, OrganizationRole.PLANNER);
        txTemplate.executeWithoutResult(tx -> {
            var stash = stashRepository.findById(org1StashId).orElseThrow();
            stash.getEntries().add(stashEntry(stash, ingredient1.getId(), 1500, IngredientUnit.GRAMS));
            stash.getEntries().add(stashEntry(stash, ingredient2.getId(), 1, IngredientUnit.LITRES));

            stash = stashRepository.findById(org2StashId).orElseThrow();
            stash.getEntries().add(stashEntry(stash, ingredient1.getId(), 2, IngredientUnit.CUPS));
            stash.getEntries().add(stashEntry(stash, ingredient2.getId(), 0.3, IngredientUnit.LITRES));
        });
        var transferRequests = List.of(
                new IngredientUseCreateEditDto(ingredient1.getId(), IngredientUnitDto.GRAMS, 1400f),
                new IngredientUseCreateEditDto(ingredient2.getId(), IngredientUnitDto.LITRES, 1f)
        );
        RestAssured.given()
                .headers(new Headers(authHeader))
                .contentType("application/json")
                .body(transferRequests)
                .request(Method.POST, moveEndpointUrl(org1StashId, org2StashId))
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value())
                .header("ETag", "1");
        Stash stash1 = stashRepository.findByIdFetchAggregate(org1StashId).orElseThrow();
        var entries = new ArrayList<>(stash1.getEntries());
        Assertions.assertAll(
                () -> Assertions.assertEquals(1, stash1.getEntries().size()),
                () -> Assertions.assertEquals(ingredient1.getId(), entries.getFirst().getIngredientId()),
                () -> Assertions.assertEquals(100, entries.getFirst().getAmount()),
                () -> Assertions.assertEquals(IngredientUnit.GRAMS, entries.getFirst().getUnit())
        );
        Stash stash2 = stashRepository.findByIdFetchAggregate(org2StashId).orElseThrow();
        var entries2 = new ArrayList<>(stash2.getEntries());
        entries2.sort(Comparator.comparing(StashEntry::getAmount));
        Assertions.assertAll(
                () -> Assertions.assertEquals(3, entries2.size()),

                () -> Assertions.assertEquals(ingredient2.getId(), entries2.getFirst().getIngredientId()),
                () -> Assertions.assertEquals(IngredientUnit.LITRES, entries2.getFirst().getUnit()),
                () -> Assertions.assertEquals(1.3, entries2.getFirst().getAmount(), 0.0001),

                () -> Assertions.assertEquals(ingredient1.getId(), entries2.get(2).getIngredientId()),
                () -> Assertions.assertEquals(2, entries2.get(2).getAmount()),
                () -> Assertions.assertEquals(IngredientUnit.CUPS, entries2.get(2).getUnit()),

                () -> Assertions.assertEquals(ingredient1.getId(), entries2.get(1).getIngredientId()),
                () -> Assertions.assertEquals(1.400, entries2.get(1).getAmount(), 0.0001),
                () -> Assertions.assertEquals(IngredientUnit.KILOGRAMS, entries2.get(1).getUnit())
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
