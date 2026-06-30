package com.medicollab.referral;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.medicollab.referral", "com.medicollab.common"})
@EnableDiscoveryClient
@EnableFeignClients
public class MediReferralApplication {
    public static void main(String[] args) {
        SpringApplication.run(MediReferralApplication.class, args);
    }
}
