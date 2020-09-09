package com.example.gateway.filter;

import com.example.gateway.redis.RedisUtil;
import com.google.gson.Gson;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Component
public class CryptResponseDecorator {

    private static final Logger logger = LoggerFactory.getLogger(CryptResponseDecorator.class);

    @Autowired
    private RedisUtil redisUtil;
    private static CryptResponseDecorator cryptResponseDecorator;

    @PostConstruct
    public void init() {
        cryptResponseDecorator = this;
        redisUtil = this.redisUtil;
    }

    private static final Gson gson = new Gson();

    public static ServerHttpResponseDecorator encryptDecorator(ServerHttpResponse response) {
        return new ServerHttpResponseDecorator(response) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                String token = response.getHeaders().get("token").get(0);
                if (StringUtils.isEmpty(token)) {

                }
                Boolean aBoolean = cryptResponseDecorator.redisUtil.setToken(token, "");
                if (null == aBoolean || !aBoolean) {
                    logger.info("重放攻击");
                } else {
                    logger.info("正常访问");
                }
                // Controller返回类型必须是Flux
                return super.writeWith(body);
            }

        };
    }
}
