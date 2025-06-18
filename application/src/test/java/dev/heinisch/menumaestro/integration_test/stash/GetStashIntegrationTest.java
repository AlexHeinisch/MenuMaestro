package dev.heinisch.menumaestro.integration_test.stash;

import dev.heinisch.menumaestro.domain.account.Account;
import dev.heinisch.menumaestro.domain.ingredient.Ingredient;
import dev.heinisch.menumaestro.domain.menu.Menu;
import dev.heinisch.menumaestro.domain.organization.Organization;
import dev.heinisch.menumaestro.domain.organization.OrganizationAccountRelation;
import dev.heinisch.menumaestro.domain.organization.OrganizationRole;
import dev.heinisch.menumaestro.domain.stash.Stash;
import dev.heinisch.menumaestro.domain.stash.StashEntry;
import dev.heinisch.menumaestro.integration_test.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.integration_test.utils.ErrorResponseAssert;
import dev.heinisch.menumaestro.integration_test.utils.RestHelper;
import dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultIngredientTestData;
import dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultMenuTestData;
import dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultOrganizationTestData;
import io.restassured.http.Method;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.IngredientUseDto;
import org.openapitools.model.StashResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.defaultAccount;

public class GetStashIntegrationTest extends BaseWebIntegrationTest {

    private RestHelper.PathRestHelper<StashResponseDto, Long> rest;

    @PostConstruct
    void initRestHelper() {
        rest = new RestHelper.PathRestHelper<>(
                StashResponseDto.class,
                this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER")),
                Method.GET,
                URI + "/{id}",
                HttpStatus.OK
        );
    }

    @Override
    protected String getBasePath() {
        return "/stash";
    }

    Account account;
    Organization organization;
    Menu menu;
    Stash menuStash;
    Long orgStashId;
    Long menuStashId;

    @BeforeEach
    void setup() {
        account = accountRepository.saveAndFlush(defaultAccount());
        organization = organizationRepository.saveAndFlush(DefaultOrganizationTestData.defaultOrganization1());
        orgStashId = organization.getStash().getId();
        menu = menuRepository.saveAndFlush(DefaultMenuTestData.defaultMenu1(organization.getId()));
        menuStash = menu.getStash();
        menuStashId = menuStash.getId();
    }

