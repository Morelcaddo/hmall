package com.hmall.common.config;

import com.hmall.common.utils.UserContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import feign.Logger;
@Slf4j
public class DefaultFeignConfig {
    @Bean
    public Logger.Level feignLogLevel(){
        log.info("正在定义Feign的日志级别");
        return Logger.Level.FULL;
    }


    @Bean
    public RequestInterceptor userInfoRequestInterceptor(){
        log.info("正在定义Feign请求拦截器");
        return requestTemplate -> {
            Long userId = UserContext.getUser();
            if(userId != null){
                requestTemplate.header("user-info", userId.toString());
            }

        };
    }
}
