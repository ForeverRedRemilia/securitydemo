package com.example.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


public class CryptFilter implements GatewayFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(CryptFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return CryptMono.cryptMono(exchange,chain);
    }

    @Override
    public int getOrder() {
        return -2;
    }

}
