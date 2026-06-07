<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="kr.hnu.ice.travel.dto.UserDTO" %>
<%
    request.setAttribute("pageTitle", "여행 일정");
    UserDTO loginUser = (UserDTO) session.getAttribute("loginUser");
%>
<%@ include file="/views/common/header.jsp" %>

<section class="page-section trip-section">
    <div class="container">
        <div class="trip-page-header d-flex flex-column flex-lg-row justify-content-between align-items-lg-center gap-4">
            <div class="page-heading">
                <h1>여행 일정</h1>
                <p>
                    <% if (loginUser != null) { %>
                        <%= loginUser.getUserName() %>님의 여행 계획을 확인하고 관리하세요.
                    <% } else { %>
                        여행 계획을 확인하고 관리하세요.
                    <% } %>
                </p>
            </div>
            <a class="btn trip-primary-button"
               href="${pageContext.request.contextPath}/trips?action=create">
                <span aria-hidden="true">+</span>
                여행 일정 생성
            </a>
        </div>

        <div class="trip-tabs" role="tablist" aria-label="여행 일정 구분">
            <button class="trip-tab active"
                    id="myTripsTab"
                    type="button"
                    role="tab"
                    data-bs-toggle="tab"
                    data-bs-target="#myTrips"
                    aria-controls="myTrips"
                    aria-selected="true">
                내 여행 일정
                <span>0</span>
            </button>
            <button class="trip-tab"
                    id="sampleTripsTab"
                    type="button"
                    role="tab"
                    data-bs-toggle="tab"
                    data-bs-target="#sampleTrips"
                    aria-controls="sampleTrips"
                    aria-selected="false">
                예시 일정
                <span>2</span>
            </button>
        </div>

        <div class="tab-content">
            <div class="tab-pane fade show active"
                 id="myTrips"
                 role="tabpanel"
                 aria-labelledby="myTripsTab"
                 tabindex="0">
                <%-- DB 연동 후 실제 여행 일정 반복 출력 영역 --%>
                <div class="trip-empty-list">
                    <div class="trip-empty-list-icon" aria-hidden="true">+</div>
                    <h2>등록된 여행 일정이 없습니다</h2>
                    <p>새 여행 일정 생성 버튼을 눌러 첫 여행 계획을 만들어보세요.</p>
                    <a class="btn trip-primary-button"
                       href="${pageContext.request.contextPath}/trips?action=create">여행 일정 생성</a>
                </div>
            </div>

            <div class="tab-pane fade"
                 id="sampleTrips"
                 role="tabpanel"
                 aria-labelledby="sampleTripsTab"
                 tabindex="0">
                <div class="trip-sample-notice">
                    화면 구성을 확인하기 위한 예시 데이터입니다. 실제 일정과 별도로 표시됩니다.
                </div>

                <div class="trip-list">
                    <article class="trip-list-item">
                        <div class="trip-date-box" aria-label="여행 시작일 7월 15일">
                            <span>JUL</span>
                            <strong>15</strong>
                            <small>2026</small>
                        </div>

                        <div class="trip-list-content">
                            <div class="trip-list-meta">
                                <span class="trip-location">제주도</span>
                                <span class="trip-period">2026.07.15 - 2026.07.18</span>
                            </div>
                            <h2>제주도 여름 여행</h2>
                            <p>친구들과 함께 제주의 바다와 맛집을 둘러보는 여행입니다.</p>
                        </div>

                        <div class="trip-list-actions">
                            <a class="btn trip-outline-button"
                               href="${pageContext.request.contextPath}/trips?action=detail&trip_id=1">상세보기</a>
                            <a class="btn trip-edit-button"
                               href="${pageContext.request.contextPath}/trips?action=edit&trip_id=1">수정</a>
                        </div>
                    </article>

                    <article class="trip-list-item">
                        <div class="trip-date-box trip-date-box-dark" aria-label="여행 시작일 8월 8일">
                            <span>AUG</span>
                            <strong>08</strong>
                            <small>2026</small>
                        </div>

                        <div class="trip-list-content">
                            <div class="trip-list-meta">
                                <span class="trip-location">부산</span>
                                <span class="trip-period">2026.08.08 - 2026.08.09</span>
                            </div>
                            <h2>부산 주말 여행</h2>
                            <p>해운대와 광안리를 중심으로 여유롭게 즐기는 주말 일정입니다.</p>
                        </div>

                        <div class="trip-list-actions">
                            <a class="btn trip-outline-button"
                               href="${pageContext.request.contextPath}/trips?action=detail&trip_id=2">상세보기</a>
                            <a class="btn trip-edit-button"
                               href="${pageContext.request.contextPath}/trips?action=edit&trip_id=2">수정</a>
                        </div>
                    </article>
                </div>
            </div>
        </div>
    </div>
</section>

<%@ include file="/views/common/footer.jsp" %>
