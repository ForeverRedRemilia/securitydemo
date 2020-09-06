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
        Gson gson = new Gson();
        ServerHttpResponse response = serverWebExchange.getResponse();
        /*ServerHttpResponseDecorator decorator = new ServerHttpResponseDecorator(response) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                Flux<? extends DataBuffer> flux = Flux.from(body);
                return super.writeWith(flux.buffer().map(dataBuffers -> {
                    StringBuilder sb = new StringBuilder("");
                    dataBuffers.forEach(dataBuffer -> {
                        byte[] content = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(content);
                        //释放掉内存
                        DataBufferUtils.release(dataBuffer);
                        String s = new String(content, StandardCharsets.UTF_8);
                        sb.append(s);
                    });
                    sb.deleteCharAt(0);
                    sb.deleteCharAt(sb.length() - 1);
                    System.out.println(sb.toString());
                    LinkedHashMap<String, Object> map1 = gson.fromJson(sb.toString(), LinkedHashMap.class);
                    map1.putAll(map);
                    System.out.println(gson.toJson(map1));
                    response.getHeaders().add("haha", "卧槽");
                    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    return response.bufferFactory().wrap(gson.toJson(map1).getBytes(StandardCharsets.UTF_8));
                }));
            }
        };*/
        //return response.writeWith(Flux.just(response.bufferFactory().wrap(bytes)));
        return webFilterChain.filter(serverWebExchange.mutate().response(CryptResponseDecorator.encryptDecorator(response)).build());
    }

}
