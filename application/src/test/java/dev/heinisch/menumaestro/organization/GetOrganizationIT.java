package dev.heinisch.menumaestro.organization;

import dev.heinisch.menumaestro.domain.account.Account;
import dev.heinisch.menumaestro.domain.organization.OrganizationAccountRelation;
import dev.heinisch.menumaestro.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.utils.ErrorResponseAssert;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.ErrorResponse;
import org.openapitools.model.OrganizationCreateDto;
import org.openapitools.model.OrganizationSummaryDto;
import org.springframework.http.HttpStatus;

import java.util.List;

import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.defaultAccount;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultOrganizationTestData.defaultCreateOrganizationDto;

public class GetOrganizationIT extends BaseWebIntegrationTest {

    @Override
    protected String getBasePath() {
        return "/organizations";
    }

    @BeforeEach
    void setup() {
        accountRepository.saveAndFlush(defaultAccount());
    }

    private ErrorResponse getAllOrgsFails(HttpStatus status) {
        return getAllOrgs()
                .then()
                .statusCode(status.value())
                .extract()
                .as(ErrorResponse.class);
    }

    private ErrorResponse getAllOrgsFails(HttpStatus status, Headers headers) {
        return getAllOrgs(headers)
                .then()
                .statusCode(status.value())
                .extract()
                .as(ErrorResponse.class);
    }

    private Response getAllOrgs() {
        return getAllOrgs(new Headers(List.of(this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_ADMIN", "ROLE_USER")))));
    }

    private Response getAllOrgs(Headers headers) {
        return RestAssured
                .given()
                .contentType("application/json")
                .headers(headers == null ? new Headers() : headers)
                .get(URI);
    }

    private Response getOrgById(long id) {
        return getOrgById(id, new Headers(List.of(this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER")))));
    }

    private Response getOrgById(long id, Headers headers) {
        return RestAssured
                .given()
                .contentType("application/json")
                .headers(headers == null ? new Headers() : headers)
                .get(URI + "/" + id);
    }

    private ErrorResponse getOrgByIdFails(long id, HttpStatus status, Headers headers) {
        return getOrgById(id, headers)
                .then()
                .statusCode(status.value())
                .extract()
                .as(ErrorResponse.class);
    }

    private ErrorResponse getOrgByIdFails(long id, HttpStatus status) {
        return getOrgById(id)
                .then()
                .statusCode(status.value())
                .extract()
                .as(ErrorResponse.class);
    }

    private OrganizationSummaryDto getOrgByIdSuccessful(long id) {
        return getOrgById(id)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(OrganizationSummaryDto.class);
    }

    private OrganizationSummaryDto createOrganizationSuccessful(OrganizationCreateDto dto) {
        return createOrganization(dto)
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(OrganizationSummaryDto.class);
    }

    private Response createOrganization(OrganizationCreateDto dto) {
        return createOrganization(dto, new Headers(List.of(this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_ADMIN", "ROLE_USER")))));
    }

    private Response createOrganization(OrganizationCreateDto dto, Headers headers) {
        return RestAssured
                .given()
                .contentType("application/json")
                .headers(headers == null ? new Headers() : headers)
                .body(dto)
                .post(URI);
    }

    @Test
    void testOrgGetById() {

        var createDto = defaultCreateOrganizationDto()
                .description("");
        OrganizationSummaryDto saveAndFlushdOrg = createOrganizationSuccessful(createDto);
        OrganizationSummaryDto response = getOrgByIdSuccessful(saveAndFlushdOrg.getId());

        Assertions.assertAll(
                () -> Assertions.assertEquals(response.getId(), saveAndFlushdOrg.getId()),
                () -> Assertions.assertEquals(response.getName(), saveAndFlushdOrg.getName()),
                () -> Assertions.assertEquals(response.getDescription(), saveAndFlushdOrg.getDescription())
        );

        var compositeKey = OrganizationAccountRelation.OrganizationAccountRelationId
                .builder()
                .organizationId(saveAndFlushdOrg.getId())
                .accountId(DEFAULT_USERNAME)
                .build();
        organizationAccountRelationRepository.deleteById(compositeKey);
    }

    @Test
    void testUserNotMemberOfOrganizationGetById() {
        accountRepository.saveAndFlush(Account.builder()
                .username("test")
                .email("test@test.com")
                .firstName("test")
                .lastName("test")
                .isGlobalAdmin(false)
                .passwordHash("")
                .build());
        var createDto = defaultCreateOrganizationDto()
                .description("");
        OrganizationSummaryDto saveAndFlushdOrg = createOrganizationSuccessful(createDto);

        ErrorResponse errorResponse = getOrgByIdFails(saveAndFlushdOrg.getId(), HttpStatus.FORBIDDEN,
                new Headers(List.of(this.generateValidAuthorizationHeader("test", List.of("ROLE_USER")))));
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("not in the required organization");
        var compositeKey = OrganizationAccountRelation.OrganizationAccountRelationId
                .builder()
                .organizationId(saveAndFlushdOrg.getId())
                .accountId(DEFAULT_USERNAME)
                .build();
        organizationAccountRelationRepository.deleteById(compositeKey);

    }

    @Test
    void testInvalidHeaderOrganizationById() {
        var createDto = defaultCreateOrganizationDto()
                .description("");
        OrganizationSummaryDto saveAndFlushdOrg = createOrganizationSuccessful(createDto);

        ErrorResponse errorResponse = getOrgByIdFails(saveAndFlushdOrg.getId(), HttpStatus.UNAUTHORIZED, new Headers(List.of(new Header("Authorization", "Bearer LOL"))));
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNAUTHORIZED)
                .messageContains("Invalid JWT token format or claims extraction failed");
        var compositeKey = OrganizationAccountRelation.OrganizationAccountRelationId
                .builder()
                .organizationId(saveAndFlushdOrg.getId())
                .accountId(DEFAULT_USERNAME)
                .build();
        organizationAccountRelationRepository.deleteById(compositeKey);

    }

    @Test
    void testMissingOrganizationOrganizationById() {
        var createDto = defaultCreateOrganizationDto()
                .description("");
        OrganizationSummaryDto dto = createOrganizationSuccessful(createDto);
        ErrorResponse errorResponse = getOrgByIdFails(dto.getId(), HttpStatus.FORBIDDEN, new Headers(List.of(this.generateValidAuthorizationHeader("test", List.of("ROLE_USER")))));
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("not in the required organization");
    }


}
