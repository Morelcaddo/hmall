package com.hmall.cart.listener;

import com.hmall.cart.service.ICartService;
import com.hmall.cart.service.impl.CartServiceImpl;
import com.hmall.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@Slf4j
public class CartClearListener {
    private final ICartService cartService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "cart.clear.queue", durable = "true",
                    arguments = @Argument(name = "x-queue-mode", value = "lazy")),
            exchange = @Exchange(name = "trade.topic"),
            key = {"order.create"}

    ))
    public void listenCartClear(Collection<Long> ids, Message message) {
        log.info("收到订单创建消息，准备清除购物车数据");
        cartService.removeByItemIds(ids);
    }
}
