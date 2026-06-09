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
<%@ include file="/views/common/header.jsp" %>

<section class="page-section trip-section">
    <div class="container">
        <div class="trip-form-card mx-auto">
            <div class="auth-heading">
                <span class="auth-kicker">Trip Information</span>
                <h1>${formHeading}</h1>
                <p>${formDescription}</p>
            </div>

            <% if (request.getAttribute("errorMessage") != null) { %>
                <div class="alert alert-danger" role="alert">
                    <%= escapeHtml(request.getAttribute("errorMessage")) %>
                </div>
            <% } %>

            <form action="${pageContext.request.contextPath}/trips" method="post">
                <input type="hidden" name="action" value="<%= escapeHtml(request.getAttribute("formMode")) %>">
                <input type="hidden" name="trip_id" value="<%= escapeHtml(request.getAttribute("tripId")) %>">

                <div class="mb-3">
                    <label class="form-label" for="tripTitle">제목</label>
                    <input class="form-control"
                           id="tripTitle"
                           name="trip_title"
                           type="text"
                           maxlength="100"
                           value="<%= escapeHtml(request.getAttribute("tripTitle")) %>"
                           placeholder="예: 제주도 여름 여행"
                           required>
                </div>

                <div class="mb-3">
                    <label class="form-label" for="destination">목적지</label>
                    <input class="form-control"
                           id="destination"
                           name="destination"
                           type="text"
                           maxlength="100"
                           value="<%= escapeHtml(request.getAttribute("destination")) %>"
                           placeholder="예: 제주도"
                           required>
                </div>

                <div class="row g-3 mb-3">
                    <div class="col-md-6">
                        <label class="form-label" for="startDate">시작일</label>
                        <input class="form-control"
                               id="startDate"
                               name="start_date"
                               type="date"
                               value="<%= escapeHtml(request.getAttribute("startDate")) %>"
                               required>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label" for="endDate">종료일</label>
                        <input class="form-control"
                               id="endDate"
                               name="end_date"
                               type="date"
                               value="<%= escapeHtml(request.getAttribute("endDate")) %>"
                               required>
                    </div>
                </div>

                <div class="mb-4">
                    <label class="form-label" for="description">설명</label>
                    <textarea class="form-control trip-description-input"
                              id="description"
                              name="description"
                              maxlength="1000"
                              placeholder="여행 목적이나 함께할 사람, 기대하는 활동을 적어보세요;"><%= escapeHtml(request.getAttribute("description")) %></textarea>
                </div>

                <div class="d-flex flex-column-reverse flex-sm-row justify-content-end gap-2">
                    <a class="btn trip-cancel-button"
                       href="${pageContext.request.contextPath}/trips">목록으로</a>
                    <button class="btn trip-primary-button" type="submit">${submitLabel}</button>
                </div>
            </form>
        </div>
    </div>
</section>

<%@ include file="/views/common/footer.jsp" %>
