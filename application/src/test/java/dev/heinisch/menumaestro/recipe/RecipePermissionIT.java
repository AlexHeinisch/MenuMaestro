package dev.heinisch.menumaestro.recipe;

import dev.heinisch.menumaestro.domain.account.Account;
import dev.heinisch.menumaestro.domain.cooking_appliance.CookingAppliance;
import dev.heinisch.menumaestro.domain.ingredient.Ingredient;
import dev.heinisch.menumaestro.domain.organization.Organization;
import dev.heinisch.menumaestro.domain.organization.OrganizationAccountRelation;
import dev.heinisch.menumaestro.domain.organization.OrganizationRole;
import dev.heinisch.menumaestro.exceptions.ForbiddenException;
import dev.heinisch.menumaestro.BaseWebIntegrationTest;
import dev.heinisch.menumaestro.utils.ErrorResponseAssert;
import dev.heinisch.menumaestro.utils.TestPageableResponse;
import dev.heinisch.menumaestro.utils.test_constants.DefaultCookingApplianceTestData;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.openapitools.model.CookingApplianceUseCreateEditDto;
import org.openapitools.model.ErrorResponse;
import org.openapitools.model.IngredientUnitDto;
import org.openapitools.model.IngredientUseCreateEditDto;
import org.openapitools.model.RecipeCreateEditDto;
import org.openapitools.model.RecipeDto;
import org.openapitools.model.RecipeVisibility;
import org.springframework.http.HttpStatus;

import java.util.List;

import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.DEFAULT_USERNAME;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultAccountTestData.defaultAccount;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultCookingApplianceTestData.defaultCookingAppliance1;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultIngredientTestData.defaultIngredient1;
import static dev.heinisch.menumaestro.utils.test_constants.DefaultRecipeTestData.defaultRecipeCreateEditDto1;

public class RecipePermissionIT extends BaseWebIntegrationTest {

    private static final String TEST_USER = "test_user";
    private static final String ADMIN_USER = "admin_user";

    @Override
    protected String getBasePath() {
        return "/recipes";
    }

    @BeforeEach
    void setup() {
        accountRepository.saveAndFlush(defaultAccount());

        accountRepository.saveAndFlush(Account.builder()
                .username(TEST_USER)
                .email("test@test.com")
                .firstName("Test")
                .lastName("Test")
                .isGlobalAdmin(false)
                .passwordHash("")
                .build());

        accountRepository.saveAndFlush(Account.builder()
                .username(ADMIN_USER)
                .email("admin@test.com")
                .firstName("Admin")
                .lastName("Admin")
                .isGlobalAdmin(true)
                .passwordHash("")
                .build());
    }

    @AfterEach
    void teardown() {
        organizationAccountRelationRepository.deleteAll();

        accountRepository.deleteById(DEFAULT_USERNAME);
        accountRepository.deleteById(TEST_USER);
        accountRepository.deleteById(ADMIN_USER);
    }

