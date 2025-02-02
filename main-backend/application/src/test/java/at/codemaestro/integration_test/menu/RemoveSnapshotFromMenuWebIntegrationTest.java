package at.codemaestro.integration_test.menu;

import at.codemaestro.domain.account.Account;
import at.codemaestro.domain.menu.Menu;
import at.codemaestro.domain.menu.MenuStatus;
import at.codemaestro.domain.organization.Organization;
import at.codemaestro.domain.organization.OrganizationAccountRelation;
import at.codemaestro.domain.organization.OrganizationRole;
import at.codemaestro.integration_test.BaseWebIntegrationTest;
import at.codemaestro.integration_test.utils.ErrorResponseAssert;
import at.codemaestro.integration_test.utils.RestHelper;
import io.restassured.http.Headers;
import io.restassured.http.Method;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.SnapshotInMenuDto;
import org.springframework.http.HttpStatus;

import java.util.List;

import static at.codemaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static at.codemaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME_2;
import static at.codemaestro.integration_test.utils.test_constants.DefaultAccountTestData.defaultAccount;
import static at.codemaestro.integration_test.utils.test_constants.DefaultMenuTestData.defaultMenu1;
import static at.codemaestro.integration_test.utils.test_constants.DefaultMenuTestData.defaultSnapshotCreateDto1;
import static at.codemaestro.integration_test.utils.test_constants.DefaultOrganizationTestData.defaultOrganization1;
import static org.assertj.core.api.Assertions.assertThat;

public class RemoveSnapshotFromMenuWebIntegrationTest extends BaseWebIntegrationTest {


    private Organization organization1;
    private Menu menu1;

    private Account account1;
    private SnapshotInMenuDto snapshot1;

    private RestHelper.DualPathWithoutReturnRestHelper<Long, Long> rest;

    @PostConstruct
    private void initRestHelper() {
        rest = new RestHelper.DualPathWithoutReturnRestHelper<>(
                this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER")),
                Method.DELETE,
                URI + "/{menu_id}/snapshots/{snapshot_id}",
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
        menuService.addSnapshotToMenu(menu1.getId(), defaultSnapshotCreateDto1().position(0));
        snapshot1 = menuService.getMenuById(menu1.getId()).getSnapshots().get(0);
    }

    @Test
    void whenRemoveSnapshotFromMenu_withValidId_thenNoContent() {
        rest.requestSuccessful(menu1.getId(), snapshot1.getId());
        var menu = menuService.getMenuById(menu1.getId());
        assertThat(menu.getSnapshots()).hasSize(0);
    }

    @Test
    void whenRemoveSnapshotFromMenu_withNonExistentMenuId_thenNotFound() {
        ErrorResponseAssert.assertThat(rest.requestFails(6666L, snapshot1.getId(), HttpStatus.NOT_FOUND))
                .hasStatus(HttpStatus.NOT_FOUND)
                .messageContains("not found")
                .messageContainsIgnoreCase("menu");
    }

    @Test
    void whenRemoveSnapshotFromMenu_withNonExistentSnapshotId_thenNotFound() {
        ErrorResponseAssert.assertThat(rest.requestFails(menu1.getId(), 666L, HttpStatus.NOT_FOUND))
                .hasStatus(HttpStatus.NOT_FOUND)
                .messageContains("not found")
                .messageContainsIgnoreCase("snapshot");
    }

    @Test
    void whenRemoveSnapshotFromMenu_withMenuClosed_thenValidationException() {
        menu1.setStatus(MenuStatus.CLOSED);
        menuRepository.saveAndFlush(menu1);

        ErrorResponseAssert.assertThat(rest.requestFails(menu1.getId(), snapshot1.getId(), HttpStatus.UNPROCESSABLE_ENTITY))
            .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
            .messageContains("Menu was closed, it cannot be changed!");
    }

    @Test
    void whenRemoveSnapshotFromMenu_withNotInOrganisation_thenForbidden() {
        var errorResponse = rest.requestFails(
                menu1.getId(),
                snapshot1.getId(),
                HttpStatus.FORBIDDEN,
                new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME_2, List.of("ROLE_USER"))));
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("not in the required organization");
    }

    @Test
    void whenRemoveSnapshotFromMenu_withNotEnoughPermissions_thenForbidden() {
        organizationAccountRelationRepository.deleteAll();
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation.builder()
                .role(OrganizationRole.SHOPPER)
                .organization(organization1)
                .account(account1)
                .build()
        );

        var errorResponse = rest.requestFails(menu1.getId(), snapshot1.getId(), HttpStatus.FORBIDDEN);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("not enough permissions")
                .messageContains("PLANNER");
    }

    @Test
    void whenRemoveSnapshotFromMenu_withNotInOrganisationButAdmin_thenOk() {
        rest.requestSuccessful(
                menu1.getId(),
                snapshot1.getId(),
                new Headers(generateValidAuthorizationHeader("someuser", List.of("ROLE_ADMIN")))
        );
    }

    @Test
    void basicAuthTests() {
        rest.basicAuthTests(1L, 2L);
    }

}
