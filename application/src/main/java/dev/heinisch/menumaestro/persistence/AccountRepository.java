package dev.heinisch.menumaestro.persistence;

import dev.heinisch.menumaestro.domain.account.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    Optional<Account> findByEmail(String email);

    @Query("SELECT DISTINCT i FROM Account i WHERE " +
        "(LOWER(i.username) LIKE LOWER(CONCAT('%',:name, '%')) OR " +
        "LOWER(i.firstName) LIKE LOWER(CONCAT('%',:name, '%')) OR " +
        "LOWER(i.lastName) LIKE LOWER(CONCAT('%',:name, '%'))) AND " +
        "i.username NOT IN :blacklist"
    )
    Page<Account> findMultipleUsernamesByAnyNameWithBlacklist(String name, List<String> blacklist, Pageable page);

}
