package kr.hnu.ice.travel.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public final class DBUtil {
    private static final String CONFIG_FILE = "db.properties";
    private static final String SCHEMA_FILE = "schema.sql";
    private static final String SAMPLE_DATA_FILE = "sample_data.sql";
    private static final Properties DB_PROPERTIES = loadProperties();
    private static volatile boolean databaseInitialized;

    static {
        try {
            Class.forName(DB_PROPERTIES.getProperty("db.driver"));
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DBUtil() {
    }

    public static Connection getConnection() throws SQLException {
        ensureDatabaseInitialized();

        try {
            return DriverManager.getConnection(
                    DB_PROPERTIES.getProperty("db.url"),
                    DB_PROPERTIES.getProperty("db.username"),
                    DB_PROPERTIES.getProperty("db.password")
            );
        } catch (SQLException e) {
            if (!isUnknownDatabase(e)) {
                throw e;
            }

            initializeDatabase();
            return DriverManager.getConnection(
                    DB_PROPERTIES.getProperty("db.url"),
                    DB_PROPERTIES.getProperty("db.username"),
                    DB_PROPERTIES.getProperty("db.password")
            );
        }
    }

    private static void ensureDatabaseInitialized() throws SQLException {
        if (databaseInitialized) {
            return;
        }

        synchronized (DBUtil.class) {
            if (!databaseInitialized) {
                initializeDatabase();
                databaseInitialized = true;
            }
        }
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();

        try (InputStream inputStream = DBUtil.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                throw new IllegalStateException(CONFIG_FILE + " not found");
            }
            properties.load(inputStream);
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + CONFIG_FILE, e);
        }
    }

    private static void initializeDatabase() throws SQLException {
        String serverUrl = toServerUrl(DB_PROPERTIES.getProperty("db.url"));

        try (Connection connection = DriverManager.getConnection(
                serverUrl,
                DB_PROPERTIES.getProperty("db.username"),
                DB_PROPERTIES.getProperty("db.password"));
             Statement statement = connection.createStatement()) {

            executeSqlFile(statement, SCHEMA_FILE);
            executeSqlFile(statement, SAMPLE_DATA_FILE);
        }
    }

    private static void executeSqlFile(Statement statement, String fileName) throws SQLException {
        for (String sql : loadSqlFile(fileName).split(";")) {
            String trimmedSql = sql.trim();
            if (!trimmedSql.isEmpty()) {
                statement.execute(trimmedSql);
            }
        }
    }

    private static String toServerUrl(String databaseUrl) {
        int queryStartIndex = databaseUrl.indexOf('?');
        String baseUrl = queryStartIndex == -1 ? databaseUrl : databaseUrl.substring(0, queryStartIndex);
        String queryString = queryStartIndex == -1 ? "" : databaseUrl.substring(queryStartIndex);

        int databasePathStartIndex = baseUrl.lastIndexOf('/');
        if (databasePathStartIndex == -1) {
            throw new IllegalStateException("Invalid database URL: " + databaseUrl);
        }

        return baseUrl.substring(0, databasePathStartIndex + 1) + queryString;
    }

    private static boolean isUnknownDatabase(SQLException e) {
        return e.getErrorCode() == 1049 || "42000".equals(e.getSQLState());
    }

    private static String loadSqlFile(String fileName) {
        try (InputStream inputStream = DBUtil.class.getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new IllegalStateException(fileName + " not found");
            }

            StringBuilder sqlFile = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sqlFile.append(line).append('\n');
                }
            }
            return sqlFile.toString();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + fileName, e);
        }
    }
}
