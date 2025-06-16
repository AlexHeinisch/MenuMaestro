package dev.heinisch.menumaestro.datagen;

import dev.heinisch.menumaestro.domain.cooking_appliance.CookingAppliance;
import dev.heinisch.menumaestro.domain.ingredient.Ingredient;
import dev.heinisch.menumaestro.persistence.CookingApplianceRepository;
import dev.heinisch.menumaestro.persistence.IngredientRepository;
import dev.heinisch.menumaestro.persistence.RecipeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.api.RecipesApi;
import org.openapitools.model.CookingApplianceUseCreateEditDto;
import org.openapitools.model.IngredientUnitDto;
import org.openapitools.model.IngredientUseCreateEditDto;
import org.openapitools.model.RecipeCreateEditDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
@Profile("!datagen-off")
@DependsOn({"ingredientDataGenerator", "cookingAppliancesDataGenerator"})
public class RecipeDataGenerator {

    private final RecipeRepository recipeRepository;
    private final RecipesApi recipesApi;
    private final IngredientRepository ingredientRepository;
    private final CookingApplianceRepository cookingApplianceRepository;
    private final ObjectMapper objectMapper;
    private final Environment environment;

    @Value("classpath:data/default_recipes.json")
    private Resource defaultRecipesFile;

    @Transactional
    public void generateRecipes() throws IOException {
        generateRecipes(null);
    }

    @Transactional
    public void generateRecipes(Collection<String> recipeNames) throws IOException {
        log.info("Generating default recipes");
        for (RecipeJsonObject i : objectMapper.readValue(defaultRecipesFile.getURI().toURL(), RecipeJsonObject[].class)) {
            if ((recipeNames != null && !recipeNames.contains(i.name)) || recipeRepository.findByRecipeValue_Name(i.name).isPresent()) {
                continue;
            }
            List<String> ingredientNames = i.getIngredients().stream()
                    .map(json -> json.name)
                    .toList();
            Map<String, Long> usedIngredients = ingredientRepository
                    .findMultipleByName(ingredientNames)
                    .stream()
                    .collect(Collectors.toMap(Ingredient::getName, Ingredient::getId));
            if (usedIngredients.size() != i.getIngredients().size()) {
                log.error("Could not find all ingredients provided in recipe '{}'", ingredientNames);
                continue;
            }

            List<String> applianceNames = i.getCookingAppliances().stream()
                    .map(json -> json.name)
                    .toList();
            Map<String, Long> usedCookingAppliances = cookingApplianceRepository
                    .findByNameIn(applianceNames)
                    .stream()
                    .collect(Collectors.toMap(CookingAppliance::getName, CookingAppliance::getId));
            if (usedCookingAppliances.size() != i.getCookingAppliances().size()) {
                log.error("Could not find all cooking appliances provided in recipe '{}'\napplicances: {}", i.getName(), applianceNames);
                continue;
            }

            var createRecipeDto = new RecipeCreateEditDto()
                    .name(i.getName())
                    .description(i.getDescription())
                    .author(i.getAuthor())
                    .servings(i.getServings())
                    .visibility(org.openapitools.model.RecipeVisibility.PUBLIC)
                    .ingredients(i.getIngredients()
                            .stream()
                            .map(ing -> new IngredientUseCreateEditDto()
                                    .id(usedIngredients.get(ing.getName()))
                                    .amount(ing.getAmount())
                                    .unit(IngredientUnitDto.fromValue(ing.getUnit())))
                            .toList()
                    )
                    .cookingAppliances(i.getCookingAppliances()
                            .stream()
                            .map(ca -> new CookingApplianceUseCreateEditDto()
                                    .id(usedCookingAppliances.get(ca.getName()))
                                    .amount(ca.getAmount()))
                            .toList());
            Authentication originalAuth = SecurityContextHolder.getContext().getAuthentication();
            try {
                SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(
                        "system",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                    )
                );
                recipesApi.createRecipe(createRecipeDto);
            } finally {
                SecurityContextHolder.getContext().setAuthentication(originalAuth);
            }
        }
        log.info("Finished generating default recipes");
    }

    /**
     * @return the set of ingredients (by name) required to generate the recipes.
     */
    public Set<String> getDependencyIngredientNames(Collection<String> recipeNames) {
        try {
            var recipeJsonObjects = objectMapper.readValue(defaultRecipesFile.getURI().toURL(), RecipeJsonObject[].class);
            return Arrays.stream(recipeJsonObjects)
                    .filter(recipe -> recipeNames == null || recipeNames.contains(recipe.name))
                    .flatMap(r-> r.ingredients.stream())
                    .map(i -> i.name)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Setter
    @Getter
    @ToString
    private static class RecipeJsonObject {
        private String name, description, author;
        private int servings;
        private List<IngredientJsonObject> ingredients;
        private List<CookingApplianceJsonObject> cookingAppliances;

        @Setter
        @Getter
        private static class IngredientJsonObject {
            private String name, unit;
            private float amount;
        }

        @Setter
        @Getter
        private static class CookingApplianceJsonObject {
            private String name;
            private int amount;
        }
    }

}
