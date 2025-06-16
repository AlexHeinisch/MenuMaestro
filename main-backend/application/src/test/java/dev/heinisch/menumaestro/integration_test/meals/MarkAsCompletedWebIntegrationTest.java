package dev.heinisch.menumaestro.integration_test.meals;

import dev.heinisch.menumaestro.domain.account.Account;
import dev.heinisch.menumaestro.domain.cooking_appliance.CookingAppliance;
import dev.heinisch.menumaestro.domain.ingredient.Ingredient;
import dev.heinisch.menumaestro.domain.ingredient.IngredientUnit;
import dev.heinisch.menumaestro.domain.menu.Meal;
import dev.heinisch.menumaestro.domain.menu.Menu;
import dev.heinisch.menumaestro.domain.organization.Organization;
import dev.heinisch.menumaestro.domain.organization.OrganizationAccountRelation;
import dev.heinisch.menumaestro.domain.organization.OrganizationRole;
import dev.heinisch.menumaestro.domain.stash.Stash;
import dev.heinisch.menumaestro.domain.stash.StashEntry;
import dev.heinisch.menumaestro.integration_test.BaseWebIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.*;
import org.springframework.http.HttpStatus;

import java.util.List;

import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.defaultAccount;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultCookingApplianceTestData.defaultCookingAppliance1;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultIngredientTestData.defaultIngredient1;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultIngredientTestData.defaultIngredient2;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultMenuTestData.defaultMenu1;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultOrganizationTestData.defaultOrganization1;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultRecipeTestData.defaultRecipeCreateEditDto1;
import static org.assertj.core.api.Assertions.assertThat;

public class MarkAsCompletedWebIntegrationTest extends BaseWebIntegrationTest {
    private Organization organization1;
    private Ingredient ingredient1;
    private Ingredient ingredient2;
    private CookingAppliance appliance1;
    private Menu menu1;

