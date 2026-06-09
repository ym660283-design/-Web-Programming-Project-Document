<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="kr.hnu.ice.travel.dto.TripDetailDTO" %>
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
    request.setAttribute("pageTitle", "여행 일정 상세");

    List<String> scheduleDates = (List<String>) request.getAttribute("scheduleDates");
    List<String> scheduleDateLabels = (List<String>) request.getAttribute("scheduleDateLabels");
    List<TripDetailDTO> selectedScheduleDetails =
            (List<TripDetailDTO>) request.getAttribute("selectedScheduleDetails");
    Map<String, Integer> scheduleCounts = (Map<String, Integer>) request.getAttribute("scheduleCounts");

    if (scheduleDates == null) {
        scheduleDates = Collections.emptyList();
    }
    if (scheduleDateLabels == null) {
        scheduleDateLabels = Collections.emptyList();
    }
    if (selectedScheduleDetails == null) {
        selectedScheduleDetails = Collections.emptyList();
    }
    if (scheduleCounts == null) {
        scheduleCounts = Collections.emptyMap();
    }

    boolean isOwner = Boolean.TRUE.equals(request.getAttribute("isOwner"));
    int tripId = ((Integer) request.getAttribute("tripId")).intValue();
    int selectedDay = request.getAttribute("selectedDay") == null
            ? 1
            : ((Integer) request.getAttribute("selectedDay")).intValue();
    int selectedScheduleCount = selectedScheduleDetails.size();
    String selectedDateLabel = escapeHtml(request.getAttribute("selectedDateLabel"));
    String detailFormAction = escapeHtml(request.getAttribute("detailFormAction"));
    String detailSubmitLabel = escapeHtml(request.getAttribute("detailSubmitLabel"));
    String detailFormHeading = escapeHtml(request.getAttribute("detailFormHeading"));
    String detailFormNote = escapeHtml(request.getAttribute("detailFormNote"));
    String formScheduleDate = escapeHtml(request.getAttribute("formScheduleDate"));
    String formDetailId = escapeHtml(request.getAttribute("formDetailId"));
    String formPlaceName = escapeHtml(request.getAttribute("formPlaceName"));
    String formVisitTime = escapeHtml(request.getAttribute("formVisitTime"));
    String formCost = escapeHtml(request.getAttribute("formCost"));
    String formMemo = escapeHtml(request.getAttribute("formMemo"));
%>
<%@ include file="/views/common/header.jsp" %>

