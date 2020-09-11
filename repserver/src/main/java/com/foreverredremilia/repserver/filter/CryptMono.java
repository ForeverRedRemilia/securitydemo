package com.foreverredremilia.repserver.filter;

import com.foreverredremilia.repserver.security.AESUtil;
import com.foreverredremilia.repserver.security.KeyConstant;
import com.google.gson.Gson;
import io.netty.buffer.ByteBufAllocator;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class CryptMono {

    private static final Gson gson = new Gson();

    private static final DataBufferFactory dataBufferFactory =
            new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);

    public static Mono<Void> cryptMono(ServerWebExchange exchange, WebFilterChain webFilterChain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        return DataBufferUtils.join(exchange.getRequest().getBody())
                .flatMap(dataBuffer -> {
                    DataBufferUtils.retain(dataBuffer);
                    byte[] content = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(content);
                    //释放掉内存
                    DataBufferUtils.release(dataBuffer);
                    String s = new String(content, StandardCharsets.UTF_8);
                    String decrypt = AESUtil.decrypt(s, KeyConstant.AES_KEY, KeyConstant.SALT);
                    Map<String, Object> bodyMap = gson.fromJson(decrypt, HashMap.class);
                    Map<String, Object> map = AccessCheck
                            .accessCheck(request.getHeaders(), (String) bodyMap.get("token"));
                    //重放攻击&令牌检测是否通过
                    if ((boolean) map.get("access")) {
                        Map<String, Object> bodyContent = RequestHeadersBody
                                .getBodyContent((String) map.get("body"), (String) map.get("clazz"));
                        //业务数据解密是否成功
                        if ((boolean) bodyContent.get("decrypt")) {
                            bodyContent.remove("decrypt");
                            return webFilterChain.filter(exchange.mutate()
                                    .request(CryptMono.requestDecorator(exchange.getRequest(), bodyMap))
                                    .response(responseDecorator(exchange.getResponse())).build());
                        } else {
                            bodyContent.remove("decrypt");
                            map = bodyContent;
                        }
                    } else {
                        map.remove("access");
                    }
                    //重放攻击&令牌检测不通过不走入controller，直接响应
                    String token = ResponseHeaderBody.token();
                    //往响应头添加token和timestamp
                    ResponseHeaderBody.setHeaders(response.getHeaders(),token);
                    dataBuffer = response.bufferFactory().wrap(ResponseHeaderBody
                            .fillResp(map, token)
                            .getBytes(StandardCharsets.UTF_8));
                    return response.writeWith(Flux.just(dataBuffer));
                });
    }

    private static ServerHttpRequestDecorator requestDecorator(ServerHttpRequest request, Map<String,Object> map) {
        return new ServerHttpRequestDecorator(request) {
            @Override
            public Flux<DataBuffer> getBody() {
                return Flux.just(dataBufferFactory.wrap(gson.toJson(map).getBytes(StandardCharsets.UTF_8)));
            }
        };
    }

    public static ServerHttpResponseDecorator responseDecorator(ServerHttpResponse response) {
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
