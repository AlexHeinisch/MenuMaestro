package dev.heinisch.menumaestro.datagen;

import dev.heinisch.menumaestro.domain.organization.Organization;
import dev.heinisch.menumaestro.persistence.OrganizationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;

@Profile("!datagen-off")
@DependsOn("recipeDataGenerator")
@Component
@Slf4j
@RequiredArgsConstructor
public class OrganizationDataGenerator {
    private final OrganizationRepository organizationRepository;

    @Value("classpath:data/default_organizations.json")
    private Resource defaultOrganizationsFile;


    private final PlatformTransactionManager txManager;
    private final ObjectMapper objectMapper;

    @Transactional
    public void generateOrganizations() {
        log.info("Generating default organizations");
        new TransactionTemplate(txManager).executeWithoutResult(tx -> {
            try {
                for (OrganizationJsonObject orgJson:
                     objectMapper.readValue(defaultOrganizationsFile.getURI().toURL(), OrganizationJsonObject[].class)) {
                    organizationRepository.save(Organization.builder()
                            .name(orgJson.name)
                            .description(orgJson.description)
                            .build());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Data
    private static class OrganizationJsonObject {
        private String name;
        private String description;
    }
}
