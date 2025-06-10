package at.codemaestro.integration_test.menu;

import at.codemaestro.domain.account.Account;
import at.codemaestro.domain.menu.MenuStatus;
import at.codemaestro.domain.organization.Organization;
import at.codemaestro.domain.organization.OrganizationAccountRelation;
import at.codemaestro.domain.organization.OrganizationRole;
import at.codemaestro.integration_test.BaseWebIntegrationTest;
import at.codemaestro.integration_test.utils.ErrorResponseAssert;
import at.codemaestro.integration_test.utils.RestHelper;
import at.codemaestro.integration_test.utils.TestPageableResponse;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.Headers;
import io.restassured.http.Method;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.ErrorResponse;
import org.openapitools.model.MenuSummaryDto;
import org.springframework.http.HttpStatus;

import java.util.List;

import static at.codemaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static at.codemaestro.integration_test.utils.test_constants.DefaultAccountTestData.defaultAccount;
import static at.codemaestro.integration_test.utils.test_constants.DefaultMenuTestData.defaultMenuCreateDto1;
import static at.codemaestro.integration_test.utils.test_constants.DefaultMenuTestData.defaultMenuCreateDto2;
import static at.codemaestro.integration_test.utils.test_constants.DefaultMenuTestData.defaultMenuCreateDto3;
import static at.codemaestro.integration_test.utils.test_constants.DefaultOrganizationTestData.defaultOrganization1;
import static at.codemaestro.integration_test.utils.test_constants.DefaultOrganizationTestData.defaultOrganization2;
import static org.assertj.core.api.Assertions.assertThat;


public class SearchMenusWebIntegrationTest extends BaseWebIntegrationTest {


    private Organization organization1, organization2;
    private MenuSummaryDto menu1, menu2, menu3;

