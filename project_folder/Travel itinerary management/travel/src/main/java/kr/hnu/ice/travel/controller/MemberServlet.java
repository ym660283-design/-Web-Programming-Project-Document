package kr.hnu.ice.travel.controller;

import kr.hnu.ice.travel.dao.TripDAO;
import kr.hnu.ice.travel.dao.TripMemberDAO;
import kr.hnu.ice.travel.dto.TripDTO;
import kr.hnu.ice.travel.dto.UserDTO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

@WebServlet(urlPatterns = {"/share", "/members"})
public class MemberServlet extends HttpServlet {
    private static final int SHARE_CODE_RETRY_LIMIT = 5;
    private static final String PENDING_SHARE_CODE = "pendingShareCode";
    private static final String JOIN_VIEW = "/views/member/join.jsp";
    private static final String MANAGE_VIEW = "/views/member/manage.jsp";

    private final TripDAO tripDAO = new TripDAO();
    private final TripMemberDAO memberDAO = new TripMemberDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        if ("/share".equals(request.getServletPath())) {
            showShareInvitation(request, response);
            return;
        }
        if ("/members".equals(request.getServletPath())) {
            showMemberManagement(request, response);
            return;
        }
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        if ("/share".equals(request.getServletPath())) {
            joinSharedTrip(request, response);
            return;
        }
        if ("/members".equals(request.getServletPath())) {
            manageMembers(request, response);
            return;
        }
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private void showShareInvitation(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String shareCode = trim(request.getParameter("code"));
        if (shareCode.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "공유 코드가 올바르지 않습니다.");
            return;
        }

        UserDTO loginUser = UserServlet.getLoginUser(request);
        if (loginUser == null) {
            request.getSession(true).setAttribute(PENDING_SHARE_CODE, shareCode);
            UserServlet.redirectToLogin(request, response);
            return;
        }

        try {
            TripDTO trip = tripDAO.findByShareCode(shareCode, loginUser.getUserId());
            if (trip == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "유효하지 않거나 만료된 공유 링크입니다.");
                return;
            }
            if (trip.isOwner() || trip.getMemberRole() != null) {
                response.sendRedirect(detailPath(request, trip.getTripId(), "already-member"));
                return;
            }

