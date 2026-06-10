<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="kr.hnu.ice.travel.dto.TripDTO" %>
<%!
    private String escapeHtml(Object value) {
        if (value == null) return "";
        return value.toString().replace("&", "&amp;").replace("\"", "&quot;")
                .replace("'", "&#39;").replace("<", "&lt;").replace(">", "&gt;");
    }
%>
<%
    request.setAttribute("pageTitle", "여행 일정 참여");
    TripDTO trip = (TripDTO) request.getAttribute("trip");
%>
<%@ include file="/views/common/header.jsp" %>

<section class="page-section trip-section">
    <div class="container">
        <div class="member-join-card">
            <span class="member-eyebrow">SHARED TRIP</span>
            <h1><%= escapeHtml(trip.getTripTitle()) %></h1>
            <p><strong><%= escapeHtml(trip.getOwnerName()) %></strong>님이 여행 일정을 공유했습니다.</p>

            <dl class="member-trip-summary">
                <div><dt>목적지</dt><dd><%= escapeHtml(trip.getDestination()) %></dd></div>
                <div><dt>여행 기간</dt><dd><%= escapeHtml(trip.getTripPeriod()) %></dd></div>
                <div><dt>참여 권한</dt><dd>열람자</dd></div>
            </dl>

            <p class="member-join-notice">
                참여하면 내 여행 일정 목록에 추가됩니다. 편집 권한은 일정 작성자가 별도로 부여할 수 있습니다.
            </p>
            <div class="member-join-actions">
                <form action="${pageContext.request.contextPath}/share" method="post">
                    <input type="hidden" name="code" value="<%= escapeHtml(request.getAttribute("shareCode")) %>">
                    <button class="btn trip-primary-button" type="submit">일정에 참여하기</button>
                </form>
                <a class="btn trip-outline-button" href="${pageContext.request.contextPath}/trips">참여하지 않기</a>
            </div>
        </div>
    </div>
</section>

<%@ include file="/views/common/footer.jsp" %>
