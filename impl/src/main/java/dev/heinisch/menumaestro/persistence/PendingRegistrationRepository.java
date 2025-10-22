package dev.heinisch.menumaestro.persistence;

import dev.heinisch.menumaestro.domain.account.PendingRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface PendingRegistrationRepository extends JpaRepository<PendingRegistration, Long> {

    Optional<PendingRegistration> findByVerificationToken(String verificationToken);

    Optional<PendingRegistration> findByEmail(String email);

    Optional<PendingRegistration> findByUsername(String username);

    List<PendingRegistration> findAllByExpiresAtBefore(Instant expirationTime);

}
