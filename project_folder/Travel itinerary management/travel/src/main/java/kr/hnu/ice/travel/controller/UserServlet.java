package kr.hnu.ice.travel.controller;

import kr.hnu.ice.travel.dao.UserDAO;
import kr.hnu.ice.travel.dto.UserDTO;
import kr.hnu.ice.travel.util.SessionNames;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.regex.Pattern;

@WebServlet(urlPatterns = {"/login", "/logout", "/register", "/user/check-login-id"})
public class UserServlet extends HttpServlet {
    private static final String LOGIN_VIEW = "/views/user/login.jsp";
    private static final String REGISTER_VIEW = "/views/user/register.jsp";
    private static final String TRIP_LIST_VIEW = "/views/trip/list.jsp";
    private static final Pattern LOGIN_ID_PATTERN = Pattern.compile("(?=.*[A-Za-z])[A-Za-z0-9]{4,20}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String path = request.getServletPath();

        if ("/login".equals(path)) {
            showLoginForm(request, response);
            return;
        }

        if ("/register".equals(path)) {
            showRegisterForm(request, response);
            return;
        }

        if ("/user/check-login-id".equals(path)) {
            checkLoginId(request, response);
            return;
        }

        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");

        String path = request.getServletPath();

        if ("/login".equals(path)) {
            login(request, response);
            return;
        }

        if ("/logout".equals(path)) {
            logout(request, response);
            return;
        }

        if ("/register".equals(path)) {
            register(request, response);
            return;
        }

        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private void showLoginForm(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute(SessionNames.LOGIN_USER) != null) {
            response.sendRedirect(request.getContextPath() + TRIP_LIST_VIEW);
            return;
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher(LOGIN_VIEW);
        dispatcher.forward(request, response);
    }

    private void showRegisterForm(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute(SessionNames.LOGIN_USER) != null) {
            response.sendRedirect(request.getContextPath() + TRIP_LIST_VIEW);
            return;
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher(REGISTER_VIEW);
        dispatcher.forward(request, response);
    }

    private void login(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String loginId = request.getParameter("login_id");
        String password = request.getParameter("password");

        try {
            UserDTO loginUser = userDAO.findByLoginIdAndPassword(loginId, password);

            if (loginUser == null) {
                request.setAttribute("errorMessage", "아이디 또는 비밀번호가 올바르지 않습니다.");
                request.getRequestDispatcher(LOGIN_VIEW).forward(request, response);
                return;
            }

            request.getSession(true).setAttribute(SessionNames.LOGIN_USER, loginUser);
            response.sendRedirect(request.getContextPath() + TRIP_LIST_VIEW);
        } catch (SQLException e) {
            request.setAttribute("errorMessage", "로그인 처리 중 오류가 발생했습니다.");
            request.getRequestDispatcher(LOGIN_VIEW).forward(request, response);
        }
    }

    private void register(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String loginId = trim(request.getParameter("login_id"));
        String password = request.getParameter("password");
        String passwordConfirm = request.getParameter("password_confirm");
        String userName = trim(request.getParameter("user_name"));
        String email = trim(request.getParameter("email"));
        String loginIdChecked = trim(request.getParameter("login_id_checked"));
        String checkedLoginId = trim(request.getParameter("checked_login_id"));

        String validationMessage = validateRegistration(
                loginId, password, passwordConfirm, userName, email, loginIdChecked, checkedLoginId);
        if (validationMessage != null) {
            forwardRegisterError(request, response, validationMessage);
            return;
        }

        try {
            if (userDAO.existsByLoginId(loginId)) {
                forwardRegisterError(request, response, "이미 사용 중인 아이디입니다.");
                return;
            }

            UserDTO user = new UserDTO();
            user.setLoginId(loginId);
            user.setPassword(password);
            user.setUserName(userName);
            user.setEmail(email);

            userDAO.insert(user);
            response.sendRedirect(request.getContextPath() + REGISTER_VIEW + "?registered=true");
        } catch (SQLException e) {
            request.setAttribute("errorMessage", "회원가입 처리 중 오류가 발생했습니다.");
            request.getRequestDispatcher(REGISTER_VIEW).forward(request, response);
        }
    }

    private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        response.sendRedirect(request.getContextPath() + LOGIN_VIEW + "?logout=1");
    }

    private void checkLoginId(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String loginId = trim(request.getParameter("login_id"));
        response.setContentType("application/json; charset=UTF-8");

        try (PrintWriter writer = response.getWriter()) {
            if (!isValidLoginId(loginId)) {
                writer.write("{\"available\":false,\"message\":\"아이디는 영문을 포함하여 4~20자의 영문과 숫자로 입력해주세요.\"}");
                return;
            }

            try {
                if (userDAO.existsByLoginId(loginId)) {
                    writer.write("{\"available\":false,\"message\":\"이미 사용 중인 아이디입니다.\"}");
                } else {
                    writer.write("{\"available\":true,\"message\":\"사용 가능한 아이디입니다.\"}");
                }
            } catch (SQLException e) {
                throw new ServletException("아이디 중복 확인 중 오류가 발생했습니다.", e);
            }
        }
    }

    private String validateRegistration(String loginId, String password, String passwordConfirm,
                                        String userName, String email,
                                        String loginIdChecked, String checkedLoginId) {
        if (!isValidLoginId(loginId)) {
            return "아이디는 영문을 포함하여 4~20자의 영문과 숫자로 입력해주세요.";
        }
        if (!"true".equals(loginIdChecked) || !loginId.equals(checkedLoginId)) {
            return "아이디 중복확인을 먼저 완료해주세요.";
        }
        if (userName.isEmpty()) {
            return "이름을 입력해주세요.";
        }
        if (email.isEmpty() || !EMAIL_PATTERN.matcher(email).matches()) {
            return "올바른 이메일 주소를 입력해주세요.";
        }
        if (password == null || password.length() < 8) {
            return "비밀번호를 8자 이상 입력해주세요.";
        }
        if (!password.equals(passwordConfirm)) {
            return "비밀번호가 일치하지 않습니다.";
        }
        return null;
    }

    private boolean isValidLoginId(String loginId) {
        return loginId != null && LOGIN_ID_PATTERN.matcher(loginId).matches();
    }

    private void forwardRegisterError(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException, ServletException {
        request.setAttribute("errorMessage", message);
        request.getRequestDispatcher(REGISTER_VIEW).forward(request, response);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
