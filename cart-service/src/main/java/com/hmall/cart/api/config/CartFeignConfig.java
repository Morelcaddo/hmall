package com.hmall.cart.api.config;

import com.hmall.cart.api.fallback.CartClientFallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Slf4j
public class CartFeignConfig {
    @Bean
    public CartClientFallback cartClientFallback() {
        return new CartClientFallback();
    }
}
