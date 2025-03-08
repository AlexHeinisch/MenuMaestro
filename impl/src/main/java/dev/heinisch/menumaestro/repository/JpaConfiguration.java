package dev.heinisch.menumaestro.repository;

import io.hypersistence.utils.spring.repository.BaseJpaRepositoryImpl;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(value = "dev.heinisch.menumaestro.repository", repositoryBaseClass = BaseJpaRepositoryImpl.class)
@EntityScan("dev.heinisch.menumaestro.model")
public class JpaConfiguration {
}
