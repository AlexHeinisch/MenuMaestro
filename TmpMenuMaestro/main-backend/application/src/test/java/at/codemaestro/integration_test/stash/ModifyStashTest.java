package at.codemaestro.integration_test.stash;

import at.codemaestro.domain.ingredient.Ingredient;
import at.codemaestro.domain.ingredient.IngredientUnit;
import at.codemaestro.domain.ingredient_computation.IngredientUse;
import at.codemaestro.domain.menu.Menu;
import at.codemaestro.domain.organization.Organization;
import at.codemaestro.domain.stash.Stash;
import at.codemaestro.domain.stash.StashEntry;
import at.codemaestro.integration_test.BaseWebIntegrationTest;
import at.codemaestro.integration_test.utils.test_constants.DefaultIngredientTestData;
import at.codemaestro.integration_test.utils.test_constants.DefaultMenuTestData;
import at.codemaestro.integration_test.utils.test_constants.DefaultOrganizationTestData;
import at.codemaestro.service.StashService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openapitools.model.IngredientUnitDto;
import org.openapitools.model.IngredientUseCreateEditDto;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ModifyStashTest extends BaseWebIntegrationTest {

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
