package kr.hnu.ice.travel.dao;

import kr.hnu.ice.travel.dto.TripMemberDTO;
import kr.hnu.ice.travel.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class TripMemberDAO {
    public List<TripMemberDTO> findByTripId(int tripId) throws SQLException {
        String sql = "SELECT tm.member_id, tm.trip_id, tm.user_id, tm.role, tm.joined_at, "
                + "u.login_id, u.user_name, u.email "
                + "FROM trip_members tm "
                + "JOIN users u ON u.user_id = tm.user_id "
                + "WHERE tm.trip_id = ? ORDER BY tm.joined_at ASC, tm.member_id ASC";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, tripId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<TripMemberDTO> members = new ArrayList<>();
                while (resultSet.next()) {
                    members.add(mapMember(resultSet));
                }
                return members;
            }
        }
    }

    public boolean addMember(int tripId, int userId, String role) throws SQLException {
        String sql = "INSERT IGNORE INTO trip_members (trip_id, user_id, role) VALUES (?, ?, ?)";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, tripId);
            statement.setInt(2, userId);
            statement.setString(3, role);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean updateRole(int memberId, int tripId, String role) throws SQLException {
        String sql = "UPDATE trip_members SET role = ? WHERE member_id = ? AND trip_id = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, role);
            statement.setInt(2, memberId);
            statement.setInt(3, tripId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean remove(int memberId, int tripId) throws SQLException {
        String sql = "DELETE FROM trip_members WHERE member_id = ? AND trip_id = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, memberId);
            statement.setInt(2, tripId);
            return statement.executeUpdate() > 0;
        }
    }

    private TripMemberDTO mapMember(ResultSet resultSet) throws SQLException {
        TripMemberDTO member = new TripMemberDTO();
        Timestamp joinedAt = resultSet.getTimestamp("joined_at");

        member.setMemberId(resultSet.getInt("member_id"));
        member.setTripId(resultSet.getInt("trip_id"));
        member.setUserId(resultSet.getInt("user_id"));
        member.setLoginId(resultSet.getString("login_id"));
        member.setUserName(resultSet.getString("user_name"));
        member.setEmail(resultSet.getString("email"));
        member.setRole(resultSet.getString("role"));
        if (joinedAt != null) {
            member.setJoinedAt(joinedAt.toLocalDateTime());
        }
        return member;
    }
}
