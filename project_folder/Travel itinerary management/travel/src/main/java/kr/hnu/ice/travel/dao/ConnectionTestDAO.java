package kr.hnu.ice.travel.dao;

import kr.hnu.ice.travel.util.DBUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionTestDAO {
    public String findCurrentDatabaseName() throws SQLException {
        try (Connection connection = DBUtil.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT DATABASE()")) {

            if (!connection.isValid(3)) {
                throw new SQLException("Database connection is not valid");
            }

            if (resultSet.next()) {
                return resultSet.getString(1);
            }

            throw new SQLException("Database name query returned no rows");
        }
    }
}
