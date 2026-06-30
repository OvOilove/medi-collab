package com.medicollab.common.dto;


import java.io.Serializable;

/**
 * 登录响应
 */
public class LoginResponse implements Serializable {
    public LoginResponse() {}

    public LoginResponse(String token, Long userId, String username, String realName, String role) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.realName = realName;
        this.role = role;
    }

    private static final long serialVersionUID = 1L;

    private String token;
    private Long userId;
    private String username;
    private String realName;
    private String role;
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
