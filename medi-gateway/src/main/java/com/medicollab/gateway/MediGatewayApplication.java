package com.medicollab.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.medicollab.gateway", "com.medicollab.common"})
@EnableDiscoveryClient
public class MediGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(MediGatewayApplication.class, args);
    }
}
