package at.codemaestro.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories("at.codemaestro.persistence")
@EntityScan(basePackages = "at.codemaestro.domain")
@Configuration
@Slf4j
public class HibernateConfiguration {

}
