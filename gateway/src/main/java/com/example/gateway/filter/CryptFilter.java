package com.example.gateway.filter;

import com.example.gateway.redis.RedisUtil;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class CryptFilter implements GatewayFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(CryptFilter.class);

    @Resource
    private RedisUtil redisUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponse serverHttpResponse = exchange.getResponse();
        /*Boolean aBoolean = redisUtil.setToken("safhasfjioj", "");
        Map<String, Object> map = new LinkedHashMap<>();
        if (null == aBoolean || !aBoolean) {
            serverHttpResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
            logger.info("重放攻击");
            map.put("status", "-1");
            map.put("msg", "重放攻击！");
        } else {
            serverHttpResponse.setStatusCode(HttpStatus.OK);
            logger.info("正常访问");
            map.put("status", "2");
            map.put("msg", "正常访问");
        }*/
        Gson gson = new Gson();
        serverHttpResponse.getHeaders().add("sanvni", "ssss");
        return chain.filter(exchange.mutate()
                .request(exchange.getRequest().mutate().uri(URI.create("/test/test")).build())
                .response(CryptResponseDecorator.encryptDecorator(serverHttpResponse))
                .build());
    }

    @Override
    public int getOrder() {
        return -3;
    }

}
