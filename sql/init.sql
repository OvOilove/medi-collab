-- ===================================================
-- MediCollab 智慧医疗协同平台 — 数据库初始化脚本
-- ===================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS medicollab_user DEFAULT CHARSET utf8mb4;
CREATE DATABASE IF NOT EXISTS medicollab_appointment DEFAULT CHARSET utf8mb4;
CREATE DATABASE IF NOT EXISTS medicollab_record DEFAULT CHARSET utf8mb4;
CREATE DATABASE IF NOT EXISTS medicollab_referral DEFAULT CHARSET utf8mb4;

-- ===================================================
-- 用户服务表
-- ===================================================
USE medicollab_user;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(200) NOT NULL COMMENT '密码 (BCrypt加密)',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    id_card VARCHAR(18) COMMENT '身份证号',
    role VARCHAR(20) NOT NULL DEFAULT 'PATIENT' COMMENT '角色: ADMIN/DOCTOR/PATIENT',
    status INT DEFAULT 1 COMMENT '状态: 1-正常 0-禁用',
    hospital_id BIGINT COMMENT '所属医院ID',
    hospital_name VARCHAR(100) COMMENT '所属医院名称',
    department_id BIGINT COMMENT '所属科室ID',
    department_name VARCHAR(100) COMMENT '所属科室名称',
    title VARCHAR(50) COMMENT '职称',
    deleted INT DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_role (role),
    INDEX idx_hospital (hospital_id)
) COMMENT '系统用户表';

-- 初始化数据
INSERT INTO sys_user (username, password, real_name, phone, role, status, hospital_name, department_name, title) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '系统管理员', '13800000000', 'ADMIN', 1, '医联体管理中心', '信息科', '高级工程师'),
('doctor_zhang', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '张医生', '13800000001', 'DOCTOR', 1, '第一人民医院', '心内科', '主任医师'),
('doctor_li', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '李医生', '13800000002', 'DOCTOR', 1, '第二人民医院', '骨科', '副主任医师'),
('doctor_wang', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '王医生', '13800000003', 'DOCTOR', 1, '社区医院', '全科', '主治医师'),
('patient_zhao', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '赵患者', '13900000001', 'PATIENT', 1, NULL, NULL, NULL),
('patient_qian', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '钱患者', '13900000002', 'PATIENT', 1, NULL, NULL, NULL);

-- ===================================================
-- 预约服务表
-- ===================================================
USE medicollab_appointment;

CREATE TABLE IF NOT EXISTS department (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '科室名称',
    description VARCHAR(500) COMMENT '科室描述',
    hospital_id BIGINT COMMENT '所属医院ID',
    hospital_name VARCHAR(100) COMMENT '所属医院名称',
    location VARCHAR(100) COMMENT '科室位置',
    max_daily_appointments INT DEFAULT 100 COMMENT '每日最大预约数',
    deleted INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '科室表';

CREATE TABLE IF NOT EXISTS doctor_schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    doctor_name VARCHAR(50) COMMENT '医生姓名',
    department_id BIGINT COMMENT '科室ID',
    department_name VARCHAR(100) COMMENT '科室名称',
    hospital_id BIGINT COMMENT '医院ID',
    hospital_name VARCHAR(100) COMMENT '医院名称',
    work_date DATE NOT NULL COMMENT '出诊日期',
    start_time TIME COMMENT '开始时间',
    end_time TIME COMMENT '结束时间',
    max_patients INT DEFAULT 50 COMMENT '最大接诊数',
    booked_count INT DEFAULT 0 COMMENT '已预约数',
    status INT DEFAULT 1 COMMENT '1-有效 0-停诊',
    deleted INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_doctor_date (doctor_id, work_date),
    INDEX idx_dept (department_id)
) COMMENT '医生排班表';

CREATE TABLE IF NOT EXISTS appointment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    patient_name VARCHAR(50) COMMENT '患者姓名',
    doctor_id BIGINT COMMENT '医生ID',
    doctor_name VARCHAR(50) COMMENT '医生姓名',
    department_id BIGINT COMMENT '科室ID',
    department_name VARCHAR(100) COMMENT '科室名称',
    hospital_id BIGINT COMMENT '医院ID',
    hospital_name VARCHAR(100) COMMENT '医院名称',
    schedule_id BIGINT COMMENT '排班ID',
    appointment_date DATE COMMENT '预约日期',
    time_slot TIME COMMENT '时段',
    queue_number INT COMMENT '排队号',
    status VARCHAR(20) DEFAULT 'BOOKED' COMMENT '状态: BOOKED/CANCELLED/COMPLETED',
    remark VARCHAR(500) COMMENT '备注',
    deleted INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_patient (patient_id),
    INDEX idx_doctor (doctor_id),
    INDEX idx_status (status)
) COMMENT '预约挂号表';