            request.setAttribute("trip", trip);
            request.setAttribute("shareCode", shareCode);
            request.getRequestDispatcher(JOIN_VIEW).forward(request, response);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "공유 일정을 불러오는 중 오류가 발생했습니다.");
        }
    }

    private void joinSharedTrip(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        UserDTO loginUser = UserServlet.getLoginUser(request);
        if (loginUser == null) {
            String shareCode = trim(request.getParameter("code"));
            request.getSession(true).setAttribute(PENDING_SHARE_CODE, shareCode);
            UserServlet.redirectToLogin(request, response);
            return;
        }

        String shareCode = trim(request.getParameter("code"));
        try {
            TripDTO trip = tripDAO.findByShareCode(shareCode, loginUser.getUserId());
            if (trip == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "유효하지 않거나 만료된 공유 링크입니다.");
                return;
            }
            if (!trip.isOwner() && trip.getMemberRole() == null) {
                memberDAO.addMember(trip.getTripId(), loginUser.getUserId(), "viewer");
            }
            response.sendRedirect(detailPath(request, trip.getTripId(), "joined"));
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "여행 일정 참여 중 오류가 발생했습니다.");
        }
    }

    private void showMemberManagement(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        UserDTO loginUser = requireLogin(request, response);
        if (loginUser == null) {
            return;
        }

        int tripId = parsePositiveInt(request.getParameter("trip_id"));
        if (tripId < 1) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "여행 일정 번호가 올바르지 않습니다.");
            return;
        }

        try {
            TripDTO trip = tripDAO.findByIdAndOwnerId(tripId, loginUser.getUserId());
            if (trip == null) {
                redirectToTripDetail(request, response, tripId, "member-access-denied");
                return;
            }
            if (trip.getShareCode() == null || trip.getShareCode().trim().isEmpty()) {
                String shareCode = updateShareCodeWithRetry(tripId, loginUser.getUserId());
                trip.setShareCode(shareCode);
            }
            request.setAttribute("trip", trip);
            request.setAttribute("members", memberDAO.findByTripId(tripId));
            request.setAttribute("shareLink", createShareLink(request, trip.getShareCode()));
            setManagementMessage(request);
            request.getRequestDispatcher(MANAGE_VIEW).forward(request, response);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "참여자 정보를 불러오는 중 오류가 발생했습니다.");
        }
    }

    private void manageMembers(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        UserDTO loginUser = requireLogin(request, response);
        if (loginUser == null) {
            return;
        }

        int tripId = parsePositiveInt(request.getParameter("trip_id"));
        String action = trim(request.getParameter("action"));

        try {
            TripDTO trip = tripDAO.findByIdAndOwnerId(tripId, loginUser.getUserId());
            if (trip == null) {
                redirectToTripDetail(request, response, tripId, "member-access-denied");
                return;
            }

            String status;
            if ("updateRole".equals(action)) {
                status = updateRole(request, tripId);
            } else if ("remove".equals(action)) {
                status = removeMember(request, tripId);
            } else if ("regenerateLink".equals(action)) {
                updateShareCodeWithRetry(tripId, loginUser.getUserId());
                status = "link-regenerated";
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            response.sendRedirect(request.getContextPath() + "/members?trip_id=" + tripId + "&status=" + status);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "참여자 관리 중 오류가 발생했습니다.");
        }
    }

    private String updateRole(HttpServletRequest request, int tripId) throws SQLException {
        int memberId = parsePositiveInt(request.getParameter("member_id"));
        String role = trim(request.getParameter("role"));
        if (memberId < 1 || (!"viewer".equals(role) && !"editor".equals(role))) {
            return "invalid-request";
        }
        return memberDAO.updateRole(memberId, tripId, role) ? "role-updated" : "member-not-found";
    }

    private String removeMember(HttpServletRequest request, int tripId) throws SQLException {
        int memberId = parsePositiveInt(request.getParameter("member_id"));
        return memberId > 0 && memberDAO.remove(memberId, tripId) ? "removed" : "member-not-found";
    }

    private UserDTO requireLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserDTO loginUser = UserServlet.getLoginUser(request);
        if (loginUser == null) {
            UserServlet.redirectToLogin(request, response);
        }
        return loginUser;
    }

    private void setManagementMessage(HttpServletRequest request) {
        String status = trim(request.getParameter("status"));
        if ("role-updated".equals(status)) {
            request.setAttribute("successMessage", "참여자 권한을 변경했습니다.");
        } else if ("removed".equals(status)) {
            request.setAttribute("successMessage", "참여자를 일정에서 내보냈습니다.");
        } else if ("link-regenerated".equals(status)) {
            request.setAttribute("successMessage", "공유 링크를 재발급했습니다. 이전 링크는 더 이상 사용할 수 없습니다.");
        } else if ("invalid-request".equals(status) || "member-not-found".equals(status)) {
            request.setAttribute("errorMessage", "참여자 관리 요청이 올바르지 않습니다.");
        }
    }

    private String createShareLink(HttpServletRequest request, String shareCode) {
        StringBuilder link = new StringBuilder();
        link.append(request.getScheme()).append("://").append(request.getServerName());
        boolean defaultPort = ("http".equals(request.getScheme()) && request.getServerPort() == 80)
                || ("https".equals(request.getScheme()) && request.getServerPort() == 443);
        if (!defaultPort) {
            link.append(':').append(request.getServerPort());
        }
        return link.append(request.getContextPath()).append("/share?code=").append(shareCode).toString();
    }

    private String detailPath(HttpServletRequest request, int tripId, String status) {
        return request.getContextPath() + "/trip-details?trip_id=" + tripId + "&status=" + status;
    }

    private void redirectToTripDetail(HttpServletRequest request, HttpServletResponse response,
                                      int tripId, String status) throws IOException {
        response.sendRedirect(detailPath(request, tripId, status));
    }

    private String createShareCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private String updateShareCodeWithRetry(int tripId, int ownerId) throws SQLException {
        SQLException duplicateException = null;

        for (int attempt = 0; attempt < SHARE_CODE_RETRY_LIMIT; attempt++) {
            String shareCode = createShareCode();
            try {
                if (!tripDAO.updateShareCode(tripId, ownerId, shareCode)) {
                    throw new SQLException("공유 코드를 변경할 여행 일정을 찾을 수 없습니다.");
                }
                return shareCode;
            } catch (SQLException e) {
                if (!isDuplicateKey(e)) {
                    throw e;
                }
                duplicateException = e;
            }
        }

        throw duplicateException == null
                ? new SQLException("공유 코드 생성에 실패했습니다.")
                : duplicateException;
    }

    private boolean isDuplicateKey(SQLException e) {
        return e.getErrorCode() == 1062 && "23000".equals(e.getSQLState());
    }

    private int parsePositiveInt(String value) {
        try {
            return Integer.parseInt(trim(value));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
