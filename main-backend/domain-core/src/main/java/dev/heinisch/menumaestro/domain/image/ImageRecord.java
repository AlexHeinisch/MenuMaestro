package dev.heinisch.menumaestro.domain.image;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
public class ImageRecord {

    @Id
    @NotNull
    private String id;

    @Column(nullable = false)
    @NotNull
    private Instant createdAt;

    @Column(nullable = false)
    @NotNull
    private String mimeType;

    @Column(nullable = false)
    @NotNull
    private String uploadedBy;

    @Column(columnDefinition = "bytea", nullable = false)
    @NotNull
    private byte[] data;
}
