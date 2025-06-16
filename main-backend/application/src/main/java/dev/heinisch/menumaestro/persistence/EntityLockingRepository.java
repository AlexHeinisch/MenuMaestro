package dev.heinisch.menumaestro.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;


@Repository
public class EntityLockingRepository {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Finds and locks the entity with PESSIMISTIC_WRITE.
     * @param entityClass identifies the entity type.
     * @param id of the entity, only long id supported.
     * @param timeoutMilliseconds how long to wait before throwing
     * @return an optional of the entity.
     * @param <E> type of the entity.
     */
    public <E> Optional<E> lockEntity(Class<E> entityClass, long id, int timeoutMilliseconds) {
        return Optional.ofNullable(entityManager.find(entityClass, id, LockModeType.PESSIMISTIC_WRITE, Map.of("javax.persistence.lock.timeout", timeoutMilliseconds)));
    }
}
