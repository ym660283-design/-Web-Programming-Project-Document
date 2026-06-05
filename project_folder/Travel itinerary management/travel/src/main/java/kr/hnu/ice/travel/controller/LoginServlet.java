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
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (request.getSession(false) != null
                && request.getSession(false).getAttribute(SessionNames.LOGIN_USER) != null) {
            response.sendRedirect(request.getContextPath() + "/views/trip/list.jsp");
            return;
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/views/user/login.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");

        String loginId = request.getParameter("login_id");
        String password = request.getParameter("password");

        try {
            UserDTO loginUser = userDAO.findByLoginIdAndPassword(loginId, password);

            if (loginUser == null) {
                request.setAttribute("errorMessage", "아이디 또는 비밀번호가 올바르지 않습니다.");
                request.getRequestDispatcher("/views/user/login.jsp").forward(request, response);
                return;
            }

            request.getSession(true).setAttribute(SessionNames.LOGIN_USER, loginUser);
            response.sendRedirect(request.getContextPath() + "/views/trip/list.jsp");
        } catch (SQLException e) {
            request.setAttribute("errorMessage", "로그인 처리 중 오류가 발생했습니다.");
            request.getRequestDispatcher("/views/user/login.jsp").forward(request, response);
        }
    }
}
