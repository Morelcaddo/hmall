package com.hmall.cart.api.fallback;

import com.hmall.cart.api.client.CartClient;
import com.hmall.common.exception.BizIllegalException;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collection;

public class CartClientFallback implements FallbackFactory<CartClient> {

    @Override
    public CartClient create(Throwable cause) {
        return ids -> {
            throw new BizIllegalException("清空购物车异常");
        };
    }
}
