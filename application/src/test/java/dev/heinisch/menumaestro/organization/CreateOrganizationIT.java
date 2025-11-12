package dev.heinisch.menumaestro.organization;

import dev.heinisch.menumaestro.domain.organization.OrganizationAccountRelation;
import dev.heinisch.menumaestro.domain.organization.OrganizationRole;
import dev.heinisch.menumaestro.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.utils.ErrorResponseAssert;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
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
import static org.assertj.core.api.Assertions.assertThat;

public class CreateOrganizationIT extends BaseWebIntegrationTest {

    @Override
    protected String getBasePath() {
        return "/organizations";
    }

    @BeforeEach
    void setup() {
        accountRepository.save(defaultAccount());
    }

    @Test
    void whenCreateOrg_withValidDto_thenCreated() {
        // arrange
        var createDto = defaultCreateOrganizationDto();

        // act
        var summaryDto = createOrganizationSuccessful(createDto);

        // assert
        assertThat(summaryDto.getName()).isEqualTo(createDto.getName());
        assertThat(summaryDto.getDescription()).isEqualTo(createDto.getDescription());
        assertThat(summaryDto.getId()).isNotNull();

        var compositeKey = OrganizationAccountRelation.OrganizationAccountRelationId
                .builder()
                .organizationId(summaryDto.getId())
                .accountId(DEFAULT_USERNAME)
                .build();
        assertThat(organizationAccountRelationRepository.findById(compositeKey))
                .isNotEmpty()
                .get()
                .extracting(OrganizationAccountRelation::getRole)
                .isEqualTo(OrganizationRole.OWNER);

        // cleanup
        organizationAccountRelationRepository.deleteById(compositeKey);
    }

    @Test
    void whenCreateOrg_withoutValidJWT_thenUnauthorized() {
        var createDto = defaultCreateOrganizationDto();
        var errorResponse = createOrganizationFails(
                createDto,
                HttpStatus.UNAUTHORIZED,
                new Headers(List.of(new Header("Authorization", "Bearer LOL")))
        );
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.UNAUTHORIZED)
                .messageContains("Invalid JWT token format or claims extraction failed");
    }

    @Test
    void whenCreateOrg_withoutJWT_thenForbidden() {
        var createDto = defaultCreateOrganizationDto();
        var errorResponse = createOrganizationFails(createDto, HttpStatus.FORBIDDEN, new Headers(List.of()));
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("Access Denied");
    }

    @Test
    void whenCreateOrg_withBlankOrgName_thenUnprocessableEntity() {
        var createDto = defaultCreateOrganizationDto()
                .name("");
        ErrorResponseAssert.assertThat(createOrganizationFails(createDto, HttpStatus.UNPROCESSABLE_ENTITY))
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageEquals("Validation error occurred!")
                .detailsContainSubstring("organization name cannot be blank");
    }

    @Test
    void whenCreateOrg_withTooLongOrgName_thenUnprocessableEntity() {
        var createDto = defaultCreateOrganizationDto()
                .name("foobar".repeat(30));
        ErrorResponseAssert.assertThat(createOrganizationFails(createDto, HttpStatus.UNPROCESSABLE_ENTITY))
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageEquals("Validation error occurred!")
                .detailsContainSubstring("organization name is too long");
    }

    @Test
    void whenCreateOrg_withTooLongDescription_thenUnprocessableEntity() {
        var createDto = defaultCreateOrganizationDto()
                .description("foobar".repeat(850));
        ErrorResponseAssert.assertThat(createOrganizationFails(createDto, HttpStatus.UNPROCESSABLE_ENTITY))
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageEquals("Validation error occurred!")
                .detailsContainSubstring("organization description is too long");
    }


    @Test
    void whenCreateOrg_withBlankDescription_thenCreated() { // is optional
        // arrange
        var createDto = defaultCreateOrganizationDto()
                .description("");
        // act
        var summaryDto = createOrganizationSuccessful(createDto);

        // assert
        assertThat(summaryDto.getName()).isEqualTo(createDto.getName());
        assertThat(summaryDto.getDescription()).isEqualTo(createDto.getDescription());
        assertThat(summaryDto.getId()).isNotNull();

        var compositeKey = OrganizationAccountRelation.OrganizationAccountRelationId
                .builder()
                .organizationId(summaryDto.getId())
                .accountId(DEFAULT_USERNAME)
                .build();
        assertThat(organizationAccountRelationRepository.findById(compositeKey))
                .isNotEmpty()
                .get()
                .extracting(OrganizationAccountRelation::getRole)
                .isEqualTo(OrganizationRole.OWNER);

        // cleanup
        organizationAccountRelationRepository.deleteById(compositeKey);
    }

    @Test
    void whenCreateOrg_withAlreadyExistingOrg_thenConflict() {
        // arrange
        var createDto = defaultCreateOrganizationDto();
        var alreadyExistingOrg = createOrganizationSuccessful(createDto);

        // act
        var errorResponse = createOrganizationFails(createDto, HttpStatus.CONFLICT);

        // assert
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.CONFLICT)
                .messageContains("already exists");

        // cleanup
        var compositeKey = OrganizationAccountRelation.OrganizationAccountRelationId
                .builder()
                .organizationId(alreadyExistingOrg.getId())
                .accountId(DEFAULT_USERNAME)
                .build();
        organizationAccountRelationRepository.deleteById(compositeKey);
    }

    private OrganizationSummaryDto createOrganizationSuccessful(OrganizationCreateDto dto) {
        return createOrganization(dto)
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(OrganizationSummaryDto.class);
    }

    private ErrorResponse createOrganizationFails(OrganizationCreateDto dto, HttpStatus status) {
        return createOrganization(dto)
                .then()
                .statusCode(status.value())
                .extract()
                .as(ErrorResponse.class);
    }

    private ErrorResponse createOrganizationFails(OrganizationCreateDto dto, HttpStatus status, Headers headers) {
        return createOrganization(dto, headers)
                .then()
                .statusCode(status.value())
                .extract()
                .as(ErrorResponse.class);
    }

    private Response createOrganization(OrganizationCreateDto dto) {
        return createOrganization(dto, new Headers(List.of(this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER")))));
    }

    private Response createOrganization(OrganizationCreateDto dto, Headers headers) {
        return RestAssured
                .given()
                .contentType("application/json")
                .headers(headers == null ? new Headers() : headers)
                .body(dto)
                .post(URI);
    }

}
