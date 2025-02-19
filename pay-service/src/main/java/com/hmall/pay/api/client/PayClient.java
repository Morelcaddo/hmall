package com.hmall.pay.api.client;

import com.hmall.common.config.DefaultFeignConfig;
import com.hmall.pay.api.config.PayFeignConfig;
import com.hmall.pay.api.fallback.PayClientFallback;
import com.hmall.pay.domain.dto.PayOrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "pay-service", fallbackFactory = PayClientFallback.class,
        configuration = {DefaultFeignConfig.class, PayFeignConfig.class})
public interface PayClient {
    /**
     * 根据交易订单id查询支付单
     * @param id 业务订单id
     * @return 支付单信息
     */
    @GetMapping("/pay-orders/biz/{id}")
    PayOrderDTO queryPayOrderByBizOrderNo(@PathVariable("id") Long id);
}