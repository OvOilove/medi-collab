package com.medicollab.appointment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 预约挂号服务 — 启动类
 */
@SpringBootApplication(scanBasePackages = {"com.medicollab.appointment", "com.medicollab.common"})
@EnableDiscoveryClient
@EnableFeignClients
public class MediAppointmentApplication {
    public static void main(String[] args) {
        SpringApplication.run(MediAppointmentApplication.class, args);
    }
}
