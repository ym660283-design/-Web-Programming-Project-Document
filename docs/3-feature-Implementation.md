---
marp: true
paginate: true
style: |
  section {
    font-size: 23px;
  }
  table {
    font-size: 15px;
  }
  th, td {
    padding: 5px 7px;
  }
  code {
    font-size: 0.82em;
    overflow-wrap: anywhere;
  }
  .source {
    font-size: 18px;
  }
---

# 3. 기능 구현

## 여행 일정 공유 웹 서비스

Trip Planner Web

---

# 3.1 기능 구현 현황

## 사용자 관리

| 요구사항 번호 | 간단 설명 | 관련 소스 | 구현 여부 |
| --- | --- | --- | :---: |
| FR-01 | 아이디, 비밀번호, 이름, 이메일을 입력받아 회원 계정을 생성한다. | `project_folder/.../java/.../controller/UserServlet.java`<br>`project_folder/.../java/.../dao/UserDAO.java`<br>`project_folder/.../java/.../util/PasswordUtil.java`<br>`project_folder/.../webapp/views/user/register.jsp` | ✅ 완성 |
| FR-02 | 가입된 사용자의 아이디와 비밀번호를 검증하여 로그인 세션을 생성한다. | `project_folder/.../java/.../controller/UserServlet.java`<br>`project_folder/.../java/.../dao/UserDAO.java`<br>`project_folder/.../webapp/views/user/login.jsp` | ✅ 완성 |
| FR-03 | 로그인 세션을 종료하고 로그인 화면으로 이동한다. | `project_folder/.../java/.../controller/UserServlet.java`<br>`project_folder/.../webapp/views/common/header.jsp` | ✅ 완성 |

---

# 3.1 기능 구현 현황

## 여행 일정 관리

| 요구사항 번호 | 간단 설명 | 관련 소스 | 구현 여부 |
| --- | --- | --- | :---: |
| FR-04 | 제목, 목적지, 기간, 설명을 입력하여 여행 일정을 생성한다. | `project_folder/.../java/.../controller/TripServlet.java`<br>`project_folder/.../java/.../dao/TripDAO.java`<br>`project_folder/.../webapp/views/trip/form.jsp` | ✅ 완성 |
| FR-05 | 사용자가 작성했거나 참여 중인 여행 일정의 목록과 상세 정보를 조회한다. | `project_folder/.../java/.../controller/TripServlet.java`<br>`project_folder/.../java/.../controller/TripDetailServlet.java`<br>`project_folder/.../java/.../dao/TripDAO.java`<br>`project_folder/.../webapp/views/trip/list.jsp` | ✅ 완성 |
| FR-06 | 작성자 또는 편집 권한이 있는 참여자가 여행 정보를 수정한다. | `project_folder/.../java/.../controller/TripServlet.java`<br>`project_folder/.../java/.../dao/TripDAO.java`<br>`project_folder/.../webapp/views/trip/form.jsp` | ✅ 완성 |
| FR-07 | 여행 일정 작성자가 등록된 여행 일정을 삭제한다. | `project_folder/.../java/.../controller/TripServlet.java`<br>`project_folder/.../java/.../dao/TripDAO.java`<br>`project_folder/.../webapp/views/trip/list.jsp` | ✅ 완성 |

---

# 3.1 기능 구현 현황

## 날짜별 세부 일정 관리

| 요구사항 번호 | 간단 설명 | 관련 소스 | 구현 여부 |
| --- | --- | --- | :---: |
| FR-08 | 날짜, 장소, 시간, 메모, 비용, 위치를 입력하여 세부 일정을 등록한다. | `project_folder/.../java/.../controller/TripDetailServlet.java`<br>`project_folder/.../java/.../dao/TripDetailDAO.java`<br>`project_folder/.../webapp/views/trip/detail.jsp` | ✅ 완성 |
| FR-09 | 여행 날짜별로 저장된 세부 일정을 정렬하여 조회한다. | `project_folder/.../java/.../controller/TripDetailServlet.java`<br>`project_folder/.../java/.../dao/TripDetailDAO.java`<br>`project_folder/.../webapp/views/trip/detail.jsp` | ✅ 완성 |
| FR-10 | 편집 권한이 있는 사용자가 기존 세부 일정 정보를 수정한다. | `project_folder/.../java/.../controller/TripDetailServlet.java`<br>`project_folder/.../java/.../dao/TripDetailDAO.java`<br>`project_folder/.../webapp/views/trip/detail.jsp` | ✅ 완성 |
| FR-11 | 편집 권한이 있는 사용자가 불필요한 세부 일정을 삭제한다. | `project_folder/.../java/.../controller/TripDetailServlet.java`<br>`project_folder/.../java/.../dao/TripDetailDAO.java`<br>`project_folder/.../webapp/views/trip/detail.jsp` | ✅ 완성 |

