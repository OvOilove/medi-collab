package com.medicollab.common.constant;

/**
 * 系统常量
 */
public interface SystemConstants {

    /** JWT 密钥 (生产环境应从配置中心读取) */
    String JWT_SECRET = "MediCollab2024SecretKeyForJWTTokenGenerationAndValidation!!";

    /** JWT 过期时间 (毫秒) — 默认 7 天 */
    long JWT_EXPIRATION = 7 * 24 * 60 * 60 * 1000L;

    /** Token 前缀 */
    String TOKEN_PREFIX = "Bearer ";

    /** 请求头中 Token 的 key */
    String HEADER_TOKEN = "Authorization";

    /** 用户 ID 请求头 */
    String HEADER_USER_ID = "X-User-Id";

    /** 用户名请求头 */
    String HEADER_USERNAME = "X-Username";

    /** 角色请求头 */
    String HEADER_ROLE = "X-Role";

    /** 用户状态: 正常 */
    int USER_STATUS_NORMAL = 1;

    /** 用户状态: 禁用 */
    int USER_STATUS_DISABLED = 0;

    /** 角色: 超级管理员 */
    String ROLE_ADMIN = "ADMIN";

    /** 角色: 医生 */
    String ROLE_DOCTOR = "DOCTOR";

    /** 角色: 患者 */
    String ROLE_PATIENT = "PATIENT";

    /** 转诊状态: 待审批 */
    String REFERRAL_STATUS_PENDING = "PENDING";

    /** 转诊状态: 已批准 */
    String REFERRAL_STATUS_APPROVED = "APPROVED";

    /** 转诊状态: 已拒绝 */
    String REFERRAL_STATUS_REJECTED = "REJECTED";

    /** 转诊状态: 已完成 */
    String REFERRAL_STATUS_COMPLETED = "COMPLETED";

    /** 预约状态: 已预约 */
    String APPOINTMENT_STATUS_BOOKED = "BOOKED";

    /** 预约状态: 已取消 */
    String APPOINTMENT_STATUS_CANCELLED = "CANCELLED";

    /** 预约状态: 已完成 */
    String APPOINTMENT_STATUS_COMPLETED = "COMPLETED";

    /** Redis 缓存前缀: 医生 */
    String REDIS_DOCTOR_PREFIX = "medi:doctor:";

    /** Redis 缓存前缀: 科室热度 */
    String REDIS_DEPT_HOT = "medi:dept:hot";

    /** Redis 分布式锁前缀: 号源 */
    String REDIS_SLOT_LOCK = "medi:slot:lock:";
}
