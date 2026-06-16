package kr.hnu.ice.travel.dto;

import java.time.LocalDateTime;

/**
 * 작성자: 김용민
 * 기능 설명: 사용자 계정 정보를 화면과 데이터 계층 사이에서 전달하는 DTO 클래스입니다.
 */
public class UserDTO {
    private int userId;
    private String loginId;
    private String password;
    private String userName;
    private String email;
    private LocalDateTime createdAt;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
