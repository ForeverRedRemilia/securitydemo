package com.foreverredremilia.repserver.api;

import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestApi {

    @RequestMapping("/test")
    public Flux<Map<String, Object>> test() throws NoSuchAlgorithmException {
        String s = KeyGenerators.string().generateKey();
        System.out.println(s);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("applyPlace", "红魔馆");
        map.put("applyUser", "蕾米莉亚");
        map.put("applyPay", "$250,00");
        map.put("crypt", "applyPay;applyUser");
        return Flux.just(map);
    }

}
