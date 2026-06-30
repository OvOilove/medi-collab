package com.medicollab.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 用户与认证服务 — 启动类
 */
@SpringBootApplication(scanBasePackages = {"com.medicollab.user", "com.medicollab.common"})
@EnableDiscoveryClient
public class MediUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(MediUserApplication.class, args);
    }
}
