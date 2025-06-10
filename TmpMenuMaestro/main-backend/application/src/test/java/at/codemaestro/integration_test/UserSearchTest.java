package at.codemaestro.integration_test;

import at.codemaestro.domain.account.Account;
import at.codemaestro.endpoint.AccountEndpoint;
import at.codemaestro.persistence.AccountRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openapitools.model.AccountSummaryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

public class UserSearchTest extends BaseWebIntegrationTest {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountEndpoint accountEndpoint;

    public void setup() {
        accountRepository.saveAllAndFlush(List.of(
                Account.builder()
                        .email("test@test.com")
                        .firstName("Max")
                        .lastName("Mustermann")
                        .isGlobalAdmin(false)
                        .username("User1")
                        .passwordHash("123123")
                        .build(),
                Account.builder()
                        .email("test1@test.com")
                        .firstName("Max")
                        .lastName("Mustermann")
                        .isGlobalAdmin(false)
                        .username("User2")
                        .passwordHash("123123")
                        .build(),
                Account.builder()
                        .email("test1@test.com")
                        .firstName("Max")
                        .lastName("Mustermann")
                        .isGlobalAdmin(false)
                        .username("Us")
                        .passwordHash("123123")
                        .build()
        ));
    }


    @Test
    @WithMockUser(username = "test", roles = {"USER"})
    void searchUsers() {
        setup();
        var response = Assertions.assertDoesNotThrow(() -> accountEndpoint
                .searchAccounts(null, null, "User", null));
        Assertions.assertAll(
                () -> Assertions.assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> Assertions.assertNotNull(response.getBody())
        );
        List<String> body = response.getBody().getContent().stream().map(AccountSummaryDto::getUsername).toList();
        Assertions.assertAll(
                () -> Assertions.assertEquals(2, body.size()),
                () -> Assertions.assertTrue(body.contains("User1") &&
                        body.contains("User2"))
        );

    }

    @Test
    @WithMockUser(username = "test", roles = {"USER"})
    void searchNoUsers() {
        setup();
        var response = Assertions.assertDoesNotThrow(() -> accountEndpoint
                .searchAccounts(null, null, "Koch", null));
        Assertions.assertAll(
                () -> Assertions.assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> Assertions.assertNotNull(response.getBody())
        );
        List<String> body = response.getBody().getContent().stream().map(AccountSummaryDto::getUsername).toList();
        Assertions.assertAll(
                () -> Assertions.assertEquals(0, body.size())
        );
    }

    @Test
    @WithMockUser(username = "test", roles = {"USER"})
    void searchAllUsers() {
        setup();
        var response = Assertions.assertDoesNotThrow(() -> accountEndpoint
                .searchAccounts(null, null, "us", null));
        Assertions.assertAll(
                () -> Assertions.assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> Assertions.assertNotNull(response.getBody())
        );
        List<String> body = response.getBody().getContent().stream().map(AccountSummaryDto::getUsername).toList();
        Assertions.assertAll(
                () -> Assertions.assertEquals(3, body.size()),
                () -> Assertions.assertTrue(body.contains("User1") &&
                        body.contains("User2") &&
                        body.contains("Us"))
        );
    }
}
