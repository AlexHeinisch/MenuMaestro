package dev.heinisch.menumaestro.stash;

import dev.heinisch.menumaestro.domain.ingredient.Ingredient;
import dev.heinisch.menumaestro.domain.ingredient.IngredientUnit;
import dev.heinisch.menumaestro.domain.ingredient_computation.IngredientUse;
import dev.heinisch.menumaestro.domain.menu.Menu;
import dev.heinisch.menumaestro.domain.organization.Organization;
import dev.heinisch.menumaestro.domain.stash.Stash;
import dev.heinisch.menumaestro.domain.stash.StashEntry;
import dev.heinisch.menumaestro.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.utils.test_constants.DefaultIngredientTestData;
import dev.heinisch.menumaestro.utils.test_constants.DefaultMenuTestData;
import dev.heinisch.menumaestro.utils.test_constants.DefaultOrganizationTestData;
import dev.heinisch.menumaestro.service.StashService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ModifyStashIT extends BaseWebIntegrationTest {

    @Autowired
    StashService stashService;
    @Test
    public void addToStash() {
        Organization organization = organizationRepository.saveAndFlush(DefaultOrganizationTestData.defaultOrganization1());

        Menu menu = menuRepository.saveAndFlush(DefaultMenuTestData.defaultMenu1(organization.getId()));
        List<Ingredient> ingredients = ingredientRepository.saveAllAndFlush(DefaultIngredientTestData.getDefaultIngredients());

        IngredientUse ingredient = new IngredientUse(ingredients.get(0), IngredientUnit.GRAMS,50f);
        stashService.addToStash(menu.getStash().getId(),List.of(ingredient));
        Stash  stash=stashRepository.findByIdFetchAggregate(menu.getStash().getId()).orElseThrow();
        Assertions.assertEquals(1,stash.getEntries().size());
        StashEntry stashEntry=stash.getEntries().stream().findFirst().orElseThrow();

        Assertions.assertEquals(stashEntry.getIngredientId(),ingredient.ingredient().getId());
        Assertions.assertEquals(stashEntry.getAmount(),ingredient.amount());
        Assertions.assertEquals(stashEntry.getUnit().name(),ingredient.unit().name());


        stash=stashRepository.findByIdFetchAggregate(menu.getStash().getId()).orElseThrow();
        IngredientUse overwrite= new IngredientUse(ingredients.get(0), IngredientUnit.GRAMS,1f);

        stashService.addToStash(stash.getId(),List.of(overwrite));
        stash=stashRepository.findByIdFetchAggregate(menu.getStash().getId()).orElseThrow();
        Assertions.assertEquals(1,stash.getEntries().size());
        stashEntry=stash.getEntries().stream().findFirst().orElseThrow();
        Assertions.assertEquals(stashEntry.getIngredientId(),overwrite.ingredient().getId());
        Assertions.assertEquals(stashEntry.getAmount().floatValue(),50+overwrite.amount());
        Assertions.assertEquals(stashEntry.getUnit().name(),overwrite.unit().name());
    }
    @Test
    public void subtractToStash() {
        Organization organization = organizationRepository.saveAndFlush(DefaultOrganizationTestData.defaultOrganization1());

        Menu menu = menuRepository.saveAndFlush(DefaultMenuTestData.defaultMenu1(organization.getId()));
        List<Ingredient> ingredients = ingredientRepository.saveAllAndFlush(DefaultIngredientTestData.getDefaultIngredients());

        IngredientUse ingredient = new IngredientUse(ingredients.get(0), IngredientUnit.GRAMS,50f);
        stashService.addToStash(menu.getStash().getId(),List.of(ingredient));
        Stash  stash=stashRepository.findByIdFetchAggregate(menu.getStash().getId()).orElseThrow();
        Assertions.assertEquals(1,stash.getEntries().size());
        StashEntry stashEntry=stash.getEntries().stream().findFirst().orElseThrow();

        Assertions.assertEquals(stashEntry.getIngredientId(),ingredient.ingredient().getId());
        Assertions.assertEquals(stashEntry.getAmount().floatValue(),ingredient.amount());
        Assertions.assertEquals(stashEntry.getUnit().name(),ingredient.unit().name());


        stash=stashRepository.findByIdFetchAggregate(menu.getStash().getId()).orElseThrow();
        IngredientUse overwrite= new IngredientUse(ingredients.get(0), IngredientUnit.GRAMS,-50f);

        stashService.addToStash(stash.getId(),List.of(overwrite));
        stash=stashRepository.findByIdFetchAggregate(menu.getStash().getId()).orElseThrow();
        Assertions.assertEquals(0,stash.getEntries().size());
    }

    @Test
    public void unableToConvertStash() {
        Organization organization = organizationRepository.saveAndFlush(DefaultOrganizationTestData.defaultOrganization1());

        Menu menu = menuRepository.saveAndFlush(DefaultMenuTestData.defaultMenu1(organization.getId()));
        List<Ingredient> ingredients = ingredientRepository.saveAllAndFlush(DefaultIngredientTestData.getDefaultIngredients());

        IngredientUse ingredient = new IngredientUse(ingredients.get(0), IngredientUnit.GRAMS,50f);
        stashService.addToStash(menu.getStash().getId(),List.of(ingredient));

        Stash  stash=stashRepository.findByIdFetchAggregate(menu.getStash().getId()).orElseThrow();
        Assertions.assertEquals(1,stash.getEntries().size());
        StashEntry stashEntry=stash.getEntries().stream().findFirst().orElseThrow();


        stash=stashRepository.findByIdFetchAggregate(menu.getStash().getId()).orElseThrow();
        IngredientUse overwrite= new IngredientUse(ingredients.get(0), IngredientUnit.LITRES,-50f);

            stashService.addToStash(stash.getId(),List.of(overwrite));
        stash=stashRepository.findByIdFetchAggregate(menu.getStash().getId()).orElseThrow();
        Assertions.assertEquals(1,stash.getEntries().size());
        Assertions.assertEquals(stashEntry.getAmount().floatValue(),ingredient.amount());

    }

}
