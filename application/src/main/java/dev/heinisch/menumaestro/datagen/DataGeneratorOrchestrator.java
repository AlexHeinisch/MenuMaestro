package dev.heinisch.menumaestro.datagen;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@Profile("!datagen-off")
@RequiredArgsConstructor
public class DataGeneratorOrchestrator {

    private final IngredientDataGenerator ingredientDataGenerator;
    private final CookingAppliancesDataGenerator cookingAppliancesDataGenerator;

    @PostConstruct
    public void runDataGenerator() throws IOException {
        log.info("Running data generator in AUTO mode...");
        ingredientDataGenerator.generateIngredients(null);
        cookingAppliancesDataGenerator.generateCookingAppliances();
        log.info("Data generator finished.");
    }
}
