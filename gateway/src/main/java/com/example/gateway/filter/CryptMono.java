package com.example.gateway.filter;

import com.example.gateway.security.AESUtil;
import com.example.gateway.security.KeyConstant;
import com.google.gson.Gson;
import io.netty.buffer.ByteBufAllocator;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class CryptMono {

    public static final Gson gson = new Gson();
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
                    Map<String, Object> bodyMap = gson.fromJson(decrypt, HashMap.class);
                    Map<String, Object> map = AccessCheck
                            .accessCheck(request.getHeaders(), String.valueOf(bodyMap.get("token")), true);
                    if ((boolean) map.get("access")) {
                        return chain.filter(exchange.mutate()
                                .request(requestDecorator(request.mutate()
                                                .uri(URI.create(map.get("uri").toString())).build(),
                                        bodyMap, (String) map.get("clazz")))
                                .response(responseDecorator(response)).build());
                    } else {
                        dataBuffer = response.bufferFactory().wrap(gson.toJson(map).getBytes(StandardCharsets.UTF_8));
                        return response.writeWith(Flux.just(dataBuffer));
                    }
                });
    }

    private static ServerHttpRequestDecorator requestDecorator(ServerHttpRequest request,
                                                               Map<String, Object> bodyMap, String clazz) {
        String token = RequestHeadersBody.token();
        RequestHeadersBody.setHeaders(request.getHeaders(), token);
        return new ServerHttpRequestDecorator(request) {
            @Override
            public Flux<DataBuffer> getBody() {
                String content = RequestHeadersBody.getBodyContent(bodyMap, token, clazz);
                return Flux.just(dataBufferFactory.wrap(content.getBytes(StandardCharsets.UTF_8)));
            }
        };
    }

    private static ServerHttpResponseDecorator responseDecorator(ServerHttpResponse response) {
        return new ServerHttpResponseDecorator(response) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                // Controller返回类型必须是Flux
                Flux<? extends DataBuffer> flux = Flux.from(body);
                return super.writeWith(flux.buffer().map(dataBuffers -> {
                    StringBuilder sb = new StringBuilder("");
                    DataBuffer join = dataBufferFactory.join(dataBuffers);
                    byte[] content = new byte[join.readableByteCount()];
                    join.read(content);
                    DataBufferUtils.release(join);
                    String s = new String(content, StandardCharsets.UTF_8);
                    sb.append(s);
                    //去掉字符串最外层的[]
                    sb.deleteCharAt(0).deleteCharAt(sb.length() - 1);
                    HashMap<String, Object> bodyMap = gson.fromJson(AESUtil.decrypt
                            (sb.toString(), KeyConstant.AES_KEY, KeyConstant.SALT), HashMap.class);
                    Map<String, Object> map = AccessCheck.accessCheck(response.getHeaders(),
                            (String) bodyMap.get("token"), false);
                    String token = ResponseHeaderBody.token();
                    ResponseHeaderBody.setHeaders(response.getHeaders(), token);
                    if ((boolean) map.get("access")) {
                        ResponseHeaderBody.getBody(bodyMap,token);
                    }else {
                        ResponseHeaderBody.fillResp(map,token);
                    }
                    return response.bufferFactory().wrap(ResponseHeaderBody.getBody(map, token)
                            .getBytes(StandardCharsets.UTF_8));
                }));
            }
        };
    }

}
