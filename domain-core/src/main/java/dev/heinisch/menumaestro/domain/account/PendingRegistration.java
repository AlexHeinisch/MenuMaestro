package dev.heinisch.menumaestro.domain.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class PendingRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pending_registration_seq")
    @SequenceGenerator(name = "pending_registration_seq", sequenceName = "pending_registration_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotNull
    private String username;

    @Column(nullable = false, unique = true)
    @NotNull
    @Email
    private String email;

    @Column(nullable = false)
    @NotNull
    private String firstName;

    @Column(nullable = false)
    @NotNull
    private String lastName;

    @Column(nullable = false)
    @NotNull
    private String passwordHash;

    @Column(nullable = false, unique = true)
    @NotNull
    private String verificationToken;

    @Column(nullable = false)
    @NotNull
    private Instant createdAt;

    @Column(nullable = false)
    @NotNull
    private Instant expiresAt;

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
