package com.example.gateway.filter;

import com.example.gateway.redis.RedisUtil;
import com.example.gateway.security.AESUtil;
import com.example.gateway.security.KeyConstant;
import com.example.gateway.security.RSAUtil;
import com.google.gson.Gson;
import io.netty.buffer.ByteBufAllocator;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CryptMono {

    private static final Gson gson = new Gson();
    private static final DataBufferFactory dataBufferFactory =
            new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);
    private static final Logger logger = LoggerFactory.getLogger(CryptMono.class);

    public static Mono<Void> cryptMono(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        return DataBufferUtils.join(request.getBody())
                .flatMap(dataBuffer -> {
                    DataBufferUtils.retain(dataBuffer);
                    byte[] content = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(content);
                    //释放掉内存
                    DataBufferUtils.release(dataBuffer);
                    String s = new String(content, StandardCharsets.UTF_8);
                    //解密得到body
                    String decrypt = AESUtil.decrypt(s, KeyConstant.AES_KEY, KeyConstant.SALT);
                    Map<String,Object> bodyMap = gson.fromJson(decrypt, HashMap.class);
                    Map<String, Object> map = AccessCheck.accessCheck(request.getHeaders(), String.valueOf(bodyMap.get("token")));
                    if ((boolean) map.get("access")){
                        String body = String.valueOf(bodyMap.get("data"));
                        return chain.filter(exchange.mutate()
                                .request(decryptDecorator(request.mutate()
                                                .uri(URI.create(map.get("uri").toString())).build(),
                                        body.getBytes(StandardCharsets.UTF_8)))
                                .response(encryptDecorator(response)).build());
                    }else {
                        dataBuffer = response.bufferFactory().wrap(gson.toJson(map).getBytes(StandardCharsets.UTF_8));
                        return response.writeWith(Flux.just(dataBuffer));
                    }
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

    private static ServerHttpResponseDecorator encryptDecorator(ServerHttpResponse response) {
        return new ServerHttpResponseDecorator(response) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                String token = response.getHeaders().get("token").get(0);
                // Controller返回类型必须是Flux
                return super.writeWith(body);
            }

        };
    }

}
