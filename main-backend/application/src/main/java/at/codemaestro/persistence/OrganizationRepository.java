package at.codemaestro.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import at.codemaestro.domain.organization.Organization;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findByName(String name);

    @Query("""
            SELECT DISTINCT o FROM Organization o
                        LEFT JOIN OrganizationAccountRelation oar ON oar.organization = o
                                    WHERE (?1 IS NULL OR oar.account.username = ?1)
                                    AND (?2 IS NULL OR (upper(o.name) LIKE upper('%' || ?2 || '%')))
                                    AND oar.role != 'INVITED'
            """)
    Page<Organization> findByMemberUsernameAndNameContainingIgnoreCaseAndNotInvited(String username, String name, Pageable page);

    Page<Organization> findAllByIdIn(List<Long> ids, Pageable p);

    @Query("""
            SELECT DISTINCT o FROM Organization o
                                    WHERE (?1 IS NULL OR (upper(o.name) LIKE upper('%' || ?1 || '%')))
            """)
    Page<Organization> findAllByNameContainingIgnoreCase(String name, Pageable page);
}
