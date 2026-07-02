CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(200) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    id_card VARCHAR(18),
    role VARCHAR(20) NOT NULL DEFAULT 'PATIENT',
    status INT DEFAULT 1,
    hospital_id BIGINT,
    hospital_name VARCHAR(100),
    department_id BIGINT,
    department_name VARCHAR(100),
    title VARCHAR(50),
    deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
