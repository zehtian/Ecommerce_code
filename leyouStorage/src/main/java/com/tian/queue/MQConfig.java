package com.tian.queue;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class MQConfig {

    @Bean
    public Queue queueStorage(){
        //将该队列起名为storage_queue，并设置为持久化
        return new Queue("storage_queue", true);
    }

}
