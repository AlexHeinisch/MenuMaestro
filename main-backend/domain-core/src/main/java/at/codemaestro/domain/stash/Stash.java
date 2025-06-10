package at.codemaestro.domain.stash;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


@Entity
@Getter
@NoArgsConstructor
@NamedEntityGraph(name = "Stash.aggregate",
attributeNodes = {@NamedAttributeNode("entries")})
public class Stash {
    @Id
    @GeneratedValue(generator = "seq_stash_id", strategy = GenerationType.SEQUENCE)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "stash")
    Set<StashEntry> entries;

    @Column(nullable = false)
    @NotNull
    private Long versionNumber;

    @Column(nullable = false)
    @NotNull
    @Setter
    private Boolean locked;

    @Builder
    public Stash(Long id, Collection<StashEntry> entries) {
        this.id = id;
        this.versionNumber = 0L;
        this.locked = false;
        this.entries = new HashSet<>(entries);
        for (StashEntry entry : entries) {
            entry.setStash(this);
        }
    }

    public static Stash createEmptyStash() {
        return new Stash(null, Collections.emptySet());
    }

    public void incrementVersionNumber() {
        this.versionNumber++;
    }
}
