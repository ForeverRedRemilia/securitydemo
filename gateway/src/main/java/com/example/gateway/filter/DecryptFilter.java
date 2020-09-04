package com.example.gateway.filter;

import com.google.gson.Gson;
import com.example.gateway.security.AESUtil;
import com.example.gateway.security.KeyConstant;
import com.example.gateway.security.RSAUtil;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class DecryptFilter implements GatewayFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponse serverHttpResponse = exchange.getResponse();
        serverHttpResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
        try {
            String abcdefg = RSAUtil.encrypt("abcdefg", KeyConstant.PUBLIC_KEY);
            System.out.println(abcdefg);
            String decrypt = RSAUtil.decrypt(abcdefg, KeyConstant.PRIVATE_KEY);
            System.out.println(decrypt);
            String abcdefg1 = AESUtil.encrypt("東方幻想乡空战姬", KeyConstant.AES_KEY, KeyConstant.SALT);
            System.out.println(abcdefg1);
            String decrypt1 = AESUtil.decrypt(abcdefg1, KeyConstant.AES_KEY, KeyConstant.SALT);
            System.out.println(decrypt1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Object object = exchange.getAttribute("cachedRequestBodyObject");
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("status", "-1");
        map.put("msg", "解密失败！");
        Gson gson = new Gson();
        byte[] bytes = gson.toJson(map).getBytes(StandardCharsets.UTF_8);
        Map<String, Object> map1 = gson.fromJson(gson.toJson(map), Map.class);
        DataBuffer buffer = serverHttpResponse.bufferFactory().wrap(bytes);
        return serverHttpResponse.writeWith(Flux.just(buffer));
        //return serverHttpResponse.writeWith(Flux.just());
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
