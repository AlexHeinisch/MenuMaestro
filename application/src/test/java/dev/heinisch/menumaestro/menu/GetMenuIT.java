package dev.heinisch.menumaestro.menu;

import dev.heinisch.menumaestro.domain.account.Account;
import dev.heinisch.menumaestro.domain.menu.Menu;
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
import org.openapitools.model.MenuDetailDto;
import org.springframework.http.HttpStatus;

import java.util.List;

import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME_2;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.defaultAccount;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultMenuTestData.DEFAULT_MENU_DESCRIPTION_1;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultMenuTestData.DEFAULT_MENU_NAME_1;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultMenuTestData.DEFAULT_MENU_NUMBER_OF_PEOPLE_1;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultMenuTestData.defaultMenu1;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultOrganizationTestData.DEFAULT_ORG_DESCRIPTION;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultOrganizationTestData.DEFAULT_ORG_NAME;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultOrganizationTestData.defaultOrganization1;
import static org.assertj.core.api.Assertions.assertThat;

public class GetMenuIT extends BaseWebIntegrationTest {


    private Organization organization1;
    private Menu menu1;

    private Account account1;

    private RestHelper.PathRestHelper<MenuDetailDto, Long> rest;

    @PostConstruct
    private void initRestHelper() {
        rest = new RestHelper.PathRestHelper<>(
                MenuDetailDto.class,
                this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER")),
                Method.GET,
                URI + "/{id}",
                HttpStatus.OK
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
    void whenGetMenu_withValidId_thenOk() {
        var menu = rest.requestSuccessful(menu1.getId());
        assertThat(menu.getName()).isEqualTo(DEFAULT_MENU_NAME_1);
        assertThat(menu.getDescription()).isEqualTo(DEFAULT_MENU_DESCRIPTION_1);
        assertThat(menu.getId()).isEqualTo(menu1.getId());
        assertThat(menu.getNumberOfPeople()).isEqualTo(DEFAULT_MENU_NUMBER_OF_PEOPLE_1);
        assertThat(menu.getOrganization().getId()).isEqualTo(organization1.getId());
        assertThat(menu.getOrganization().getName()).isEqualTo(DEFAULT_ORG_NAME);
        assertThat(menu.getOrganization().getDescription()).isEqualTo(DEFAULT_ORG_DESCRIPTION);
    }

    @Test
    void whenGetMenu_withNonExistentId_thenNotFound() {
        ErrorResponseAssert.assertThat(rest.requestFails(6666L, HttpStatus.NOT_FOUND))
                .hasStatus(HttpStatus.NOT_FOUND)
                .messageContains("not found");
    }

    @Test
    void whenGetMenu_withNotInOrganisation_thenForbidden() {
        var errorResponse = rest.requestFails(menu1.getId(),
                HttpStatus.FORBIDDEN,
                new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME_2, List.of("ROLE_USER"))));
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("not in the required organization");
    }

    @Test
    void whenGetMenu_withNotEnoughPermissions_thenForbidden() {
        organizationAccountRelationRepository.deleteAll();
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation.builder()
                .role(OrganizationRole.INVITED)
                .organization(organization1)
                .account(account1)
                .build()
        );

        var errorResponse = rest.requestFails(menu1.getId(), HttpStatus.FORBIDDEN);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("not enough permissions")
                .messageContains("MEMBER");
    }

    @Test
    void whenGetMenu_withNotInOrganisationButAdmin_thenOk() {
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
