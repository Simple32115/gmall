package com.atguigu.gmall.order.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//线程池配置类
@Configuration
public class ThreadPoolConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(@Value("${gmall.threadPool.corePoolSize}") Integer corePoolSize
            , @Value("${gmall.threadPool.maximunPoolSize}") Integer maximunPoolSize
            , @Value("${gmall.threadPool.keepAliveTime}") Integer keepAliveTime){

        return new ThreadPoolExecutor(corePoolSize, maximunPoolSize, keepAliveTime,
                TimeUnit.SECONDS, new LinkedBlockingDeque<>(Integer.MAX_VALUE/2));
    }
}
