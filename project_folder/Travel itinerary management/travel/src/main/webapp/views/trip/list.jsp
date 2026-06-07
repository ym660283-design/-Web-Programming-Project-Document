<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="kr.hnu.ice.travel.dto.UserDTO" %>
<%
    request.setAttribute("pageTitle", "여행 일정");
    UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
%>
<%@ include file="/views/common/header.jsp" %>

<section class="page-section">
    <div class="container">
        <div class="page-heading">
            <span class="auth-kicker">My Trips</span>
            <h1>여행 일정</h1>
            <p><%= loginUser.getUserName() %>님, 로그인되었습니다. 앞으로 이 화면에 여행 일정 목록을 연결하면 됩니다.</p>
        </div>
    </div>
</section>

<%@ include file="/views/common/footer.jsp" %>
