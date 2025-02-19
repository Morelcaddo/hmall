package com.hmall.pay;

import com.hmall.common.config.DefaultFeignConfig;
import com.hmall.user.api.client.UserClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan("com.hmall.pay.mapper")
@SpringBootApplication
@EnableFeignClients(clients = {UserClient.class})
public class PayApplication {
    public static void main(String[] args) {
        SpringApplication.run(PayApplication.class, args);
    }
}