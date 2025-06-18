package dev.heinisch.menumaestro.domain.menu;

import dev.heinisch.menumaestro.domain.stash.Stash;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The menu entity.
 */
@Entity
@NoArgsConstructor
@Getter
@Setter
public class Menu {
    @Id
    @GeneratedValue(generator = "seq_menu_id", strategy = GenerationType.SEQUENCE)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false)
    @NotNull
    private String name;

    @Column(nullable = false)
    @NotNull
    private String description;

    @Column(nullable = false)
    @NotNull
    private Long organizationId;

    @Column(nullable = false)
    @NotNull
    private int numberOfPeople;

    @Column(nullable = false)
    @NotNull
    @Enumerated(EnumType.STRING)
    private MenuStatus status;

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MenuItem> items = new HashSet<>();

    /**
     * Explicit relation is simpler in this case, even though it is more tightly coupled than a long:id.
     */
    @OneToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, orphanRemoval = true)
    private Stash stash;

    @Builder
    public Menu(String name, String description, Integer numberOfPeople, Long organizationId,
                List<MenuItem> items) {
        this.name = name;
        this.description = description;
        this.numberOfPeople = numberOfPeople;
        this.status=MenuStatus.SERVING;
        this.organizationId=organizationId;
        if (items != null) {
            this.items = new HashSet<>(items);
            assignPositionFromItemList(items);
        } else {
            this.items = new HashSet<>();
        }
        this.stash = Stash.createEmptyStash();
    }

    public void assignPositionFromItemIdList(List<Long> itemIds) {
        Map<Long, Integer> positionByItemId = new HashMap<>();
        for (int i = 0; i < itemIds.size(); i++) {
            positionByItemId.put(itemIds.get(i), i);
        }
        for (MenuItem item : items) {
            item.setPosition(positionByItemId.get(item.getId()));
        }
    }

    public void assignPositionFromItemList(List<MenuItem> items) {
        int sort = 0;
        for (MenuItem item : items) {
            item.setPosition(sort++);
            item.setMenu(this);
        }
    }

    private void fixPositionsOfItemsOnDelete(Integer deletedPosition) {
        items.stream()
                .filter(item -> item.getPosition() > deletedPosition)
                .forEach(item -> item.setPosition(item.getPosition() - 1));
    }

    private void fixPositionsOfItemsOnInsert(Integer insertedPosition) {
        items.stream()
            .filter(item -> item.getPosition() >= insertedPosition)
            .forEach(item -> item.setPosition(item.getPosition() + 1));
    }

    public void removeMeal(Meal meal) {
        this.getItems().remove(meal);
        this.fixPositionsOfItemsOnDelete(meal.getPosition());
    }

    public void addMenuItem(MenuItem menuItem) {
        this.fixPositionsOfItemsOnInsert(menuItem.getPosition());
        this.getItems().add(menuItem);
    }

    public void removeSnapshot(Snapshot snapshot) {
        this.getItems().remove(snapshot);
        this.fixPositionsOfItemsOnDelete(snapshot.getPosition());
    }
}
