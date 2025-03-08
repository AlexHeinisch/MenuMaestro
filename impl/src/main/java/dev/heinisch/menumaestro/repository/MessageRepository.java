package dev.heinisch.menumaestro.repository;

import dev.heinisch.menumaestro.model.Message;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends BaseJpaRepository<Message, Long> {

    Page<Message> findAll(Pageable pageable);
}
