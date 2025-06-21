package dev.heinisch.menumaestro.persistence;

import dev.heinisch.menumaestro.domain.stash.Stash;
import dev.heinisch.menumaestro.domain.stash.projections.StashIdName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface StashRepository extends JpaRepository<Stash, Long> {
    @Query("""
            SELECT o.id FROM Organization o
                        LEFT JOIN Menu menu ON menu.organizationId = o.id
                        WHERE o.stash.id = :stashId
                            OR menu.stash.id = :stashId
            """)
    Optional<Long> getOrganizationIdOfStash(Long stashId);

    @Query("""
            SELECT NULL AS id,
                   CASE
                   WHEN o.id IS NOT NULL THEN CONCAT('Stash of Organization ', o.name)
                   ELSE CONCAT('Stash of Menu ', menu.name)
                   END AS name,
                   COALESCE(o.id, menu.organizationId) AS organizationId
                                FROM Stash s LEFT JOIN Organization o ON o.stash.id = s.id
                LEFT JOIN Menu menu ON menu.stash.id = s.id
            WHERE s.id = ?1
            """)
    Optional<StashIdName> getStashName(Long stashId);

    @Query("""
            SELECT s.id AS id, (CASE WHEN o.id IS NOT NULL THEN CONCAT('Organization ', o.name)
                        ELSE CONCAT('Menu ', menu.name)
                        END) AS name,
                        COALESCE(o.id, menu.organizationId) AS organizationId
            FROM Stash s LEFT JOIN Organization o ON o.stash.id = s.id
                LEFT JOIN Menu menu ON menu.stash.id = s.id
            WHERE ('organization ' || lower(o.name) LIKE '%'||lower(?1)||'%' OR 'menu '||lower(menu.name) LIKE '%'||lower(?1)||'%')
            AND (s.locked = false)
            AND EXISTS( SELECT 1 FROM OrganizationAccountRelation oar
                        WHERE oar.account.username = ?2
                        AND oar.role IN ?3
                        AND (oar.organization.id = o.id OR oar.organization.id = menu.organizationId))
            """)
    Page<StashIdName> searchByName(String stashName, String username, Set<String> orgRoles, Pageable pageable);

    @EntityGraph("Stash.aggregate")
    @Query("SELECT s FROM Stash s WHERE s.id = ?1")
    Optional<Stash> findByIdFetchAggregate(Long id);
}
