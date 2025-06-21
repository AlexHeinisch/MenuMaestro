package dev.heinisch.menumaestro.organization;

import dev.heinisch.menumaestro.domain.account.Account;
import dev.heinisch.menumaestro.domain.organization.Organization;
import dev.heinisch.menumaestro.domain.organization.OrganizationAccountRelation;
import dev.heinisch.menumaestro.domain.organization.OrganizationRole;
import dev.heinisch.menumaestro.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.utils.ErrorResponseAssert;
import dev.heinisch.menumaestro.utils.RestHelper;
import io.restassured.http.Method;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.RespondToInvitationRequest;
import org.springframework.http.HttpStatus;

import java.util.List;

import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.defaultAccount;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultOrganizationTestData.defaultOrganization1;
import static org.assertj.core.api.Assertions.assertThat;

public class RespondToInvitationOrganizationIT extends BaseWebIntegrationTest {

    private Organization organization1;
    private Account account1;

    private RestHelper.PathAndBodyWithoutReturnRestHelper<Long, RespondToInvitationRequest> rest;

    @PostConstruct
    private void initRestHelper() {
        rest = new RestHelper.PathAndBodyWithoutReturnRestHelper<>(
                this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER")),
                Method.PUT,
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
        organization1 = organizationRepository.saveAndFlush(defaultOrganization1());
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation
                .builder()
                .role(OrganizationRole.INVITED)
                .account(account1)
                .organization(organization1)
                .build());
    }

    @Test
    void whenRespondToInvitation_withValidDataAccept_thenNoContent() {
        rest.requestSuccessful(organization1.getId(), defaultRespondToInvitationRequest());
        var roles = organizationAccountRelationRepository.findAllByUsername(account1.getUsername());
        assertThat(roles).hasSize(1);
        assertThat(roles.get(0).getRole()).isEqualTo(OrganizationRole.MEMBER);
    }

    @Test
    void whenRespondToInvitation_withValidDataDecline_thenNoContent() {
        rest.requestSuccessful(organization1.getId(), defaultRespondToInvitationRequest().accept(false));
        var roles = organizationAccountRelationRepository.findAllByUsername(account1.getUsername());
        assertThat(roles).hasSize(0);
    }

    @Test
    void whenRespondToInvitation_withAccountNotInvited_thenConflict() {
        organizationAccountRelationRepository.deleteAll();
        var errorResponse = rest.requestFails(organization1.getId(), defaultRespondToInvitationRequest(), HttpStatus.CONFLICT);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.CONFLICT)
                .messageContainsIgnoreCase("has not been invited");
    }

    @Test
    void whenRespondToInvitation_withNonExistentOrganization_thenNotFound() {
        var errorResponse = rest.requestFails(-666L, defaultRespondToInvitationRequest(), HttpStatus.NOT_FOUND);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.NOT_FOUND)
                .messageContainsIgnoreCase("organization")
                .messageContainsIgnoreCase("not found");
    }

    @Test
    void whenRespondToInvitation_withTargetAccountAlreadyInOrganization_thenConflict() {
        organizationAccountRelationRepository.deleteById(OrganizationAccountRelation.OrganizationAccountRelationId
                .builder()
                .organizationId(organization1.getId())
                .accountId(account1.getUsername())
                .build()
        );
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation.builder()
                .role(OrganizationRole.MEMBER)
                .organization(organization1)
                .account(account1)
                .build()
        );
        var errorResponse = rest.requestFails(organization1.getId(), defaultRespondToInvitationRequest(), HttpStatus.CONFLICT);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.CONFLICT)
                .messageContainsIgnoreCase("already member of organization");
    }

    @Test
    void basicAuthTests() {
        rest.basicAuthTests(organization1.getId(), defaultRespondToInvitationRequest());
    }

    private RespondToInvitationRequest defaultRespondToInvitationRequest() {
        return new RespondToInvitationRequest().accept(true);
    }
}
