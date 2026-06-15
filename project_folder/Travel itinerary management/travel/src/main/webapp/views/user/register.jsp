<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
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
    request.setAttribute("pageTitle", "회원가입");
%>
<%@ include file="/views/common/header.jsp" %>

<section class="auth-section">
    <div class="container">
        <div class="auth-card mx-auto">
            <div class="auth-heading text-center">
                <span class="auth-kicker">Join TripLog</span>
                <h1>여행을 함께 시작해요</h1>
                <p>계정을 만들고 여행 일정을 계획하고 공유해보세요.</p>
            </div>

            <% if (request.getAttribute("errorMessage") != null) { %>
                <div class="alert alert-danger" role="alert">
                    <%= request.getAttribute("errorMessage") %>
                </div>
            <% } %>
            <form id="registerForm"
                  action="${pageContext.request.contextPath}/register"
                  method="post"
                  novalidate>
                <div class="mb-3">
                    <label class="form-label" for="loginId">아이디</label>
                    <div class="input-group">
                        <input class="form-control"
                               id="loginId"
                               name="login_id"
                               type="text"
                               minlength="4"
                               maxlength="20"
                               pattern="(?=.*[A-Za-z])[A-Za-z0-9]+"
                               autocomplete="username"
                               value="<%= escapeHtml(request.getAttribute("loginId")) %>"
                               aria-describedby="loginIdHelp loginIdCheckMessage"
                               required>
                        <button class="btn btn-outline-primary"
                                id="loginIdCheckButton"
                                type="submit"
                                formaction="${pageContext.request.contextPath}/user/check-login-id"
                                formmethod="post"
                                formnovalidate>
                            중복확인
                        </button>
                    </div>
                    <div id="loginIdHelp" class="form-text">영문을 반드시 포함하여 4~20자로 입력해주세요.</div>
                    <div id="loginIdCheckMessage"
                         class="form-text <%= escapeHtml(request.getAttribute("loginIdCheckClass")) %>">
                        <%= escapeHtml(request.getAttribute("loginIdCheckMessage")) %>
                    </div>
                    <div class="invalid-feedback">영문을 포함한 아이디를 4~20자로 입력해주세요.</div>
                    <input id="loginIdChecked"
                           name="login_id_checked"
                           type="hidden"
                           value="<%= escapeHtml(request.getAttribute("loginIdChecked")) %>">
                    <input id="checkedLoginId"
                           name="checked_login_id"
                           type="hidden"
                           value="<%= escapeHtml(request.getAttribute("checkedLoginId")) %>">
                </div>

                <div class="mb-3">
                    <label class="form-label" for="userName">이름</label>
                    <input class="form-control"
                           id="userName"
                           name="user_name"
                           type="text"
                           maxlength="30"
                           autocomplete="name"
                           value="<%= escapeHtml(request.getAttribute("userName")) %>"
                           required>
                    <div class="invalid-feedback">이름을 입력해주세요.</div>
                </div>

                <div class="mb-3">
                    <label class="form-label" for="email">이메일</label>
                    <input class="form-control"
                           id="email"
                           name="email"
                           type="email"
                           maxlength="100"
                           autocomplete="email"
                           value="<%= escapeHtml(request.getAttribute("email")) %>"
                           required>
                    <div class="invalid-feedback">올바른 이메일 주소를 입력해주세요.</div>
                </div>

                <div class="row g-3 mb-4">
                    <div class="col-md-6">
                        <label class="form-label" for="password">비밀번호</label>
                        <input class="form-control"
                               id="password"
                               name="password"
                               type="password"
                               minlength="8"
                               maxlength="50"
                               autocomplete="new-password"
                               required>
                        <div class="invalid-feedback">비밀번호를 8자 이상 입력해주세요.</div>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label" for="passwordConfirm">비밀번호 확인</label>
                        <input class="form-control"
                               id="passwordConfirm"
                               name="password_confirm"
                               type="password"
                               minlength="8"
                               maxlength="50"
                               autocomplete="new-password"
                               required>
                        <div class="invalid-feedback">비밀번호가 일치하지 않습니다.</div>
                    </div>
                </div>

                <button class="btn auth-submit w-100" type="submit">회원가입</button>
            </form>

            <p class="auth-link text-center mb-0">
                이미 계정이 있나요?
                <a href="${pageContext.request.contextPath}/login">로그인</a>
            </p>
        </div>
    </div>
</section>

<%@ include file="/views/common/footer.jsp" %>
