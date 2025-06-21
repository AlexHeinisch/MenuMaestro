package dev.heinisch.menumaestro.stash;

import dev.heinisch.menumaestro.domain.account.Account;
import dev.heinisch.menumaestro.domain.ingredient.IngredientUnit;
import dev.heinisch.menumaestro.domain.menu.Menu;
import dev.heinisch.menumaestro.domain.organization.Organization;
import dev.heinisch.menumaestro.domain.organization.OrganizationAccountRelation;
import dev.heinisch.menumaestro.domain.organization.OrganizationRole;
import dev.heinisch.menumaestro.domain.stash.Stash;
import dev.heinisch.menumaestro.domain.stash.StashEntry;
import dev.heinisch.menumaestro.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.utils.ErrorResponseAssert;
import dev.heinisch.menumaestro.utils.RestHelper;
import dev.heinisch.menumaestro.utils.test_constants.DefaultIngredientTestData;
import dev.heinisch.menumaestro.utils.test_constants.DefaultMenuTestData;
import dev.heinisch.menumaestro.utils.test_constants.DefaultOrganizationTestData;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.http.Method;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.IngredientUnitDto;
import org.openapitools.model.IngredientUseCreateEditDto;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.defaultAccount;


public class PatchStashWebIT extends BaseWebIntegrationTest {

    private RestHelper.PathAndBodyWithoutReturnRestHelper<Long, List<IngredientUseCreateEditDto>> rest;
    private Header authHeader;


