package com.foreverredremilia.repserver.filter;

import com.google.gson.Gson;
import io.netty.buffer.ByteBufAllocator;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class CryptMono {

    private static final Gson gson = new Gson();

    private static final DataBufferFactory dataBufferFactory = new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);

    public static Mono<Void> cryptMono(ServerWebExchange exchange, WebFilterChain webFilterChain){
       return DataBufferUtils.join(exchange.getRequest().getBody())
                .flatMap(dataBuffer -> {
                    DataBufferUtils.retain(dataBuffer);
                    byte[] content = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(content);
                    //释放掉内存
                    DataBufferUtils.release(dataBuffer);
                    String s = new String(content, StandardCharsets.UTF_8);
                    System.out.println(s);
                    return webFilterChain.filter(exchange.mutate().request(CryptMono.decryptDecorator(exchange.getRequest(), s.getBytes(StandardCharsets.UTF_8)))
                            .response(CryptResponseDecorator.encryptDecorator(exchange.getResponse())).build());
                });
    }

    private static ServerHttpRequestDecorator decryptDecorator(ServerHttpRequest request, byte[] bytes) {
        return new ServerHttpRequestDecorator(request) {
            @Override
            public Flux<DataBuffer> getBody() {
                return Flux.just(dataBufferFactory.wrap(bytes));
            }
        };
    }

    private static class InputStreamHolder {
        InputStream inputStream;
    }

}
