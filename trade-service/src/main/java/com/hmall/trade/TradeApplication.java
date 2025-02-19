package com.hmall.trade;


import com.hmall.common.config.DefaultFeignConfig;
import com.hmall.item.api.client.ItemClient;
import com.hmall.pay.api.client.PayClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan("com.hmall.trade.mapper")
@SpringBootApplication
@EnableFeignClients(clients = {ItemClient.class, PayClient.class})
public class TradeApplication {
    public static void main(String[] args) {
        SpringApplication.run(TradeApplication.class, args);
    }
}