---

# 3.1 기능 구현 현황

## 지도 및 공유 기능

| 요구사항 번호 | 간단 설명 | 관련 소스 | 구현 여부 |
| --- | --- | --- | :---: |
| FR-12 | 저장한 장소의 위도와 경도를 이용하여 Kakao 지도에 위치와 이동 동선을 표시한다. | `project_folder/.../webapp/assets/js/map.js`<br>`project_folder/.../webapp/views/trip/detail.jsp`<br>`project_folder/.../java/.../controller/TripDetailServlet.java` | ✅ 완성 |
| FR-13 | 일정별 공유 코드를 생성하고 참여용 공유 링크를 제공한다. | `project_folder/.../java/.../controller/MemberServlet.java`<br>`project_folder/.../java/.../dao/TripDAO.java`<br>`project_folder/.../webapp/views/member/manage.jsp` | ✅ 완성 |
| FR-14 | 공유 링크를 받은 로그인 사용자가 여행 일정에 참여하도록 초대 흐름을 제공한다. | `project_folder/.../java/.../controller/MemberServlet.java`<br>`project_folder/.../java/.../dao/TripMemberDAO.java`<br>`project_folder/.../webapp/views/member/join.jsp` | ✅ 완성 |
| FR-15 | 작성자가 참여자의 열람·편집 권한을 관리하고 권한에 따라 공동 편집을 제한한다. | `project_folder/.../java/.../controller/MemberServlet.java`<br>`project_folder/.../java/.../dto/TripDTO.java`<br>`project_folder/.../java/.../dao/TripMemberDAO.java`<br>`project_folder/.../webapp/views/member/manage.jsp` | ✅ 완성 |

---

# 3.2 구현 내용 설명

## FR-01: 회원가입

- **구현 여부:** ✅ 완성
- **관련 소스:** <span class="source">`UserServlet.register()`, `UserServlet.validateRegistration()`, `UserDAO.existsByLoginId()`, `UserDAO.insert()`, `PasswordUtil.hash()`, `views/user/register.jsp`</span>
- **설명:** 회원가입 폼에서 아이디, 이름, 이메일과 비밀번호를 입력받아 `UserServlet`로 전달한다. Servlet에서 입력 형식과 비밀번호 일치 여부를 검사하고 `UserDAO`로 아이디 중복을 확인한다. 검증을 통과하면 비밀번호를 PBKDF2 방식으로 암호화하여 사용자 정보와 함께 `users` 테이블에 저장한다.

---

# 3.2 구현 내용 설명

## FR-02: 로그인

- **구현 여부:** ✅ 완성
- **관련 소스:** <span class="source">`UserServlet.login()`, `UserDAO.findByLoginIdAndPassword()`, `PasswordUtil.matches()`, `views/user/login.jsp`</span>
- **설명:** 로그인 폼에서 입력한 아이디와 비밀번호를 `UserServlet`로 전달한다. `UserDAO`가 아이디로 사용자를 조회하고 입력한 비밀번호의 계산 결과를 DB의 암호화된 값과 비교한다. 인증에 성공하면 사용자 정보를 세션에 저장하여 이후 요청에서도 로그인 상태를 유지한다.

---

# 3.2 구현 내용 설명

## FR-03: 로그아웃

- **구현 여부:** ✅ 완성
- **관련 소스:** <span class="source">`UserServlet.logout()`, `views/common/header.jsp`</span>
- **설명:** 공통 헤더의 로그아웃 버튼을 누르면 요청이 `UserServlet.logout()`으로 전달된다. Servlet은 `invalidate()`를 호출하여 로그인 사용자와 임시 정보가 저장된 세션을 종료한다. 처리 후 로그아웃 완료 메시지와 함께 로그인 화면으로 이동한다.

---

# 3.2 구현 내용 설명

## FR-04: 여행 일정 생성

- **구현 여부:** ✅ 완성
- **관련 소스:** <span class="source">`TripServlet.createTrip()`, `TripServlet.insertTripWithUniqueShareCode()`, `TripDAO.insert()`, `views/trip/form.jsp`</span>
- **설명:** 일정 생성 폼에서 제목, 목적지, 시작일, 종료일과 설명을 입력받아 `TripServlet`로 전달한다. Servlet에서 로그인 여부와 여행 기간을 검증하고 입력 내용을 `TripDTO`에 담는다. 고유 공유 코드를 생성한 뒤 `TripDAO`를 통해 `trips` 테이블에 저장하고 상세 화면으로 이동한다.

---

# 3.2 구현 내용 설명

