package dev.heinisch.menumaestro.persistence;

import dev.heinisch.menumaestro.domain.stash.StashEntry;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * For almost all cases, use {@link StashRepository} instead.
 */
@Repository
public interface StashEntryRepository extends CrudRepository<StashEntry, Long> {
}
