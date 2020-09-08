package com.foreverredremilia.repserver.filter;

import com.google.gson.Gson;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class CryptRequestDecorator {

    private static final Gson gson = new Gson();

    public static ServerHttpRequestDecorator decryptDecorator(ServerHttpRequest request, ServerHttpResponse response) {
        return new ServerHttpRequestDecorator(request) {
            @Override
            public Flux<DataBuffer> getBody() {
                Flux<DataBuffer> flux = super.getBody();
                InputStreamHolder holder = new InputStreamHolder();
                flux.subscribe(dataBuffer -> holder.inputStream = dataBuffer.asInputStream());
                if (null != holder.inputStream){
                    String s = gson.toJson(holder.inputStream);
                    System.out.println(s);
                }
                return super.getBody();
            }
        };
    }

    private static class InputStreamHolder {
        InputStream inputStream;
    }

}
