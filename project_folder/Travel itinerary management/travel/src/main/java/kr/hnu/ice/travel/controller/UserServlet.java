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
import java.sql.SQLException;

@WebServlet(urlPatterns = {"/login", "/logout"})
public class UserServlet extends HttpServlet {
    private static final String LOGIN_VIEW = "/views/user/login.jsp";
    private static final String TRIP_LIST_VIEW = "/views/trip/list.jsp";

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String path = request.getServletPath();

        if ("/login".equals(path)) {
            showLoginForm(request, response);
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

    private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        response.sendRedirect(request.getContextPath() + LOGIN_VIEW + "?logout=1");
    }
}