    private RecipeDto recipeDto;
    private Account account;
    private MealDto meal1;
    private Stash menuStash;
    @Override
    protected String getBasePath() {
        return "/meals";
    }
    @BeforeEach
    public void setup() {
        organization1 = organizationRepository.saveAndFlush(defaultOrganization1());
        account = accountRepository.saveAndFlush(defaultAccount());
        organizationAccountRelationRepository.save(OrganizationAccountRelation.builder()
                .role(OrganizationRole.OWNER)
                .organization(organization1)
                .account(account)
                .build());
        ingredient1 = ingredientRepository.save(defaultIngredient1());
        ingredient2 = ingredientRepository.save(defaultIngredient2());
        appliance1 = cookingApplianceRepository.save(defaultCookingAppliance1());
        recipeDto = recipeService.createRecipe(defaultCreateEditRecipeDto());
        menu1=defaultMenu1(organization1.getId());
        menu1.setNumberOfPeople(1);
        menu1 = menuRepository.save(menu1);
        menuService.addMealToMenu(menu1.getId(), new AddMealToMenuRequest().recipeId(recipeDto.getId()));
        meal1 = mealService.getMealById(menuService.getMenuById(menu1.getId()).getMeals().get(0).getId());
        menuStash = menu1.getStash();
        menuStash.getEntries().add(StashEntry.builder()
                .stash(menuStash)
                .ingredientId(ingredient1.getId())
                .unit(IngredientUnit.KILOGRAMS)
                .amount(.1)
                .build());
        menuStash.getEntries().add(StashEntry.builder()
                .stash(menuStash)
                .ingredientId(ingredient2.getId())
                .unit(IngredientUnit.MILLILITRES)
                .amount(401.)
                .build());
        menuStash=stashRepository.saveAndFlush(menuStash);
        assertThat(menuStash.getEntries().size()).isEqualTo(2);

    }
    private RecipeCreateEditDto defaultCreateEditRecipeDto() {
        return defaultRecipeCreateEditDto1()
                .servings(1)
                .addCookingAppliancesItem(new CookingApplianceUseCreateEditDto()
                        .amount(1)
                        .id(appliance1.getId()))
                .addIngredientsItem(new IngredientUseCreateEditDto().
                        unit(IngredientUnitDto.KILOGRAMS)
                        .amount(1f)
                        .id(ingredient1.getId()))
                .addIngredientsItem(new IngredientUseCreateEditDto()
                        .unit(IngredientUnitDto.LITRES)
                        .amount(0.25f)
                        .id(ingredient2.getId()));
    }
    @Test
    void testMarkingSameStateTwice(){
        menuStash=stashRepository.findByIdFetchAggregate(menuStash.getId()).orElseThrow();
        Meal meal2=mealRepository.findById(meal1.getId()).orElseThrow();

        assertThat(menuStash.getEntries().size()).isEqualTo(2);

        var response=markMealByIdFails(meal2.getId(),false,false);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
    }
    @Test
    void testMarkingUnknown(){
        menuStash=stashRepository.findByIdFetchAggregate(menuStash.getId()).orElseThrow();
        Meal meal2=mealRepository.findById(meal1.getId()).orElseThrow();

        assertThat(menuStash.getEntries().size()).isEqualTo(2);

        var response=markMealByIdFails(meal2.getId()*50,false,false);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void testMarkingSuccessful_withStash(){
        menuStash=stashRepository.findByIdFetchAggregate(menuStash.getId()).orElseThrow();
        Meal meal2=mealRepository.findById(meal1.getId()).orElseThrow();

        assertThat(menuStash.getEntries().size()).isEqualTo(2);

        markMealByIdSuccessful(meal2.getId(),true,true);
        menuStash=stashRepository.findByIdFetchAggregate(menuStash.getId()).orElseThrow();

        assertThat(menuStash.getEntries().size()).isEqualTo(1);
        StashEntry stashEntry= menuStash.getEntries().stream().findFirst().orElseThrow();
        assertThat(stashEntry.getUnit()).isEqualTo(IngredientUnit.MILLILITRES);
        assertThat(stashEntry.getAmount()).isEqualTo(151., Offset.offset(0.0001));


    }
    @Test
    void testMarkingSuccessful_withStashAndDifferentUnits(){
        menuStash=stashRepository.findByIdFetchAggregate(menuStash.getId()).orElseThrow();
        menuStash.getEntries().clear();
        menuStash.getEntries().add(StashEntry.builder()
                .stash(menuStash)
                .ingredientId(ingredient1.getId())
                .unit(IngredientUnit.GRAMS)
                .amount(100.)
                .build());
        menuStash.getEntries().add(StashEntry.builder()
                .stash(menuStash)
                .ingredientId(ingredient2.getId())
                .unit(IngredientUnit.TABLESPOONS)
                .amount(100.)
                .build());
        menuStash=stashRepository.saveAndFlush(menuStash);

        Meal meal2=mealRepository.findById(meal1.getId()).orElseThrow();
        assertThat(menuStash.getEntries().size()).isEqualTo(2);

        markMealByIdSuccessful(meal2.getId(),true,true);
        menuStash=stashRepository.findByIdFetchAggregate(menuStash.getId()).orElseThrow();
        assertThat(menuStash.getEntries().size()).isEqualTo(1);
        StashEntry stashEntry= menuStash.getEntries().stream().findFirst().orElseThrow();

        assertThat(stashEntry.getUnit()).isEqualTo(IngredientUnit.CUPS);
        assertThat(stashEntry.getAmount()).isEqualTo(5.1875, Offset.offset(0.0001));
    }


    @Test
    void testMarkingSuccessful_withoutStash(){
        menuStash=stashRepository.findByIdFetchAggregate(menuStash.getId()).orElseThrow();
        assertThat(menuStash.getEntries().size()).isEqualTo(2);

        markMealByIdSuccessful(meal1.getId(),true,false);
        menuStash=stashRepository.findByIdFetchAggregate(menuStash.getId()).orElseThrow();

        assertThat(menuStash.getEntries().size()).isEqualTo(2);

    }
    @Test
    void testUnMarkingSuccessful_withStash(){
        menuStash=stashRepository.findByIdFetchAggregate(menuStash.getId()).orElseThrow();
        assertThat(menuStash.getEntries().size()).isEqualTo(2);

        markMealByIdSuccessful(meal1.getId(),true,false);
        markMealByIdSuccessful(meal1.getId(),false,true);
        menuStash=stashRepository.findByIdFetchAggregate(menuStash.getId()).orElseThrow();
        assertThat(menuStash.getEntries().size()).isEqualTo(2);

    }
    @Test
    void testUnMarkingSuccessful_withoutStash(){
        markMealByIdSuccessful(meal1.getId(),true,false);
        markMealByIdSuccessful(meal1.getId(),false,false);
        menuStash=stashRepository.findByIdFetchAggregate(menuStash.getId()).orElseThrow();
        assertThat(menuStash.getEntries().size()).isEqualTo(2);

    }



    private void markMealByIdSuccessful(Long id,boolean done, boolean deleteFromStash) {
        markMealById(id,done,deleteFromStash)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    private ErrorResponse markMealByIdFails(Long id,boolean done, boolean deleteFromStash) {
        return markMealByIdFails(
                id,
                done,
                deleteFromStash,
                new Headers(List.of(this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER"))))
        );
    }

    private ErrorResponse markMealByIdFails(Long id,boolean done, boolean deleteFromStash, Headers headers) {
        return markMealById(id,done,deleteFromStash, headers)
                .then()
                .extract()
                .as(ErrorResponse.class);
    }

    private Response markMealById(Long id,boolean done, boolean deleteFromStash) {
        return markMealById(id,done,deleteFromStash,
                new Headers(List.of(this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_ADMIN")))));
    }

    private Response markMealById(Long id,boolean done, boolean deleteFromStash, Headers headers) {
        return RestAssured
                .given()
                .headers(headers)
                .when()
                .put(URI + "/" + id+"/complete?done="+done+"&deleteFromStash="+deleteFromStash);
    }
}
