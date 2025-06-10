package at.codemaestro.persistence;

import at.codemaestro.domain.account.Account;
import at.codemaestro.domain.organization.Organization;
import at.codemaestro.domain.organization.OrganizationAccountRelation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationAccountRelationRepository extends JpaRepository<OrganizationAccountRelation, OrganizationAccountRelation.OrganizationAccountRelationId> {

    @Query("SELECT DISTINCT i FROM OrganizationAccountRelation i WHERE i.account.username = :username")
    List<OrganizationAccountRelation> findAllByUsername(String username);

    @Query("SELECT DISTINCT i FROM OrganizationAccountRelation i WHERE i.organization.id = :id")
    Page<OrganizationAccountRelation> findMembersByOrganisationId(long id, Pageable p);

    @Query("SELECT DISTINCT i FROM OrganizationAccountRelation i WHERE i.organization.id = :id")
    List<OrganizationAccountRelation> findMembersByOrganisationId(long id);

    @Query("SELECT DISTINCT i FROM OrganizationAccountRelation i WHERE i.account.username = :username")
    List<OrganizationAccountRelation> findByUsername(String username);

    void deleteByOrganization(Organization organization);
}
