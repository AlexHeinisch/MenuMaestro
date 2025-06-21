package dev.heinisch.menumaestro.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class DatabaseCleanerExtension implements AfterEachCallback {

    private static final List<String> TABLES_TO_IGNORE = Arrays.asList(
            "public.databasechangelog",
            "public.databasechangeloglock"
    );

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        try (Connection dbConnection = getDatabaseConnection(context)) {
            dbConnection.setAutoCommit(false);
            List<String> tables = loadTablesToClean(dbConnection);
            cleanTablesData(tables, dbConnection);
            log.debug("Deleted data from {} tables", tables.size());
        }
    }

    private Connection getDatabaseConnection(ExtensionContext context) throws SQLException {
        ApplicationContext applicationContext = SpringExtension.getApplicationContext(context);
        return applicationContext.getBean(DataSource.class).getConnection();
    }

    private List<String> loadTablesToClean(Connection connection) throws SQLException {
        var result = connection.getMetaData().getTables(connection.getCatalog(), null, null, new String[]{"TABLE"});
        List<String> tables = new ArrayList<>();
        while (result.next()) {
            String tableSchem = result.getString("TABLE_SCHEM");
            String table = toQualifiedTableName(
                    tableSchem,
                    result.getString("TABLE_NAME")
            );
            if (tableSchem.equals("PUBLIC") && !TABLES_TO_IGNORE.contains(table)) {
                tables.add(table);
            }
        }
        return tables;
    }

    private void cleanTablesData(List<String> tablesToClean, Connection connection) throws SQLException {
        connection.prepareStatement("SET REFERENTIAL_INTEGRITY FALSE").execute();
        for (String table : tablesToClean) {
            String sqlCommand = "TRUNCATE TABLE " + table;
            connection.prepareStatement(sqlCommand).execute();
            connection.commit();
        }
        connection.prepareStatement("SET REFERENTIAL_INTEGRITY TRUE").execute();
    }

    private String toQualifiedTableName(String schema, String table) {
        return schema + "." + table;
    }
}
