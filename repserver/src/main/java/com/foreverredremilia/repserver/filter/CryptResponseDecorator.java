package com.foreverredremilia.repserver.filter;

import com.foreverredremilia.repserver.security.AESUtil;
import com.foreverredremilia.repserver.security.GetCryptAnnotation;
import com.foreverredremilia.repserver.security.KeyConstant;
import com.foreverredremilia.repserver.security.RSAUtil;
import com.google.gson.Gson;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CryptResponseDecorator {

    private static final Gson gson = new Gson();

    public static ServerHttpResponseDecorator responseDecorator(ServerHttpResponse response) {
        return new ServerHttpResponseDecorator(response) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                // Controller返回类型必须是Flux
                Flux<? extends DataBuffer> flux = Flux.from(body);
                return super.writeWith(flux.buffer().map(dataBuffers -> {
                    StringBuilder sb = new StringBuilder("");
                    DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
                    DataBuffer join = dataBufferFactory.join(dataBuffers);
                    byte[] content = new byte[join.readableByteCount()];
                    join.read(content);
                    DataBufferUtils.release(join);
                    String s = new String(content, StandardCharsets.UTF_8);
                    sb.append(s);
                    //去掉字符串最外层的[]
                    sb.deleteCharAt(0).deleteCharAt(sb.length() - 1);
                    HashMap<String, Object> map = gson.fromJson(sb.toString(), HashMap.class);
                    String token = ResponseHeaderBody.token();
                    ResponseHeaderBody.setHeaders(response.getHeaders(), token);
                    return response.bufferFactory().wrap(ResponseHeaderBody.getBody(map, token)
                            .getBytes(StandardCharsets.UTF_8));
                }));
            }
        };
    }
}
