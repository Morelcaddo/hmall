package com.hmall.item.api.config;

import com.hmall.item.api.fallback.ItemClientFallback;
import feign.Logger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ItemFeignConfig {
    @Bean
    public ItemClientFallback itemClientFallback() {
        log.info("正在定义ItemClientFallback");
        return new ItemClientFallback();
    }

    @Bean
    public Logger.Level feignLogLevel(){
        log.info("正在定义Feign的日志级别");
        return Logger.Level.NONE;
    }
}
