<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    request.setAttribute("pageTitle", "로그인");
%>
<%@ include file="/views/common/header.jsp" %>

<section class="auth-section">
    <div class="container">
        <div class="auth-card auth-card-sm mx-auto">
            <div class="auth-heading text-center">
                <span class="auth-kicker">여러분의 길동무</span>
                <h1>TripLog</h1>
                <p>로그인하고 나의 여행 일정 목록으로 이동하세요.</p>
            </div>

            <% if (request.getAttribute("errorMessage") != null) { %>
                <div class="alert alert-danger" role="alert">
                    <%= request.getAttribute("errorMessage") %>
                </div>
            <% } %>

            <% if ("1".equals(request.getParameter("required"))) { %>
                <div class="alert alert-warning" role="alert">
                    로그인이 필요한 페이지입니다.
                </div>
            <% } %>

            <% if ("1".equals(request.getParameter("logout"))) { %>
                <div class="alert alert-success" role="alert">
                    로그아웃되었습니다.
                </div>
            <% } %>

            <form id="loginForm"
                  action="${pageContext.request.contextPath}/login"
                  method="post"
                  novalidate>
                <div class="mb-3">
                    <label class="form-label" for="loginId">아이디</label>
                    <input class="form-control"
                           id="loginId"
                           name="login_id"
                           type="text"
                           autocomplete="username"
                           required>
                    <div class="invalid-feedback">아이디를 입력해주세요.</div>
                </div>

                <div class="mb-4">
                    <label class="form-label" for="password">비밀번호</label>
                    <input class="form-control"
                           id="password"
                           name="password"
                           type="password"
                           autocomplete="current-password"
                           required>
                    <div class="invalid-feedback">비밀번호를 입력해주세요.</div>
                </div>

                <button class="btn auth-submit w-100" type="submit">로그인</button>
            </form>

            <p class="auth-link text-center mb-0">
                아직 계정이 없나요?
                <a href="${pageContext.request.contextPath}/views/user/register.jsp">회원가입</a>
            </p>
        </div>
    </div>
</section>

<script src="${pageContext.request.contextPath}/assets/js/login.js"></script>
<%@ include file="/views/common/footer.jsp" %>
