package dev.heinisch.menumaestro.integration_test.menu;

import dev.heinisch.menumaestro.domain.account.Account;
import dev.heinisch.menumaestro.domain.cooking_appliance.CookingAppliance;
import dev.heinisch.menumaestro.domain.ingredient.Ingredient;
import dev.heinisch.menumaestro.domain.menu.Menu;
import dev.heinisch.menumaestro.domain.menu.MenuStatus;
import dev.heinisch.menumaestro.domain.organization.Organization;
import dev.heinisch.menumaestro.domain.organization.OrganizationAccountRelation;
import dev.heinisch.menumaestro.domain.organization.OrganizationRole;
import dev.heinisch.menumaestro.integration_test.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.integration_test.utils.ErrorResponseAssert;
import dev.heinisch.menumaestro.integration_test.utils.RestHelper;
import io.restassured.http.Headers;
import io.restassured.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.AddMealToMenuRequest;
import org.openapitools.model.CookingApplianceUseCreateEditDto;
import org.openapitools.model.IngredientUnitDto;
import org.openapitools.model.IngredientUseCreateEditDto;
import org.openapitools.model.RecipeCreateEditDto;
import org.openapitools.model.RecipeDto;
import org.openapitools.model.SnapshotCreateDto;
import org.springframework.http.HttpStatus;

import java.util.List;