    private Account account1;
    private static final String TEST_USER = "test_user";
    private static final String ADMIN_USER = "admin_user";

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
        return "/menus";
    }

    @BeforeEach
    public void setup() {
        organization1 = organizationRepository.saveAndFlush(defaultOrganization1());
        organization2 = organizationRepository.saveAndFlush(defaultOrganization2());
        account1 = accountRepository.saveAndFlush(defaultAccount());
        accountRepository.saveAndFlush(Account.builder()
                .username(TEST_USER)
                .email("test@test.com")
                .firstName("Test")
                .lastName("Test")
                .isGlobalAdmin(false)
                .passwordHash("")
                .build());

        accountRepository.saveAndFlush(Account.builder()
                .username(ADMIN_USER)
                .email("admin@test.com")
                .firstName("Admin")
                .lastName("Admin")
                .isGlobalAdmin(true)
                .passwordHash("")
                .build());


        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation.builder()
                .role(OrganizationRole.OWNER)
                .organization(organization1)
                .account(account1)
                .build());
        menu1 = menuService.createMenu(defaultMenuCreateDto1().organizationId(organization1.getId()));
        menu2 = menuService.createMenu(defaultMenuCreateDto2().organizationId(organization1.getId()));
        menu3 = menuService.createMenu(defaultMenuCreateDto3().organizationId(organization2.getId()));
        var menuEntity2 = menuRepository.findById(menu2.getId()).get();
        menuEntity2.setStatus(MenuStatus.CLOSED);
        menuRepository.saveAndFlush(menuEntity2);
    }

    @Test
    public void whenGetRecipes_withNoQuery_thenOK() {
        TestPageableResponse<MenuSummaryDto> result = adminRequestSuccessful("");

        assertContainsMenu(result, menu1);
        assertContainsMenu(result, menu3);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    public void whenGetRecipes_withQueryByName_thenOK() {
        TestPageableResponse<MenuSummaryDto> result = adminRequestSuccessful("name=Alpha");

        assertContainsMenu(result, menu1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    public void whenGetRecipes_withQueryByOrganization_thenOK() {
        TestPageableResponse<MenuSummaryDto> result = adminRequestSuccessful("organization_id=" + organization2.getId());

        assertContainsMenu(result, menu3);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    public void whenGetRecipes_withQueryByStatus_thenOK() {
        TestPageableResponse<MenuSummaryDto> result = adminRequestSuccessful("status=" + MenuStatus.CLOSED.name());
        menu2.setStatus(org.openapitools.model.MenuStatus.CLOSED);

        assertContainsMenu(result, menu2);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    public void whenGetRecipes_withPageSizeOne_thenOK() {
        var result = adminRequestSuccessful("size=1");

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumberOfElements()).isEqualTo(1);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getFirst()).isEqualTo(true);
        assertThat(result.getLast()).isEqualTo(false);
    }

    @Test
    public void whenGetRecipes_withPageSizeOne_andPageNumberTwo_thenOK() {
        var result = adminRequestSuccessful("size=1&page=1");

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumberOfElements()).isEqualTo(1);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getFirst()).isEqualTo(false);
        assertThat(result.getLast()).isEqualTo(true);
    }

    @Test
    public void whenGetRecipes_withPageNumberTwo_thenOK() {
        var result = adminRequestSuccessful("page=1");

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumberOfElements()).isEqualTo(0);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getFirst()).isEqualTo(false);
        assertThat(result.getLast()).isEqualTo(true);
    }


    @Test
    public void whenGetMenu_WithUser_thenGetOnlyMemberOrganization() {
        TestPageableResponse<MenuSummaryDto> result = requestSuccessful("",
                new Headers(List.of(generateValidAuthorizationHeader(TEST_USER, List.of("ROLE_USER")))));

        assertThat(result.getTotalElements()).isEqualTo(0);

        result = requestSuccessful("",
                new Headers(List.of(generateValidAuthorizationHeader(account1.getUsername(), List.of("ROLE_USER")))));

        assertThat(result.getTotalElements()).isEqualTo(1);
        result = requestSuccessful("",
                new Headers(List.of(generateValidAuthorizationHeader(ADMIN_USER, List.of("ROLE_USER", "ROLE_ADMIN")))));
        assertThat(result.getTotalElements()).isEqualTo(2);

    }

    @Test
    public void whenGetDoneMenu_WithUser_thenGetOnlyMemberOrganization() {
        TestPageableResponse<MenuSummaryDto> result = requestSuccessful("status=" + MenuStatus.CLOSED.name(),
                new Headers(List.of(generateValidAuthorizationHeader(TEST_USER, List.of("ROLE_USER")))));

        assertThat(result.getTotalElements()).isEqualTo(0);

        result = requestSuccessful("status=" + MenuStatus.CLOSED.name(),
                new Headers(List.of(generateValidAuthorizationHeader(account1.getUsername(), List.of("ROLE_USER")))));

        assertThat(result.getTotalElements()).isEqualTo(1);
        result = requestSuccessful("status=" + MenuStatus.CLOSED.name(),
                new Headers(List.of(generateValidAuthorizationHeader(ADMIN_USER, List.of("ROLE_USER", "ROLE_ADMIN")))));
        assertThat(result.getTotalElements()).isEqualTo(1);

    }

    @Test
    public void whenGetMenu_WithUnauth_thenFail() {
        ErrorResponse errorResponse = getMenuFails("",
                HttpStatus.FORBIDDEN,
                new Headers(List.of()));
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("Access Denied");
    }

    private TestPageableResponse<MenuSummaryDto> adminRequestSuccessful(String query) {
        return requestSuccessful(query,
                new Headers(List.of(generateValidAuthorizationHeader(ADMIN_USER, List.of("ROLE_USER", "ROLE_ADMIN")))));

    }

    private TestPageableResponse<MenuSummaryDto> requestSuccessful(String query, Headers headers) {
        return rest.request(query, headers)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<TestPageableResponse<MenuSummaryDto>>() {
                });
    }

    private ErrorResponse getMenuFails(String query, HttpStatus status, Headers headers) {
        return rest.request(query, headers)
                .then()
                .statusCode(status.value())
                .extract()
                .as(ErrorResponse.class);
    }


    @Test
    void basicAuthTests() {
        rest.basicAuthTests("");
    }

    void assertContainsMenu(TestPageableResponse<MenuSummaryDto> list, MenuSummaryDto dto) {
        assertThat(list.getContent()).contains(dto);
    }
}
