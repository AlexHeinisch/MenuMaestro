package at.codemaestro.persistence;

import at.codemaestro.domain.menu.Menu;
import at.codemaestro.domain.menu.MenuStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    @Query("""
            SELECT m FROM Menu m
                WHERE (?1 IS NULL or (upper(m.name) LIKE upper('%' || ?1 || '%')))
                AND (?2 IS NULL OR m.status = ?2)
                AND (?3 IS NULL OR m.organizationId = ?3)
                AND (m.organizationId in ?4)
            """)
    Page<Menu> findByNameAsUserContainingIgnoreCaseAndOrganizationId(String name, MenuStatus menuStatus, Long organizationId, Set<Long> userOrganizationIds, Pageable pageable);

    @Query("""
            SELECT m FROM Menu m
                WHERE (?1 IS NULL or (upper(m.name) LIKE upper('%' || ?1 || '%')))
                AND (?2 IS NULL OR m.status = ?2)
                AND (?3 IS NULL OR m.organizationId = ?3)
            """)
    Page<Menu> findByNameAsAdminContainingIgnoreCaseAndOrganizationId(String name, MenuStatus menuStatus, Long organizationId, Pageable pageable);


    @Query("SELECT m FROM Menu m JOIN m.items i WHERE i.id = :mealId")
    Menu findByMealId(Long mealId);

    void deleteAllByOrganizationId(Long organizationId);
}
