package com.hmall.user.api.client;

import com.hmall.common.config.DefaultFeignConfig;
import com.hmall.user.api.config.UserFeignConfig;
import com.hmall.user.api.fallback.UserClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "user-service", fallbackFactory = UserClientFallback.class,
        configuration = {DefaultFeignConfig.class, UserFeignConfig.class})
public interface UserClient {
    @PutMapping("/users/money/deduct")
    void deductMoney(@RequestParam("pw") String pw, @RequestParam("amount") Integer amount);
}
