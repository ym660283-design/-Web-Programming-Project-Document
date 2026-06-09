<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="kr.hnu.ice.travel.dto.TripDTO" %>
<%@ page import="kr.hnu.ice.travel.dto.UserDTO" %>
<%!
    private String escapeHtml(Object value) {
        if (value == null) {
            return "";
        }

        return value.toString()
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
%>
<%
    request.setAttribute("pageTitle", "여행 일정");
    UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
    List<TripDTO> trips = (List<TripDTO>) request.getAttribute("trips");
    int tripCount = trips == null ? 0 : trips.size();
%>
<%@ include file="/views/common/header.jsp" %>

<section class="page-section trip-section">
    <div class="container">
        <div class="trip-page-header d-flex flex-column flex-lg-row justify-content-between align-items-lg-center gap-4">
            <div class="page-heading">
                <h1>여행 일정</h1>
                <p>
                    <% if (loginUser != null) { %>
                        <%= escapeHtml(loginUser.getUserName()) %>님의 여행 계획을 확인하고 관리하세요.
                    <% } else { %>
                        여행 계획을 확인하고 관리하세요.
                    <% } %>
                </p>
            </div>
            <a class="btn trip-primary-button"
               href="${pageContext.request.contextPath}/trips?action=create">
                <span aria-hidden="true">+</span>
                여행 일정 생성
            </a>
        </div>

        <% if (request.getAttribute("successMessage") != null) { %>
            <div class="alert alert-success" role="alert">
                <%= escapeHtml(request.getAttribute("successMessage")) %>
            </div>
        <% } %>

        <% if (request.getAttribute("errorMessage") != null) { %>
            <div class="alert alert-danger" role="alert">
                <%= escapeHtml(request.getAttribute("errorMessage")) %>
            </div>
        <% } %>

        <div class="trip-list-summary">
            내 여행 일정 <strong><%= tripCount %></strong>개
        </div>

        <% if (tripCount == 0) { %>
            <div class="trip-empty-list">
                <div class="trip-empty-list-icon" aria-hidden="true">+</div>
                <h2>등록된 여행 일정이 없습니다</h2>
                <p>새 여행 일정 생성 버튼을 눌러 첫 여행 계획을 만들어보세요.</p>
                <a class="btn trip-primary-button"
                   href="${pageContext.request.contextPath}/trips?action=create">여행 일정 생성</a>
            </div>
        <% } else { %>
            <div class="trip-list">
                <% for (TripDTO trip : trips) { %>
                    <article class="trip-list-item">
                        <div class="trip-date-box" aria-label="여행 시작일 <%= escapeHtml(trip.getStartDateValue()) %>">
                            <span><%= escapeHtml(trip.getStartMonthShort()) %></span>
                            <strong><%= escapeHtml(trip.getStartDayString()) %></strong>
                            <small><%= trip.getStartYear() %></small>
                        </div>

                        <div class="trip-list-content">
                            <div class="trip-list-meta">
                                <span class="trip-location"><%= escapeHtml(trip.getDestination()) %></span>
                                <span class="trip-period"><%= escapeHtml(trip.getTripPeriod()) %></span>
                                <% if (!trip.isOwner()) { %>
                                    <span class="trip-owner">작성자 <%= escapeHtml(trip.getOwnerName()) %></span>
                                <% } %>
                            </div>
                            <h2><%= escapeHtml(trip.getTripTitle()) %></h2>
                            <p><%= escapeHtml(trip.getDescription()) %></p>
                        </div>

                        <div class="trip-list-actions">
                            <a class="btn trip-outline-button"
                               href="${pageContext.request.contextPath}/trip-details?trip_id=<%= trip.getTripId() %>">상세보기</a>
                            <% if (trip.isOwner()) { %>
                                <a class="btn trip-edit-button"
                                   href="${pageContext.request.contextPath}/trips?action=edit&trip_id=<%= trip.getTripId() %>">수정</a>
                                <form action="${pageContext.request.contextPath}/trips" method="post">
                                    <input type="hidden" name="action" value="delete">
                                    <input type="hidden" name="trip_id" value="<%= trip.getTripId() %>">
                                    <button class="btn trip-delete-button" type="submit">삭제</button>
                                </form>
                            <% } %>
                        </div>
                    </article>
                <% } %>
            </div>
        <% } %>
    </div>
</section>

<%@ include file="/views/common/footer.jsp" %>
