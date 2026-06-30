package com.medicollab.common.dto;


import java.io.Serializable;

/**
 * 用户信息 DTO (跨服务传递)
 */
public class UserDTO implements Serializable {
    public UserDTO() {}

    public UserDTO(Long id, String username, String realName, String phone, String role, Integer status, Long hospitalId, String hospitalName, Long departmentId, String departmentName) {
        this.id = id;
        this.username = username;
        this.realName = realName;
        this.phone = phone;
        this.role = role;
        this.status = status;
        this.hospitalId = hospitalId;
        this.hospitalName = hospitalName;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
    }

    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String role;
    private Integer status;
    private Long hospitalId;
    private String hospitalName;
    private Long departmentId;
    private String departmentName;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Long getHospitalId() { return hospitalId; }
    public void setHospitalId(Long hospitalId) { this.hospitalId = hospitalId; }
    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
}