    void createAccountRelation(Organization organization, Account account, OrganizationRole role) {
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation.builder()
                .organization(organization)
                .account(account)
                .role(role)
                .build());
    }

    @Test
    void getStash_notFound() {
        var response = rest.requestFails(-1L, HttpStatus.NOT_FOUND);
        ErrorResponseAssert.assertThat(response)
                .hasStatus(HttpStatus.NOT_FOUND)
                .messageContains("Stash")
                .messageContains("not found");
    }

    @Test
    void getStashOfOrg_insufficientPermissionInOrg_forbidden() {
        createAccountRelation(organization, account, OrganizationRole.INVITED);
        var response = rest.requestFails(orgStashId, HttpStatus.FORBIDDEN);
        ErrorResponseAssert.assertThat(response)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("organization");
    }

    @Test
    void getStashOfOrg_success() {
        createAccountRelation(organization, account, OrganizationRole.PLANNER);
        var ingredientIds = new TransactionTemplate(txManager).execute(status -> {
            Ingredient i1 = ingredientRepository.save(DefaultIngredientTestData.defaultIngredient1());
            Ingredient i2 = ingredientRepository.save(DefaultIngredientTestData.defaultIngredient2());
            Ingredient i3 = ingredientRepository.save(DefaultIngredientTestData.defaultIngredient3());
            var stash = stashRepository.findById(orgStashId).orElseThrow();
            stash.getEntries().add(StashEntry.builder()
                    .stash(stash)
                    .ingredientId(i1.getId())
                    .unit(i1.getDefaultUnit())
                    .amount(500.)
                    .build());
            stash.getEntries().add(StashEntry.builder()
                    .stash(stash)
                    .ingredientId(i2.getId())
                    .unit(i2.getDefaultUnit())
                    .amount(10.)
                    .build());
            stash.getEntries().add(StashEntry.builder()
                    .stash(stash)
                    .ingredientId(i3.getId())
                    .unit(i3.getDefaultUnit())
                    .amount(30.)
                    .build());
            return Triple.of(i1.getId(), i2.getId(), i3.getId());
        });
        var responseWithHeaders = rest.request(orgStashId);
        StashResponseDto response = responseWithHeaders.then()
                .statusCode(200)
                .extract()
                .as(StashResponseDto.class);
        // I hope this is not too fragile and version numbers always start with 1
        String etag = responseWithHeaders.then().extract().header("ETag");
        Assertions.assertTrue(etag.contains("\""));
        etag = etag.replaceAll("\"", "");
        try {
            Integer.parseInt(etag);
        } catch (NumberFormatException e) {
            Assertions.fail("Invalid etag, should be number in quotemarks: " + etag);
        }
        basicStashAssert(response, 3);
        var ingredients = new ArrayList<>(response.getIngredients());
        ingredients.sort(Comparator.comparing(IngredientUseDto::getAmount));
        Assertions.assertAll(
                () -> Assertions.assertEquals(10, ingredients.get(0).getAmount()),
                () -> Assertions.assertEquals(DefaultIngredientTestData.DEFAULT_INGREDIENT_NAME_2, ingredients.get(0).getName()),
                () -> Assertions.assertEquals(DefaultIngredientTestData.DEFAULT_INGREDIENT_UNIT_2.toString(), ingredients.get(0).getUnit().toString()),
                () -> Assertions.assertEquals(ingredientIds.getMiddle(), ingredients.get(0).getId())
        );
        Assertions.assertAll(
                () -> Assertions.assertEquals(30, ingredients.get(1).getAmount()),
                () -> Assertions.assertEquals(DefaultIngredientTestData.DEFAULT_INGREDIENT_NAME_3, ingredients.get(1).getName()),
                () -> Assertions.assertEquals(DefaultIngredientTestData.DEFAULT_INGREDIENT_UNIT_3.toString(), ingredients.get(1).getUnit().toString()),
                () -> Assertions.assertEquals(ingredientIds.getRight(), ingredients.get(1).getId())
        );
        Assertions.assertAll(
                () -> Assertions.assertEquals(500, ingredients.get(2).getAmount()),
                () -> Assertions.assertEquals(DefaultIngredientTestData.DEFAULT_INGREDIENT_NAME_1, ingredients.get(2).getName()),
                () -> Assertions.assertEquals(DefaultIngredientTestData.DEFAULT_INGREDIENT_UNIT_1.toString(), ingredients.get(2).getUnit().toString()),
                () -> Assertions.assertEquals(ingredientIds.getLeft(), ingredients.get(2).getId())
        );
    }

    @Test
    void getStashOfMenu_notInOrg_forbidden() {
        var response = rest.requestFails(menuStashId, HttpStatus.FORBIDDEN);
        ErrorResponseAssert.assertThat(response)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("organization");
    }

    @Test
    void getStashOfMenu_insufficentRole_forbidden() {
        createAccountRelation(organization, account, OrganizationRole.INVITED);
        var response = rest.requestFails(menuStashId, HttpStatus.FORBIDDEN);
        ErrorResponseAssert.assertThat(response)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("organization");
    }

    @Test
    void getStashOfMenu_success() {
        createAccountRelation(organization, account, OrganizationRole.PLANNER);
        var response = rest.requestSuccessful(menuStashId);
        basicStashAssert(response, 0);
    }

    @Test
    void getStashOfMenu_stashLocked_NotFoundException() {
        createAccountRelation(organization, account, OrganizationRole.PLANNER);

        menuStash.setLocked(true);
        stashRepository.saveAndFlush(menuStash);

        var response = rest.requestFails(menuStashId, HttpStatus.NOT_FOUND);
        ErrorResponseAssert.assertThat(response)
            .hasStatus(HttpStatus.NOT_FOUND)
            .messageContains("Stash");
    }

    @Test
    void basicAuthTests() {
        rest.basicAuthTests(orgStashId);
    }

    private static void basicStashAssert(StashResponseDto response, int expectedNumberIngredients) {
        Assertions.assertAll(
                () -> Assertions.assertNotNull(response.getId()),
                () -> Assertions.assertNotNull(response.getIngredients()),
                () -> Assertions.assertEquals(expectedNumberIngredients, response.getIngredients().size())
        );
    }
}
