package com.medicollab.user.config;

import cn.hutool.crypto.digest.BCrypt;
import com.medicollab.user.entity.User;
import com.medicollab.user.mapper.UserMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Demo 数据初始化 — 仅 demo profile 生效
 */
@Component
@Profile("demo")
public class DemoDataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;

    public DemoDataInitializer(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public void run(String... args) {
        if (userMapper.selectCount(null) > 0) return;

        String hash = BCrypt.hashpw("123456", BCrypt.gensalt());

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(hash);
        admin.setRealName("管理员");
        admin.setPhone("13800000000");
        admin.setRole("ADMIN");
        admin.setStatus(1);
        admin.setHospitalName("医联体中心");
        admin.setDepartmentName("信息科");
        admin.setTitle("高级工程师");
        userMapper.insert(admin);

        User doctor = new User();
        doctor.setUsername("doctor_zhang");
        doctor.setPassword(hash);
        doctor.setRealName("张医生");
        doctor.setPhone("13800000001");
        doctor.setRole("DOCTOR");
        doctor.setStatus(1);
        doctor.setHospitalName("第一人民医院");
        doctor.setDepartmentName("心内科");
        doctor.setTitle("主任医师");
        userMapper.insert(doctor);

        User patient = new User();
        patient.setUsername("patient_zhao");
        patient.setPassword(hash);
        patient.setRealName("赵患者");
        patient.setPhone("13900000001");
        patient.setRole("PATIENT");
        patient.setStatus(1);
        userMapper.insert(patient);

        System.out.println("=== Demo 数据初始化完成: 3个用户 (密码均为123456) ===");
    }
}
