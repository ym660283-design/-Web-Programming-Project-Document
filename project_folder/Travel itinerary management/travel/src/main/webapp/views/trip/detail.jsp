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
                        <h2 id="timelineHeading">DAY 1 일정</h2>
                    </div>
                    <span class="trip-schedule-count" id="scheduleCount">3개 일정</span>
                </div>

                <div class="schedule-viewer">
                    <div class="schedule-day-list schedule-day-list-viewer"
                         id="scheduleViewDayList"
                         role="tablist" aria-label="여행 날짜 선택">
                        <button class="schedule-day-button active" type="button" role="tab"
                                data-schedule-day="1" data-schedule-date="${scheduleDate1}"
                                data-schedule-label="${scheduleDateLabel1}" aria-selected="true">
                            <span>DAY 1</span>
                            <strong>${scheduleDateLabel1}</strong>
                        </button>
                        <button class="schedule-day-button" type="button" role="tab"
                                data-schedule-day="2" data-schedule-date="${scheduleDate2}"
                                data-schedule-label="${scheduleDateLabel2}" aria-selected="false">
                            <span>DAY 2</span>
                            <strong>${scheduleDateLabel2}</strong>
                        </button>
                        <% if (((Integer) request.getAttribute("scheduleDayCount")) > 2) { %>
                        <button class="schedule-day-button" type="button" role="tab"
                                data-schedule-day="3" data-schedule-date="${scheduleDate3}"
                                data-schedule-label="${scheduleDateLabel3}" aria-selected="false">
                            <span>DAY 3</span>
                            <strong>${scheduleDateLabel3}</strong>
                        </button>
                        <button class="schedule-day-button" type="button" role="tab"
                                data-schedule-day="4" data-schedule-date="${scheduleDate4}"
                                data-schedule-label="${scheduleDateLabel4}" aria-selected="false">
                            <span>DAY 4</span>
                            <strong>${scheduleDateLabel4}</strong>
                        </button>
                        <% } %>
                    </div>

                    <div class="schedule-timeline" data-day-panel="1">
                        <article class="schedule-item">
                            <time>10:00</time>
                            <div class="schedule-marker" aria-hidden="true"></div>
                            <div class="schedule-item-content">
                                <div><span class="schedule-place-type">관광</span><strong>함덕해수욕장</strong></div>
                                <p>바닷가 산책 후 근처 카페에서 잠시 쉬기</p>
                                <span class="schedule-cost">예상 비용 10,000원</span>
                            </div>
                        </article>
                        <article class="schedule-item">
                            <time>13:00</time>
                            <div class="schedule-marker" aria-hidden="true"></div>
                            <div class="schedule-item-content">
                                <div><span class="schedule-place-type">식사</span><strong>동문시장</strong></div>
                                <p>제주 향토 음식으로 점심 식사</p>
                                <span class="schedule-cost">예상 비용 25,000원</span>
                            </div>
                        </article>
                        <article class="schedule-item">
                            <time>16:30</time>
                            <div class="schedule-marker" aria-hidden="true"></div>
                            <div class="schedule-item-content">
                                <div><span class="schedule-place-type">카페</span><strong>해안 카페</strong></div>
                                <p>노을을 보며 하루 일정 정리</p>
                                <span class="schedule-cost">예상 비용 12,000원</span>
                            </div>
                        </article>
                    </div>

                    <div class="schedule-empty-day d-none" data-empty-day>
                        <div class="trip-empty-icon" aria-hidden="true">+</div>
                        <h3 id="emptyDayHeading">DAY 2 일정이 비어 있습니다</h3>
                        <p>아직 이 날짜에 등록된 세부 일정이 없습니다.</p>
                    </div>
                </div>
            </div>
        </div>

        <section class="schedule-manage-card">
            <button class="schedule-manage-toggle collapsed"
                    type="button"
                    data-bs-toggle="collapse"
                    data-bs-target="#scheduleManagePanel"
                    aria-expanded="false"
                    aria-controls="scheduleManagePanel">
                <span class="schedule-manage-icon" aria-hidden="true">+</span>
                <span>
                    <small>EDITOR TOOLS</small>
                    <strong>세부 일정 등록 / 수정 / 삭제</strong>
                    <em>수정 권한이 있는 사용자를 위한 관리 영역입니다.</em>
                </span>
                <span class="schedule-manage-arrow" aria-hidden="true">⌄</span>
            </button>

            <div class="collapse" id="scheduleManagePanel">
                <div class="schedule-manage-body">
                    <section class="schedule-form-card schedule-form-card-flat" aria-labelledby="scheduleFormHeading">
                        <div class="schedule-card-title">
                            <span>NEW PLAN</span>
                            <h2 id="scheduleFormHeading">세부 일정 등록 / 수정</h2>
                        </div>

                        <form>
                            <input id="scheduleDetailId" type="hidden" name="detail_id" value="">
                            <div class="mb-3">
                                <span class="form-label d-block">일정 날짜</span>
                                <input id="scheduleDate" type="hidden" name="schedule_date" value="${scheduleDate1}">
                                <div class="dropdown schedule-date-dropdown">
                                    <button class="schedule-date-dropdown-toggle"
                                            id="scheduleDateDropdown"
                                            type="button"
                                            data-bs-toggle="dropdown"
                                            aria-expanded="false">
                                        <span>
                                            <small id="selectedManageDay">DAY 1</small>
                                            <strong id="selectedManageDate">${scheduleDateLabel1}</strong>
                                        </span>
                                        <span class="schedule-date-dropdown-arrow" aria-hidden="true">⌄</span>
                                    </button>

                                    <div class="dropdown-menu schedule-form-day-list"
                                         id="scheduleManageDayList"
                                         aria-labelledby="scheduleDateDropdown">
                                    <button class="dropdown-item schedule-form-day-button active"
                                            type="button"
                                            data-form-schedule-day="1"
                                            data-form-schedule-date="${scheduleDate1}"
                                            data-form-schedule-label="${scheduleDateLabel1}"
                                            aria-selected="true">
                                        <span>DAY 1</span>
                                        <strong>${scheduleDateLabel1}</strong>
                                    </button>
                                    <button class="dropdown-item schedule-form-day-button"
                                            type="button"
                                            data-form-schedule-day="2"
                                            data-form-schedule-date="${scheduleDate2}"
                                            data-form-schedule-label="${scheduleDateLabel2}"
                                            aria-selected="false">
                                        <span>DAY 2</span>
                                        <strong>${scheduleDateLabel2}</strong>
                                    </button>
                                    <% if (((Integer) request.getAttribute("scheduleDayCount")) > 2) { %>
                                    <button class="dropdown-item schedule-form-day-button"
                                            type="button"
                                            data-form-schedule-day="3"
                                            data-form-schedule-date="${scheduleDate3}"
                                            data-form-schedule-label="${scheduleDateLabel3}"
                                            aria-selected="false">
                                        <span>DAY 3</span>
                                        <strong>${scheduleDateLabel3}</strong>
                                    </button>
                                    <button class="dropdown-item schedule-form-day-button"
                                            type="button"
                                            data-form-schedule-day="4"
                                            data-form-schedule-date="${scheduleDate4}"
                                            data-form-schedule-label="${scheduleDateLabel4}"
                                            aria-selected="false">
                                        <span>DAY 4</span>
                                        <strong>${scheduleDateLabel4}</strong>
                                    </button>
                                    <% } %>
                                    </div>
                                </div>
                            </div>
                            <div class="mb-3">
                                <label class="form-label" for="placeName">
                                    방문 장소 <span class="schedule-required-mark" aria-hidden="true">*</span>
                                </label>
                                <input class="form-control" id="placeName" name="place_name" type="text"
                                       placeholder="예: 함덕해수욕장"
                                       aria-describedby="placeNameError"
                                       required>
                                <div class="invalid-feedback" id="placeNameError">필수 입력 칸입니다.</div>
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
                                           aria-describedby="visitTimeError"
                                           required>
                                    <div class="invalid-feedback" id="visitTimeError">필수 입력 칸입니다.</div>
                                </div>
                                <div class="col-sm-6">
                                    <label class="form-label" for="cost">예상 비용</label>
                                    <div class="input-group">
                                        <input class="form-control" id="cost" name="cost" type="number"
                                               min="0" step="1000" placeholder="0">
                                        <span class="input-group-text">원</span>
                                    </div>
                                </div>
                            </div>
                            <div class="mb-4">
                                <label class="form-label" for="memo">메모</label>
                                <textarea class="form-control schedule-memo" id="memo" name="memo"
                                          placeholder="이동 방법이나 예약 정보를 적어보세요."></textarea>
                            </div>
                            <div class="schedule-form-actions">
                                <button class="btn trip-primary-button flex-fill"
                                        id="scheduleSubmitButton"
                                        type="button">세부 일정 등록</button>
                                <button class="btn schedule-edit-cancel-button d-none"
                                        id="scheduleEditCancel"
                                        type="button">수정 취소</button>
                                <button class="btn schedule-reset-button"
                                        id="scheduleFormReset"
                                        type="button">입력 초기화</button>
                            </div>
                            <p class="schedule-form-note" id="scheduleFormNote">
                                현재는 화면 예시이며 입력 내용은 저장되지 않습니다.
                            </p>
                        </form>
                    </section>

                    <section class="schedule-manage-list" aria-labelledby="scheduleManageListHeading">
                        <div class="schedule-timeline-heading">
                            <div class="schedule-card-title">
                                <span>MANAGE PLANS</span>
                                <h2 id="scheduleManageListHeading">DAY 1 등록 일정 관리</h2>
                            </div>
                            <span class="schedule-count" id="manageScheduleCount">3개 일정</span>
                        </div>

                        <div class="schedule-manage-items" data-manage-day-panel="1">
                            <article class="schedule-manage-item">
                                <div>
                                    <time>10:00</time>
                                    <strong>함덕해수욕장</strong>
                                    <span>관광 · 10,000원</span>
                                </div>
                                <div class="schedule-manage-actions">
                                    <button class="btn trip-edit-button"
                                            type="button"
                                            data-edit-schedule
                                            data-detail-id="1"
                                            data-day="1"
                                            data-place="함덕해수욕장"
                                            data-time="10:00"
                                            data-cost="10000"
                                            data-memo="바닷가 산책 후 근처 카페에서 잠시 쉬기">수정</button>
                                    <button class="btn schedule-delete-button" type="button">삭제</button>
                                </div>
                            </article>
                            <article class="schedule-manage-item">
                                <div>
                                    <time>13:00</time>
                                    <strong>동문시장</strong>
                                    <span>식사 · 25,000원</span>
                                </div>
                                <div class="schedule-manage-actions">
                                    <button class="btn trip-edit-button"
                                            type="button"
                                            data-edit-schedule
                                            data-detail-id="2"
                                            data-day="1"
                                            data-place="동문시장"
                                            data-time="13:00"
                                            data-cost="25000"
                                            data-memo="제주 향토 음식으로 점심 식사">수정</button>
                                    <button class="btn schedule-delete-button" type="button">삭제</button>
                                </div>
                            </article>
                            <article class="schedule-manage-item">
                                <div>
                                    <time>16:30</time>
                                    <strong>해안 카페</strong>
                                    <span>카페 · 12,000원</span>
                                </div>
                                <div class="schedule-manage-actions">
                                    <button class="btn trip-edit-button"
                                            type="button"
                                            data-edit-schedule
                                            data-detail-id="3"
                                            data-day="1"
                                            data-place="해안 카페"
                                            data-time="16:30"
                                            data-cost="12000"
                                            data-memo="노을을 보며 하루 일정 정리">수정</button>
                                    <button class="btn schedule-delete-button" type="button">삭제</button>
                                </div>
                            </article>
                        </div>

                        <div class="schedule-manage-empty d-none" data-manage-empty>
                            <div class="trip-empty-icon" aria-hidden="true">+</div>
                            <h3 id="manageEmptyHeading">DAY 2에 등록된 일정이 없습니다</h3>
                            <p>상단에서 선택한 날짜에 등록된 일정이 여기에 표시됩니다.</p>
                        </div>
                    </section>
                </div>
            </div>
        </section>
    </div>
</section>

<script src="${pageContext.request.contextPath}/assets/js/trip.js?v=1"></script>
<%@ include file="/views/common/footer.jsp" %>
