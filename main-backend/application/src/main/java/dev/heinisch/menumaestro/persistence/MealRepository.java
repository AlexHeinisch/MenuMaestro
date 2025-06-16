package dev.heinisch.menumaestro.persistence;

import dev.heinisch.menumaestro.domain.menu.Meal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MealRepository extends JpaRepository<Meal, Long> {
}
