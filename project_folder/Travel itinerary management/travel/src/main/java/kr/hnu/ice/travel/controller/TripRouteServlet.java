package kr.hnu.ice.travel.controller;

import kr.hnu.ice.travel.dao.TripDAO;
import kr.hnu.ice.travel.dao.TripDetailDAO;
import kr.hnu.ice.travel.dto.TripDTO;
import kr.hnu.ice.travel.dto.TripDetailDTO;
import kr.hnu.ice.travel.dto.UserDTO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/trip-route")
public class TripRouteServlet extends HttpServlet {
    private static final String DIRECTIONS_API_URL =
            "https://apis-navi.kakaomobility.com/v1/directions";

    private final TripDAO tripDAO = new TripDAO();
    private final TripDetailDAO tripDetailDAO = new TripDetailDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UserDTO loginUser = UserServlet.getLoginUser(request);
        if (loginUser == null) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "{\"ok\":false,\"message\":\"로그인이 필요합니다.\"}");
            return;
        }

        int tripId = parseInteger(request.getParameter("trip_id"));
        int selectedDay = parseInteger(request.getParameter("day"));
        if (tripId <= 0 || selectedDay <= 0) {
            writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                    "{\"ok\":false,\"message\":\"경로를 조회할 여행 정보가 올바르지 않습니다.\"}");
            return;
        }

        String restApiKey = trim(getServletContext().getInitParameter("kakaoRestApiKey"));
        if (restApiKey.isEmpty() || "YOUR_KAKAO_REST_API_KEY".equals(restApiKey)) {
            writeJson(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "{\"ok\":false,\"message\":\"Kakao REST API 키가 설정되지 않았습니다.\"}");
            return;
        }

        try {
            TripDTO trip = tripDAO.findAccessibleById(tripId, loginUser.getUserId());
            if (trip == null) {
                writeJson(response, HttpServletResponse.SC_NOT_FOUND,
                        "{\"ok\":false,\"message\":\"조회할 수 있는 여행 일정이 없습니다.\"}");
                return;
            }

            List<TripDetailDTO> routeDetails = findSelectedDayRouteDetails(trip, selectedDay);
            if (routeDetails.size() < 2) {
                writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                        "{\"ok\":false,\"message\":\"도로 경로를 계산하려면 위치가 저장된 장소가 2개 이상 필요합니다.\"}");
                return;
            }

            try {
                writeKakaoRouteResponse(response, restApiKey, routeDetails);
            } catch (IOException e) {
                writeJson(response, HttpServletResponse.SC_BAD_GATEWAY,
                        "{\"ok\":false,\"message\":\"Kakao 길찾기 API를 호출하지 못했습니다.\"}");
            }
        } catch (SQLException e) {
            writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "{\"ok\":false,\"message\":\"여행 경로 정보를 불러오는 중 오류가 발생했습니다.\"}");
        }
    }

    private List<TripDetailDTO> findSelectedDayRouteDetails(TripDTO trip, int selectedDay) throws SQLException {
        int maxDay = (int) ChronoUnit.DAYS.between(trip.getStartDate(), trip.getEndDate()) + 1;
        int normalizedDay = Math.max(1, Math.min(selectedDay, maxDay));
        LocalDate selectedDate = trip.getStartDate().plusDays(normalizedDay - 1L);

        List<TripDetailDTO> routeDetails = new ArrayList<>();
        for (TripDetailDTO detail : tripDetailDAO.findByTripId(trip.getTripId())) {
            if (selectedDate.equals(detail.getScheduleDate()) && detail.hasLocation()) {
                routeDetails.add(detail);
            }
        }

        return routeDetails;
    }

    private void writeKakaoRouteResponse(HttpServletResponse response, String restApiKey,
                                         List<TripDetailDTO> routeDetails) throws IOException {

        StringBuilder segments = new StringBuilder();
        segments.append("{\"ok\":true,\"segments\":[");

        for (int index = 0; index < routeDetails.size() - 1; index += 1) {
            if (index > 0) {
                segments.append(',');
            }
            segments.append(callDirectionsApi(restApiKey, routeDetails.get(index), routeDetails.get(index + 1)));
        }

        segments.append("]}");
        writeJson(response, HttpServletResponse.SC_OK, segments.toString());
    }

    private String callDirectionsApi(String restApiKey, TripDetailDTO origin, TripDetailDTO destination)
            throws IOException {

        String requestUrl = DIRECTIONS_API_URL
                + "?origin=" + encodeCoordinate(origin)
                + "&destination=" + encodeCoordinate(destination)
                + "&priority=RECOMMEND"
                + "&summary=false";

        HttpURLConnection connection = (HttpURLConnection) new URL(requestUrl).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(8000);
        connection.setRequestProperty("Authorization", "KakaoAK " + restApiKey);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");

        int statusCode = connection.getResponseCode();
        String body = readResponseBody(statusCode >= 400 ? connection.getErrorStream() : connection.getInputStream());

        if (statusCode < 200 || statusCode >= 300) {
            throw new IOException("Kakao Directions API request failed: " + statusCode + " " + body);
        }

        return body;
    }

    private String encodeCoordinate(TripDetailDTO detail) throws IOException {
        String coordinate = detail.getLongitudeValue() + "," + detail.getLatitudeValue();
        return URLEncoder.encode(coordinate, "UTF-8");
    }

    private String readResponseBody(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }

        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        }
        return body.toString();
    }

    private void writeJson(HttpServletResponse response, int statusCode, String body) throws IOException {
        response.setStatus(statusCode);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write(body);
    }

    private int parseInteger(String value) {
        try {
            return Integer.parseInt(trim(value));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
