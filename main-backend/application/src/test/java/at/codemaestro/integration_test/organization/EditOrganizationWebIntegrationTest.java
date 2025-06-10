package at.codemaestro.integration_test.organization;

import at.codemaestro.domain.account.Account;
import at.codemaestro.domain.organization.OrganizationAccountRelation;
import at.codemaestro.integration_test.BaseWebIntegrationTest;
import at.codemaestro.integration_test.utils.ErrorResponseAssert;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.ErrorResponse;
import org.openapitools.model.OrganizationCreateDto;
import org.openapitools.model.OrganizationEditDto;
import org.openapitools.model.OrganizationSummaryDto;
import org.springframework.http.HttpStatus;

import java.util.List;

import static at.codemaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static at.codemaestro.integration_test.utils.test_constants.DefaultAccountTestData.defaultAccount;
import static at.codemaestro.integration_test.utils.test_constants.DefaultOrganizationTestData.defaultCreateOrganizationDto;

public class EditOrganizationWebIntegrationTest extends BaseWebIntegrationTest {

    @Override
    protected String getBasePath() {
        return "/organizations";
    }


    @BeforeEach
    void setup() {
        accountRepository.save(defaultAccount());
    }

    @AfterEach
    void teardown() {
        accountRepository.deleteById(DEFAULT_USERNAME);
    }

    private Response editOrgById(long id, OrganizationEditDto organizationEditDto) {
        return editOrgById(id, organizationEditDto, new Headers(List.of(this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER")))));
    }


    private Response editOrgById(long id, OrganizationEditDto organizationEditDto, Headers headers) {
        return RestAssured
                .given()
                .contentType("application/json")
                .headers(headers == null ? new Headers() : headers)
                .body(organizationEditDto)
                .put(URI + "/" + id);
    }

    private ErrorResponse editOrgByIdFails(long id, OrganizationEditDto organizationEditDto, HttpStatus status, Headers headers) {
        return editOrgById(id, organizationEditDto, headers)
                .then()
                .statusCode(status.value())
                .extract()
                .as(ErrorResponse.class);
    }

    private ErrorResponse editOrgByIdFails(long id, OrganizationEditDto organizationEditDto, HttpStatus status) {
        return editOrgById(id, organizationEditDto)
                .then()
                .statusCode(status.value())
                .extract()
                .as(ErrorResponse.class);
    }

    private OrganizationSummaryDto editOrgByIdSuccessful(long id, OrganizationEditDto organizationEditDto) {
        return editOrgById(id, organizationEditDto)
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

    private OrganizationSummaryDto getOrgByIdSuccessful(long id) {
        return getOrgById(id)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(OrganizationSummaryDto.class);
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


    @Test
    void testOrgGetById() {

        var createDto = defaultCreateOrganizationDto()
                .description("");
        OrganizationSummaryDto savedOrg = createOrganizationSuccessful(createDto);
        OrganizationEditDto organizationEditDto = new OrganizationEditDto();
        organizationEditDto.description("New description");
        organizationEditDto.name("New name");
        OrganizationSummaryDto editResponse = editOrgByIdSuccessful(savedOrg.getId(), organizationEditDto);
        OrganizationSummaryDto getResponse = getOrgByIdSuccessful(savedOrg.getId());

        Assertions.assertAll(
                () -> Assertions.assertEquals(editResponse.getId(), savedOrg.getId()),
                () -> Assertions.assertEquals(editResponse.getName(), "New name"),
                () -> Assertions.assertEquals(editResponse.getDescription(), "New description"),
                () -> Assertions.assertEquals(editResponse.getId(), getResponse.getId()),
                () -> Assertions.assertEquals(editResponse.getName(), getResponse.getName()),
                () -> Assertions.assertEquals(editResponse.getDescription(), getResponse.getDescription())

        );

        var compositeKey = OrganizationAccountRelation.OrganizationAccountRelationId
                .builder()
                .organizationId(savedOrg.getId())
                .accountId(DEFAULT_USERNAME)
                .build();
        organizationAccountRelationRepository.deleteById(compositeKey);
    }

    @Test
    void testUserNotMemberOfOrganizationGetById() {
        accountRepository.save(Account.builder()
                .username("test")
                .email("test@test.com")
                .firstName("test")
                .lastName("test")
                .isGlobalAdmin(false)
                .passwordHash("")
                .build());
        var createDto = defaultCreateOrganizationDto()
                .description("");
        OrganizationSummaryDto savedOrg = createOrganizationSuccessful(createDto);
        OrganizationEditDto organizationEditDto = new OrganizationEditDto();
        organizationEditDto.description("New description");
        organizationEditDto.name("New name");

        ErrorResponse errorResponse = editOrgByIdFails(savedOrg.getId(), organizationEditDto, HttpStatus.FORBIDDEN,
                new Headers(List.of(this.generateValidAuthorizationHeader("test", List.of("ROLE_USER")))));
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("not in the required organization");
        var compositeKey = OrganizationAccountRelation.OrganizationAccountRelationId
                .builder()
                .organizationId(savedOrg.getId())
                .accountId(DEFAULT_USERNAME)
                .build();
        organizationAccountRelationRepository.deleteById(compositeKey);

    }

    @Test
    void testInvalidHeaderOrganizationById() {
        var createDto = defaultCreateOrganizationDto()
                .description("");
        OrganizationSummaryDto savedOrg = createOrganizationSuccessful(createDto);
        OrganizationEditDto organizationEditDto = new OrganizationEditDto();
        organizationEditDto.description("New description");
        organizationEditDto.name("New name");

        ErrorResponse errorResponse = editOrgByIdFails(savedOrg.getId(), organizationEditDto, HttpStatus.UNAUTHORIZED, new Headers(List.of(new Header("Authorization", "Bearer LOL"))));
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNAUTHORIZED)
                .messageContains("Invalid JWT token format or claims extraction failed");
        var compositeKey = OrganizationAccountRelation.OrganizationAccountRelationId
                .builder()
                .organizationId(savedOrg.getId())
                .accountId(DEFAULT_USERNAME)
                .build();
        organizationAccountRelationRepository.deleteById(compositeKey);

    }


}
