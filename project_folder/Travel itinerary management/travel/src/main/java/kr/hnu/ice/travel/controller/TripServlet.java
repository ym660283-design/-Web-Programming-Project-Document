package kr.hnu.ice.travel.controller;

import kr.hnu.ice.travel.dao.TripDAO;
import kr.hnu.ice.travel.dto.TripDTO;
import kr.hnu.ice.travel.dto.UserDTO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@WebServlet("/trips")
public class TripServlet extends HttpServlet {
    private static final String LIST_VIEW = "/views/trip/list.jsp";
    private static final String FORM_VIEW = "/views/trip/form.jsp";

    private final TripDAO tripDAO = new TripDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UserDTO loginUser = UserServlet.getLoginUser(request);
        if (loginUser == null) {
            UserServlet.redirectToLogin(request, response);
            return;
        }

        String action = request.getParameter("action");

        if ("create".equals(action)) {
            showCreateForm(request, response);
            return;
        }

        if ("edit".equals(action)) {
            showEditForm(request, response, loginUser);
            return;
        }

        showList(request, response, loginUser);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        UserDTO loginUser = UserServlet.getLoginUser(request);
        if (loginUser == null) {
            UserServlet.redirectToLogin(request, response);
            return;
        }

        String action = request.getParameter("action");

        if ("create".equals(action)) {
            createTrip(request, response, loginUser);
            return;
        }

        if ("update".equals(action)) {
            updateTrip(request, response, loginUser);
            return;
        }