-- 初始化科室
INSERT INTO department (name, description, hospital_id, hospital_name, location) VALUES
('心内科', '心血管疾病诊治', 1, '第一人民医院', '门诊楼3层A区'),
('骨科', '骨骼关节疾病诊治', 1, '第一人民医院', '门诊楼2层B区'),
('神经内科', '神经系统疾病诊治', 2, '第二人民医院', '门诊楼1层C区'),
('全科', '常见病综合诊治', 3, '社区医院', '一楼全科诊室'),
('儿科', '儿童疾病诊治', 2, '第二人民医院', '门诊楼4层D区');

-- 初始化排班
INSERT INTO doctor_schedule (doctor_id, doctor_name, department_id, department_name, hospital_id, hospital_name, work_date, start_time, end_time, max_patients) VALUES
(2, '张医生', 1, '心内科', 1, '第一人民医院', CURDATE(), '08:00', '12:00', 30),
(2, '张医生', 1, '心内科', 1, '第一人民医院', DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00', '12:00', 30),
(3, '李医生', 2, '骨科', 1, '第一人民医院', CURDATE(), '13:00', '17:00', 20),
(4, '王医生', 4, '全科', 3, '社区医院', CURDATE(), '08:00', '17:00', 50);

-- ===================================================
-- 病历服务表
-- ===================================================
USE medicollab_record;

CREATE TABLE IF NOT EXISTS medical_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    patient_name VARCHAR(50) COMMENT '患者姓名',
    doctor_id BIGINT COMMENT '医生ID',
    doctor_name VARCHAR(50) COMMENT '医生姓名',
    department_id BIGINT COMMENT '科室ID',
    department_name VARCHAR(100) COMMENT '科室名称',
    hospital_id BIGINT COMMENT '医院ID',
    hospital_name VARCHAR(100) COMMENT '医院名称',
    chief_complaint VARCHAR(500) COMMENT '主诉',
    present_illness TEXT COMMENT '现病史',
    past_history TEXT COMMENT '既往史',
    diagnosis VARCHAR(500) COMMENT '诊断',
    treatment_plan TEXT COMMENT '治疗方案',
    prescription TEXT COMMENT '处方',
    advice TEXT COMMENT '医嘱',
    audit_hash VARCHAR(64) COMMENT '审计哈希 (SHA-256)',
    deleted INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_patient (patient_id),
    INDEX idx_doctor (doctor_id)
) COMMENT '电子病历表';

CREATE TABLE IF NOT EXISTS exam_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    patient_name VARCHAR(50) COMMENT '患者姓名',
    doctor_id BIGINT COMMENT '开单医生ID',
    doctor_name VARCHAR(50) COMMENT '开单医生姓名',
    exam_type VARCHAR(50) COMMENT '检查类型: CT/X光/超声/血常规',
    exam_part VARCHAR(100) COMMENT '检查部位',
    findings TEXT COMMENT '检查所见',
    conclusion VARCHAR(500) COMMENT '诊断结论',
    image_urls TEXT COMMENT '影像URL (JSON)',
    status VARCHAR(20) DEFAULT 'PUBLISHED' COMMENT 'DRAFT/PUBLISHED',
    record_id BIGINT COMMENT '关联病历ID',
    prev_hash VARCHAR(64) COMMENT '前一条哈希',
    current_hash VARCHAR(64) COMMENT '当前报告哈希',
    deleted INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_patient (patient_id),
    INDEX idx_prev_hash (prev_hash)
) COMMENT '检查报告表（含哈希审计链）';

-- ===================================================
-- 转诊服务表
-- ===================================================
USE medicollab_referral;

CREATE TABLE IF NOT EXISTS referral (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    patient_name VARCHAR(50) COMMENT '患者姓名',
    from_doctor_id BIGINT COMMENT '转出医生ID',
    from_doctor_name VARCHAR(50) COMMENT '转出医生姓名',
    from_dept_id BIGINT COMMENT '转出科室ID',
    from_dept_name VARCHAR(100) COMMENT '转出科室名称',
    from_hospital_id BIGINT COMMENT '转出医院ID',
    from_hospital_name VARCHAR(100) COMMENT '转出医院名称',
    to_dept_id BIGINT COMMENT '转入科室ID',
    to_dept_name VARCHAR(100) COMMENT '转入科室名称',
    to_hospital_id BIGINT COMMENT '转入医院ID',
    to_hospital_name VARCHAR(100) COMMENT '转入医院名称',
    to_doctor_id BIGINT COMMENT '接收医生ID',
    to_doctor_name VARCHAR(50) COMMENT '接收医生姓名',
    referral_reason VARCHAR(500) COMMENT '转诊原因',
    diagnosis VARCHAR(500) COMMENT '初步诊断',
    attachments TEXT COMMENT '附带材料',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING/APPROVED/REJECTED/COMPLETED',
    reject_reason VARCHAR(500) COMMENT '拒绝原因',
    feedback TEXT COMMENT '回转反馈',
    deleted INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_patient (patient_id),
    INDEX idx_from_hospital (from_hospital_id),
    INDEX idx_to_hospital (to_hospital_id),
    INDEX idx_status (status)
) COMMENT '转诊记录表';
