package dev.heinisch.menumaestro.domain.organization;

import dev.heinisch.menumaestro.domain.stash.Stash;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Organization {
    @Id
    @GeneratedValue(generator = "seq_organization_id", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotNull
    private String name;

    @Column(nullable = false)
    @NotNull
    private String description;

    /**
     * Explicit relation is simpler in this case, even though it is more tightly coupled than a long:id.
     */
    @OneToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, orphanRemoval = true)
    private Stash stash;

    @Builder
    public Organization(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.stash = Stash.createEmptyStash();
    }
}
