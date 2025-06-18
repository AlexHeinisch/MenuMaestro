package dev.heinisch.menumaestro.domain.menu;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
public class Snapshot extends MenuItem {

    @Column(nullable = false)
    @NotBlank
    private String name;

    @Transient
    @Setter(AccessLevel.PACKAGE)
    private SnapshotMetadata metadata;

    @Builder
    public Snapshot(Long id, Menu menu, Integer position, String name) {
        super(id, menu, position);
        this.name = name;
    }
}
