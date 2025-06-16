package dev.heinisch.menumaestro.integration_test.organization;

import dev.heinisch.menumaestro.domain.account.Account;
import dev.heinisch.menumaestro.domain.organization.OrganizationAccountRelation;
import dev.heinisch.menumaestro.domain.organization.OrganizationRole;
import dev.heinisch.menumaestro.integration_test.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.integration_test.utils.RestHelper;
import dev.heinisch.menumaestro.integration_test.utils.TestPageableResponse;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.Headers;
import io.restassured.http.Method;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.OrganizationSummaryDto;
import org.springframework.http.HttpStatus;

import java.util.List;

import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.defaultAccount;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.defaultAccount2;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.defaultAccount3;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultOrganizationTestData.defaultOrganization1;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultOrganizationTestData.defaultOrganization2;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultOrganizationTestData.defaultOrganization3;
import static org.assertj.core.api.Assertions.assertThat;

public class GetInvitationsOrganizationWebIntegrationTest extends BaseWebIntegrationTest {

    private OrganizationSummaryDto organization1, organization2;
    private Account account1;

    private RestHelper.QueryRestHelper rest;

    @PostConstruct
    private void initRestHelper() {
        rest = new RestHelper.QueryRestHelper(
                this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER")),
                Method.GET,
                URI + "/invitations",
                HttpStatus.OK
        );
    }

    @Override
    protected String getBasePath() {
        return "/organizations";
    }

    @BeforeEach
    void setup() {
        account1 = accountRepository.saveAndFlush(defaultAccount());
        accountRepository.saveAndFlush(defaultAccount2());
        accountRepository.saveAndFlush(defaultAccount3());
        var org1 = organizationRepository.saveAndFlush(defaultOrganization1());
        var org2 = organizationRepository.saveAndFlush(defaultOrganization2());
        var org3 = organizationRepository.saveAndFlush(defaultOrganization3());
        organization1 = organizationService.getOrganizationById(org1.getId());
        organization2 = organizationService.getOrganizationById(org2.getId());
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation
                .builder()
                .role(OrganizationRole.INVITED)
                .account(account1)
                .organization(org1)
                .build());
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation
                .builder()
                .role(OrganizationRole.INVITED)
                .account(account1)
                .organization(org2)
                .build());
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation
                .builder()
                .role(OrganizationRole.MEMBER)
                .account(account1)
                .organization(org3)
                .build());
    }

    @Test
    void whenGetInvitations_withValidData_thenOK() {
        var response = requestSuccessful("");
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getContent()).contains(organization1);
        assertThat(response.getContent()).contains(organization2);
    }

    @Test
    void whenGetInvitations_withUserWithoutInvitations_thenOK() {
        var response = requestSuccessful("", new Headers(this.generateValidAuthorizationHeader("someuser", List.of("ROLE_USER"))));
        assertThat(response.getTotalElements()).isEqualTo(0);
    }

    @Test
    public void whenGetInvitations_withPageSizeOne_thenOK() {
        var result = requestSuccessful("size=1");

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumberOfElements()).isEqualTo(1);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getFirst()).isEqualTo(true);
        assertThat(result.getLast()).isEqualTo(false);
    }

    @Test
    public void whenGetInvitations_withPageSizeOne_andPageNumberTwo_thenOK() {
        var result = requestSuccessful("size=1&page=1");

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumberOfElements()).isEqualTo(1);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getFirst()).isEqualTo(false);
        assertThat(result.getLast()).isEqualTo(true);
    }

    @Test
    public void whenGetInvitations_withPageNumberTwo_thenOK() {
        var result = requestSuccessful("page=1");

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumberOfElements()).isEqualTo(0);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getFirst()).isEqualTo(false);
        assertThat(result.getLast()).isEqualTo(true);
    }

    @Test
    void basicAuthTests() {
        rest.basicAuthTests("");
    }

    private TestPageableResponse<OrganizationSummaryDto> requestSuccessful(String query) {
        return requestSuccessful(query, new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER"))));
    }

    private TestPageableResponse<OrganizationSummaryDto> requestSuccessful(String query, Headers headers) {
        return rest.request(query, headers)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<TestPageableResponse<OrganizationSummaryDto>>() {
                });
    }
}
