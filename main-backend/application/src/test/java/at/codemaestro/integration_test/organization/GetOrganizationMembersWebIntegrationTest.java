package at.codemaestro.integration_test.organization;

import at.codemaestro.domain.account.Account;
import at.codemaestro.domain.organization.Organization;
import at.codemaestro.domain.organization.OrganizationAccountRelation;
import at.codemaestro.domain.organization.OrganizationRole;
import at.codemaestro.integration_test.BaseWebIntegrationTest;
import at.codemaestro.integration_test.utils.RestHelper;
import at.codemaestro.integration_test.utils.TestPageableResponse;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.Headers;
import io.restassured.http.Method;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.OrganizationMemberDto;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.stream.Collectors;

import static at.codemaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static at.codemaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME_2;
import static at.codemaestro.integration_test.utils.test_constants.DefaultAccountTestData.defaultAccount;
import static at.codemaestro.integration_test.utils.test_constants.DefaultAccountTestData.defaultAccount2;
import static at.codemaestro.integration_test.utils.test_constants.DefaultOrganizationTestData.defaultOrganization1;
import static org.assertj.core.api.Assertions.assertThat;

public class GetOrganizationMembersWebIntegrationTest extends BaseWebIntegrationTest {

    private Organization organization1;
    private Account account1, account2;

    private RestHelper.QueryWithPathRestHelper<Long> rest;

    @PostConstruct
    private void initRestHelper() {
        rest = new RestHelper.QueryWithPathRestHelper<>(
                this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER")),
                Method.GET,
                URI + "/{id}/members",
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
        account2 = accountRepository.saveAndFlush(defaultAccount2());
        organization1 = organizationRepository.saveAndFlush(defaultOrganization1());
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation
                .builder()
                .role(OrganizationRole.MEMBER)
                .account(account1)
                .organization(organization1)
                .build()
        );
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation
                .builder()
                .role(OrganizationRole.OWNER)
                .account(account2)
                .organization(organization1)
                .build()
        );
    }

    @Test
    void whenGetOrganizations_withValidAuth_thenOK() {
        var result = requestSuccessful(organization1.getId(), "", new Headers(this.generateValidAuthorizationHeader(DEFAULT_USERNAME_2, List.of("ROLE_ADMIN"))));
        assertThat(result.getTotalElements()).isEqualTo(2);
        var usernames = result.getContent().stream().map(OrganizationMemberDto::getUsername).collect(Collectors.toSet());
        assertThat(usernames).contains(account1.getUsername());
        assertThat(usernames).contains(account2.getUsername());
    }

    @Test
    public void whenGetOrganizations_withPageSizeOne_thenOK() {
        var result = requestSuccessful(organization1.getId(), "size=1");

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumberOfElements()).isEqualTo(1);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getFirst()).isEqualTo(true);
        assertThat(result.getLast()).isEqualTo(false);
    }

    @Test
    public void whenGetOrganizations_withPageSizeOne_andPageNumberTwo_thenOK() {
        var result = requestSuccessful(organization1.getId(), "size=1&page=1");

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumberOfElements()).isEqualTo(1);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getFirst()).isEqualTo(false);
        assertThat(result.getLast()).isEqualTo(true);
    }

    @Test
    public void whenGetOrganizations_withPageNumberTwo_thenOK() {
        var result = requestSuccessful(organization1.getId(), "page=1");

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumberOfElements()).isEqualTo(0);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getFirst()).isEqualTo(false);
        assertThat(result.getLast()).isEqualTo(true);
    }

    @Test
    void basicAuthTests() {
        rest.basicAuthTests(organization1.getId(), "");
    }

    private TestPageableResponse<OrganizationMemberDto> requestSuccessful(Long id, String query) {
        return requestSuccessful(id, query, new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER"))));
    }

    private TestPageableResponse<OrganizationMemberDto> requestSuccessful(Long id, String query, Headers headers) {
        return rest.request(id, query, headers)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<TestPageableResponse<OrganizationMemberDto>>() {
                });
    }
}
