package dev.heinisch.menumaestro.integration_test.organization;

import dev.heinisch.menumaestro.domain.account.Account;
import dev.heinisch.menumaestro.domain.organization.Organization;
import dev.heinisch.menumaestro.domain.organization.OrganizationAccountRelation;
import dev.heinisch.menumaestro.domain.organization.OrganizationRole;
import dev.heinisch.menumaestro.integration_test.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.integration_test.utils.ErrorResponseAssert;
import dev.heinisch.menumaestro.integration_test.utils.RestHelper;
import io.restassured.http.Headers;
import io.restassured.http.Method;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME_3;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.defaultAccount;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.defaultAccount2;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.defaultAccount3;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultOrganizationTestData.defaultOrganization1;
import static org.assertj.core.api.Assertions.assertThat;

public class RemoveMemberOrganizationWebIntegrationTest extends BaseWebIntegrationTest {

    private Organization organization1;
    private Account account1, account2, account3;

    private RestHelper.DualPathWithoutReturnRestHelper<Long, String> rest;

    @PostConstruct
    private void initRestHelper() {
        rest = new RestHelper.DualPathWithoutReturnRestHelper<>(
                this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER")),
                Method.DELETE,
                URI + "/{id}/members/{username}",
                HttpStatus.NO_CONTENT
        );
    }

    @Override
    protected String getBasePath() {
        return "/organizations";
    }

    @BeforeEach
    void setup() {
        account1 = accountRepository.saveAndFlush(defaultAccount());
        account2 = accountRepository.saveAndFlush(defaultAccount2());
        account3 = accountRepository.saveAndFlush(defaultAccount3());
        organization1 = organizationRepository.saveAndFlush(defaultOrganization1());
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation
                .builder()
                .role(OrganizationRole.ADMIN)
                .account(account1)
                .organization(organization1)
                .build());
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation
                .builder()
                .role(OrganizationRole.MEMBER)
                .account(account2)
                .organization(organization1)
                .build());
    }

    @Test
    void whenKickMember_withValidData_thenNoContent() {
        rest.requestSuccessful(organization1.getId(), account2.getUsername());
        var roles = organizationAccountRelationRepository.findAllByUsername(account2.getUsername());
        assertThat(roles).hasSize(0);
    }

    @Test
    void whenKickMember_withNonExistentOrganization_thenNotFound() {
        var errorResponse = rest.requestFails(-666L, account2.getUsername(), HttpStatus.NOT_FOUND);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.NOT_FOUND)
                .messageContainsIgnoreCase("organization")
                .messageContainsIgnoreCase("not found");
    }

    @Test
    void whenKickMember_withNonExistentAccount_thenNotFound() {
        var errorResponse = rest.requestFails(organization1.getId(), "notauser", HttpStatus.NOT_FOUND);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.NOT_FOUND)
                .messageContainsIgnoreCase("account")
                .messageContainsIgnoreCase("not find");
    }

    @Test
    void whenKickMember_withAccountNotInOrganization_thenConflict() {
        var errorResponse = rest.requestFails(organization1.getId(), account3.getUsername(), HttpStatus.CONFLICT);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.CONFLICT)
                .messageContainsIgnoreCase("not part of organization");
    }

    @Test
    void whenKickMember_withKickOwnAccount_thenConflict() {
        var errorResponse = rest.requestFails(organization1.getId(), account1.getUsername(), HttpStatus.CONFLICT);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.CONFLICT)
                .messageContainsIgnoreCase("cannot remove yourself");
    }

    @Test
    void whenKickMember_withTargetAccountIsOwner_thenConflict() {
        organizationAccountRelationRepository.deleteById(OrganizationAccountRelation.OrganizationAccountRelationId
                .builder()
                .organizationId(organization1.getId())
                .accountId(account2.getUsername())
                .build()
        );
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation.builder()
                .role(OrganizationRole.OWNER)
                .organization(organization1)
                .account(account2)
                .build()
        );
        var errorResponse = rest.requestFails(organization1.getId(), account2.getUsername(), HttpStatus.CONFLICT);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.CONFLICT)
                .messageContainsIgnoreCase("Cannot kick the owner");
    }

    @Test
    void whenKickMember_withNotInOrganisation_thenForbidden() {
        var errorResponse = rest.requestFails(organization1.getId(), account2.getUsername(),
                HttpStatus.FORBIDDEN,
                new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME_3, List.of("ROLE_USER"))));
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("not in the required organization");
    }

    @Test
    void whenKickMember_withNotEnoughPermissions_thenForbidden() {
        organizationAccountRelationRepository.deleteById(OrganizationAccountRelation.OrganizationAccountRelationId
                .builder()
                .organizationId(organization1.getId())
                .accountId(account1.getUsername())
                .build()
        );
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation.builder()
                .role(OrganizationRole.PLANNER)
                .organization(organization1)
                .account(account1)
                .build()
        );

        var errorResponse = rest.requestFails(organization1.getId(), account2.getUsername(), HttpStatus.FORBIDDEN);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("not enough permissions")
                .messageContains("ADMIN");
    }

    @Test
    void whenKickMember_withNotInOrganisationButAdmin_thenOk() {
        rest.requestSuccessful(
                organization1.getId(),
                account2.getUsername(),
                new Headers(generateValidAuthorizationHeader("someuser", List.of("ROLE_ADMIN")))
        );
    }

    @Test
    void basicAuthTests() {
        rest.basicAuthTests(organization1.getId(), account2.getUsername());
    }
}
