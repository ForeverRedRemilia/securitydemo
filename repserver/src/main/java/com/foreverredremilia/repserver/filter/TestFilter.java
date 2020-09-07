package com.foreverredremilia.repserver.filter;

import com.google.gson.Gson;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class TestFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("status", "987");
        map.put("error", "重放攻击！");
        ServerHttpResponse response = serverWebExchange.getResponse();
        return webFilterChain.filter(serverWebExchange.mutate().response(CryptResponseDecorator.encryptDecorator(response)).build());
    }

}