    private Response createRecipe(RecipeCreateEditDto dto) {
        return createRecipe(dto, new Headers(List.of(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER")))));
    }

    private Response createRecipe(RecipeCreateEditDto dto, Headers headers) {
        return RestAssured
                .given()
                .contentType("application/json")
                .headers(headers == null ? new Headers() : headers)
                .body(dto)
                .post(URI);
    }

    private RecipeDto createRecipeSuccessful(RecipeCreateEditDto dto) {
        return createRecipe(dto)
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(RecipeDto.class);
    }

    private ErrorResponse createRecipeFails(RecipeCreateEditDto dto, HttpStatus status, Headers headers) {
        return createRecipe(dto, headers)
                .then()
                .statusCode(status.value())
                .extract()
                .as(ErrorResponse.class);
    }

    private Response getRecipeById(Long id) {
        return getRecipeById(id, new Headers(List.of(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER")))));
    }

    private Response getRecipeById(Long id, Headers headers) {
        return RestAssured
                .given()
                .contentType("application/json")
                .headers(headers == null ? new Headers() : headers)
                .get(URI + "/" + id);
    }

    private RecipeDto getRecipeByIdSuccessful(Long id) {
        return getRecipeById(id)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(RecipeDto.class);
    }

    private ErrorResponse getRecipeByIdFails(Long id, HttpStatus status, Headers headers) {
        return getRecipeById(id, headers)
                .then()
                .statusCode(status.value())
                .extract()
                .as(ErrorResponse.class);
    }

    @Test
    void testUnauthorizedUserCannotCreateRecipe() {
        ErrorResponse errorResponse = createRecipeFails(defaultCreateEditRecipeDto(), HttpStatus.FORBIDDEN, new Headers());
        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("Access Denied");
    }

    @Test
    void testUserCanCreateAndAccessOwnPrivateRecipe() {
        RecipeCreateEditDto dto = defaultCreateEditRecipeDto()
                .visibility(RecipeVisibility.PRIVATE)
                .author(DEFAULT_USERNAME);

        RecipeDto createdRecipe = createRecipeSuccessful(dto);
        RecipeDto retrievedRecipe = getRecipeByIdSuccessful(createdRecipe.getId());

        Assertions.assertAll(
                () -> Assertions.assertEquals(createdRecipe.getId(), retrievedRecipe.getId()),
                () -> Assertions.assertEquals(dto.getName(), retrievedRecipe.getName()),
                () -> Assertions.assertEquals(dto.getDescription(), retrievedRecipe.getDescription()),
                () -> Assertions.assertEquals(dto.getAuthor(), retrievedRecipe.getAuthor()),
                () -> Assertions.assertEquals(dto.getVisibility(), retrievedRecipe.getVisibility())
        );
    }

    @Test
    void testOtherUserCannotAccessPrivateRecipe() {
        RecipeCreateEditDto dto = defaultCreateEditRecipeDto()
                .visibility(RecipeVisibility.PRIVATE)
                .author(DEFAULT_USERNAME);

        RecipeDto createdRecipe = createRecipeSuccessful(dto);

        ErrorResponse errorResponse = getRecipeByIdFails(createdRecipe.getId(), HttpStatus.FORBIDDEN,
                new Headers(List.of(generateValidAuthorizationHeader(TEST_USER, List.of("ROLE_USER")))));

        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("Account 'test_user' is not the author of this private recipe");
    }

    @Test
    void testAdminCanAccessPrivateRecipe() {
        RecipeCreateEditDto dto = defaultCreateEditRecipeDto()
                .visibility(RecipeVisibility.PRIVATE)
                .author(DEFAULT_USERNAME);

        RecipeDto createdRecipe = createRecipeSuccessful(dto);

        Response response = getRecipeById(createdRecipe.getId(),
                new Headers(List.of(generateValidAuthorizationHeader(ADMIN_USER, List.of("ROLE_ADMIN")))));

        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatusCode());
    }

    @Test
    void testAnyUserCanAccessPublicRecipe() {
        RecipeCreateEditDto dto = defaultCreateEditRecipeDto()
                .visibility(RecipeVisibility.PUBLIC)
                .author(DEFAULT_USERNAME);

        RecipeDto createdRecipe = createRecipeSuccessful(dto);

        Response response = getRecipeById(createdRecipe.getId(),
                new Headers(List.of(generateValidAuthorizationHeader(TEST_USER, List.of("ROLE_USER")))));

        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatusCode());
    }

    private Response editRecipe(Long id, RecipeCreateEditDto dto) {
        return editRecipe(id, dto, new Headers(List.of(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER")))));
    }

    private Response editRecipe(Long id, RecipeCreateEditDto dto, Headers headers) {
        return RestAssured
                .given()
                .contentType("application/json")
                .headers(headers == null ? new Headers() : headers)
                .body(dto)
                .put(URI + "/" + id);
    }

    private RecipeDto editRecipeSuccessful(Long id, RecipeCreateEditDto dto) {
        return editRecipe(id, dto)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(RecipeDto.class);
    }

    private ErrorResponse editRecipeFails(Long id, RecipeCreateEditDto dto, HttpStatus status, Headers headers) {
        return editRecipe(id, dto, headers)
                .then()
                .statusCode(status.value())
                .extract()
                .as(ErrorResponse.class);
    }

    @Test
    void testUserCanEditOwnRecipe() {
        RecipeCreateEditDto createDto = defaultCreateEditRecipeDto()
                .author(DEFAULT_USERNAME);
        RecipeDto createdRecipe = createRecipeSuccessful(createDto);

        RecipeCreateEditDto editDto = defaultCreateEditRecipeDto()
                .name("New name");
        RecipeDto editedRecipe = editRecipeSuccessful(createdRecipe.getId(), editDto);

        Assertions.assertAll(
                () -> Assertions.assertEquals(createdRecipe.getId(), editedRecipe.getId()),
                () -> Assertions.assertEquals(editDto.getName(), editedRecipe.getName()),
                () -> Assertions.assertEquals(editDto.getDescription(), editedRecipe.getDescription()),
                () -> Assertions.assertEquals(editDto.getServings(), editedRecipe.getServings()),
                () -> Assertions.assertEquals(editDto.getVisibility(), editedRecipe.getVisibility())
        );
    }

    @Test
    void testOtherUserCannotEditRecipe() {
        RecipeCreateEditDto createDto = defaultCreateEditRecipeDto()
                .author(DEFAULT_USERNAME);
        RecipeDto createdRecipe = createRecipeSuccessful(createDto);

        RecipeCreateEditDto editDto = defaultCreateEditRecipeDto()
                .name("Newname")
                .author(TEST_USER);
        ErrorResponse errorResponse = editRecipeFails(createdRecipe.getId(), editDto, HttpStatus.FORBIDDEN,
                new Headers(List.of(generateValidAuthorizationHeader(TEST_USER, List.of("ROLE_USER")))));

        ErrorResponseAssert.assertThat(errorResponse)
                .hasStatus(HttpStatus.FORBIDDEN)
                .messageContains("Access Denied");
    }

    @Test
    void testAdminCanEditAnyRecipe() {
        RecipeCreateEditDto createDto = defaultCreateEditRecipeDto()
                .author(DEFAULT_USERNAME);
        RecipeDto createdRecipe = createRecipeSuccessful(createDto);

        RecipeCreateEditDto editDto = defaultCreateEditRecipeDto()
                .name("New name");
        Response response = editRecipe(createdRecipe.getId(), editDto,
                new Headers(List.of(generateValidAuthorizationHeader(ADMIN_USER, List.of("ROLE_ADMIN")))));

        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatusCode());
    }


    private TestPageableResponse<RecipeDto> getRecipesSuccessful(String query) {
        return getRecipes(query)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<TestPageableResponse<RecipeDto>>() {
                });
    }

    private Response getRecipes(String query) {
        return getRecipes(
                query,
                new Headers(this.generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER", "ROLE_ADMIN")))
        );
    }

    private Response getRecipes(String query, Headers headers) {
        return RestAssured
                .given()
                .headers(headers)
                .when()
                .get(URI + (StringUtils.isBlank(query) ? "" : "?" + query));
    }

    @Test
    void testSearchRecipes_UserCanSeeOwnPrivateRecipes() {
        RecipeCreateEditDto privateDto = defaultCreateEditRecipeDto()
                .visibility(RecipeVisibility.PRIVATE)
                .author(DEFAULT_USERNAME)
                .name("Private Recipe");

        createRecipeSuccessful(privateDto);

        TestPageableResponse<RecipeDto> result = getRecipesSuccessful("Private Recipe");

        Assertions.assertEquals(result.getContent().size(), 1);
    }

    @Test
    void testSearchRecipes_UserCannotSeeOthersPrivateRecipes() {
        RecipeCreateEditDto privateDto = defaultCreateEditRecipeDto()
                .visibility(RecipeVisibility.PRIVATE)
                .author(DEFAULT_USERNAME)
                .name("Private Recipe");

        createRecipeSuccessful(privateDto);
        var result = getRecipes("Private",
                new Headers(List.of(generateValidAuthorizationHeader(TEST_USER, List.of("ROLE_USER")))))
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<TestPageableResponse<RecipeDto>>() {
                });

        Assertions.assertEquals(result.getContent().size(), 0);
    }

    @Test
    void testSearchRecipes_AdminCanSeeAllRecipes() {
        RecipeCreateEditDto privateDto = defaultCreateEditRecipeDto()
                .visibility(RecipeVisibility.PRIVATE)
                .author(DEFAULT_USERNAME)
                .name("Private Recipe");

        createRecipeSuccessful(privateDto);

        var result = getRecipes("Private",
                new Headers(List.of(generateValidAuthorizationHeader(ADMIN_USER, List.of("ROLE_ADMIN")))))
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<TestPageableResponse<RecipeDto>>() {
                });

        Assertions.assertEquals(1, result.getContent().size());
    }

    @Test
    void testSearchRecipes_EveryoneCanSeePublicRecipes() {
        RecipeCreateEditDto publicDto = defaultCreateEditRecipeDto()
                .visibility(RecipeVisibility.PUBLIC)
                .author(DEFAULT_USERNAME)
                .name("Public Recipe");

        createRecipeSuccessful(publicDto);

        var result = getRecipes("Public",
                new Headers(List.of(generateValidAuthorizationHeader(TEST_USER, List.of("ROLE_USER")))))
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<TestPageableResponse<RecipeDto>>() {
                });

        Assertions.assertEquals(result.getContent().size(), 1);
    }

    @Test
    void testSearchRecipes_UnauthorizedUserCanSearchPublicRecipes() {
        RecipeCreateEditDto publicDto = defaultCreateEditRecipeDto()
                .visibility(RecipeVisibility.PUBLIC)
                .author(DEFAULT_USERNAME)
                .name("Public Recipe");
        createRecipeSuccessful(publicDto);

        var result = getRecipes("Public Recipe", new Headers())
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<TestPageableResponse<RecipeDto>>() {
                });

        Assertions.assertEquals(1, result.getContent().size());
    }

    @Test
    void testSearchRecipes_OrganizationVisibility() {
        Organization org = organizationRepository.saveAndFlush(Organization.builder()
                .name("Test Organization")
                .description("Organization Description")
                .build());

        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation.builder()
                .organization(org)
                .role(OrganizationRole.MEMBER)
                .account(accountRepository.findById(TEST_USER).get())
                .build());
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation.builder()
                .organization(org)
                .role(OrganizationRole.OWNER)
                .account(accountRepository.findById(ADMIN_USER).get())
                .build());

        RecipeCreateEditDto orgRecipe = defaultCreateEditRecipeDto()
                .visibility(RecipeVisibility.ORGANIZATION)
                .author(ADMIN_USER)
                .name("Organization Recipe");

        createRecipeSuccessful(orgRecipe);

        var orgUserResult = getRecipes("Organization Recipe",
                new Headers(List.of(generateValidAuthorizationHeader(TEST_USER, List.of("ROLE_USER")))))
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<TestPageableResponse<RecipeDto>>() {
                });

