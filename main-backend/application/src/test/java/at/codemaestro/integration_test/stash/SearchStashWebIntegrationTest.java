package at.codemaestro.integration_test.stash;

import at.codemaestro.domain.account.Account;
import at.codemaestro.domain.menu.Menu;
import at.codemaestro.domain.organization.Organization;
import at.codemaestro.domain.organization.OrganizationAccountRelation;
import at.codemaestro.domain.organization.OrganizationRole;
import at.codemaestro.integration_test.BaseWebIntegrationTest;
import at.codemaestro.integration_test.utils.RestHelper;
import at.codemaestro.integration_test.utils.test_constants.DefaultAccountTestData;
import at.codemaestro.integration_test.utils.test_constants.DefaultMenuTestData;
import at.codemaestro.integration_test.utils.test_constants.DefaultOrganizationTestData;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.Method;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.StashSearchResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static at.codemaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;

public class SearchStashWebIntegrationTest extends BaseWebIntegrationTest {

    private RestHelper.QueryRestHelper rest;
    private TransactionTemplate txTemplate;

    @PostConstruct
    void initRestHelper() {
        rest = new RestHelper.QueryRestHelper(
                this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER")),
                Method.GET,
                URI,
                HttpStatus.OK
        );
    }

    @BeforeEach
    void setup() {
        txTemplate = new TransactionTemplate();
    }

    @Override
    protected String getBasePath() {
        return "/stash";
    }

    @Test
    void basicAuthTest() {
        rest.basicAuthTests("");
    }

    @Test
    void search_findsOrgStashAndMenuStash() {
        Organization organization = organizationRepository.saveAndFlush(DefaultOrganizationTestData.defaultOrganization1());
        Menu menu = menuRepository.saveAndFlush(DefaultMenuTestData.defaultMenu1(organization.getId()));
        Account account = accountRepository.saveAndFlush(DefaultAccountTestData.defaultAccount());
        createAccountRelation(organization, account, OrganizationRole.PLANNER);

        List<StashSearchResponseDto> search = new ArrayList<>(rest.request(null)
                .then().statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<>() {
                }));
        search.sort(Comparator.comparing(StashSearchResponseDto::getName));
        Assertions.assertAll(
                () -> Assertions.assertEquals(2, search.size()),
                () -> Assertions.assertTrue(search.get(1).getName().contains(organization.getName())),
                () -> Assertions.assertEquals(organization.getStash().getId(), search.get(1).getId()),
                () -> Assertions.assertTrue(search.get(0).getName().contains(menu.getName())),
                () -> Assertions.assertEquals(menu.getStash().getId(), search.get(0).getId())
        );
    }

    @Test
    void search_menuStashLocked_findsOrgStashOnly() {
        Organization organization = organizationRepository.saveAndFlush(DefaultOrganizationTestData.defaultOrganization1());
        Menu menu = DefaultMenuTestData.defaultMenu1(organization.getId());
        menu.getStash().setLocked(true);
        menuRepository.saveAndFlush(menu);
        Account account = accountRepository.saveAndFlush(DefaultAccountTestData.defaultAccount());
        createAccountRelation(organization, account, OrganizationRole.PLANNER);

        List<StashSearchResponseDto> search = new ArrayList<>(rest.request(null)
            .then().statusCode(HttpStatus.OK.value())
            .extract()
            .as(new TypeRef<>() {
            }));
        search.sort(Comparator.comparing(StashSearchResponseDto::getName));
        Assertions.assertAll(
            () -> Assertions.assertEquals(1, search.size()),
            () -> Assertions.assertTrue(search.get(0).getName().contains(organization.getName())),
            () -> Assertions.assertEquals(organization.getStash().getId(), search.get(0).getId())
        );
    }

    @Test
    void searchForMenu_findsMenuStashOnly() {
        Organization organization = organizationRepository.saveAndFlush(DefaultOrganizationTestData.defaultOrganization1());
        Menu menu = menuRepository.saveAndFlush(DefaultMenuTestData.defaultMenu1(organization.getId()));
        Account account = accountRepository.saveAndFlush(DefaultAccountTestData.defaultAccount());
        createAccountRelation(organization, account, OrganizationRole.PLANNER);

        List<StashSearchResponseDto> search = new ArrayList<>(rest.request("name=Menu 1")
                .then().statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<>() {
                }));
        search.sort(Comparator.comparing(StashSearchResponseDto::getName));
        Assertions.assertAll(
                () -> Assertions.assertEquals(1, search.size()),
                () -> Assertions.assertTrue(search.get(0).getName().contains(menu.getName())),
                () -> Assertions.assertEquals(menu.getStash().getId(), search.get(0).getId())
        );
    }

    @Test
    void search_notInOrg_doesNotFindOrgOrMenuStash() {
        Organization organization = organizationRepository.saveAndFlush(DefaultOrganizationTestData.defaultOrganization1());
        Menu menu = menuRepository.saveAndFlush(DefaultMenuTestData.defaultMenu1(organization.getId()));
        Account account = accountRepository.saveAndFlush(DefaultAccountTestData.defaultAccount());
        createAccountRelation(organization, account, OrganizationRole.MEMBER);
        List<StashSearchResponseDto> search = new ArrayList<>(rest.request("name=")
                .then().statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<>() {
                }));
        Assertions.assertEquals(0, search.size());
    }

    void createAccountRelation(Organization organization, Account account, OrganizationRole role) {
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation.builder()
                .organization(organization)
                .account(account)
                .role(role)
                .build());
    }
}
