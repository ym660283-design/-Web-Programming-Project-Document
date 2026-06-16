package kr.hnu.ice.travel.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 작성자: 김준섭
 * 기능 설명: 여행 참여자 정보를 화면과 데이터 계층 사이에서 전달하는 DTO 클래스입니다.
 */
public class TripMemberDTO {
    private static final DateTimeFormatter JOINED_AT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    private int memberId;
    private int tripId;
    private int userId;
    private String loginId;
    private String userName;
    private String email;
    private String role;
    private LocalDateTime joinedAt;

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public String getRoleLabel() {
        return "editor".equals(role) ? "편집자" : "열람자";
    }

    public String getJoinedAtText() {
        return joinedAt == null ? "" : joinedAt.format(JOINED_AT_FORMATTER);
    }
}
