package dev.heinisch.menumaestro.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories("dev.heinisch.menumaestro.persistence")
@EntityScan(basePackages = "dev.heinisch.menumaestro.domain")
@Configuration
@Slf4j
public class HibernateConfiguration {

}
