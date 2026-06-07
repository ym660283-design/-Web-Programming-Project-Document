<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    request.setAttribute("pageTitle", "여행 일정 상세");
%>
<%@ include file="/views/common/header.jsp" %>

<section class="page-section trip-section">
    <div class="container">
        <div class="trip-detail-nav">
            <a href="${pageContext.request.contextPath}/views/trip/list.jsp">여행 일정</a>
            <span>/</span>
            <strong>상세보기</strong>
        </div>

        <div class="trip-detail-hero">
            <div>
                <span class="trip-detail-label">${destination}</span>
                <h1>${tripTitle}</h1>
                <p>${description}</p>
            </div>
            <a class="btn trip-edit-button"
               href="${pageContext.request.contextPath}/trips?action=edit&trip_id=${tripId}">일정 수정</a>
        </div>

        <div class="trip-detail-grid">
            <aside class="trip-overview-card">
                <h2>여행 정보</h2>

                <dl class="trip-overview-list">
                    <div>
                        <dt>목적지</dt>
                        <dd>${destination}</dd>
                    </div>
                    <div>
                        <dt>여행 기간</dt>
                        <dd>${tripPeriod}</dd>
                    </div>
                    <div>
                        <dt>일정</dt>
                        <dd>${tripDuration}</dd>
                    </div>
                </dl>

                <div class="trip-overview-description">
                    <span>여행 설명</span>
                    <p>${description}</p>
                </div>
            </aside>

            <div class="trip-schedule-card">
                <div class="trip-schedule-heading">
                    <div>
                        <span>TRIP SCHEDULE</span>
                        <h2>세부 일정</h2>
                    </div>
                    <span class="trip-schedule-count">준비 중</span>
                </div>

                <div class="trip-empty-schedule">
                    <div class="trip-empty-icon" aria-hidden="true">+</div>
                    <h3>아직 등록된 세부 일정이 없습니다</h3>
                    <p>날짜별 장소와 시간 정보는 세부 일정 기능에서 추가할 수 있습니다.</p>
                </div>
            </div>
        </div>
    </div>
</section>

<%@ include file="/views/common/footer.jsp" %>
