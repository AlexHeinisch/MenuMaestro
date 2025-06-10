package at.codemaestro.integration_test;

import at.codemaestro.mapper.CookingApplianceMapper;
import at.codemaestro.mapper.IngredientMapper;
import at.codemaestro.persistence.AccountRepository;
import at.codemaestro.persistence.CookingApplianceRepository;
import at.codemaestro.persistence.IngredientRepository;
import at.codemaestro.persistence.MealRepository;
import at.codemaestro.persistence.MenuRepository;
import at.codemaestro.persistence.OrganizationAccountRelationRepository;
import at.codemaestro.persistence.OrganizationRepository;
import at.codemaestro.persistence.RecipeRepository;
import at.codemaestro.persistence.ShoppingListRepository;
import at.codemaestro.persistence.StashRepository;
import at.codemaestro.properties.JwtProperties;
import at.codemaestro.service.AccountService;
import at.codemaestro.service.JwtService;
import at.codemaestro.service.MealService;
import at.codemaestro.service.MenuService;
import at.codemaestro.service.OrganizationService;
import at.codemaestro.service.RecipeService;
import at.codemaestro.test_support.DatabaseCleanerExtension;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.parsing.Parser;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@ActiveProfiles({"test", "datagen-off"})
@Nested
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(DatabaseCleanerExtension.class)
public class BaseWebIntegrationTest {

    protected static String URI;

    @LocalServerPort
    private int port;

    @PostConstruct
    void init() {
        URI = "http://localhost:" + port + getBasePath();
    }

    @BeforeAll
    static void defaultParser() {
        RestAssured.defaultParser = Parser.JSON;
    }

    protected String getBasePath() {
        return "";
    }

    protected Header generateValidAuthorizationHeader(String username, List<String> authorityStrings) {
        HashMap<String, Object> authorities = new HashMap<>();
        authorities.put(jwtProperties.getAccountAccessToken().getRoleClaimName(), authorityStrings);
        return new Header("Authorization", "Bearer " + jwtService.generateAccountAccessToken(authorities, username, new Date(System.currentTimeMillis()+10 * 60 * 1000L)));
    }

    @Autowired
    protected JwtService jwtService;

    @Autowired
    protected JwtProperties jwtProperties;

    @Autowired
    protected IngredientRepository ingredientRepository;

    @Autowired
    protected IngredientMapper ingredientMapper;

    @Autowired
    protected OrganizationRepository organizationRepository;

    @Autowired
    protected CookingApplianceRepository cookingApplianceRepository;

    @Autowired
    protected RecipeService recipeService;

    @Autowired
    protected MenuRepository menuRepository;

    @Autowired
    protected AccountRepository accountRepository;

    @Autowired
    protected MenuService menuService;

    @Autowired
    protected MealService mealService;

    @Autowired
    protected MealRepository mealRepository;

    @Autowired
    protected OrganizationAccountRelationRepository organizationAccountRelationRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected CookingApplianceMapper cookingApplianceMapper;

    @Autowired
    protected RecipeRepository recipeRepository;

    @Autowired
    protected PlatformTransactionManager txManager;

    @Autowired
    protected ShoppingListRepository shoppingListRepository;

    @Autowired
    protected StashRepository stashRepository;

    @Autowired
    protected AccountService accountService;

    @Autowired
    protected OrganizationService organizationService;

}
