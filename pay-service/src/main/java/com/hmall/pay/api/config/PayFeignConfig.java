package com.hmall.pay.api.config;


import com.hmall.pay.api.fallback.PayClientFallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
public class PayFeignConfig {
    @Bean
    public PayClientFallback payClientFallback() {
        return new PayClientFallback();
    }
}
