package com.medicollab.referral.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "medicollab.referral";
    public static final String QUEUE = "medi.referral.notify.queue";
    public static final String ROUTING_KEY = "referral.notify";

    @Bean
    public TopicExchange referralExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue referralNotifyQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding binding(Queue referralNotifyQueue, TopicExchange referralExchange) {
        return BindingBuilder.bind(referralNotifyQueue).to(referralExchange).with(ROUTING_KEY);
    }
}
