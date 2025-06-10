package at.codemaestro.integration_test.organization;

import at.codemaestro.integration_test.BaseWebIntegrationTest;
import at.codemaestro.integration_test.utils.RestHelper;
import at.codemaestro.integration_test.utils.TestPageableResponse;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.Headers;
import io.restassured.http.Method;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.OrganizationSummaryDto;
import org.springframework.http.HttpStatus;

import java.util.List;

import static at.codemaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static at.codemaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME_2;
import static at.codemaestro.integration_test.utils.test_constants.DefaultAccountTestData.defaultAccount;
import static at.codemaestro.integration_test.utils.test_constants.DefaultAccountTestData.defaultAccount2;
import static at.codemaestro.integration_test.utils.test_constants.DefaultAccountTestData.defaultAccount3;
import static at.codemaestro.integration_test.utils.test_constants.DefaultOrganizationTestData.defaultCreateOrganizationDto;
import static at.codemaestro.integration_test.utils.test_constants.DefaultOrganizationTestData.defaultCreateOrganizationDto2;
import static at.codemaestro.integration_test.utils.test_constants.DefaultOrganizationTestData.defaultCreateOrganizationDto3;
import static org.assertj.core.api.Assertions.assertThat;

public class GetOrganizationsWebIntegrationTest extends BaseWebIntegrationTest {

    private OrganizationSummaryDto organization1, organization2, organization3;

    private RestHelper.QueryRestHelper rest;

    @PostConstruct
    private void initRestHelper() {
        rest = new RestHelper.QueryRestHelper(
                this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER")),
                Method.GET,
                URI,
                HttpStatus.OK
        );
    }

    @Override
    protected String getBasePath() {
        return "/organizations";
    }

    @BeforeEach
    void setup() {
        accountRepository.saveAndFlush(defaultAccount());
        accountRepository.saveAndFlush(defaultAccount2());
        accountRepository.saveAndFlush(defaultAccount3());
        organization1 = organizationService.createOrganization(defaultCreateOrganizationDto(), DEFAULT_USERNAME);
        organization2 = organizationService.createOrganization(defaultCreateOrganizationDto2(), DEFAULT_USERNAME_2);
        organization3 = organizationService.createOrganization(defaultCreateOrganizationDto3(), DEFAULT_USERNAME);
    }

    @Test
    void whenGetOrganizations_withNormalUser_thenOK() {
        var result = requestSuccessful("");
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).contains(organization1);
        assertThat(result.getContent()).contains(organization3);
    }

    @Test
    void whenGetOrganizations_withNormalUser_thenOKK() {
        var result = requestSuccessful("page=0&size=10");
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).contains(organization1);
        assertThat(result.getContent()).contains(organization3);
    }

    @Test
    void whenGetOrganizations_withAdminUser_thenOK() {
        var result = requestSuccessful("", new Headers(this.generateValidAuthorizationHeader(DEFAULT_USERNAME_2, List.of("ROLE_ADMIN"))));
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).contains(organization1);
        assertThat(result.getContent()).contains(organization2);
        assertThat(result.getContent()).contains(organization3);
    }

    @Test
    void whenGetOrganizations_withQueryForName_thenOK() {
        var result = requestSuccessful("name=alph");
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).contains(organization1);
    }

    @Test
    public void whenGetOrganizations_withPageSizeOne_thenOK() {
        var result = requestSuccessful("size=1");

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumberOfElements()).isEqualTo(1);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getFirst()).isEqualTo(true);
        assertThat(result.getLast()).isEqualTo(false);
    }

    @Test
    public void whenGetOrganizations_withPageSizeOne_andPageNumberTwo_thenOK() {
        var result = requestSuccessful("size=1&page=1");

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumberOfElements()).isEqualTo(1);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getFirst()).isEqualTo(false);
        assertThat(result.getLast()).isEqualTo(true);
    }

    @Test
    public void whenGetOrganizations_withPageNumberTwo_thenOK() {
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
