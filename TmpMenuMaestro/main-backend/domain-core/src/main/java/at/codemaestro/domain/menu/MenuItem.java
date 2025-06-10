package at.codemaestro.domain.menu;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.JoinColumn;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class MenuItem {
    @Id
    @GeneratedValue(generator = "seq_menu_item_id", strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(optional = false)
    @NotNull
    @Getter(AccessLevel.PUBLIC)
    @Setter(AccessLevel.PACKAGE)
    @JoinColumn(name = "menu_id")
    protected Menu menu;

    @Column(nullable = false)
    @NotNull
    private Integer position;

    public MenuItem(Long id, Menu menu, Integer position) {
        this.id = id;
        this.menu = menu;
        this.position = position;
    }
}
