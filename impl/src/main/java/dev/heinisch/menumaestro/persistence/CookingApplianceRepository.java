package dev.heinisch.menumaestro.persistence;

import dev.heinisch.menumaestro.domain.cooking_appliance.CookingAppliance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import java.util.Set;

public interface CookingApplianceRepository extends JpaRepository<CookingAppliance, Long> {

    long countByIdIn(Set<Long> ids);

    Page<CookingAppliance> findCookwareByNameContainingIgnoreCase(String name, Pageable pageable);

    Optional<CookingAppliance> findByName(String name);

    List<CookingAppliance> findByNameIn(List<String> names);
}
