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

    public static ServerHttpResponseDecorator encryptDecorator(ServerHttpResponse response) {
        return new ServerHttpResponseDecorator(response) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                response.getHeaders().set("token","wdnmd");
                System.out.println(response.getHeaders());
                // Controller返回类型必须是Flux
                Flux<? extends DataBuffer> flux = Flux.from(body);
                return super.writeWith(flux.buffer().map(dataBuffers -> {
                    StringBuilder sb = new StringBuilder("");
                    //解决返回的response不完整问题
                    /*dataBuffers.forEach(dataBuffer -> {
                        byte[] content = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(content);
                        //释放掉内存
                        DataBufferUtils.release(dataBuffer);
                        String s = new String(content, StandardCharsets.UTF_8);
                        sb.append(s);
                    });*/
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
                    //获取需要加密的字段
                    List<String> crypts = null;
                    try {
                        crypts = GetCryptAnnotation.getCrypt(Class.forName(String.valueOf(map.get("clazz"))));
                    } catch (Exception e) {
                        map.clear();
                        map.put("status", "983");
                        map.put("error", "内部错误！");
                        e.printStackTrace();
                    }
                    map.remove("clazz");
                    if (null != crypts) {
                        for (String key : crypts) {
                            //RSA加密
                            String encrypt = RSAUtil.encrypt(String.valueOf(map.get(key)), KeyConstant.REQ_PUB_KEY);
                            if (null == encrypt) {
                                map.clear();
                                map.put("status", "985");
                                map.put("error", "服务端加密失败！");
                                break;
                            }
                            map.put(key, encrypt);
                        }
                    }
                    System.out.println(gson.toJson(map));
                    //AES加密
                    String result = AESUtil.encrypt(gson.toJson(map), KeyConstant.AES_KEY, KeyConstant.SALT);
                    System.out.println(result);
                    return response.bufferFactory().wrap(result.getBytes(StandardCharsets.UTF_8));
                }));
            }
        };
    }
}
