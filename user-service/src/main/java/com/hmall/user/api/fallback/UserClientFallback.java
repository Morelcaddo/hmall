package com.hmall.user.api.fallback;

import com.hmall.common.exception.BizIllegalException;
import com.hmall.user.api.client.UserClient;
import org.springframework.cloud.openfeign.FallbackFactory;

public class UserClientFallback implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable cause) {
        return (pw, amount) -> {
            throw new BizIllegalException("扣减余额失败！请稍后再试");
        };
    }
}
