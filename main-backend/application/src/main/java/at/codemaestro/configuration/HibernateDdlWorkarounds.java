package at.codemaestro.configuration;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;
import java.util.Objects;

/**
 * Performs additional ddl that is not possible with hibernate basic schema management.
 * In particular hibernate will not create foreign keys when there is no jpa relation such as {@code ManyToOne}
 * Thus this system adds such constraints after hibernate otherwise initialized the database schema.
 * <br>
 * In a production environment, hibernate schema management and this component would be replaced by liquibase,
 * where the developer has the necessary freedom to create the schema.
 * <br>
 * Hibernate ddl mode: this class tries to recognize if it should execute based on hibernate ddl mode.
 */
@Slf4j
@Component
public class HibernateDdlWorkarounds {

    public static final String[] CUSTOM_DDL = new String[]{
            "ALTER TABLE menu ADD CONSTRAINT fk_menu_organization_id FOREIGN KEY (organization_id) REFERENCES organization(id)",
            "ALTER TABLE shopping_list_item ADD CONSTRAINT fk_shopping_list_ingredient_id FOREIGN KEY (ingredient_id) REFERENCES ingredient(id)",
            "ALTER TABLE recipe_value ADD CONSTRAINT fk_recipe_value_image_id FOREIGN KEY (image_id) REFERENCES image_record(id)",
            "ALTER TABLE stash_entry ADD CONSTRAINT fk_stash_entry_ingredient_id FOREIGN KEY (ingredient_id) REFERENCES ingredient(id)",
    };

    public static final String[] CUSTOM_DDL_POSTGRES = new String[]{
            "CREATE EXTENSION IF NOT EXISTS fuzzystrmatch",
            "CREATE EXTENSION IF NOT EXISTS pg_trgm",
            "CREATE INDEX ingredient_name_trgm_idx ON Ingredient USING gin (name gin_trgm_ops)"
    };

    public static final String[] CUSTOM_DDL_H2 = new String[]{
            """
            DROP ALIAS IF EXISTS LEVENSHTEIN;
            CREATE ALIAS LEVENSHTEIN AS $$
            int levenshtein(String a, String b) {
                return 9999;
            }
            $$;
            """
    };

    /**
     * Map: {@code ddl-mode -> bool}: enable this component. Ddl modes not in the map are not supported by this component.
     */
    private static final Map<String, Boolean> modes = Map.of("create-drop", true, "validate", false);

    private final PlatformTransactionManager txMananager;
    private final boolean enable;
    private final boolean isPostgresDriver;

    @PersistenceContext
    private EntityManager entityManager;

    public HibernateDdlWorkarounds(PlatformTransactionManager txMananager,
                                   @Value("${spring.jpa.hibernate.ddl-auto}") String hibernateDdlMode,
                                   @Value("${spring.datasource.driverClassName}") String driverClassName) {
        this.txMananager = txMananager;
        enable = Objects.requireNonNull(modes.get(hibernateDdlMode),
                "HibernateDdlWorkarounds: ddl-mode must be set and one of: " + modes.keySet() + " but was: " + hibernateDdlMode);
        isPostgresDriver = driverClassName.startsWith("org.postgresql");
    }

    @PostConstruct
    public void applyAdditionalDdl() {
        if (enable) {
            new TransactionTemplate(txMananager).executeWithoutResult(status -> {
                entityManager.unwrap(Session.class).doWork(work -> {
                    for (String ddl : CUSTOM_DDL) {
                        work.prepareStatement(ddl).executeUpdate();
                    }
                    if (isPostgresDriver) {
                        for (String ddl : CUSTOM_DDL_POSTGRES) {
                            work.prepareStatement(ddl).executeUpdate();
                        }
                    } else {
                        for (String ddl: CUSTOM_DDL_H2) {
                            work.prepareStatement(ddl).executeUpdate();
                        }
                    }
                });
            });
            log.info("successfully executed workaround ddl. Driver postgres: {}", isPostgresDriver);
        }
    }
}
