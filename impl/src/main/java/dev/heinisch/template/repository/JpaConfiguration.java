package dev.heinisch.template.repository;

import io.hypersistence.utils.spring.repository.BaseJpaRepositoryImpl;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(value = "dev.heinisch.template.repository", repositoryBaseClass = BaseJpaRepositoryImpl.class)
@EntityScan("dev.heinisch.template.model")
public class JpaConfiguration {
}
