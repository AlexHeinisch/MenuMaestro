package dev.heinisch.menumaestro.datagen;

import dev.heinisch.menumaestro.domain.menu.Meal;
import dev.heinisch.menumaestro.domain.menu.Menu;
import dev.heinisch.menumaestro.domain.menu.MenuItem;
import dev.heinisch.menumaestro.domain.menu.Snapshot;
import dev.heinisch.menumaestro.domain.organization.Organization;
import dev.heinisch.menumaestro.domain.recipe.RecipeValue;
import dev.heinisch.menumaestro.persistence.MenuRepository;
import dev.heinisch.menumaestro.persistence.OrganizationRepository;
import dev.heinisch.menumaestro.persistence.RecipeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Profile("!datagen-off")
@DependsOn("recipeDataGenerator")
@Component
@Slf4j
@RequiredArgsConstructor
public class MenuDataGenerator {
    private final RecipeRepository recipeRepository;
    private final MenuRepository menuRepository;
    private final ObjectMapper objectMapper;
    private final OrganizationRepository organizationRepository;

    @Value("classpath:data/default_menus.json")
    private Resource defaultMenusFile;

    @Transactional
    public void generateMenus(Set<String> menuNames) throws IOException {
        log.info("Generating default menus");
        List<Organization> organizations = organizationRepository.findAll();
        for (MenuJsonObject menuData : objectMapper.readValue(defaultMenusFile.getURI().toURL(), MenuJsonObject[].class)) {
            if (menuNames != null && !menuNames.contains(menuData.name)) {
                continue;
            }
            List<MenuItem> items = new ArrayList<>(menuData.snapshots.stream()
                    .map(snapshotJson -> Snapshot.builder()
                            .name(snapshotJson.name)
                            .position(snapshotJson.position)
                            .build())
                    .toList());
            items.addAll(menuData.meals.stream()
                    .map(mealJson -> Meal.builder()
                            .name(mealJson.name)
                            .position(mealJson.position)
                            .isDone(mealJson.isDone)
                            .numberOfPeople(mealJson.numberOfPeople != null ? mealJson.numberOfPeople : menuData.numberOfPeople)
                            .recipe(RecipeValue.copyOf(recipeRepository.findByRecipeValue_Name(mealJson.recipeName).orElseThrow().getRecipeValue()))
                            .build()).toList()
            );
            items.sort(Comparator.comparing(MenuItem::getPosition));
            Long organizationId = organizations.stream().filter(o -> menuData.organizationName.equals(o.getName())).findFirst().orElseThrow().getId();
            menuRepository.saveAndFlush(Menu.builder()
                    .name(menuData.getName())
                    .description(menuData.getDescription())
                    .numberOfPeople(menuData.getNumberOfPeople())
                    .organizationId(organizationId)
                    .items(items)
                    .build());
        }
    }

    /**
     * @return the set of recipes (by name) required to generate the menus.
     */
    public Set<String> getDependencyRecipeNames(Collection<String> menuNames) {
        try {
            var menuJsonObjects = objectMapper.readValue(defaultMenusFile.getURI().toURL(), MenuJsonObject[].class);
            return Arrays.stream(menuJsonObjects)
                    .filter(m -> menuNames == null || menuNames.contains(m.name))
                    .flatMap(m -> m.meals.stream())
                    .map(m -> m.recipeName)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Data
    private static class MenuJsonObject {
        private String name;

        private String description;

        private String organizationName;

        private Integer numberOfPeople;

        private List<MealJsonObject> meals;

        private List<SnapshotJsonObject> snapshots;

        @Data
        private static class SnapshotJsonObject {
            String name;
            Integer position;
        }

        @Data
        private static class MealJsonObject {
            String name;
            String recipeName;
            Integer position;
            Integer numberOfPeople;
            Boolean isDone;
        }
    }
}
