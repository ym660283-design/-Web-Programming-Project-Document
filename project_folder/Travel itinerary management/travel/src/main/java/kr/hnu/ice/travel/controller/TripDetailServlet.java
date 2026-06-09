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
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@WebServlet("/trip-details")
public class TripDetailServlet extends HttpServlet {
    private static final String DETAIL_VIEW = "/views/trip/detail.jsp";

    private final TripDAO tripDAO = new TripDAO();
    private final TripDetailDAO tripDetailDAO = new TripDetailDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UserDTO loginUser = UserServlet.getLoginUser(request);
        if (loginUser == null) {
            UserServlet.redirectToLogin(request, response);
            return;
        }

        int tripId = parseTripId(request, response);
        if (tripId == -1) {
            return;
        }

        try {
            TripDTO trip = tripDAO.findAccessibleById(tripId, loginUser.getUserId());
            if (trip == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "조회할 수 있는 여행 일정이 없습니다.");
                return;
            }

            TripDetailDTO editingDetail = null;
            int selectedDay = parseSelectedDay(request, trip);

            if ("editDetail".equals(request.getParameter("action"))) {
                if (!trip.isOwner()) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "작성자만 세부 일정을 수정할 수 있습니다.");
                    return;
                }

                int detailId = parseDetailId(request, response);
                if (detailId == -1) {
                    return;
                }

                editingDetail = tripDetailDAO.findByIdAndTripId(detailId, tripId);
                if (editingDetail == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "수정할 세부 일정을 찾을 수 없습니다.");
                    return;
                }
                selectedDay = calculateDay(trip, editingDetail.getScheduleDate());
            }

            showDetail(request, response, trip, selectedDay, editingDetail);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "여행 일정 상세 정보를 불러오는 중 오류가 발생했습니다.");
        }
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

        int tripId = parseTripId(request, response);
        if (tripId == -1) {
            return;
        }

        try {
            TripDTO trip = tripDAO.findByIdAndOwnerId(tripId, loginUser.getUserId());
            if (trip == null) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "작성자만 세부 일정을 관리할 수 있습니다.");
                return;
            }

            String action = request.getParameter("action");
            if ("createDetail".equals(action)) {
                createDetail(request, response, trip);
                return;
            }

            if ("updateDetail".equals(action)) {
                updateDetail(request, response, trip);
                return;
            }

            if ("deleteDetail".equals(action)) {
                deleteDetail(request, response, trip);
                return;
            }

            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "세부 일정 처리 중 오류가 발생했습니다.");
        }
    }

    private void showDetail(HttpServletRequest request, HttpServletResponse response,
                            TripDTO trip, int selectedDay, TripDetailDTO editingDetail)
            throws IOException, ServletException, SQLException {

        List<TripDetailDTO> details = tripDetailDAO.findByTripId(trip.getTripId());

        setTripAttributes(request, trip);
        setScheduleDateAttributes(request, trip, selectedDay, details);
        setFormAttributes(request, trip, selectedDay, editingDetail);
        setStatusMessage(request);
        request.getRequestDispatcher(DETAIL_VIEW).forward(request, response);
    }

    private void createDetail(HttpServletRequest request, HttpServletResponse response, TripDTO trip)
            throws IOException, ServletException, SQLException {

        TripDetailDTO detail = buildDetailFromRequest(request, trip.getTripId());
        String validationMessage = validateDetail(detail, trip);

        if (validationMessage != null) {
            forwardDetailError(request, response, trip, detail, null, validationMessage);
            return;
        }

        tripDetailDAO.insert(detail);
        response.sendRedirect(createDetailRedirectPath(request, trip, detail.getScheduleDate(), "detail-created"));
    }

    private void updateDetail(HttpServletRequest request, HttpServletResponse response, TripDTO trip)
            throws IOException, ServletException, SQLException {

        int detailId = parseDetailId(request, response);
        if (detailId == -1) {
            return;
        }

        TripDetailDTO detail = buildDetailFromRequest(request, trip.getTripId());
        detail.setDetailId(detailId);

        String validationMessage = validateDetail(detail, trip);
        if (validationMessage != null) {
            forwardDetailError(request, response, trip, detail, detail, validationMessage);
            return;
        }

        boolean updated = tripDetailDAO.update(detail);
        if (!updated) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "수정할 세부 일정을 찾을 수 없습니다.");
            return;
        }

        response.sendRedirect(createDetailRedirectPath(request, trip, detail.getScheduleDate(), "detail-updated"));
    }

    private void deleteDetail(HttpServletRequest request, HttpServletResponse response, TripDTO trip)
            throws IOException, SQLException {

        int detailId = parseDetailId(request, response);
        if (detailId == -1) {
            return;
        }

        int selectedDay = parseSelectedDay(request, trip);
        boolean deleted = tripDetailDAO.delete(detailId, trip.getTripId());
        if (!deleted) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "삭제할 세부 일정을 찾을 수 없습니다.");
            return;
        }

        response.sendRedirect(request.getContextPath()
                + "/trip-details?trip_id=" + trip.getTripId()
                + "&day=" + selectedDay
                + "&status=detail-deleted");
    }

    private void forwardDetailError(HttpServletRequest request, HttpServletResponse response,
                                    TripDTO trip, TripDetailDTO submittedDetail,
                                    TripDetailDTO editingDetail, String message)
            throws IOException, ServletException, SQLException {

        int selectedDay = calculateDay(trip, submittedDetail.getScheduleDate());

        setTripAttributes(request, trip);
        setScheduleDateAttributes(request, trip, selectedDay, tripDetailDAO.findByTripId(trip.getTripId()));
        setFormAttributes(request, trip, selectedDay, editingDetail == null ? submittedDetail : editingDetail);
        setSubmittedFormAttributes(request, submittedDetail, editingDetail != null);
        request.setAttribute("errorMessage", message);
        request.getRequestDispatcher(DETAIL_VIEW).forward(request, response);
    }

    private TripDetailDTO buildDetailFromRequest(HttpServletRequest request, int tripId) {
        TripDetailDTO detail = new TripDetailDTO();

        detail.setTripId(tripId);
        detail.setScheduleDate(parseDate(request.getParameter("schedule_date")));
        detail.setPlaceName(trim(request.getParameter("place_name")));
        detail.setVisitTime(parseTime(request.getParameter("visit_time")));
        detail.setCost(parseCost(request.getParameter("cost")));
        detail.setMemo(trim(request.getParameter("memo")));
        detail.setSortOrder(0);

        return detail;
    }

    private String validateDetail(TripDetailDTO detail, TripDTO trip) {
        if (detail.getScheduleDate() == null
                || detail.getScheduleDate().isBefore(trip.getStartDate())
                || detail.getScheduleDate().isAfter(trip.getEndDate())) {
            return "세부 일정 날짜는 여행 기간 안에서 선택해주세요.";
        }

        if (detail.getPlaceName().isEmpty()) {
            return "방문 장소를 입력해주세요.";
        }

        if (detail.getVisitTime() == null) {
            return "방문 시간을 입력해주세요.";
        }

        if (detail.getCost() < 0) {
            return "예상 비용은 0원 이상으로 입력해주세요.";
        }

        return null;
    }

    private void setTripAttributes(HttpServletRequest request, TripDTO trip) {
        request.setAttribute("tripId", trip.getTripId());
        request.setAttribute("tripTitle", trip.getTripTitle());
        request.setAttribute("destination", trip.getDestination());
        request.setAttribute("startDate", trip.getStartDateValue());
        request.setAttribute("endDate", trip.getEndDateValue());
        request.setAttribute("tripPeriod", trip.getTripPeriod());
        request.setAttribute("tripDuration", trip.getTripDuration());
        request.setAttribute("description", trip.getDescription());
        request.setAttribute("ownerName", trip.getOwnerName());
        request.setAttribute("isOwner", trip.isOwner());
    }

    private void setScheduleDateAttributes(HttpServletRequest request, TripDTO trip,
                                           int selectedDay, List<TripDetailDTO> details) {
        List<String> scheduleDates = new ArrayList<>();
        List<String> scheduleDateLabels = new ArrayList<>();
        Map<String, Integer> scheduleCounts = new HashMap<>();

        for (TripDetailDTO detail : details) {
            String dateValue = detail.getScheduleDateValue();
            Integer count = scheduleCounts.get(dateValue);
            scheduleCounts.put(dateValue, count == null ? 1 : count + 1);
        }

        LocalDate currentDate = trip.getStartDate();
        LocalDate endDate = trip.getEndDate();

        while (!currentDate.isAfter(endDate)) {
            scheduleDates.add(currentDate.toString());
            scheduleDateLabels.add(formatDateLabel(currentDate));
            currentDate = currentDate.plusDays(1);
        }

        if (scheduleDates.isEmpty()) {
            selectedDay = 1;
        } else if (selectedDay < 1) {
            selectedDay = 1;
        } else if (selectedDay > scheduleDates.size()) {
            selectedDay = scheduleDates.size();
        }

        String selectedDate = scheduleDates.isEmpty() ? "" : scheduleDates.get(selectedDay - 1);
        String selectedDateLabel = scheduleDateLabels.isEmpty() ? "" : scheduleDateLabels.get(selectedDay - 1);
        List<TripDetailDTO> selectedDetails = new ArrayList<>();

        for (TripDetailDTO detail : details) {
            if (selectedDate.equals(detail.getScheduleDateValue())) {
                selectedDetails.add(detail);
            }
        }

        request.setAttribute("selectedDay", selectedDay);
        request.setAttribute("selectedDate", selectedDate);
        request.setAttribute("selectedDateLabel", selectedDateLabel);
        request.setAttribute("selectedScheduleDetails", selectedDetails);
        request.setAttribute("selectedScheduleCount", selectedDetails.size());
        request.setAttribute("scheduleDayCount", scheduleDates.size());
        request.setAttribute("scheduleDates", scheduleDates);
        request.setAttribute("scheduleDateLabels", scheduleDateLabels);
        request.setAttribute("scheduleCounts", scheduleCounts);
        request.setAttribute("firstScheduleDate", scheduleDates.isEmpty() ? "" : scheduleDates.get(0));
        request.setAttribute("firstScheduleDateLabel", scheduleDateLabels.isEmpty() ? "" : scheduleDateLabels.get(0));
    }

    private void setFormAttributes(HttpServletRequest request, TripDTO trip,
                                   int selectedDay, TripDetailDTO editingDetail) {
        LocalDate selectedDate = trip.getStartDate().plusDays(selectedDay - 1L);
        boolean editMode = editingDetail != null && editingDetail.getDetailId() > 0;

        request.setAttribute("detailFormAction", editMode ? "updateDetail" : "createDetail");
        request.setAttribute("detailSubmitLabel", editMode ? "세부 일정 수정" : "세부 일정 등록");
        request.setAttribute("detailFormHeading", editMode ? "세부 일정 수정" : "세부 일정 등록");
        request.setAttribute("detailFormNote", editMode
                ? "선택한 세부 일정을 수정하고 있습니다."
                : "입력한 세부 일정은 서버에서 저장됩니다.");
        request.setAttribute("editingDetail", editingDetail);

        if (editingDetail == null) {
            request.setAttribute("formDetailId", "");
            request.setAttribute("formScheduleDate", selectedDate.toString());
            request.setAttribute("formPlaceName", "");
            request.setAttribute("formVisitTime", "");
            request.setAttribute("formCost", "");
            request.setAttribute("formMemo", "");
            return;
        }

        setSubmittedFormAttributes(request, editingDetail, editMode);
    }

    private void setSubmittedFormAttributes(HttpServletRequest request,
                                            TripDetailDTO detail, boolean editMode) {
        request.setAttribute("detailFormAction", editMode ? "updateDetail" : "createDetail");
        request.setAttribute("detailSubmitLabel", editMode ? "세부 일정 수정" : "세부 일정 등록");
        request.setAttribute("detailFormHeading", editMode ? "세부 일정 수정" : "세부 일정 등록");
        request.setAttribute("detailFormNote", editMode
                ? "선택한 세부 일정을 수정하고 있습니다."
                : "입력한 세부 일정은 서버에서 저장됩니다.");
        request.setAttribute("formDetailId", detail.getDetailId() == 0 ? "" : detail.getDetailId());
        request.setAttribute("formScheduleDate", detail.getScheduleDateValue());
        request.setAttribute("formPlaceName", detail.getPlaceName());
        request.setAttribute("formVisitTime", detail.getVisitTimeValue());
        request.setAttribute("formCost", detail.getCost() == 0 ? "" : detail.getCost());
        request.setAttribute("formMemo", detail.getMemo());
    }

    private String formatDateLabel(LocalDate date) {
        return date.getMonthValue() + "월 "
                + date.getDayOfMonth() + "일 "
                + date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.KOREAN);
    }

    private void setStatusMessage(HttpServletRequest request) {
        String status = request.getParameter("status");

        if ("created".equals(status)) {
            request.setAttribute("successMessage", "여행 일정이 생성되었습니다.");
        } else if ("updated".equals(status)) {
            request.setAttribute("successMessage", "여행 일정이 수정되었습니다.");
        } else if ("detail-created".equals(status)) {
            request.setAttribute("successMessage", "세부 일정이 등록되었습니다.");
        } else if ("detail-updated".equals(status)) {
            request.setAttribute("successMessage", "세부 일정이 수정되었습니다.");
        } else if ("detail-deleted".equals(status)) {
            request.setAttribute("successMessage", "세부 일정이 삭제되었습니다.");
        }
    }

    private String createDetailRedirectPath(HttpServletRequest request, TripDTO trip,
                                            LocalDate scheduleDate, String status) {
        return request.getContextPath()
                + "/trip-details?trip_id=" + trip.getTripId()
                + "&day=" + calculateDay(trip, scheduleDate)
                + "&status=" + status;
    }

    private int parseTripId(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            return Integer.parseInt(trim(request.getParameter("trip_id")));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "여행 일정 번호가 올바르지 않습니다.");
            return -1;
        }
    }

    private int parseDetailId(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            return Integer.parseInt(trim(request.getParameter("detail_id")));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "세부 일정 번호가 올바르지 않습니다.");
            return -1;
        }
    }

    private int parseSelectedDay(HttpServletRequest request, TripDTO trip) {
        try {
            int selectedDay = Integer.parseInt(trim(request.getParameter("day")));
            int maxDay = (int) ChronoUnit.DAYS.between(trip.getStartDate(), trip.getEndDate()) + 1;
            if (selectedDay < 1) {
                return 1;
            }
            return Math.min(selectedDay, maxDay);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private int calculateDay(TripDTO trip, LocalDate scheduleDate) {
        if (scheduleDate == null || scheduleDate.isBefore(trip.getStartDate())) {
            return 1;
        }

        int day = (int) ChronoUnit.DAYS.between(trip.getStartDate(), scheduleDate) + 1;
        int maxDay = (int) ChronoUnit.DAYS.between(trip.getStartDate(), trip.getEndDate()) + 1;
        if (day < 1) {
            return 1;
        }
        return Math.min(day, maxDay);
    }

    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(trim(value));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private LocalTime parseTime(String value) {
        try {
            return LocalTime.parse(trim(value));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private int parseCost(String value) {
        String trimmedValue = trim(value);
        if (trimmedValue.isEmpty()) {
            return 0;
        }

        try {
            return Integer.parseInt(trimmedValue);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
