package dev.heinisch.menumaestro.account;

import dev.heinisch.menumaestro.domain.account.Account;
import dev.heinisch.menumaestro.domain.organization.Organization;
import dev.heinisch.menumaestro.domain.organization.OrganizationAccountRelation;
import dev.heinisch.menumaestro.domain.organization.OrganizationRole;
import dev.heinisch.menumaestro.domain.recipe.Recipe;
import dev.heinisch.menumaestro.domain.recipe.RecipeValue;
import dev.heinisch.menumaestro.domain.recipe.RecipeVisibility;
import dev.heinisch.menumaestro.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.utils.ErrorResponseAssert;
import io.restassured.RestAssured;
import io.restassured.http.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.OrganizationRoleEnum;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.*;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultOrganizationTestData.defaultOrganization1;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultRecipeValueTestData.defaultRecipeValue;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles({"datagen-off", "test"})
public class DeleteAccountIT extends BaseWebIntegrationTest {

    Account account1, account2, account3;
    Organization organization1;

    @Override
    protected  String getBasePath() {
        return "/accounts";
    }

    @BeforeEach
    void setup() {
        account1 = accountRepository.saveAndFlush(defaultAccount());
        account2 = accountRepository.saveAndFlush(defaultAccount2());
        account3 = accountRepository.saveAndFlush(defaultAccount3());
        organization1 = organizationRepository.saveAndFlush(defaultOrganization1());
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation
            .builder()
            .role(OrganizationRole.OWNER)
            .account(account1)
            .organization(organization1)
            .build()
        );
    }

    @Test
    void whenDeleteAccount_withValidIdAndOrganizationWithAdmin_thenDeleteAccountAndAdminBecomesOwner() {
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation
            .builder()
            .role(OrganizationRole.MEMBER)
            .account(account2)
            .organization(organization1)
            .build()
        );
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation
            .builder()
            .role(OrganizationRole.ADMIN)
            .account(account3)
            .organization(organization1)
            .build()
        );

        RestAssured.given()
            .contentType("application/json")
            .headers(new Headers(this.generateValidAuthorizationHeader(account1.getUsername(), List.of("ROLE_USER"))))
            .delete(URI + "/" + account1.getUsername())
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value());

        assertAll(
            () -> assertTrue(accountRepository.findById(account1.getUsername()).isEmpty()),
            () -> assertEquals(OrganizationRoleEnum.OWNER.getValue().toLowerCase(),
                organizationAccountRelationRepository.findByUsername(account3.getUsername())
                    .getFirst().getRole().toString().toLowerCase())
        );
    }

    @Test
    void whenDeleteAccount_withValidIdAndOrganizationWithUser_thenDeleteAccountAndUserBecomesOwner() {
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation
            .builder()
            .role(OrganizationRole.MEMBER)
            .account(account2)
            .organization(organization1)
            .build()
        );

        RestAssured.given()
            .contentType("application/json")
            .headers(new Headers(this.generateValidAuthorizationHeader(account1.getUsername(), List.of("ROLE_USER"))))
            .delete(URI + "/" + account1.getUsername())
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value());

        assertAll(
            () -> assertTrue(accountRepository.findById(account1.getUsername()).isEmpty()),
            () -> assertEquals(OrganizationRoleEnum.OWNER.getValue().toLowerCase(),
                organizationAccountRelationRepository.findByUsername(account2.getUsername())
                    .getFirst().getRole().toString().toLowerCase())
        );

    }

    @Test
    void whenDeleteAccount_withValidIdAndOrganizationWithNoUser_thenDeleteAccountAndDeleteOrganization() {
        RestAssured.given()
            .contentType("application/json")
            .headers(new Headers(this.generateValidAuthorizationHeader(account1.getUsername(), List.of("ROLE_USER"))))
            .delete(URI + "/" + account1.getUsername())
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value());

        assertAll(
            () -> assertTrue(accountRepository.findById(account1.getUsername()).isEmpty()),
            () -> assertTrue(organizationRepository.findById(organization1.getId()).isEmpty())
        );
    }

    @Test
    void whenDeleteAccount_withPublicRecipe_thenChangeAuthorAndDeleteAccount() {
        RecipeValue recipeValue = defaultRecipeValue();
        recipeValue.setAuthor(account1.getUsername());
        Recipe recipe = recipeRepository.saveAndFlush(Recipe.builder()
            .recipeValue(recipeValue)
            .visibility(RecipeVisibility.PUBLIC)
            .build());

        RestAssured.given()
            .contentType("application/json")
            .headers(new Headers(this.generateValidAuthorizationHeader(account1.getUsername(), List.of("ROLE_USER"))))
            .delete(URI + "/" + account1.getUsername())
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value());

        assertAll(
            () -> assertTrue(accountRepository.findById(account1.getUsername()).isEmpty()),
            () -> assertEquals("Deleted User",
                recipeRepository.findById(recipe.getId()).get().getRecipeValue().getAuthor())
        );
    }

    @Test
    void whenDeleteAccount_RecipeWithOrganizationVisibility_thenChangeAuthorAndDeleteAccount() {
        RecipeValue recipeValue = defaultRecipeValue();
        recipeValue.setAuthor(account1.getUsername());
        Recipe recipe = recipeRepository.saveAndFlush(Recipe.builder()
            .recipeValue(recipeValue)
            .visibility(RecipeVisibility.ORGANIZATION)
            .build());

        RestAssured.given()
            .contentType("application/json")
            .headers(new Headers(this.generateValidAuthorizationHeader(account1.getUsername(), List.of("ROLE_USER"))))
            .delete(URI + "/" + account1.getUsername())
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value());

        assertAll(
            () -> assertTrue(accountRepository.findById(account1.getUsername()).isEmpty()),
            () -> assertEquals("Deleted User",
                recipeRepository.findById(recipe.getId()).get().getRecipeValue().getAuthor())
        );
    }

    @Test
    void whenDeleteAccount_withPrivateRecipe_thenDeleteRecipeAndDeleteAccount() {
        RecipeValue recipeValue = defaultRecipeValue();
        recipeValue.setAuthor(account1.getUsername());
        Recipe recipe = recipeRepository.saveAndFlush(Recipe.builder()
            .recipeValue(recipeValue)
            .visibility(RecipeVisibility.PRIVATE)
            .build());

        RestAssured.given()
            .contentType("application/json")
            .headers(new Headers(this.generateValidAuthorizationHeader(account1.getUsername(), List.of("ROLE_USER"))))
            .delete(URI + "/" + account1.getUsername())
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value());

        assertAll(
            () -> assertTrue(accountRepository.findById(account1.getUsername()).isEmpty()),
            () -> assertTrue(recipeRepository.findById(recipe.getId()).isEmpty())
        );
    }

    @Test
    void whenDeleteAccount_withNonExistingUsername_thenNotFound() {
        var errorResponse = RestAssured.given()
            .contentType("application/json")
            .headers(new Headers(this.generateValidAuthorizationHeader("NonExisting", List.of("ROLE_USER"))))
            .delete(URI + "/" + "NonExisting")
            .then()
            .extract()
            .as(org.openapitools.model.ErrorResponse.class);

        ErrorResponseAssert.assertThat(errorResponse)
            .hasStatus(HttpStatus.NOT_FOUND)
            .messageContains("Username 'NonExisting' not found!");
    }
}