import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME_2;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultAccountTestData.defaultAccount;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultCookingApplianceTestData.defaultCookingAppliance1;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultIngredientTestData.defaultIngredient1;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultIngredientTestData.defaultIngredient2;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultMenuTestData.defaultMenu1;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultMenuTestData.defaultSnapshotCreateDto1;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultOrganizationTestData.defaultOrganization1;
import static dev.heinisch.menumaestro.integration_test.utils.test_constants.DefaultRecipeTestData.defaultRecipeCreateEditDto1;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class AddSnapshotToMenuWebIntegrationTest extends BaseWebIntegrationTest {


    private Organization organization1;
    private Ingredient ingredient1;
    private Ingredient ingredient2;
    private CookingAppliance appliance1;
    private Menu menu1;

    private RecipeDto recipeDto;
    private Account account1;

    private RestHelper.PathAndBodyWithoutReturnRestHelper<Long, SnapshotCreateDto> rest;

    @PostConstruct
    private void initRestHelper() {
        rest = new RestHelper.PathAndBodyWithoutReturnRestHelper<>(
                this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER")),
                Method.POST,
                URI + "/{id}/snapshots",
                HttpStatus.NO_CONTENT
        );
    }

    @Override
    protected String getBasePath() {
        return "/menus";
    }

    @BeforeEach
    public void setup() {
        organization1 = organizationRepository.saveAndFlush(defaultOrganization1());
        account1 = accountRepository.saveAndFlush(defaultAccount());
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation.builder()
                .role(OrganizationRole.OWNER)
                .organization(organization1)
                .account(account1)
                .build());
        ingredient1 = ingredientRepository.saveAndFlush(defaultIngredient1());
        ingredient2 = ingredientRepository.saveAndFlush(defaultIngredient2());
        appliance1 = cookingApplianceRepository.saveAndFlush(defaultCookingAppliance1());
        recipeDto = recipeService.createRecipe(defaultCreateEditRecipeDto());
        menu1 = menuRepository.saveAndFlush(defaultMenu1(organization1.getId()));
        menuService.addMealToMenu(menu1.getId(), new AddMealToMenuRequest().recipeId(recipeDto.getId()));
    }

    @Test
    void whenAddSnapshotToMenu_withValidData_thenOk() {
        assertThat(menuService.getMenuById(menu1.getId()).getSnapshots()).hasSize(0);
        rest.requestSuccessful(menu1.getId(), defaultSnapshotCreateDto());
        assertThat(menuService.getMenuById(menu1.getId()).getSnapshots()).hasSize(1);
    }

    @Test
    void whenAddSnapshotToMenu_withNonExistentMenu_thenNotFound() {
        ErrorResponseAssert.assertThat(rest.requestFails(-66L, defaultSnapshotCreateDto(), HttpStatus.NOT_FOUND))
                .hasStatus(HttpStatus.NOT_FOUND)
                .messageContains("Menu")
                .messageContains("not found");
    }

    @Test
    void whenAddSnapshotToMenu_withMenuClosed_thenValidationException() {
        menu1.setStatus(MenuStatus.CLOSED);
        menuRepository.saveAndFlush(menu1);

        ErrorResponseAssert.assertThat(rest.requestFails(menu1.getId(), defaultSnapshotCreateDto(), HttpStatus.UNPROCESSABLE_ENTITY))
            .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
            .messageContains("Menu was closed, it cannot be changed!");
    }



    @Test
    void whenAddSnapshotToMenu_withTooLongSnapshotName_thenUnprocessableEntity() {
        ErrorResponseAssert.assertThat(rest.requestFails(menu1.getId(), defaultSnapshotCreateDto().name("HELLO".repeat(30)), HttpStatus.UNPROCESSABLE_ENTITY))
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error")
                .detailsContainSubstring("too long")
                .detailsHaveSize(1);
    }

    @Test
    void whenAddSnapshotToMenu_withBlankSnapshotName_thenUnprocessableEntity() {
        ErrorResponseAssert.assertThat(rest.requestFails(menu1.getId(), defaultSnapshotCreateDto().name(""), HttpStatus.UNPROCESSABLE_ENTITY))
                .hasStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .messageContains("Validation error")
                .detailsContainSubstring("blank")
                .detailsHaveSize(1);
    }

    @Test
    void whenAddSnapshotToMenu_withAlreadyExistingSnapshotName_thenUnprocessableEntity() {
        menuService.addSnapshotToMenu(menu1.getId(), defaultSnapshotCreateDto());
        ErrorResponseAssert.assertThat(rest.requestFails(menu1.getId(), defaultSnapshotCreateDto(), HttpStatus.CONFLICT))
                .hasStatus(HttpStatus.CONFLICT)
                .messageContains("already exists");
    }

    @Test
    void whenAddSnapshotToMenu_withNotInOrganisation_thenForbidden() {
        var errorResponse = rest.requestFails(menu1.getId(),
                defaultSnapshotCreateDto(),
                HttpStatus.FORBIDDEN,
                new Headers(generateValidAuthorizationHeader(DEFAULT_USERNAME_2, List.of("ROLE_USER"))));
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("not in the required organization");
    }

    @Test
    void whenAddSnapshotToMenu_withNotEnoughPermissions_thenForbidden() {
        organizationAccountRelationRepository.deleteAll();
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation.builder()
                .role(OrganizationRole.SHOPPER)
                .organization(organization1)
                .account(account1)
                .build()
        );

        var errorResponse = rest.requestFails(menu1.getId(), defaultSnapshotCreateDto(), HttpStatus.FORBIDDEN);
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("not enough permissions")
                .messageContains("PLANNER");
    }

    @Test
    void whenAddSnapshotToMenu_withNotInOrganisationButAdmin_thenOk() {
        assertThat(menuService.getMenuById(menu1.getId()).getSnapshots()).hasSize(0);
        rest.requestSuccessful(
                menu1.getId(),
                defaultSnapshotCreateDto(),
                new Headers(generateValidAuthorizationHeader("someuser", List.of("ROLE_ADMIN")))
        );
        assertThat(menuService.getMenuById(menu1.getId()).getSnapshots()).hasSize(1);
    }

    @Test
    void basicAuthTests() {
        rest.basicAuthTests(menu1.getId(), defaultSnapshotCreateDto());
    }

    private RecipeCreateEditDto defaultCreateEditRecipeDto() {
        return defaultRecipeCreateEditDto1()
                .author(DEFAULT_USERNAME)
                .addCookingAppliancesItem(new CookingApplianceUseCreateEditDto()
                        .amount(1)
                        .id(appliance1.getId()))
                .addIngredientsItem(new IngredientUseCreateEditDto().
                        unit(IngredientUnitDto.GRAMS)
                        .amount(250f)
                        .id(ingredient1.getId()))
                .addIngredientsItem(new IngredientUseCreateEditDto()
                        .unit(IngredientUnitDto.LITRES)
                        .amount(0.25f)
                        .id(ingredient2.getId()));
    }

    private SnapshotCreateDto defaultSnapshotCreateDto() {
        return defaultSnapshotCreateDto1().position(1);
    }
}
