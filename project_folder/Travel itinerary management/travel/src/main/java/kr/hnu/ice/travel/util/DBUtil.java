package kr.hnu.ice.travel.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DBUtil {
    private static final String CONFIG_FILE = "db.properties";
    private static final Properties DB_PROPERTIES = loadProperties();

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
        return DriverManager.getConnection(
                DB_PROPERTIES.getProperty("db.url"),
                DB_PROPERTIES.getProperty("db.username"),
                DB_PROPERTIES.getProperty("db.password")
        );
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
}
