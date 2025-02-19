package com.hmall.cart.api.client;

import com.hmall.cart.api.config.CartFeignConfig;
import com.hmall.cart.api.fallback.CartClientFallback;
import com.hmall.common.config.DefaultFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

@FeignClient(value = "cart-service", fallbackFactory = CartClientFallback.class,
        configuration = {DefaultFeignConfig.class, CartFeignConfig.class})

public interface CartClient {
    @DeleteMapping("/carts")
    void removeByItemIds(@RequestParam("ids") Collection<Long> ids);
}
