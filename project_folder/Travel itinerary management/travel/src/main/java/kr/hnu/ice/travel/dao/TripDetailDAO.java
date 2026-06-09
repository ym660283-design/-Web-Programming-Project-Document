package kr.hnu.ice.travel.dao;

import kr.hnu.ice.travel.dto.TripDetailDTO;
import kr.hnu.ice.travel.util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class TripDetailDAO {
    public List<TripDetailDTO> findByTripId(int tripId) throws SQLException {
        String sql = "SELECT detail_id, trip_id, schedule_date, place_name, visit_time, "
                + "memo, cost, sort_order, latitude, longitude "
                + "FROM trip_details "
                + "WHERE trip_id = ? "
                + "ORDER BY schedule_date ASC, visit_time ASC, sort_order ASC, detail_id ASC";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, tripId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<TripDetailDTO> details = new ArrayList<>();
                while (resultSet.next()) {
                    details.add(mapTripDetail(resultSet));
                }
                return details;
            }
        }
    }

    public TripDetailDTO findByIdAndTripId(int detailId, int tripId) throws SQLException {
        String sql = "SELECT detail_id, trip_id, schedule_date, place_name, visit_time, "
                + "memo, cost, sort_order, latitude, longitude "
                + "FROM trip_details "
                + "WHERE detail_id = ? AND trip_id = ?";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, detailId);
            preparedStatement.setInt(2, tripId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapTripDetail(resultSet);
                }
                return null;
            }
        }
    }

    public boolean insert(TripDetailDTO detail) throws SQLException {
        String sql = "INSERT INTO trip_details "
                + "(trip_id, schedule_date, place_name, visit_time, memo, cost, sort_order, latitude, longitude) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            setDetailParameters(preparedStatement, detail);
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public boolean update(TripDetailDTO detail) throws SQLException {
        String sql = "UPDATE trip_details "
                + "SET schedule_date = ?, place_name = ?, visit_time = ?, memo = ?, "
                + "cost = ?, sort_order = ?, latitude = ?, longitude = ? "
                + "WHERE detail_id = ? AND trip_id = ?";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setDate(1, Date.valueOf(detail.getScheduleDate()));
            preparedStatement.setString(2, detail.getPlaceName());
            preparedStatement.setTime(3, Time.valueOf(detail.getVisitTime()));
            preparedStatement.setString(4, detail.getMemo());
            preparedStatement.setInt(5, detail.getCost());
            preparedStatement.setInt(6, detail.getSortOrder());
            setCoordinate(preparedStatement, 7, detail.getLatitude());
            setCoordinate(preparedStatement, 8, detail.getLongitude());
            preparedStatement.setInt(9, detail.getDetailId());
            preparedStatement.setInt(10, detail.getTripId());
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public boolean delete(int detailId, int tripId) throws SQLException {
        String sql = "DELETE FROM trip_details WHERE detail_id = ? AND trip_id = ?";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, detailId);
            preparedStatement.setInt(2, tripId);
            return preparedStatement.executeUpdate() > 0;
        }
    }

    private void setDetailParameters(PreparedStatement preparedStatement, TripDetailDTO detail)
            throws SQLException {

        preparedStatement.setInt(1, detail.getTripId());
        preparedStatement.setDate(2, Date.valueOf(detail.getScheduleDate()));
        preparedStatement.setString(3, detail.getPlaceName());
        preparedStatement.setTime(4, Time.valueOf(detail.getVisitTime()));
        preparedStatement.setString(5, detail.getMemo());
        preparedStatement.setInt(6, detail.getCost());
        preparedStatement.setInt(7, detail.getSortOrder());
        setCoordinate(preparedStatement, 8, detail.getLatitude());
        setCoordinate(preparedStatement, 9, detail.getLongitude());
    }

    private void setCoordinate(PreparedStatement preparedStatement, int parameterIndex, BigDecimal coordinate)
            throws SQLException {

        if (coordinate == null) {
            preparedStatement.setNull(parameterIndex, Types.DECIMAL);
            return;
        }

        preparedStatement.setBigDecimal(parameterIndex, coordinate);
    }

    private TripDetailDTO mapTripDetail(ResultSet resultSet) throws SQLException {
        TripDetailDTO detail = new TripDetailDTO();
        Time visitTime = resultSet.getTime("visit_time");

        detail.setDetailId(resultSet.getInt("detail_id"));
        detail.setTripId(resultSet.getInt("trip_id"));
        detail.setScheduleDate(resultSet.getDate("schedule_date").toLocalDate());
        detail.setPlaceName(resultSet.getString("place_name"));
        detail.setVisitTime(visitTime == null ? null : visitTime.toLocalTime());
        detail.setMemo(resultSet.getString("memo"));
        detail.setCost(resultSet.getInt("cost"));
        detail.setSortOrder(resultSet.getInt("sort_order"));
        detail.setLatitude(resultSet.getBigDecimal("latitude"));
        detail.setLongitude(resultSet.getBigDecimal("longitude"));

        return detail;
    }
}
