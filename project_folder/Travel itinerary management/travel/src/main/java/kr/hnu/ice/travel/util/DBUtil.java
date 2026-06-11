package kr.hnu.ice.travel.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public final class DBUtil {
    private static final String CONFIG_FILE = "db.properties";
    private static final String[] TABLE_DEFINITIONS = {
            "CREATE TABLE IF NOT EXISTS users ("
                    + "user_id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "login_id VARCHAR(50) NOT NULL UNIQUE, "
                    + "password VARCHAR(255) NOT NULL, "
                    + "user_name VARCHAR(50) NOT NULL, "
                    + "email VARCHAR(100) NOT NULL, "
                    + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",
            "CREATE TABLE IF NOT EXISTS trips ("
                    + "trip_id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "user_id INT NOT NULL, "
                    + "trip_title VARCHAR(100) NOT NULL, "
                    + "destination VARCHAR(100) NOT NULL, "
                    + "start_date DATE NOT NULL, "
                    + "end_date DATE NOT NULL, "
                    + "description TEXT, "
                    + "share_code VARCHAR(50) UNIQUE, "
                    + "CONSTRAINT fk_trips_user FOREIGN KEY (user_id) REFERENCES users(user_id) "
                    + "ON DELETE CASCADE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",
            "CREATE TABLE IF NOT EXISTS trip_details ("
                    + "detail_id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "trip_id INT NOT NULL, "
                    + "schedule_date DATE NOT NULL, "
                    + "place_name VARCHAR(100) NOT NULL, "
                    + "visit_time TIME, "
                    + "memo TEXT, "
                    + "cost INT NOT NULL DEFAULT 0, "
                    + "sort_order INT NOT NULL DEFAULT 0, "
                    + "latitude DECIMAL(10, 7), "
                    + "longitude DECIMAL(10, 7), "
                    + "CONSTRAINT fk_trip_details_trip FOREIGN KEY (trip_id) REFERENCES trips(trip_id) "
                    + "ON DELETE CASCADE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",
            "CREATE TABLE IF NOT EXISTS trip_members ("
                    + "member_id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "trip_id INT NOT NULL, "
                    + "user_id INT NOT NULL, "
                    + "role VARCHAR(20) NOT NULL DEFAULT 'viewer', "
                    + "joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "UNIQUE KEY uk_trip_members_trip_user (trip_id, user_id), "
                    + "CONSTRAINT fk_trip_members_trip FOREIGN KEY (trip_id) REFERENCES trips(trip_id) "
                    + "ON DELETE CASCADE, "
                    + "CONSTRAINT fk_trip_members_user FOREIGN KEY (user_id) REFERENCES users(user_id) "
                    + "ON DELETE CASCADE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
    };
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
        String databaseUrl = DB_PROPERTIES.getProperty("db.url");
        String username = DB_PROPERTIES.getProperty("db.username");
        String password = DB_PROPERTIES.getProperty("db.password");
        String databaseName = getDatabaseName(databaseUrl);

        try (Connection connection = DriverManager.getConnection(
                toServerUrl(databaseUrl),
                username,
                password);
             Statement statement = connection.createStatement()) {

            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + quoteIdentifier(databaseName)
                    + " DEFAULT CHARACTER SET utf8mb4"
                    + " DEFAULT COLLATE utf8mb4_unicode_ci");
        }

        try (Connection connection = DriverManager.getConnection(databaseUrl, username, password);
             Statement statement = connection.createStatement()) {

            for (String tableDefinition : TABLE_DEFINITIONS) {
                statement.executeUpdate(tableDefinition);
            }
            ensureTripDetailLocationColumns(connection, statement);
        }
    }

    private static void ensureTripDetailLocationColumns(Connection connection, Statement statement)
            throws SQLException {

        if (!hasColumn(connection, "trip_details", "latitude")) {
            statement.execute("ALTER TABLE trip_details ADD COLUMN latitude DECIMAL(10, 7)");
        }

        if (!hasColumn(connection, "trip_details", "longitude")) {
            statement.execute("ALTER TABLE trip_details ADD COLUMN longitude DECIMAL(10, 7)");
        }
    }

    private static boolean hasColumn(Connection connection, String tableName, String columnName)
            throws SQLException {

        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getColumns(null, null, tableName, columnName)) {
            if (resultSet.next()) {
                return true;
            }
        }

        try (ResultSet resultSet = metaData.getColumns(null, null, tableName.toUpperCase(), columnName.toUpperCase())) {
            return resultSet.next();
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

    private static String getDatabaseName(String databaseUrl) {
        int queryStartIndex = databaseUrl.indexOf('?');
        String baseUrl = queryStartIndex == -1 ? databaseUrl : databaseUrl.substring(0, queryStartIndex);
        int databasePathStartIndex = baseUrl.lastIndexOf('/');

        if (databasePathStartIndex == -1 || databasePathStartIndex == baseUrl.length() - 1) {
            throw new IllegalStateException("Database name is missing from URL: " + databaseUrl);
        }

        return baseUrl.substring(databasePathStartIndex + 1);
    }

    private static String quoteIdentifier(String identifier) {
        return "`" + identifier.replace("`", "``") + "`";
    }

    private static boolean isUnknownDatabase(SQLException e) {
        return e.getErrorCode() == 1049 || "42000".equals(e.getSQLState());
    }
}
