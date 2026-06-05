package kr.hnu.ice.travel.dao;

import kr.hnu.ice.travel.dto.UserDTO;
import kr.hnu.ice.travel.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class UserDAO {
    public UserDTO findByLoginIdAndPassword(String loginId, String password) throws SQLException {
        String sql = "SELECT user_id, login_id, user_name, email, created_at "
                + "FROM users WHERE login_id = ? AND password = ?";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, loginId);
            preparedStatement.setString(2, password);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapUser(resultSet);
                }
                return null;
            }
        }
    }

    private UserDTO mapUser(ResultSet resultSet) throws SQLException {
        UserDTO user = new UserDTO();
        Timestamp createdAt = resultSet.getTimestamp("created_at");

        user.setUserId(resultSet.getInt("user_id"));
        user.setLoginId(resultSet.getString("login_id"));
        user.setUserName(resultSet.getString("user_name"));
        user.setEmail(resultSet.getString("email"));

        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }

        return user;
    }
}
