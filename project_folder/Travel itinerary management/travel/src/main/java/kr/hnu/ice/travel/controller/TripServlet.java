package kr.hnu.ice.travel.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/trips")
public class TripServlet extends HttpServlet {
    private static final String LIST_VIEW = "/views/trip/list.jsp";
    private static final String FORM_VIEW = "/views/trip/form.jsp";
    private static final String DETAIL_VIEW = "/views/trip/detail.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if ("create".equals(action)) {
            setFormAttributes(
                    request,
                    "create",
                    "여행 일정 생성",
                    "새 여행 일정 만들기",
                    "여행의 기본 정보를 입력해 새로운 계획을 시작하세요.",
                    "여행 일정 생성"
            );
            request.getRequestDispatcher(FORM_VIEW).forward(request, response);
            return;
        }

        if ("edit".equals(action)) {
            setSampleTrip(request);
            setFormAttributes(
                    request,
                    "update",
                    "여행 일정 수정",
                    "여행 일정 수정하기",
                    "변경할 여행 정보를 확인하고 수정하세요.",
                    "수정 내용 저장"
            );
            request.getRequestDispatcher(FORM_VIEW).forward(request, response);
            return;
        }

        if ("detail".equals(action)) {
            setSampleTrip(request);
            setSampleSchedule(request);
            request.getRequestDispatcher(DETAIL_VIEW).forward(request, response);
            return;
        }

        request.getRequestDispatcher(LIST_VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.sendRedirect(request.getContextPath() + "/views/trip/list.jsp");
    }

    private void setSampleTrip(HttpServletRequest request) {
        String tripId = request.getParameter("trip_id");
        request.setAttribute("tripId", tripId);

        if ("2".equals(tripId)) {
            request.setAttribute("tripTitle", "부산 주말 여행");
            request.setAttribute("destination", "부산");
            request.setAttribute("startDate", "2026-08-08");
            request.setAttribute("endDate", "2026-08-09");
            request.setAttribute("tripPeriod", "2026.08.08 - 2026.08.09");
            request.setAttribute("tripDuration", "1박 2일");
            request.setAttribute("description", "해운대와 광안리를 중심으로 여유롭게 즐기는 주말 일정입니다.");
            return;
        }

        request.setAttribute("tripTitle", "제주도 여름 여행");
        request.setAttribute("destination", "제주도");
        request.setAttribute("startDate", "2026-07-15");
        request.setAttribute("endDate", "2026-07-18");
        request.setAttribute("tripPeriod", "2026.07.15 - 2026.07.18");
        request.setAttribute("tripDuration", "3박 4일");
        request.setAttribute("description", "친구들과 함께 제주의 바다와 맛집을 둘러보는 여행입니다.");
    }

    private void setSampleSchedule(HttpServletRequest request) {
        if ("2".equals(request.getParameter("trip_id"))) {
            request.setAttribute("scheduleDayCount", 2);
            request.setAttribute("scheduleDate1", "2026-08-08");
            request.setAttribute("scheduleDateLabel1", "8월 8일 토요일");
            request.setAttribute("scheduleDate2", "2026-08-09");
            request.setAttribute("scheduleDateLabel2", "8월 9일 일요일");
            return;
        }

        request.setAttribute("scheduleDayCount", 4);
        request.setAttribute("scheduleDate1", "2026-07-15");
        request.setAttribute("scheduleDateLabel1", "7월 15일 수요일");
        request.setAttribute("scheduleDate2", "2026-07-16");
        request.setAttribute("scheduleDateLabel2", "7월 16일 목요일");
        request.setAttribute("scheduleDate3", "2026-07-17");
        request.setAttribute("scheduleDateLabel3", "7월 17일 금요일");
        request.setAttribute("scheduleDate4", "2026-07-18");
        request.setAttribute("scheduleDateLabel4", "7월 18일 토요일");
    }

    private void setFormAttributes(
            HttpServletRequest request,
            String formMode,
            String pageTitle,
            String formHeading,
            String formDescription,
            String submitLabel) {

        request.setAttribute("formMode", formMode);
        request.setAttribute("pageTitle", pageTitle);
        request.setAttribute("formHeading", formHeading);
        request.setAttribute("formDescription", formDescription);
        request.setAttribute("submitLabel", submitLabel);
    }
}
