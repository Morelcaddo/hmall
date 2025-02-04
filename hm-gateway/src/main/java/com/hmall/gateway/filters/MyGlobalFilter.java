package com.hmall.gateway.filters;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class MyGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // TODO 模拟登录校验的逻辑
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();
        //System.out.println("Headers=" + headers);
        return chain.filter(exchange);
    }

    //现在我们要把这个过滤器放入过滤器链，且优先级要在NettyRoutingFilter之前，
    //只需要实现Ordered接口，并实现getOrder方法即可，改方法的返回值越小优先级越高。
    //NettyRoutingFilter优先级最靠后值int的最大值，所以设置为0即可
    @Override
    public int getOrder() {
        return 0;
    }
}
