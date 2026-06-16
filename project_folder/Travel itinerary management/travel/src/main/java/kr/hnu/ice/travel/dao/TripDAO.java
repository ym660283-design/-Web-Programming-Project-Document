package kr.hnu.ice.travel.dao;

import kr.hnu.ice.travel.dto.TripDTO;
import kr.hnu.ice.travel.util.DBUtil;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 작성자: 김용민, 김준섭
 * 기능 설명: 여행 일정 데이터의 조회, 저장, 수정, 삭제를 담당하는 DAO 클래스입니다.
 */
public class TripDAO {
    public List<TripDTO> findAccessibleTrips(int userId) throws SQLException {
        String sql = "SELECT t.trip_id, t.user_id, u.user_name AS owner_name, "
                + "t.trip_title, t.destination, t.start_date, t.end_date, t.description, t.share_code, tm.role "
                + "FROM trips t "
                + "JOIN users u ON t.user_id = u.user_id "
                + "LEFT JOIN trip_members tm ON tm.trip_id = t.trip_id AND tm.user_id = ? "
                + "WHERE t.user_id = ? OR tm.user_id IS NOT NULL "
                + "ORDER BY t.start_date ASC, t.trip_id DESC";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<TripDTO> trips = new ArrayList<>();
                while (resultSet.next()) {
                    trips.add(mapTrip(resultSet, userId));
                }
                return trips;
            }
        }
    }

    public TripDTO findAccessibleById(int tripId, int userId) throws SQLException {
        String sql = "SELECT t.trip_id, t.user_id, u.user_name AS owner_name, "
                + "t.trip_title, t.destination, t.start_date, t.end_date, t.description, t.share_code, tm.role "
                + "FROM trips t "
                + "JOIN users u ON t.user_id = u.user_id "
                + "LEFT JOIN trip_members tm ON tm.trip_id = t.trip_id AND tm.user_id = ? "
                + "WHERE t.trip_id = ? "
                + "AND (t.user_id = ? OR EXISTS ("
                + "    SELECT 1 FROM trip_members tm WHERE tm.trip_id = t.trip_id AND tm.user_id = ?"
                + "))";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, tripId);
            preparedStatement.setInt(3, userId);
            preparedStatement.setInt(4, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapTrip(resultSet, userId);
                }
                return null;
            }
        }
    }

    public TripDTO findByIdAndOwnerId(int tripId, int ownerId) throws SQLException {
        String sql = "SELECT t.trip_id, t.user_id, u.user_name AS owner_name, "
                + "t.trip_title, t.destination, t.start_date, t.end_date, t.description, t.share_code, NULL AS role "
                + "FROM trips t "
                + "JOIN users u ON t.user_id = u.user_id "
                + "WHERE t.trip_id = ? AND t.user_id = ?";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, tripId);
            preparedStatement.setInt(2, ownerId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapTrip(resultSet, ownerId);
                }
                return null;
            }
        }
    }

    public int insert(TripDTO trip) throws SQLException {
        String sql = "INSERT INTO trips "
                + "(user_id, trip_title, destination, start_date, end_date, description, share_code) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, trip.getUserId());
            preparedStatement.setString(2, trip.getTripTitle());
            preparedStatement.setString(3, trip.getDestination());
            preparedStatement.setDate(4, Date.valueOf(trip.getStartDate()));
            preparedStatement.setDate(5, Date.valueOf(trip.getEndDate()));
            preparedStatement.setString(6, trip.getDescription());
            preparedStatement.setString(7, trip.getShareCode());
            preparedStatement.executeUpdate();

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
                throw new SQLException("Failed to get generated trip_id.");
            }
        }
    }

    public TripDTO findByShareCode(String shareCode, int currentUserId) throws SQLException {
        String sql = "SELECT t.trip_id, t.user_id, u.user_name AS owner_name, "
                + "t.trip_title, t.destination, t.start_date, t.end_date, t.description, t.share_code, tm.role "
                + "FROM trips t "
                + "JOIN users u ON t.user_id = u.user_id "
                + "LEFT JOIN trip_members tm ON tm.trip_id = t.trip_id AND tm.user_id = ? "
                + "WHERE t.share_code = ?";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, currentUserId);
            statement.setString(2, shareCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapTrip(resultSet, currentUserId) : null;
            }
        }
    }

    public boolean update(TripDTO trip) throws SQLException {
        String sql = "UPDATE trips "
                + "SET trip_title = ?, destination = ?, start_date = ?, end_date = ?, description = ? "
                + "WHERE trip_id = ? AND (user_id = ? OR EXISTS ("
                + "SELECT 1 FROM trip_members "
                + "WHERE trip_members.trip_id = trips.trip_id "
                + "AND trip_members.user_id = ? AND trip_members.role = 'editor'))";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, trip.getTripTitle());
            preparedStatement.setString(2, trip.getDestination());
            preparedStatement.setDate(3, Date.valueOf(trip.getStartDate()));
            preparedStatement.setDate(4, Date.valueOf(trip.getEndDate()));
            preparedStatement.setString(5, trip.getDescription());
            preparedStatement.setInt(6, trip.getTripId());
            preparedStatement.setInt(7, trip.getUserId());
            preparedStatement.setInt(8, trip.getUserId());
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public boolean updateShareCode(int tripId, int ownerId, String shareCode) throws SQLException {
        String sql = "UPDATE trips SET share_code = ? WHERE trip_id = ? AND user_id = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, shareCode);
            statement.setInt(2, tripId);
            statement.setInt(3, ownerId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(int tripId, int ownerId) throws SQLException {
        String sql = "DELETE FROM trips WHERE trip_id = ? AND user_id = ?";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, tripId);
            preparedStatement.setInt(2, ownerId);
            return preparedStatement.executeUpdate() > 0;
        }
    }

    private TripDTO mapTrip(ResultSet resultSet, int currentUserId) throws SQLException {
        TripDTO trip = new TripDTO();

        trip.setTripId(resultSet.getInt("trip_id"));
        trip.setUserId(resultSet.getInt("user_id"));
        trip.setOwnerName(resultSet.getString("owner_name"));
        trip.setTripTitle(resultSet.getString("trip_title"));
        trip.setDestination(resultSet.getString("destination"));
        trip.setStartDate(resultSet.getDate("start_date").toLocalDate());
        trip.setEndDate(resultSet.getDate("end_date").toLocalDate());
        trip.setDescription(resultSet.getString("description"));
        trip.setShareCode(resultSet.getString("share_code"));
        trip.setOwner(trip.getUserId() == currentUserId);
        trip.setMemberRole(resultSet.getString("role"));

        return trip;
    }
}