<section class="page-section trip-section">
    <div class="container">
        <div class="trip-detail-nav">
            <a href="${pageContext.request.contextPath}/trips">여행 일정</a>
            <span>/</span>
            <strong>상세보기</strong>
        </div>

        <% if (request.getAttribute("successMessage") != null) { %>
            <div class="alert alert-success" role="alert">
                <%= escapeHtml(request.getAttribute("successMessage")) %>
            </div>
        <% } %>

        <% if (request.getAttribute("errorMessage") != null) { %>
            <div class="alert alert-danger" role="alert">
                <%= escapeHtml(request.getAttribute("errorMessage")) %>
            </div>
        <% } %>

        <div class="trip-detail-hero">
            <div>
                <span class="trip-detail-label"><%= escapeHtml(request.getAttribute("destination")) %></span>
                <h1><%= escapeHtml(request.getAttribute("tripTitle")) %></h1>
                <p><%= escapeHtml(request.getAttribute("description")) %></p>
            </div>
            <% if (isOwner) { %>
                <div class="trip-detail-actions">
                    <a class="btn trip-edit-button"
                       href="${pageContext.request.contextPath}/trips?action=edit&amp;trip_id=<%= tripId %>">일정 수정</a>
                    <form action="${pageContext.request.contextPath}/trips" method="post">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="trip_id" value="<%= tripId %>">
                        <button class="btn trip-delete-button" type="submit">일정 삭제</button>
                    </form>
                </div>
            <% } %>
        </div>

        <div class="trip-detail-grid">
            <aside class="trip-overview-card">
                <h2>여행 정보</h2>

                <dl class="trip-overview-list">
                    <div>
                        <dt>목적지</dt>
                        <dd><%= escapeHtml(request.getAttribute("destination")) %></dd>
                    </div>
                    <div>
                        <dt>여행 기간</dt>
                        <dd><%= escapeHtml(request.getAttribute("tripPeriod")) %></dd>
                    </div>
                    <div>
                        <dt>일정</dt>
                        <dd><%= escapeHtml(request.getAttribute("tripDuration")) %></dd>
                    </div>
                    <div>
                        <dt>작성자</dt>
                        <dd><%= escapeHtml(request.getAttribute("ownerName")) %></dd>
                    </div>
                </dl>

                <div class="trip-overview-description">
                    <span>여행 설명</span>
                    <p><%= escapeHtml(request.getAttribute("description")) %></p>
                </div>
            </aside>

            <div class="trip-schedule-card">
                <div class="trip-schedule-heading">
                    <div>
                        <span>TRIP SCHEDULE</span>
                        <h2>DAY <%= selectedDay %> 일정</h2>
                        <small><%= selectedDateLabel %></small>
                    </div>
                    <span class="trip-schedule-count"><%= selectedScheduleCount %>개 일정</span>
                </div>

                <div class="schedule-viewer">
                    <div class="schedule-day-list schedule-day-list-viewer"
                         role="tablist" aria-label="여행 날짜 선택">
                        <% for (int i = 0; i < scheduleDates.size(); i++) {
                            int day = i + 1;
                            String activeClass = day == selectedDay ? " active" : "";
                            Integer dayCount = scheduleCounts.get(scheduleDates.get(i));
                            int count = dayCount == null ? 0 : dayCount.intValue();
                        %>
                            <a class="schedule-day-button<%= activeClass %>" role="tab"
                               href="${pageContext.request.contextPath}/trip-details?trip_id=<%= tripId %>&amp;day=<%= day %>"
                               aria-selected="<%= day == selectedDay ? "true" : "false" %>">
                                <span>DAY <%= day %></span>
                                <strong><%= escapeHtml(scheduleDateLabels.get(i)) %></strong>
                                <small><%= count %>개 일정</small>
                            </a>
                        <% } %>
                    </div>

                    <% if (selectedScheduleDetails.isEmpty()) { %>
                        <div class="schedule-empty-day">
                            <div class="trip-empty-icon" aria-hidden="true">+</div>
                            <h3>DAY <%= selectedDay %> 일정이 비어 있습니다</h3>
                            <p>아직 이 날짜에 등록된 세부 일정이 없습니다.</p>
                        </div>
                    <% } else { %>
                        <div class="schedule-timeline">
                            <% for (TripDetailDTO detail : selectedScheduleDetails) { %>
                                <article class="schedule-item">
                                    <time><%= escapeHtml(detail.getVisitTimeValue()) %></time>
                                    <div class="schedule-marker" aria-hidden="true"></div>
                                    <div class="schedule-item-content">
                                        <div>
                                            <span class="schedule-place-type">장소</span>
                                            <strong><%= escapeHtml(detail.getPlaceName()) %></strong>
                                        </div>
                                        <p><%= detail.getMemo() == null || detail.getMemo().trim().isEmpty()
                                                ? "등록된 메모가 없습니다."
                                                : escapeHtml(detail.getMemo()) %></p>
                                        <span class="schedule-cost">예상 비용 <%= escapeHtml(detail.getCostText()) %></span>
                                    </div>
                                </article>
                            <% } %>
                        </div>
                    <% } %>
                </div>
            </div>
        </div>

        <% if (isOwner) { %>
        <section class="schedule-manage-card">
            <div class="schedule-manage-toggle">
                <span class="schedule-manage-icon" aria-hidden="true">+</span>
                <span>
                    <small>EDITOR TOOLS</small>
                    <strong>세부 일정 등록 / 수정 / 삭제</strong>
                    <em>수정 권한이 있는 사용자를 위한 관리 영역입니다.</em>
                </span>
            </div>

            <div class="schedule-manage-body">
                <section class="schedule-form-card schedule-form-card-flat" aria-labelledby="scheduleFormHeading">
                    <div class="schedule-card-title">
                        <span>PLAN FORM</span>
                        <h2 id="scheduleFormHeading"><%= detailFormHeading %></h2>
                    </div>

                    <form action="${pageContext.request.contextPath}/trip-details" method="post">
                        <input type="hidden" name="action" value="<%= detailFormAction %>">
                        <input type="hidden" name="trip_id" value="<%= tripId %>">
                        <input type="hidden" name="detail_id" value="<%= formDetailId %>">

                        <div class="mb-3">
                            <label class="form-label" for="scheduleDate">일정 날짜</label>
                            <select class="form-select" id="scheduleDate" name="schedule_date" required>
                                <% for (int i = 0; i < scheduleDates.size(); i++) {
                                    int day = i + 1;
                                    String dateValue = scheduleDates.get(i);
                                    String selected = dateValue.equals(formScheduleDate) ? " selected" : "";
                                %>
                                    <option value="<%= escapeHtml(dateValue) %>"<%= selected %>>
                                        DAY <%= day %> - <%= escapeHtml(scheduleDateLabels.get(i)) %>
                                    </option>
                                <% } %>
                            </select>
                        </div>

                        <div class="mb-3">
                            <label class="form-label" for="placeName">
                                방문 장소 <span class="schedule-required-mark" aria-hidden="true">*</span>
                            </label>
                            <input class="form-control" id="placeName" name="place_name" type="text"
                                   maxlength="100"
                                   value="<%= formPlaceName %>"
                                   placeholder="예: 함덕해수욕장"
                                   required>
                        </div>

                        <div class="row g-3 mb-3">
                            <div class="col-sm-6">
                                <label class="form-label" for="visitTime">
                                    방문 시간 <span class="schedule-required-mark" aria-hidden="true">*</span>
                                </label>
                                <input class="form-control"
                                       id="visitTime"
                                       name="visit_time"
                                       type="time"
                                       value="<%= formVisitTime %>"
                                       required>
                            </div>
                            <div class="col-sm-6">
                                <label class="form-label" for="cost">예상 비용</label>
                                <div class="input-group">
                                    <input class="form-control" id="cost" name="cost" type="number"
                                           min="0" step="1000"
                                           value="<%= formCost %>"
                                           placeholder="0">
                                    <span class="input-group-text">원</span>
                                </div>
                            </div>
                        </div>

                        <div class="mb-4">
                            <label class="form-label" for="memo">메모</label>
                            <textarea class="form-control schedule-memo" id="memo" name="memo"
                                      placeholder="이동 방법이나 예약 정보를 적어보세요."><%= formMemo %></textarea>
                        </div>

                        <div class="schedule-form-actions">
                            <button class="btn trip-primary-button" type="submit"><%= detailSubmitLabel %></button>
                            <% if ("updateDetail".equals(request.getAttribute("detailFormAction"))) { %>
                                <a class="btn schedule-edit-cancel-button"
                                   href="${pageContext.request.contextPath}/trip-details?trip_id=<%= tripId %>&amp;day=<%= selectedDay %>">수정 취소</a>
                            <% } %>
                            <button class="btn schedule-reset-button" type="reset">입력 초기화</button>
                        </div>
                        <p class="schedule-form-note"><%= detailFormNote %></p>
                    </form>
                </section>

                <section class="schedule-manage-list" aria-labelledby="scheduleManageListHeading">
                    <div class="schedule-timeline-heading">
                        <div class="schedule-card-title">
                            <span>MANAGE PLANS</span>
                            <h2 id="scheduleManageListHeading">DAY <%= selectedDay %> 등록 일정 관리</h2>
                        </div>
                        <span class="schedule-count"><%= selectedScheduleCount %>개 일정</span>
                    </div>

                    <% if (selectedScheduleDetails.isEmpty()) { %>
                        <div class="schedule-manage-empty">
                            <div class="trip-empty-icon" aria-hidden="true">+</div>
                            <h3>DAY <%= selectedDay %>에 등록된 일정이 없습니다</h3>
                            <p>왼쪽 폼에서 이 날짜의 세부 일정을 등록하세요.</p>
                        </div>
                    <% } else { %>
                        <div class="schedule-manage-items">
                            <% for (TripDetailDTO detail : selectedScheduleDetails) { %>
                                <article class="schedule-manage-item">
                                    <div>
                                        <time><%= escapeHtml(detail.getVisitTimeValue()) %></time>
                                        <strong><%= escapeHtml(detail.getPlaceName()) %></strong>
                                        <span><%= escapeHtml(detail.getMemo()) %> · <%= escapeHtml(detail.getCostText()) %></span>
                                    </div>
                                    <div class="schedule-manage-actions">
                                        <a class="btn trip-edit-button"
                                           href="${pageContext.request.contextPath}/trip-details?trip_id=<%= tripId %>&amp;day=<%= selectedDay %>&amp;action=editDetail&amp;detail_id=<%= detail.getDetailId() %>">수정</a>
                                        <form action="${pageContext.request.contextPath}/trip-details" method="post">
                                            <input type="hidden" name="action" value="deleteDetail">
                                            <input type="hidden" name="trip_id" value="<%= tripId %>">
                                            <input type="hidden" name="detail_id" value="<%= detail.getDetailId() %>">
                                            <input type="hidden" name="day" value="<%= selectedDay %>">
                                            <button class="btn schedule-delete-button" type="submit">삭제</button>
                                        </form>
                                    </div>
                                </article>
                            <% } %>
                        </div>
                    <% } %>
                </section>
            </div>
        </section>
        <% } %>
    </div>
</section>

<%@ include file="/views/common/footer.jsp" %>
