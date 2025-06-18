package dev.heinisch.menumaestro.integration_test.menu;

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
import org.openapitools.model.MenuCreateDto;
import org.openapitools.model.MenuSummaryDto;
import org.springframework.http.HttpStatus;

import java.util.List;

import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME_2;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.defaultAccount;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultMenuTestData.DEFAULT_MENU_NAME_1;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultMenuTestData.defaultMenuCreateDto1;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultOrganizationTestData.defaultOrganization1;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateMenuWebIntegrationTest extends BaseWebIntegrationTest {


    private Organization organization1;
    private Account account1;

    private RestHelper.BodyRestHelper<MenuSummaryDto, MenuCreateDto> rest;

    @PostConstruct
    private void initRestHelper() {
        rest = new RestHelper.BodyRestHelper<>(
                MenuSummaryDto.class,
                this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER")),
                Method.POST,
                URI,
                HttpStatus.CREATED
        );
    }

    @Override
    protected String getBasePath() {
        return "/menus";
    }

    @BeforeEach
    public void setup() {
        organization1 = organizationRepository.saveAndFlush(defaultOrganization1());
        account1 = accountRepository.saveAndFlush(defaultAccount());
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation.builder()
                .role(OrganizationRole.OWNER)
                .organization(organization1)
                .account(account1)
                .build());
    }


    @Test
    void whenCreateMenu_withValidData_thenCreated() {
        var result = rest.requestSuccessful(defaultMenuCreateDto1().organizationId(organization1.getId()));
        assertThat(result.getName()).isEqualTo(DEFAULT_MENU_NAME_1);
        assertThat(result.getOrganization().getId()).isEqualTo(organization1.getId());
        assertThat(result.getOrganization().getName()).isEqualTo(organization1.getName());
        assertThat(result.getOrganization().getDescription()).isEqualTo(organization1.getDescription());
    }

    @Test
    void whenCreateMenu_withBlankName_thenUnprocessableEntity() {
        ErrorResponseAssert.assertThat(rest.requestFails(defaultMenuCreateDto1().organizationId(organization1.getId()).name(""), HttpStatus.UNPROCESSABLE_ENTITY))
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error")
                .detailsContainSubstring("blank")
                .detailsHaveSize(1);
    }

    @Test
    void whenCreateMenu_withTooLongName_thenUnprocessableEntity() {
        ErrorResponseAssert.assertThat(rest.requestFails(defaultMenuCreateDto1().organizationId(organization1.getId()).name("FOOOOOBBBBBAAAAARRR".repeat(100)), HttpStatus.UNPROCESSABLE_ENTITY))
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error")
                .detailsContainSubstring("too long")
                .detailsHaveSize(1);
    }

    @Test
    void whenCreateMenu_withTooLongDescription_thenUnprocessableEntity() {
        ErrorResponseAssert.assertThat(rest.requestFails(defaultMenuCreateDto1().organizationId(organization1.getId()).description("FOOOOOBBBBBAAAAARRR".repeat(100)), HttpStatus.UNPROCESSABLE_ENTITY))
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error")
                .detailsContainSubstring("too long")
                .detailsHaveSize(1);
    }

    @Test
    void whenCreateMenu_withNotInOrganisation_thenForbidden() {
        var errorResponse = rest.requestFails(
                defaultMenuCreateDto1().organizationId(organization1.getId()),
                HttpStatus.FORBIDDEN,
                new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME_2, List.of("ROLE_USER"))));
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("not in the required organization");
    }

    @Test
    void whenCreateMenu_withNotEnoughPermissions_thenForbidden() {
        organizationAccountRelationRepository.deleteAll();
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation.builder()
                .role(OrganizationRole.SHOPPER)
                .organization(organization1)
                .account(account1)
                .build()
        );

        var errorResponse = rest.requestFails(defaultMenuCreateDto1().organizationId(organization1.getId()), HttpStatus.FORBIDDEN);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("not enough permissions")
                .messageContains("PLANNER");
    }

    @Test
    void whenCreateMenu_withNotInOrganisationButAdmin_thenOk() {
        rest.requestSuccessful(
                defaultMenuCreateDto1().organizationId(organization1.getId()),
                new Headers(generateValidAuthorizationHeader("someuser", List.of("ROLE_ADMIN")))
        );
    }

    @Test
    void basicAuthTests() {
        rest.basicAuthTests(defaultMenuCreateDto1().organizationId(20L));
    }

}
