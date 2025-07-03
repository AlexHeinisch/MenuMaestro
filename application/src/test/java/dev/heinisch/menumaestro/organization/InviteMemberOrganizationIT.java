package dev.heinisch.menumaestro.organization;

import dev.heinisch.menumaestro.domain.account.Account;
import dev.heinisch.menumaestro.domain.organization.Organization;
import dev.heinisch.menumaestro.domain.organization.OrganizationAccountRelation;
import dev.heinisch.menumaestro.domain.organization.OrganizationRole;
import dev.heinisch.menumaestro.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.utils.ErrorResponseAssert;
import dev.heinisch.menumaestro.utils.RestHelper;
import io.restassured.http.Headers;
import io.restassured.http.Method;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.InviteMemberRequest;
import org.springframework.http.HttpStatus;

import java.util.List;

import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME_3;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.defaultAccount;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.defaultAccount2;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.defaultAccount3;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultOrganizationTestData.defaultOrganization1;
import static org.assertj.core.api.Assertions.assertThat;

public class InviteMemberOrganizationIT extends BaseWebIntegrationTest {

    private Organization organization1;
    private Account account1, account2;

    private RestHelper.PathAndBodyWithoutReturnRestHelper<Long, InviteMemberRequest> rest;

    @PostConstruct
    private void initRestHelper() {
        rest = new RestHelper.PathAndBodyWithoutReturnRestHelper<>(
                this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER")),
                Method.POST,
                URI + "/{id}/members",
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
        accountRepository.saveAndFlush(defaultAccount3());
        organization1 = organizationRepository.saveAndFlush(defaultOrganization1());
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation
                .builder()
                .role(OrganizationRole.ADMIN)
                .account(account1)
                .organization(organization1)
                .build());
    }

    @Test
    void whenInviteMember_withValidData_thenNoContent() {
        rest.requestSuccessful(organization1.getId(), defaultInviteMemberRequest());
        var roles = organizationAccountRelationRepository.findAllByUsername(account2.getUsername());
        assertThat(roles).hasSize(1);
        assertThat(roles.get(0).getRole()).isEqualTo(OrganizationRole.INVITED);
    }

    @Test
    void whenInviteMember_withNonExistentOrganization_thenNotFound() {
        var errorResponse = rest.requestFails(-666L, defaultInviteMemberRequest(), HttpStatus.NOT_FOUND);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.NOT_FOUND)
                .messageContainsIgnoreCase("organization")
                .messageContainsIgnoreCase("not found");
    }

    @Test
    void whenInviteMember_withNonExistentAccount_thenNotFound() {
        var errorResponse = rest.requestFails(organization1.getId(), new InviteMemberRequest().username("notauser"), HttpStatus.NOT_FOUND);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.NOT_FOUND)
                .messageContainsIgnoreCase("account")
                .messageContainsIgnoreCase("not find");
    }

    @Test
    void whenInviteMember_withTargetAccountAlreadyInOrganization_thenConflict() {
        organizationAccountRelationRepository.deleteById(OrganizationAccountRelation.OrganizationAccountRelationId
                .builder()
                .organizationId(organization1.getId())
                .accountId(account2.getUsername())
                .build()
        );
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation.builder()
                .role(OrganizationRole.MEMBER)
                .organization(organization1)
                .account(account2)
                .build()
        );
        var errorResponse = rest.requestFails(organization1.getId(), defaultInviteMemberRequest(), HttpStatus.CONFLICT);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.CONFLICT)
                .messageContainsIgnoreCase("already in organization");
    }

    @Test
    void whenInviteMember_withNotInOrganisation_thenForbidden() {
        var errorResponse = rest.requestFails(organization1.getId(), defaultInviteMemberRequest(),
                HttpStatus.FORBIDDEN,
                new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME_3, List.of("ROLE_USER"))));
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("not in the required organization");
    }

    @Test
    void whenInviteMember_withNotEnoughPermissions_thenForbidden() {
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

        var errorResponse = rest.requestFails(organization1.getId(), defaultInviteMemberRequest(), HttpStatus.FORBIDDEN);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("not enough permissions")
                .messageContains("ADMIN");
    }

    @Test
    void whenInviteMember_withNotInOrganisationButAdmin_thenOk() {
        rest.requestSuccessful(
                organization1.getId(),
                defaultInviteMemberRequest(),
                new Headers(generateValidAuthorizationHeader("someuser", List.of("ROLE_ADMIN")))
        );
    }

    @Test
    void basicAuthTests() {
        rest.basicAuthTests(organization1.getId(), defaultInviteMemberRequest());
    }

    private InviteMemberRequest defaultInviteMemberRequest() {
        return new InviteMemberRequest().username(account2.getUsername());
    }
}