        if ("delete".equals(action)) {
            deleteTrip(request, response, loginUser);
            return;
        }

        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }

    private void showList(HttpServletRequest request, HttpServletResponse response, UserDTO loginUser)
            throws IOException, ServletException {

        try {
            List<TripDTO> trips = tripDAO.findAccessibleTrips(loginUser.getUserId());
            request.setAttribute("trips", trips);
            request.setAttribute("tripCount", trips.size());
            setStatusMessage(request);
            request.getRequestDispatcher(LIST_VIEW).forward(request, response);
        } catch (SQLException e) {
            request.setAttribute("errorMessage", "여행 일정 목록을 불러오는 중 오류가 발생했습니다.");
            request.getRequestDispatcher(LIST_VIEW).forward(request, response);
        }
    }

    private void showCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        setFormAttributes(
                request,
                "create",
                "여행 일정 생성",
                "새 여행 일정 만들기",
                "여행의 기본 정보를 입력해 새로운 계획을 시작하세요.",
                "여행 일정 생성",
                new TripDTO()
        );
        request.getRequestDispatcher(FORM_VIEW).forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response, UserDTO loginUser)
            throws IOException, ServletException {

        int tripId = parseTripId(request, response);
        if (tripId == -1) {
            return;
        }

        try {
            TripDTO trip = tripDAO.findByIdAndOwnerId(tripId, loginUser.getUserId());
            if (trip == null) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "작성자만 여행 일정을 수정할 수 있습니다.");
                return;
            }

            setFormAttributes(
                    request,
                    "update",
                    "여행 일정 수정",
                    "여행 일정 수정하기",
                    "변경할 여행 정보를 확인하고 수정하세요.",
                    "수정 내용 저장",
                    trip
            );
            request.getRequestDispatcher(FORM_VIEW).forward(request, response);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "여행 일정 정보를 불러오는 중 오류가 발생했습니다.");
        }
    }

    private void createTrip(HttpServletRequest request, HttpServletResponse response, UserDTO loginUser)
            throws IOException, ServletException {

        TripDTO trip = buildTripFromRequest(request, loginUser.getUserId());
        String validationMessage = validateTrip(trip);

        if (validationMessage != null) {
            forwardFormError(
                    request,
                    response,
                    "create",
                    "여행 일정 생성",
                    "새 여행 일정 만들기",
                    "여행의 기본 정보를 입력해 새로운 계획을 시작하세요.",
                    "여행 일정 생성",
                    trip,
                    validationMessage
            );
            return;
        }

        trip.setShareCode(createShareCode());

        try {
            int tripId = tripDAO.insert(trip);
            response.sendRedirect(request.getContextPath() + "/trip-details?trip_id=" + tripId + "&status=created");
        } catch (SQLException e) {
            forwardFormError(
                    request,
                    response,
                    "create",
                    "여행 일정 생성",
                    "새 여행 일정 만들기",
                    "여행의 기본 정보를 입력해 새로운 계획을 시작하세요.",
                    "여행 일정 생성",
                    trip,
                    "여행 일정 생성 중 오류가 발생했습니다."
            );
        }
    }

    private void updateTrip(HttpServletRequest request, HttpServletResponse response, UserDTO loginUser)
            throws IOException, ServletException {

        int tripId = parseTripId(request, response);
        if (tripId == -1) {
            return;
        }

        TripDTO trip = buildTripFromRequest(request, loginUser.getUserId());
        trip.setTripId(tripId);

        String validationMessage = validateTrip(trip);
        if (validationMessage != null) {
            forwardFormError(
                    request,
                    response,
                    "update",
                    "여행 일정 수정",
                    "여행 일정 수정하기",
                    "변경할 여행 정보를 확인하고 수정하세요.",
                    "수정 내용 저장",
                    trip,
                    validationMessage
            );
            return;
        }

        try {
            boolean updated = tripDAO.update(trip);
            if (!updated) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "작성자만 여행 일정을 수정할 수 있습니다.");
                return;
            }

            response.sendRedirect(request.getContextPath() + "/trip-details?trip_id=" + tripId + "&status=updated");
        } catch (SQLException e) {
            forwardFormError(
                    request,
                    response,
                    "update",
                    "여행 일정 수정",
                    "여행 일정 수정하기",
                    "변경할 여행 정보를 확인하고 수정하세요.",
                    "수정 내용 저장",
                    trip,
                    "여행 일정 수정 중 오류가 발생했습니다."
            );
        }
    }

    private void deleteTrip(HttpServletRequest request, HttpServletResponse response, UserDTO loginUser)
            throws IOException {

        int tripId = parseTripId(request, response);
        if (tripId == -1) {
            return;
        }

        try {
            boolean deleted = tripDAO.delete(tripId, loginUser.getUserId());
            if (!deleted) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "작성자만 여행 일정을 삭제할 수 있습니다.");
                return;
            }

            response.sendRedirect(request.getContextPath() + "/trips?status=deleted");
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "여행 일정 삭제 중 오류가 발생했습니다.");
        }
    }

    private TripDTO buildTripFromRequest(HttpServletRequest request, int userId) {
        TripDTO trip = new TripDTO();

        trip.setUserId(userId);
        trip.setTripTitle(trim(request.getParameter("trip_title")));
        trip.setDestination(trim(request.getParameter("destination")));
        trip.setStartDate(parseDate(request.getParameter("start_date")));
        trip.setEndDate(parseDate(request.getParameter("end_date")));
        trip.setDescription(trim(request.getParameter("description")));

        return trip;
    }

    private String validateTrip(TripDTO trip) {
        if (trip.getTripTitle().isEmpty()) {
            return "여행 제목을 입력해주세요.";
        }
        if (trip.getDestination().isEmpty()) {
            return "목적지를 입력해주세요.";
        }
        if (trip.getStartDate() == null || trip.getEndDate() == null) {
            return "여행 시작일과 종료일을 입력해주세요.";
        }
        if (trip.getStartDate().isAfter(trip.getEndDate())) {
            return "여행 종료일은 시작일 이후로 입력해주세요.";
        }
        return null;
    }

    private void forwardFormError(HttpServletRequest request, HttpServletResponse response,
                                  String formMode, String pageTitle, String formHeading,
                                  String formDescription, String submitLabel,
                                  TripDTO trip, String errorMessage)
            throws IOException, ServletException {

        setFormAttributes(request, formMode, pageTitle, formHeading, formDescription, submitLabel, trip);
        request.setAttribute("errorMessage", errorMessage);
        request.getRequestDispatcher(FORM_VIEW).forward(request, response);
    }

    private void setFormAttributes(
            HttpServletRequest request,
            String formMode,
            String pageTitle,
            String formHeading,
            String formDescription,
            String submitLabel,
            TripDTO trip) {

        request.setAttribute("formMode", formMode);
        request.setAttribute("pageTitle", pageTitle);
        request.setAttribute("formHeading", formHeading);
        request.setAttribute("formDescription", formDescription);
        request.setAttribute("submitLabel", submitLabel);
        request.setAttribute("tripId", trip.getTripId() == 0 ? "" : trip.getTripId());
        request.setAttribute("tripTitle", trip.getTripTitle());
        request.setAttribute("destination", trip.getDestination());
        request.setAttribute("startDate", trip.getStartDateValue());
        request.setAttribute("endDate", trip.getEndDateValue());
        request.setAttribute("description", trip.getDescription());
    }

    private void setStatusMessage(HttpServletRequest request) {
        String status = request.getParameter("status");

        if ("deleted".equals(status)) {
            request.setAttribute("successMessage", "여행 일정이 삭제되었습니다.");
        }
    }

    private int parseTripId(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            return Integer.parseInt(trim(request.getParameter("trip_id")));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "여행 일정 번호가 올바르지 않습니다.");
            return -1;
        }
    }

    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(trim(value));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private String createShareCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
