package com.foreverredremilia.repserver.filter;

import com.google.gson.Gson;
import io.netty.buffer.ByteBufAllocator;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class TestFilter implements WebFilter {

    private final DataBufferFactory dataBufferFactory = new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("status", "987");
        map.put("error", "重放攻击！");
        ServerHttpRequest request = serverWebExchange.getRequest();
        ServerHttpResponse response = serverWebExchange.getResponse();
        return DataBufferUtils.join(serverWebExchange.getRequest().getBody())
                .flatMap(dataBuffer -> {
                    DataBufferUtils.retain(dataBuffer);
                    byte[] content = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(content);
                    //释放掉内存
                    DataBufferUtils.release(dataBuffer);
                    String s = new String(content, StandardCharsets.UTF_8);
                    System.out.println(s);
                    ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(
                            serverWebExchange.getRequest()) {
                        @Override
                        public Flux<DataBuffer> getBody() {
                            return Flux.just(dataBufferFactory.wrap(s.getBytes()));
                        }
                    };
                    return webFilterChain.filter(serverWebExchange.mutate().request(mutatedRequest)
                            .response(CryptResponseDecorator.encryptDecorator(response)).build());
                });

    }

    private static class InputStreamHolder {
        InputStream inputStream;
    }

}
