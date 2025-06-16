package dev.heinisch.menumaestro.datagen;


import dev.heinisch.menumaestro.domain.cooking_appliance.CookingAppliance;
import dev.heinisch.menumaestro.persistence.CookingApplianceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
@Profile("!datagen-off")
public class CookingAppliancesDataGenerator {

    private final CookingApplianceRepository cookingApplianceRepository;
    private final ObjectMapper objectMapper;

    @Value("classpath:data/default_cooking_appliance.json")
    private Resource defaultCookingAppliancesFile;

    @Transactional
    public void generateCookingAppliances() throws IOException {
        log.info("Generating default cookingAppliance");
        for (CookingApplianceJsonObject i : objectMapper.readValue(defaultCookingAppliancesFile.getURI().toURL(), CookingApplianceJsonObject[].class)) {
            if (cookingApplianceRepository.findByName(i.name).isPresent()) {
                continue;
            }
            CookingAppliance entity = CookingAppliance
                    .builder()
                    .name(i.name)
                    .build();
            cookingApplianceRepository.save(entity);
        }
        cookingApplianceRepository.flush();
        log.info("Finished generating default cookingAppliance");
    }


    @Setter
    @Getter
    @ToString
    private static class CookingApplianceJsonObject {
        private String name;
    }

}
