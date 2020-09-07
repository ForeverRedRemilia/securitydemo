package com.foreverredremilia.repserver.filter;

import com.foreverredremilia.repserver.security.AESUtil;
import com.foreverredremilia.repserver.security.KeyConstant;
import com.foreverredremilia.repserver.security.RSAUtil;
import com.google.gson.Gson;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class CryptResponseDecorator {

    private static Gson gson = new Gson();

    public static ServerHttpResponseDecorator encryptDecorator(ServerHttpResponse response) {
        return new ServerHttpResponseDecorator(response) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                // Controller返回类型必须是Flux
                Flux<? extends DataBuffer> flux = Flux.from(body);
                return super.writeWith(flux.buffer().map(dataBuffers -> {
                    StringBuilder sb = new StringBuilder("");
                    //解决返回的response不完整问题
                    dataBuffers.forEach(dataBuffer -> {
                        byte[] content = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(content);
                        //释放掉内存
                        DataBufferUtils.release(dataBuffer);
                        String s = new String(content, StandardCharsets.UTF_8);
                        sb.append(s);
                    });
                    //去掉字符串最外层的[]
                    sb.deleteCharAt(0).deleteCharAt(sb.length() - 1);
                    HashMap<String, Object> map = gson.fromJson(sb.toString(), HashMap.class);
                    //获取需要加密的字段
                    String[] crypts = String.valueOf(map.get("crypt")).split(";");
                    for (String key : crypts) {
                        String encrypt = RSAUtil.encrypt(String.valueOf(map.get(key)), KeyConstant.REQ_PUB_KEY);
                        if (null == encrypt) {
                            map = new HashMap<>();
                            map.put("status", "985");
                            map.put("error", "服务端加密失败！");
                            break;
                        }
                        map.put(key, encrypt);
                    }
                    System.out.println(gson.toJson(map));
                    //AES加密
                    String result = AESUtil.encrypt(gson.toJson(map), KeyConstant.AES_KEY, KeyConstant.SALT);
                    return response.bufferFactory().wrap(result.getBytes(StandardCharsets.UTF_8));
                }));
            }
        };
    }
}
