package dev.heinisch.menumaestro.datagen;

import dev.heinisch.menumaestro.domain.ingredient.Ingredient;
import dev.heinisch.menumaestro.domain.ingredient.IngredientCategory;
import dev.heinisch.menumaestro.domain.ingredient.IngredientStatus;
import dev.heinisch.menumaestro.domain.ingredient.IngredientUnit;
import dev.heinisch.menumaestro.persistence.IngredientRepository;
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
import java.util.HashMap;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Component
@Profile("!datagen-off")
public class IngredientDataGenerator {

    private final IngredientRepository ingredientRepository;
    private final ObjectMapper objectMapper;

    @Value("classpath:data/default_ingredients.json")
    private Resource defaultIngredientsFile;

    @Transactional
    public void generateIngredients(Set<String> ingredientNames) throws IOException {
        log.info("Generating default ingredients");
        HashMap<String, Ingredient> parentStore = new HashMap<>();
        for (IngredientJsonObject i : objectMapper.readValue(defaultIngredientsFile.getURI().toURL(), IngredientJsonObject[].class)) {
            if ((ingredientNames != null && !ingredientNames.contains(i.name))
                    || ingredientRepository.findByName(i.name).isPresent()) {
                continue;
            }
            Ingredient entity = Ingredient
                    .builder()
                    .name(i.name)
                    .category(IngredientCategory.valueOf(i.category))
                    .parent(i.parent_ingredient == null ? null : parentStore.get(i.parent_ingredient))
                    .status(IngredientStatus.PUBLIC)
                    .defaultUnit(IngredientUnit.valueOf(i.default_unit))
                    .build();
            parentStore.put(i.name, ingredientRepository.save(entity));
        }
        ingredientRepository.flush();
        log.info("Finished generating default ingredients");
    }


    @Setter
    @Getter
    @ToString
    private static class IngredientJsonObject {
        private String name, category, parent_ingredient, default_unit;
    }

}