        // User not in organization cannot see the recipe
        var nonOrgUserResult = getRecipes("Organization Recipe",
                new Headers(List.of(generateValidAuthorizationHeader(DEFAULT_USERNAME, List.of("ROLE_USER")))))
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<TestPageableResponse<RecipeDto>>() {
                });

        Assertions.assertAll(
                () -> Assertions.assertEquals(1, orgUserResult.getContent().size()),
                () -> Assertions.assertEquals(0, nonOrgUserResult.getContent().size())
        );

    }

    @Test
    void testSearchRecipes_MultipleOrganizations() {
        Organization org1 = organizationRepository.saveAndFlush(Organization.builder()
                .name("Organization 1")
                .description("Organization Description")
                .build());

        Organization org2 = organizationRepository.saveAndFlush(Organization.builder()
                .name("Organization 2")
                .description("Organization Description")
                .build());

        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation.builder()
                .organization(org1)
                .role(OrganizationRole.MEMBER)
                .account(accountRepository.findById(TEST_USER).get())
                .build());
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation.builder()
                .organization(org2)
                .role(OrganizationRole.MEMBER)
                .account(accountRepository.findById(DEFAULT_USERNAME).get())
                .build());
        RecipeCreateEditDto recipe1 = defaultCreateEditRecipeDto()
                .visibility(RecipeVisibility.ORGANIZATION)
                .author(TEST_USER)
                .name("Org1 Recipe");

        RecipeCreateEditDto recipe2 = defaultCreateEditRecipeDto()
                .visibility(RecipeVisibility.ORGANIZATION)
                .author(DEFAULT_USERNAME)
                .name("Org2 Recipe");

        createRecipeSuccessful(recipe1);
        createRecipeSuccessful(recipe2);

        var result = getRecipes("Recipe",
                new Headers(List.of(generateValidAuthorizationHeader(TEST_USER, List.of("ROLE_USER")))))
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<TestPageableResponse<RecipeDto>>() {
                });

        Assertions.assertAll(
                () -> Assertions.assertEquals(1, result.getContent().size()),
                () -> Assertions.assertEquals("Org1 Recipe", result.getContent().get(0).getName())
        );
    }

    @Test
    void testSearchRecipes_AdminCanSeeAllOrganizationRecipes() {
        Organization org = organizationRepository.saveAndFlush(Organization.builder()
                .name("Test Organization")
                .description("Organization Description")
                .build());
        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation.builder()
                .organization(org)
                .role(OrganizationRole.MEMBER)
                .account(accountRepository.findById(DEFAULT_USERNAME).get())
                .build());

        RecipeCreateEditDto orgRecipe = defaultCreateEditRecipeDto()
                .visibility(RecipeVisibility.ORGANIZATION)
                .author(DEFAULT_USERNAME)
                .name("Organization Recipe");

        createRecipeSuccessful(orgRecipe);

        var adminResult = getRecipes("Organization Recipe",
                new Headers(List.of(generateValidAuthorizationHeader(ADMIN_USER, List.of("ROLE_ADMIN")))))
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<TestPageableResponse<RecipeDto>>() {
                });

        Assertions.assertEquals(1, adminResult.getContent().size(), "Admin should see organization recipes");
    }

    @Test
    void testSearchRecipes_userWithoutOrganizationNotBreakingEverything() {
        RecipeCreateEditDto orgRecipe = defaultCreateEditRecipeDto()
                .visibility(RecipeVisibility.ORGANIZATION)
                .author(TEST_USER)
                .name("Organization Recipe");
        // User is not in any organization, he should still see the recipe  he created
        // When he later adds an org they will see the recipe
        createRecipeSuccessful(orgRecipe);

        var adminResult = getRecipes("Organization Recipe",
                new Headers(List.of(generateValidAuthorizationHeader(TEST_USER, List.of("ROLE_USER")))))
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<TestPageableResponse<RecipeDto>>() {
                });

        Assertions.assertEquals(1, adminResult.getContent().size());
    }

    @Test
    void testSearchRecipes_MixedVisibilityWithOrganization() {
        Organization org = organizationRepository.saveAndFlush(Organization.builder()
                .name("Test Organization")
                .description("Organization Description")
                .build());

        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation.builder()
                .organization(org)
                .role(OrganizationRole.MEMBER)
                .account(accountRepository.findById(TEST_USER).get())
                .build());

        organizationAccountRelationRepository.saveAndFlush(OrganizationAccountRelation.builder()
                .organization(org)
                .role(OrganizationRole.MEMBER)
                .account(accountRepository.findById(DEFAULT_USERNAME).get())
                .build());

        RecipeCreateEditDto publicRecipe = defaultCreateEditRecipeDto()
                .visibility(RecipeVisibility.PUBLIC)
                .author(DEFAULT_USERNAME)
                .name("Public Recipe");

        RecipeCreateEditDto privateRecipe = defaultCreateEditRecipeDto()
                .visibility(RecipeVisibility.PRIVATE)
                .author(DEFAULT_USERNAME)
                .name("Private Recipe");

        RecipeCreateEditDto orgRecipe = defaultCreateEditRecipeDto()
                .visibility(RecipeVisibility.ORGANIZATION)
                .author(DEFAULT_USERNAME)
                .name("Organization Recipe");

        createRecipeSuccessful(publicRecipe);
        createRecipeSuccessful(privateRecipe);
        createRecipeSuccessful(orgRecipe);

        var orgMemberResult = getRecipes("Recipe",
                new Headers(List.of(generateValidAuthorizationHeader(TEST_USER, List.of("ROLE_USER")))))
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(new TypeRef<TestPageableResponse<RecipeDto>>() {
                });

        Assertions.assertEquals(2, orgMemberResult.getContent().size());
    }
    @Test
    void testHasAccessToRecipe(){
        RecipeCreateEditDto defaultRecipe = defaultCreateEditRecipeDto();
        defaultRecipe.author("Unknown user");
        defaultRecipe.setVisibility(RecipeVisibility.PRIVATE);
        RecipeDto recipeWithUnkownuser = recipeService.createRecipe(defaultRecipe);
        Assertions.assertThrows(ForbiddenException.class,() ->recipeService.hasAccessToRecipe(recipeWithUnkownuser.getId(),"username")) ;
        defaultRecipe = defaultCreateEditRecipeDto();
        defaultRecipe.setVisibility(RecipeVisibility.PRIVATE);

        RecipeDto privateRecipe = recipeService.createRecipe(defaultRecipe);

        Assertions.assertThrows(ForbiddenException.class,() ->recipeService.hasAccessToRecipe(privateRecipe.getId(),"unkown user")) ;


    }
    private RecipeCreateEditDto defaultCreateEditRecipeDto() {
        Ingredient ingredient = ingredientRepository.saveAndFlush(defaultIngredient1());
        CookingAppliance appliance = cookingApplianceRepository.saveAndFlush(defaultCookingAppliance1());

        DefaultCookingApplianceTestData.defaultCookingAppliance1();
        return defaultRecipeCreateEditDto1()
                .addCookingAppliancesItem(new CookingApplianceUseCreateEditDto()
                        .amount(1)
                        .id(appliance.getId()))
                .addIngredientsItem(new IngredientUseCreateEditDto()
                        .unit(IngredientUnitDto.LITRES)
                        .amount(0.25f)
                        .id(ingredient.getId()));
    }

}
