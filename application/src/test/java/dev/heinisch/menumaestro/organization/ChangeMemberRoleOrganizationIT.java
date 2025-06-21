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
import org.openapitools.model.ChangeMemberRoleRequest;
import org.openapitools.model.OrganizationRoleEnum;
import org.springframework.http.HttpStatus;

import java.util.List;

import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME_3;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.defaultAccount;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.defaultAccount2;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.defaultAccount3;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultOrganizationTestData.defaultOrganization1;
import static org.assertj.core.api.Assertions.assertThat;

public class ChangeMemberRoleOrganizationIT extends BaseWebIntegrationTest {

    private Organization organization1;
    private Account account1, account2, account3;

    private RestHelper.DualPathAndBodyWithoutReturnRestHelper<Long, String, ChangeMemberRoleRequest> rest;

    @PostConstruct
    private void initRestHelper() {
        rest = new RestHelper.DualPathAndBodyWithoutReturnRestHelper<>(
                this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER")),
                Method.PUT,
                URI + "/{id}/members/{username}/role",
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
    void whenChangeMemberRole_withValidData_thenNoContent() {
        rest.requestSuccessful(organization1.getId(), account2.getUsername(), new ChangeMemberRoleRequest().role(OrganizationRoleEnum.SHOPPER));
        var roles = organizationAccountRelationRepository.findAllByUsername(account2.getUsername());
        assertThat(roles).hasSize(1);
        assertThat(roles.get(0).getRole()).isEqualTo(OrganizationRole.SHOPPER);
    }

    @Test
    void whenChangeMemberRole_toInvited_thenUnprocessableEntity() {
        var errorResponse = rest.requestFails(-666L, account2.getUsername(), new ChangeMemberRoleRequest().role(OrganizationRoleEnum.INVITED), HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContainsIgnoreCase("Validation error")
                .detailsContainSubstring("blacklist")
                .detailsHaveSize(1);
    }

    @Test
    void whenChangeMemberRole_toOwner_thenUnprocessableEntity() {
        var errorResponse = rest.requestFails(-666L, account2.getUsername(), new ChangeMemberRoleRequest().role(OrganizationRoleEnum.OWNER), HttpStatus.UNPROCESSABLE_ENTITY);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContainsIgnoreCase("Validation error")
                .detailsContainSubstring("blacklist")
                .detailsHaveSize(1);
    }

    @Test
    void whenChangeMemberRole_withNonExistentOrganization_thenNotFound() {
        var errorResponse = rest.requestFails(-666L, account2.getUsername(), new ChangeMemberRoleRequest().role(OrganizationRoleEnum.SHOPPER), HttpStatus.NOT_FOUND);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.NOT_FOUND)
                .messageContainsIgnoreCase("organization")
                .messageContainsIgnoreCase("not found");
    }

    @Test
    void whenChangeMemberRole_withNonExistentAccount_thenNotFound() {
        var errorResponse = rest.requestFails(organization1.getId(), "notauser", new ChangeMemberRoleRequest().role(OrganizationRoleEnum.SHOPPER), HttpStatus.NOT_FOUND);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.NOT_FOUND)
                .messageContainsIgnoreCase("account")
                .messageContainsIgnoreCase("not find");
    }

    @Test
    void whenChangeMemberRole_withAccountNotInOrganization_thenConflict() {
        var errorResponse = rest.requestFails(organization1.getId(), account3.getUsername(), new ChangeMemberRoleRequest().role(OrganizationRoleEnum.SHOPPER), HttpStatus.CONFLICT);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.CONFLICT)
                .messageContainsIgnoreCase("not part of organization");
    }

    @Test
    void whenChangeMemberRole_withChangeOwnAccountRole_thenConflict() {
        var errorResponse = rest.requestFails(organization1.getId(), account1.getUsername(), new ChangeMemberRoleRequest().role(OrganizationRoleEnum.SHOPPER), HttpStatus.CONFLICT);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.CONFLICT)
                .messageContainsIgnoreCase("cannot change your own role");
    }

    @Test
    void whenChangeMemberRole_withTargetAccountIsOwner_thenConflict() {
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
        var errorResponse = rest.requestFails(organization1.getId(), account2.getUsername(), new ChangeMemberRoleRequest().role(OrganizationRoleEnum.SHOPPER), HttpStatus.CONFLICT);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.CONFLICT)
                .messageContainsIgnoreCase("Cannot change the role of owner");
    }

    @Test
    void whenChangeMemberRole_withNotInOrganisation_thenForbidden() {
        var errorResponse = rest.requestFails(organization1.getId(), account2.getUsername(), new ChangeMemberRoleRequest().role(OrganizationRoleEnum.SHOPPER),
                HttpStatus.FORBIDDEN,
                new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME_3, List.of("ROLE_USER"))));
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("not in the required organization");
    }

    @Test
    void whenChangeMemberRole_withNotEnoughPermissions_thenForbidden() {
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

        var errorResponse = rest.requestFails(organization1.getId(), account2.getUsername(), new ChangeMemberRoleRequest().role(OrganizationRoleEnum.SHOPPER), HttpStatus.FORBIDDEN);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("not enough permissions")
                .messageContains("ADMIN");
    }

    @Test
    void whenChangeMemberRole_withNotInOrganisationButAdmin_thenOk() {
        rest.requestSuccessful(
                organization1.getId(),
                account2.getUsername(),
                new ChangeMemberRoleRequest().role(OrganizationRoleEnum.SHOPPER),
                new Headers(generateValidAuthorizationHeader("someuser", List.of("ROLE_ADMIN")))
        );
    }

    @Test
    void basicAuthTests() {
        rest.basicAuthTests(organization1.getId(), account2.getUsername(), new ChangeMemberRoleRequest().role(OrganizationRoleEnum.SHOPPER));
    }
}
