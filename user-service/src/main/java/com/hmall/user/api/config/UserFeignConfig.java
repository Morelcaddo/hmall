package com.hmall.user.api.config;

import com.hmall.user.api.fallback.UserClientFallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class UserFeignConfig {
    @Bean
    public UserClientFallback userClientFallback() {
        return new UserClientFallback();
    }
}
