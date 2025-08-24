package dev.heinisch.menumaestro.persistence;

import dev.heinisch.menumaestro.domain.account.Account;
import dev.heinisch.menumaestro.domain.account.PendingAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PendingAccountRepository extends JpaRepository<PendingAccount, String> {

    Optional<PendingAccount> findByEmail(String email);

}
