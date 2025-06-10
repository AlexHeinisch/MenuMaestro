package at.codemaestro.datagen;

import at.codemaestro.domain.organization.Organization;
import at.codemaestro.domain.organization.OrganizationAccountRelation;
import at.codemaestro.domain.organization.OrganizationRole;
import at.codemaestro.persistence.AccountRepository;
import at.codemaestro.persistence.OrganizationAccountRelationRepository;
import at.codemaestro.persistence.OrganizationRepository;
import at.codemaestro.properties.InitialOrganizationProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
@Profile("!datagen-off")
@DependsOn(value = "initialAccountGenerator")
public class InitialOrganizationGenerator {

    private final InitialOrganizationProperties initialOrganizationProperties;
    private final OrganizationRepository organizationRepository;
    private final OrganizationAccountRelationRepository organizationAccountRelationRepository;
    private final AccountRepository accountRepository;

    @PostConstruct
    @Transactional
    public void generateOrganizations() {
        if (initialOrganizationProperties.getOrganizations().isEmpty()) {
            log.info("No initial organizations found!");
            return;
        }
        log.info("Generating initial organizations...");
        initialOrganizationProperties.getOrganizations().forEach(org -> {
            Organization entity = Organization.builder()
                    .name(org.getName())
                    .description(org.getDescription())
                    .build();
            organizationRepository.save(entity);
            org.getMembers().forEach(member -> {
                organizationAccountRelationRepository.save(OrganizationAccountRelation.builder()
                    .organization(entity)
                    .account(accountRepository.findById(member.getUsername()).orElseThrow())
                    .role(OrganizationRole.valueOf(member.getRole()))
                    .build()
                );
            });
        });
        log.info("Finished generating initial organizations...");
    }
}
