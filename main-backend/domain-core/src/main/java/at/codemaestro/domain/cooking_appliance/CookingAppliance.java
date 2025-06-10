package at.codemaestro.domain.cooking_appliance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The cooking appliance entity, representing a type of cookware, e.g. stove or pot.
 * Intended to be a small, global list of items needed.
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CookingAppliance {
    @Id
    @GeneratedValue(generator = "cooking_appliance_seq", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(
        name = "cooking_appliance_seq",
        sequenceName = "cooking_appliance_seq",
        initialValue = 1,
        allocationSize = 1
    )
    private Long id;

    @Column(nullable = false)
    @NotBlank
    private String name;

    @Builder
    public CookingAppliance(@NotBlank String name) {
        this.name = name;
    }
}
