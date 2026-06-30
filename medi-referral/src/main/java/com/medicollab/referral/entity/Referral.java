package com.medicollab.referral.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 转诊记录实体
 */
@TableName("referral")
public class Referral implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long patientId;
    private String patientName;
    private Long fromDoctorId;
    private String fromDoctorName;
    private Long fromDeptId;
    private String fromDeptName;
    private Long fromHospitalId;
    private String fromHospitalName;
    private Long toDeptId;
    private String toDeptName;
    private Long toHospitalId;
    private String toHospitalName;
    private Long toDoctorId;
    private String toDoctorName;
    private String referralReason;      // 转诊原因
    private String diagnosis;           // 初步诊断
    private String attachments;         // 附带材料JSON
    private String status;              // PENDING/APPROVED/REJECTED/COMPLETED
    private String rejectReason;        // 拒绝原因
    private String feedback;            // 回转反馈

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public Long getFromDoctorId() { return fromDoctorId; }
    public void setFromDoctorId(Long fromDoctorId) { this.fromDoctorId = fromDoctorId; }
    public String getFromDoctorName() { return fromDoctorName; }
    public void setFromDoctorName(String fromDoctorName) { this.fromDoctorName = fromDoctorName; }
    public Long getFromDeptId() { return fromDeptId; }
    public void setFromDeptId(Long fromDeptId) { this.fromDeptId = fromDeptId; }
    public String getFromDeptName() { return fromDeptName; }
    public void setFromDeptName(String fromDeptName) { this.fromDeptName = fromDeptName; }
    public Long getFromHospitalId() { return fromHospitalId; }
    public void setFromHospitalId(Long fromHospitalId) { this.fromHospitalId = fromHospitalId; }
    public String getFromHospitalName() { return fromHospitalName; }
    public void setFromHospitalName(String fromHospitalName) { this.fromHospitalName = fromHospitalName; }
    public Long getToDeptId() { return toDeptId; }
    public void setToDeptId(Long toDeptId) { this.toDeptId = toDeptId; }
    public String getToDeptName() { return toDeptName; }
    public void setToDeptName(String toDeptName) { this.toDeptName = toDeptName; }
    public Long getToHospitalId() { return toHospitalId; }
    public void setToHospitalId(Long toHospitalId) { this.toHospitalId = toHospitalId; }
    public String getToHospitalName() { return toHospitalName; }
    public void setToHospitalName(String toHospitalName) { this.toHospitalName = toHospitalName; }
    public Long getToDoctorId() { return toDoctorId; }
    public void setToDoctorId(Long toDoctorId) { this.toDoctorId = toDoctorId; }
    public String getToDoctorName() { return toDoctorName; }
    public void setToDoctorName(String toDoctorName) { this.toDoctorName = toDoctorName; }
    public String getReferralReason() { return referralReason; }
    public void setReferralReason(String referralReason) { this.referralReason = referralReason; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getAttachments() { return attachments; }
    public void setAttachments(String attachments) { this.attachments = attachments; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
