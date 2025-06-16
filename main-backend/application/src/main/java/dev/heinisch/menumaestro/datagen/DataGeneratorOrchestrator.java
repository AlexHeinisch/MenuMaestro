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
    private final OrganizationDataGenerator organizationDataGenerator;
    private final RecipeDataGenerator recipeDataGenerator;
    private final MenuDataGenerator menuDataGenerator;
    private final StashDatagenerator stashDatagenerator;

    @PostConstruct
    public void runDataGenerator() throws IOException {
        log.info("Running data generator in AUTO mode...");
        organizationDataGenerator.generateOrganizations();
        ingredientDataGenerator.generateIngredients(null);
        stashDatagenerator.generateStashEntries();
        cookingAppliancesDataGenerator.generateCookingAppliances();
        recipeDataGenerator.generateRecipes();
        menuDataGenerator.generateMenus(null);
        log.info("Data generator finished.");
    }
}
