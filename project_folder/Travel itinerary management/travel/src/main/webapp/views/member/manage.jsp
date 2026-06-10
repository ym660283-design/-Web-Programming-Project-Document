<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.List" %>
<%@ page import="kr.hnu.ice.travel.dto.TripDTO" %>
<%@ page import="kr.hnu.ice.travel.dto.TripMemberDTO" %>
<%!
    private String escapeHtml(Object value) {
        if (value == null) return "";
        return value.toString().replace("&", "&amp;").replace("\"", "&quot;")
                .replace("'", "&#39;").replace("<", "&lt;").replace(">", "&gt;");
    }
%>
<%
    request.setAttribute("pageTitle", "참여자 관리");
    TripDTO trip = (TripDTO) request.getAttribute("trip");
    List<TripMemberDTO> members = (List<TripMemberDTO>) request.getAttribute("members");
    if (members == null) members = Collections.emptyList();
%>
<%@ include file="/views/common/header.jsp" %>

<section class="page-section trip-section">
    <div class="container">
        <div class="trip-detail-nav">
            <a href="${pageContext.request.contextPath}/trips">여행 일정</a>
            <span>/</span>
            <a href="${pageContext.request.contextPath}/trip-details?trip_id=<%= trip.getTripId() %>">
                <%= escapeHtml(trip.getTripTitle()) %>
            </a>
            <span>/</span>
            <strong>참여자 관리</strong>
        </div>

        <div class="member-page-heading">
            <div>
                <span class="member-eyebrow">MEMBER &amp; PERMISSION</span>
                <h1>참여자 관리</h1>
                <p>공유 링크, 참여자 초대와 일정 편집 권한을 관리합니다.</p>
            </div>
            <a class="btn trip-outline-button"
               href="${pageContext.request.contextPath}/trip-details?trip_id=<%= trip.getTripId() %>">일정으로 돌아가기</a>
        </div>

        <% if (request.getAttribute("successMessage") != null) { %>
            <div class="alert alert-success"><%= escapeHtml(request.getAttribute("successMessage")) %></div>
        <% } %>
        <% if (request.getAttribute("errorMessage") != null) { %>
            <div class="alert alert-danger"><%= escapeHtml(request.getAttribute("errorMessage")) %></div>
        <% } %>

        <div class="member-management-grid">
            <section class="member-panel">
                <span class="member-eyebrow">SHARE LINK</span>
                <h2>공유 링크</h2>
                <p>친구에게 링크를 전달하세요. 로그인한 친구가 참여에 동의할 때만 열람자로 추가됩니다.</p>
                <div class="member-share-box">
                    <input class="form-control" id="shareLink" type="text"
                           value="<%= escapeHtml(request.getAttribute("shareLink")) %>" readonly>
                    <button class="btn trip-primary-button" type="button" data-copy-share-link>링크 복사</button>
                </div>
                <form action="${pageContext.request.contextPath}/members" method="post">
                    <input type="hidden" name="action" value="regenerateLink">
                    <input type="hidden" name="trip_id" value="<%= trip.getTripId() %>">
                    <button class="btn trip-outline-button" type="submit">공유 링크 재발급</button>
                </form>
            </section>

            <section class="member-panel">
                <span class="member-eyebrow">INVITATION FLOW</span>
                <h2>친구 초대 방법</h2>
                <p>공유 링크를 메신저로 보내면 친구가 여행 정보를 확인한 뒤 직접 참여 여부를 선택합니다.</p>
                <ol class="member-invite-steps">
                    <li>공유 링크를 복사해 친구에게 전달합니다.</li>
                    <li>친구가 로그인하고 참여 동의 화면을 확인합니다.</li>
                    <li>친구가 동의하면 참여자 목록에 추가됩니다.</li>
                </ol>
            </section>
        </div>

        <section class="member-panel member-list-panel">
            <div class="member-list-heading">
                <div>
                    <span class="member-eyebrow">PARTICIPANTS</span>
                    <h2>참여자 <%= members.size() %>명</h2>
                </div>
                <p>편집자는 여행 기본 정보와 세부 일정을 수정할 수 있습니다.</p>
            </div>

            <% if (members.isEmpty()) { %>
                <div class="member-empty">아직 참여자가 없습니다. 공유 링크를 친구에게 전달하세요.</div>
            <% } else { %>
                <div class="member-list">
                    <% for (TripMemberDTO member : members) { %>
                        <article class="member-item">
                            <div class="member-avatar"><%= escapeHtml(
                                    member.getUserName() == null || member.getUserName().isEmpty()
                                            ? "?"
                                            : member.getUserName().substring(0, 1)) %></div>
                            <div class="member-identity">
                                <strong><%= escapeHtml(member.getUserName()) %></strong>
                                <span><%= escapeHtml(member.getLoginId()) %> · <%= escapeHtml(member.getEmail()) %></span>
                                <small><%= escapeHtml(member.getJoinedAtText()) %> 참여</small>
                            </div>
                            <form class="member-role-form" action="${pageContext.request.contextPath}/members" method="post">
                                <input type="hidden" name="action" value="updateRole">
                                <input type="hidden" name="trip_id" value="<%= trip.getTripId() %>">
                                <input type="hidden" name="member_id" value="<%= member.getMemberId() %>">
                                <select class="form-select" name="role" aria-label="참여자 권한">
                                    <option value="viewer"<%= "viewer".equals(member.getRole()) ? " selected" : "" %>>열람자</option>
                                    <option value="editor"<%= "editor".equals(member.getRole()) ? " selected" : "" %>>편집자</option>
                                </select>
                                <button class="btn trip-edit-button" type="submit">권한 저장</button>
                            </form>
                            <form action="${pageContext.request.contextPath}/members" method="post">
                                <input type="hidden" name="action" value="remove">
                                <input type="hidden" name="trip_id" value="<%= trip.getTripId() %>">
                                <input type="hidden" name="member_id" value="<%= member.getMemberId() %>">
                                <button class="btn trip-delete-button" type="submit">내보내기</button>
                            </form>
                        </article>
                    <% } %>
                </div>
            <% } %>
        </section>
    </div>
</section>

<script>
document.addEventListener("DOMContentLoaded", function () {
    var button = document.querySelector("[data-copy-share-link]");
    var input = document.getElementById("shareLink");
    if (!button || !input) return;
    button.addEventListener("click", function () {
        input.select();
        if (navigator.clipboard && window.isSecureContext) {
            navigator.clipboard.writeText(input.value);
        } else {
            document.execCommand("copy");
        }
        button.textContent = "복사됨";
    });
});
</script>

<%@ include file="/views/common/footer.jsp" %>