## FR-05: 여행 일정 조회

- **구현 여부:** ✅ 완성
- **관련 소스:** <span class="source">`TripServlet.showList()`, `TripDAO.findAccessibleTrips()`, `TripDetailServlet.showDetail()`, `TripDAO.findAccessibleById()`, `views/trip/list.jsp`, `views/trip/detail.jsp`</span>
- **설명:** `TripDAO`에서 사용자가 작성했거나 `trip_members`에 참여자로 등록된 일정을 함께 조회한다. 상세 요청에서는 작성자 또는 참여자인지 확인하여 접근 가능한 일정만 불러온다. 조회한 여행 기본 정보와 날짜별 세부 일정을 JSP에 전달하여 목록과 상세 화면에 표시한다.

---

# 3.2 구현 내용 설명

## FR-06: 여행 일정 수정

- **구현 여부:** ✅ 완성
- **관련 소스:** <span class="source">`TripServlet.showEditForm()`, `TripServlet.updateTrip()`, `TripDAO.update()`, `TripDTO.canEdit()`, `views/trip/form.jsp`
- **설명:** 수정 요청 시 일정 작성자 또는 `editor` 권한의 참여자인지 확인한다. 권한이 있으면 기존 여행 정보를 입력 폼에 표시하고 수정된 값과 여행 기간을 다시 검증한다. 검증을 통과하면 `TripDAO.update()`로 해당 여행 정보를 변경한다.

---

# 3.2 구현 내용 설명

## FR-07: 여행 일정 삭제

- **구현 여부:** ✅ 완성
- **관련 소스:** <span class="source">`TripServlet.deleteTrip()`, `TripDAO.delete()`, `views/trip/list.jsp`, `views/trip/detail.jsp`</span>
- **설명:** 삭제 버튼은 여행 일정 작성자에게만 표시되며 요청에는 삭제할 여행 번호가 전달된다. `TripServlet`과 `TripDAO`에서 로그인 사용자와 작성자가 같은지 확인한 뒤 해당 여행을 삭제한다. 외래키의 `ON DELETE CASCADE` 설정에 따라 연결된 세부 일정과 참여자 정보도 함께 삭제된다.

---

# 3.2 구현 내용 설명

## FR-08: 세부 일정 등록

- **구현 여부:** ✅ 완성
- **관련 소스:** <span class="source">`TripDetailServlet.createDetail()`, `TripDetailServlet.validateDetail()`, `TripDetailDAO.insert()`, `views/trip/detail.jsp`</span>
- **설명:** 상세 화면에서 날짜, 장소, 시간, 메모, 비용과 위치 정보를 입력받아 `TripDetailServlet`로 전달한다. Servlet에서 편집 권한, 여행 기간 내 날짜인지와 비용·좌표 값이 올바른지 검증한다. 검증된 정보는 여행 번호와 함께 `TripDetailDAO`를 통해 `trip_details` 테이블에 저장한다.

---

# 3.2 구현 내용 설명

## FR-09: 세부 일정 조회

- **구현 여부:** ✅ 완성
- **관련 소스:** <span class="source">`TripDetailServlet.showDetail()`, `TripDetailServlet.setScheduleDateAttributes()`, `TripDetailDAO.findByTripId()`, `views/trip/detail.jsp`</span>
- **설명:** `TripDetailDAO`에서 요청한 여행 번호에 해당하는 세부 일정만 조회한다. 조회 결과는 날짜, 사용자가 지정한 표시 순서와 방문 시간을 기준으로 정렬한다. 여행 기간을 일차별 탭으로 구성하고 선택한 날짜의 장소·시간·메모·비용·위치 정보를 출력한다.

---

# 3.2 구현 내용 설명

## FR-10: 세부 일정 수정

- **구현 여부:** ✅ 완성
- **관련 소스:** <span class="source">`TripDetailServlet.updateDetail()`, `TripDetailDAO.findByIdAndTripId()`, `TripDetailDAO.update()`, `views/trip/detail.jsp`</span>
- **설명:** 여행 번호와 세부 일정 번호를 함께 조회하여 해당 여행에 포함된 일정인지 확인한다. 기존 값을 수정 폼에 표시하고 편집 권한과 날짜·비용·위치 좌표를 다시 검증한다. 검증을 통과하면 `TripDetailDAO.update()`로 해당 세부 일정만 수정한다.

---

# 3.2 구현 내용 설명

## FR-11: 세부 일정 삭제

