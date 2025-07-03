package dev.heinisch.menumaestro;

import dev.heinisch.menumaestro.mapper.CookingApplianceMapper;
import dev.heinisch.menumaestro.mapper.IngredientMapper;
import dev.heinisch.menumaestro.persistence.AccountRepository;
import dev.heinisch.menumaestro.persistence.CookingApplianceRepository;
import dev.heinisch.menumaestro.persistence.IngredientRepository;
import dev.heinisch.menumaestro.persistence.MealRepository;
import dev.heinisch.menumaestro.persistence.MenuRepository;
import dev.heinisch.menumaestro.persistence.OrganizationAccountRelationRepository;
import dev.heinisch.menumaestro.persistence.OrganizationRepository;
import dev.heinisch.menumaestro.persistence.RecipeRepository;
import dev.heinisch.menumaestro.persistence.ShoppingListRepository;
import dev.heinisch.menumaestro.persistence.StashRepository;
import dev.heinisch.menumaestro.properties.JwtProperties;
import dev.heinisch.menumaestro.service.AccountService;
import dev.heinisch.menumaestro.service.EmailService;
import dev.heinisch.menumaestro.service.JwtService;
import dev.heinisch.menumaestro.service.MealService;
import dev.heinisch.menumaestro.service.MenuService;
import dev.heinisch.menumaestro.service.OrganizationService;
import dev.heinisch.menumaestro.service.RecipeService;
import dev.heinisch.menumaestro.utils.DatabaseCleanerExtension;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@ActiveProfiles({"test", "datagen-off"})
@Nested
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(DatabaseCleanerExtension.class)
public abstract class BaseWebIntegrationTest {

    protected static String URI;

    @LocalServerPort
    private int port;

    @PostConstruct
    void init() {
        URI = "http://localhost:" + port + "/api/v1" + getBasePath();
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

    @MockitoBean
    protected EmailService emailService;

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
