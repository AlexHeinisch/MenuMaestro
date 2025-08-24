package dev.heinisch.menumaestro.domain.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
public class PendingAccount {

    @Id
    @NotNull
    private String username;

    @Column(nullable = false)
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

    @Column(nullable = false)
    private Boolean isGlobalAdmin;

    @Column(nullable = true)
    private String confirmationToken;

    @Column(nullable = false)
    private Instant creationDate;

    @Column(nullable = true)
    private Instant lastTokenIssued;
}