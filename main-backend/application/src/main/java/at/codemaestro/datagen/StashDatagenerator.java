package at.codemaestro.datagen;

import at.codemaestro.domain.ingredient.IngredientUnit;
import at.codemaestro.domain.stash.Stash;
import at.codemaestro.domain.stash.StashEntry;
import at.codemaestro.persistence.IngredientRepository;
import at.codemaestro.persistence.OrganizationRepository;
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
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Component
@Profile("!datagen-off")
public class StashDatagenerator {
    private final ObjectMapper objectMapper;
    private final OrganizationRepository organizationRepository;
    private final IngredientRepository ingredientRepository;

    @Value("classpath:data/default_stash_entries.json")
    private Resource defaultIngredientsFile;

    @Transactional
    public void generateStashEntries() throws IOException {
        var stashes = objectMapper.readValue(defaultIngredientsFile.getURI().toURL(), StashJsonObject[].class);
        for (StashJsonObject stashData : stashes) {
            Stash stash = organizationRepository.findByName(stashData.organization).orElseThrow().getStash();
            for (StashEntryJsonObject entryData : stashData.entries) {
                StashEntry entry = StashEntry.builder()
                        .ingredientId(ingredientRepository.findByName(entryData.ingredient).orElseThrow().getId())
                        .amount(entryData.amount)
                        .unit(IngredientUnit.valueOf(entryData.unit))
                        .stash(stash)
                        .build();
                stash.getEntries().add(entry);
            }
        }
    }



    @Setter
    @Getter
    @ToString
    private static class StashJsonObject {
        private  String organization;
        private List<StashEntryJsonObject> entries;
    }

    @Setter
    @Getter
    @ToString
    private static class StashEntryJsonObject {
        String unit;
        String ingredient;
        double amount;
    }
}