    @PostConstruct
    void initRestHelper() {
        authHeader = this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER"));
        rest = new RestHelper.PathAndBodyWithoutReturnRestHelper<>(
                authHeader,
                Method.PATCH,
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
    Long orgStashId;
    Long menuStashId;

    long ingredient1Id;
    long ingredient2Id;

    TransactionTemplate txTemplate;

    @BeforeEach
    void setup() {
        txTemplate = new TransactionTemplate(txManager);
        account = accountRepository.saveAndFlush(defaultAccount());
        organization = organizationRepository.saveAndFlush(DefaultOrganizationTestData.defaultOrganization1());
        orgStashId = organization.getStash().getId();
        menu = menuRepository.saveAndFlush(DefaultMenuTestData.defaultMenu1(organization.getId()));
        menuStashId = menu.getStash().getId();

        ingredient1Id = ingredientRepository.saveAndFlush(DefaultIngredientTestData.defaultIngredient1()).getId();
        ingredient2Id = ingredientRepository.saveAndFlush(DefaultIngredientTestData.defaultIngredient2()).getId();
    }

    @Test
    void patchStash_notFound() {
        var response = rest.requestFails(-1L, List.of(), HttpStatus.NOT_FOUND);
        ErrorResponseAssert.assertThat(response)
                .hasStatus(HttpStatus.NOT_FOUND)
                .messageContains("Stash")
                .messageContains("not found");
    }

    @Test
    void patchStashOfOrg_insufficientPermissionInOrg_forbidden() {
        createAccountRelation(organization, account, OrganizationRole.MEMBER);
        var response = rest.requestFails(orgStashId, List.of(), HttpStatus.FORBIDDEN);
        ErrorResponseAssert.assertThat(response)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("organization");
    }

    @Test
    void patchStashOfMenu_notInOrg_forbidden() {
        var response = rest.requestFails(menuStashId, List.of(), HttpStatus.FORBIDDEN);
        ErrorResponseAssert.assertThat(response)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("organization");
    }

    @Test
    void patchStashOfMenu_stashLocked_ConflictException() {
        menu.getStash().setLocked(true);
        stashRepository.saveAndFlush(menu.getStash());

        createAccountRelation(organization, account, OrganizationRole.PLANNER);
        var response = rest.requestFails(menuStashId, List.of(), HttpStatus.CONFLICT);
        ErrorResponseAssert.assertThat(response)
            .hasStatus(HttpStatus.CONFLICT)
            .messageContains("locked");
    }

    @Test
    void basicAuthTests() {
        rest.basicAuthTests(orgStashId, List.of());
    }

    @Test
    void patch_ingredientNotThereBeforeWithEtag_success() {
        createAccountRelation(organization, account, OrganizationRole.PLANNER);
        rest.requestSuccessful(orgStashId, List.of(new IngredientUseCreateEditDto(ingredient2Id, IngredientUnitDto.KILOGRAMS, 2f)),
                new Headers(authHeader, new Header("If-Match", "0")));
        List<StashEntry> modifiedEntries = new ArrayList<>(stashRepository.findByIdFetchAggregate(orgStashId).orElseThrow().getEntries());

        Assertions.assertAll(
                () -> Assertions.assertEquals(1, modifiedEntries.size()),
                () -> Assertions.assertEquals(ingredient2Id, modifiedEntries.getFirst().getIngredientId()),
                () -> Assertions.assertEquals(IngredientUnit.KILOGRAMS, modifiedEntries.getFirst().getUnit()),
                () -> Assertions.assertEquals(2., modifiedEntries.getFirst().getAmount())
        );
    }

    @Test
    void patch_editExistingIngredient_success() {
        createAccountRelation(organization, account, OrganizationRole.PLANNER);
        txTemplate.executeWithoutResult(tx -> {
            Stash stash = stashRepository.findById(orgStashId).orElseThrow();
            stash.getEntries().add(stashEntry(stash, ingredient1Id, 100., IngredientUnit.GRAMS));
        });
        // check setup
        Assertions.assertEquals(1, stashRepository.findByIdFetchAggregate(orgStashId).orElseThrow().getEntries().size());
        rest.requestSuccessful(orgStashId, List.of(new IngredientUseCreateEditDto(ingredient1Id, IngredientUnitDto.GRAMS, 50f)));
        List<StashEntry> modifiedEntries = new ArrayList<>(stashRepository.findByIdFetchAggregate(orgStashId).orElseThrow().getEntries());

        Assertions.assertAll(
                () -> Assertions.assertEquals(1, modifiedEntries.size()),
                () -> Assertions.assertEquals(ingredient1Id, modifiedEntries.getFirst().getIngredientId()),
                () -> Assertions.assertEquals(IngredientUnit.GRAMS, modifiedEntries.getFirst().getUnit()),
                () -> Assertions.assertEquals(50., modifiedEntries.getFirst().getAmount())
        );
    }

    @Test
    void patch_deleteIngredient_success_andOtherIngredientRemains() {
        createAccountRelation(organization, account, OrganizationRole.PLANNER);
        txTemplate.executeWithoutResult(tx -> {
            Stash stash = stashRepository.findById(orgStashId).orElseThrow();
            stash.getEntries().add(stashEntry(stash, ingredient1Id, 100., IngredientUnit.GRAMS));
            stash.getEntries().add(stashEntry(stash, ingredient1Id, 1., IngredientUnit.TEASPOONS));
        });
        // check setup
        Assertions.assertEquals(2, stashRepository.findByIdFetchAggregate(orgStashId).orElseThrow().getEntries().size());
        rest.request(orgStashId, List.of(new IngredientUseCreateEditDto(ingredient1Id, IngredientUnitDto.GRAMS, 0f)))
                .then()
                .statusCode(200)
                .header("ETag", "1");
        List<StashEntry> modifiedEntries = new ArrayList<>(stashRepository.findByIdFetchAggregate(orgStashId).orElseThrow().getEntries());
        Assertions.assertEquals(1, modifiedEntries.size());
        Assertions.assertEquals(IngredientUnit.TEASPOONS, modifiedEntries.getFirst().getUnit());
    }

    @Test
    void patch_ifMatchInvalid_fails() {
        createAccountRelation(organization, account, OrganizationRole.PLANNER);
        rest.requestFails(orgStashId, List.of(), HttpStatus.BAD_REQUEST, new Headers(authHeader, new Header("If-Match", "notanetag")));
    }

    @Test
    void emptyPatch_butEtagMismatch_httpPreconditionFailed() {
        createAccountRelation(organization, account, OrganizationRole.PLANNER);
        txTemplate.executeWithoutResult(tx -> {
            Stash stash = stashRepository.findById(orgStashId).orElseThrow();
            stash.getEntries().add(stashEntry(stash, ingredient1Id, 100., IngredientUnit.GRAMS));
            stash.getEntries().add(stashEntry(stash, ingredient1Id, 1., IngredientUnit.TEASPOONS));
        });
        // check setup
        Assertions.assertEquals(2, stashRepository.findByIdFetchAggregate(orgStashId).orElseThrow().getEntries().size());
        rest.request(orgStashId, List.of(), new Headers(authHeader, new Header("If-Match", "666")))
                .then()
                .statusCode(HttpStatus.PRECONDITION_FAILED.value());
    }

    @Test
    void patch_deleteIngredient_alreadyRemoved_success() {
        createAccountRelation(organization, account, OrganizationRole.PLANNER);

        rest.requestSuccessful(orgStashId, List.of(new IngredientUseCreateEditDto(ingredient1Id, IngredientUnitDto.GRAMS, 0f)));
        Set<StashEntry> modifiedEntries = stashRepository.findByIdFetchAggregate(orgStashId).orElseThrow().getEntries();
        Assertions.assertEquals(0, modifiedEntries.size());
    }

    @Test
    void patch_ingredientUnitChange_success() {
        createAccountRelation(organization, account, OrganizationRole.PLANNER);
        txTemplate.executeWithoutResult(tx -> {
            Stash stash = stashRepository.findById(orgStashId).orElseThrow();
            stash.getEntries().add(stashEntry(stash, ingredient1Id, 100., IngredientUnit.GRAMS));
        });
        // check setup
        Assertions.assertEquals(1, stashRepository.findByIdFetchAggregate(orgStashId).orElseThrow().getEntries().size());
        rest.requestSuccessful(orgStashId, List.of(new IngredientUseCreateEditDto(ingredient1Id, IngredientUnitDto.GRAMS, 0f),
                new IngredientUseCreateEditDto(ingredient1Id, IngredientUnitDto.TABLESPOONS, 8f)));
        List<StashEntry> modifiedEntries = new ArrayList<>(stashRepository.findByIdFetchAggregate(orgStashId).orElseThrow().getEntries());
        Assertions.assertAll(
                () -> Assertions.assertEquals(1, modifiedEntries.size()),
                () -> Assertions.assertEquals(ingredient1Id, modifiedEntries.getFirst().getIngredientId()),
                () -> Assertions.assertEquals(IngredientUnit.TABLESPOONS, modifiedEntries.getFirst().getUnit()),
                () -> Assertions.assertEquals(8., modifiedEntries.getFirst().getAmount())
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
