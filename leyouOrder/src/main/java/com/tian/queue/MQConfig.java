package com.tian.queue;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConfig {

    @Bean
    public Queue queueOrder(){
        //初始化队列，起名order_queue
        return new Queue("order_queue", true);
    }

    @Bean
    public Queue queueStatusOrder(){
        //初始化队列，起名order_status_queue
        return new Queue("order_status_queue", true);
    }


}
