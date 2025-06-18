package dev.heinisch.menumaestro.integration_test.menu;

import dev.heinisch.menumaestro.domain.account.Account;
import dev.heinisch.menumaestro.domain.menu.Menu;
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
import org.springframework.http.HttpStatus;

import java.util.List;

import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME_2;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.defaultAccount;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultMenuTestData.defaultMenu1;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultOrganizationTestData.defaultOrganization1;
import static org.assertj.core.api.Assertions.assertThat;

public class DeleteMenuWebIntegrationTest extends BaseWebIntegrationTest {

    private Organization organization1;
    private Menu menu1;

    private Account account1;

    private RestHelper.PathWithoutReturnRestHelper<Long> rest;

    @PostConstruct
    private void initRestHelper() {
        rest = new RestHelper.PathWithoutReturnRestHelper<>(
                this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER")),
                Method.DELETE,
                URI + "/{id}",
                HttpStatus.NO_CONTENT
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
        menu1 = menuRepository.saveAndFlush(defaultMenu1(organization1.getId()));
    }

    @Test
    void whenDeleteMenu_withValidId_thenNoContent() {
        rest.requestSuccessful(menu1.getId());
        assertThat(menuRepository.findById(menu1.getId())).isEmpty();
    }

    @Test
    void whenDeleteMenu_withNonExistentId_thenNotFound() {
        ErrorResponseAssert.assertThat(rest.requestFails(6666L, HttpStatus.NOT_FOUND))
                .hasStatus(HttpStatus.NOT_FOUND)
                .messageContains("not found");
    }

    @Test
    void whenDeleteMenu_withNotInOrganisation_thenForbidden() {
        var errorResponse = rest.requestFails(menu1.getId(),
                HttpStatus.FORBIDDEN,
                new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME_2, List.of("ROLE_USER"))));
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("not in the required organization");
    }

    @Test
    void whenDeleteMenu_withNotEnoughPermissions_thenForbidden() {
        organizationAccountRelationRepository.deleteAll();
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation.builder()
                .role(OrganizationRole.PLANNER)
                .organization(organization1)
                .account(account1)
                .build()
        );

        var errorResponse = rest.requestFails(menu1.getId(), HttpStatus.FORBIDDEN);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("not enough permissions")
                .messageContains("ADMIN");
    }

    @Test
    void whenDeleteMenu_withNotInOrganisationButAdmin_thenOk() {
        rest.requestSuccessful(
                menu1.getId(),
                new Headers(generateValidAuthorizationHeader("someuser", List.of("ROLE_ADMIN")))
        );
    }

    @Test
    void basicAuthTests() {
        rest.basicAuthTests(1L);
    }

}
