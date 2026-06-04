package kr.hnu.ice.travel.util;

import kr.hnu.ice.travel.dao.ConnectionTestDAO;

import java.sql.SQLException;

public class DBConnectionTest {
    public static void main(String[] args) {
        ConnectionTestDAO connectionTestDAO = new ConnectionTestDAO();

        try {
            String databaseName = connectionTestDAO.findCurrentDatabaseName();
            System.out.println("DB connection success: " + databaseName);
        } catch (SQLException e) {
            throw new IllegalStateException("DB connection failed", e);
        }
    }
}
