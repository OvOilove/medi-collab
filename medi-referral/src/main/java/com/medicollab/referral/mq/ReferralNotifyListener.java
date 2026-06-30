package com.medicollab.referral.mq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 消息监听器 — 处理转诊通知
 */
@Component
public class ReferralNotifyListener {
    private static final Logger log = LoggerFactory.getLogger(ReferralNotifyListener.class);


    @RabbitListener(queues = "medi.referral.notify.queue")
    public void handleReferralNotification(String message) {
        log.info("📨 收到转诊通知: {}", message);
        // 实际场景：推送消息给目标医院的医生
    }
}
