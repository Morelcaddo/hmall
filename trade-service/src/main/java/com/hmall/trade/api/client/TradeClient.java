package com.hmall.trade.api.client;

import com.hmall.common.config.DefaultFeignConfig;
import com.hmall.trade.domain.po.Order;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(value = "trade-service", configuration = DefaultFeignConfig.class)
public interface TradeClient {
    @PutMapping("/orders")
    void updateById(Order order);
}
