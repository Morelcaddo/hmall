package com.hmall.item.api.config;

import com.hmall.item.api.fallback.ItemClientFallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
@Slf4j
public class ItemFeignConfig {
    @Bean
    public ItemClientFallback itemClientFallback() {
        log.info("正在定义ItemClientFallback");
        return new ItemClientFallback();
    }
}