- **구현 여부:** ✅ 완성
- **관련 소스:** <span class="source">`TripDetailServlet.deleteDetail()`, `TripDetailDAO.delete()`, `views/trip/detail.jsp`</span>
- **설명:** 상세 화면에서 삭제할 여행 번호와 세부 일정 번호를 `TripDetailServlet`로 전달한다. Servlet에서 사용자의 편집 권한과 두 번호의 관계를 확인하고 `TripDetailDAO`에서 일치하는 일정만 삭제한다. 삭제 후 기존 날짜 탭으로 이동하여 처리 결과를 표시한다.

---

# 3.2 구현 내용 설명

## FR-12: 여행 장소 위치 표시

- **구현 여부:** ✅ 완성
- **관련 소스:** <span class="source">`assets/js/map.js`의 `initializeTripMap()`, `initializeLocationSearch()`, `loadRoadRoute()`, `TripDetailServlet.showTripRoute()`, `views/trip/detail.jsp`</span>
- **설명:** Kakao 장소 검색으로 선택한 장소의 위도와 경도를 세부 일정과 함께 DB에 저장한다. `map.js`가 저장된 좌표를 읽어 Kakao 지도에 일정 순서별 마커와 장소 정보창을 생성한다. 위치가 여러 개이면 도로 경로와 이동시간을 표시하며, API 호출 실패 시 직선거리 기준 예상값을 제공한다.

---

# 3.2 구현 내용 설명

## FR-13: 공유 링크 생성

- **구현 여부:** ✅ 완성
- **관련 소스:** <span class="source">`MemberServlet.createShareCode()`, `MemberServlet.createShareLink()`, `MemberServlet.updateShareCodeWithRetry()`, `TripDAO.updateShareCode()`, `views/member/manage.jsp`</span>
- **설명:** `MemberServlet`에서 UUID를 이용해 일정마다 다른 공유 코드를 생성한다. 생성한 코드를 `trips` 테이블에 저장하고 서버 주소와 결합하여 참여 링크를 만든다. 작성자가 링크를 재발급하면 DB의 공유 코드가 변경되어 이전 링크는 사용할 수 없다.

---

# 3.2 구현 내용 설명

## FR-14: 참여자 초대

- **구현 여부:** ✅ 완성
- **관련 소스:** <span class="source">`MemberServlet.showShareInvitation()`, `MemberServlet.joinSharedTrip()`, `TripDAO.findByShareCode()`, `TripMemberDAO.addMember()`, `views/member/join.jsp`</span>
- **설명:** 공유 링크로 접속하면 `MemberServlet`이 URL의 공유 코드로 대상 여행을 조회한다. 비로그인 사용자는 코드를 세션에 보관한 뒤 로그인하고, 초대 화면으로 다시 이동한다. 참여를 승인하면 사용자와 여행 정보를 `viewer` 권한으로 `trip_members`에 저장하며 중복 참여를 방지한다.

---

# 3.2 구현 내용 설명

## FR-15: 공동 편집 권한 관리

- **구현 여부:** ✅ 완성
- **관련 소스:** <span class="source">`MemberServlet.manageMembers()`, `MemberServlet.updateRole()`, `TripMemberDAO.updateRole()`, `TripDTO.canEdit()`, `views/member/manage.jsp`</span>
- **설명:** 여행 작성자는 관리 화면에서 참여자 역할을 `viewer` 또는 `editor`로 변경하거나 참여자를 제거할 수 있다. `TripDTO.canEdit()`은 작성자 또는 `editor`인지 판단하여 JSP의 수정 기능 표시 여부를 결정한다. Servlet에서도 권한을 다시 검사하여 작성자와 `editor`에게만 여행 및 세부 일정 수정을 허용한다.

---

# 3.3 테스트 방법

- **목적:** 실제 사용자가 여행을 만들고, 세부 일정을 등록하고, 다른 사용자를 초대하는 흐름을 시연하며 주요 요구사항을 함께 확인한다.
- **확인 방식:** 화면 전환, 저장 결과, 권한별 버튼 표시, 지도 표시 여부를 중심으로 확인한다.

---

## 시연 순서

1. **회원가입 및 로그인**
   - 새 계정을 가입하고 로그인한다.

2. **여행 일정 생성 및 조회**
   - 여행 제목, 목적지, 기간, 설명을 입력해 일정을 생성한다.
   - 목록과 상세 화면에서 저장된 일정이 보이는지 확인한다.

3. **세부 일정 등록 및 지도 확인**
   - 날짜별로 장소, 시간, 메모, 비용을 입력한다.
   - 장소 검색으로 위치를 선택하고 지도 마커와 동선이 표시되는지 확인한다.

4. **공유 링크와 공동 편집 확인**
   - 작성자 계정에서 공유 링크를 생성한다.
   - 참여자 계정으로 링크에 접속해 일정에 참여한다.
   - 작성자가 참여자 권한을 변경한 뒤 수정 가능 여부를 확인한다.


