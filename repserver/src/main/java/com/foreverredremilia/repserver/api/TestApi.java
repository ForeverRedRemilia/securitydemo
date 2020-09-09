package com.foreverredremilia.repserver.api;

import com.foreverredremilia.repserver.bean.vo.TestVo;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
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
    public Flux<TestVo> test(@RequestBody Map<String,Object> map) {
        System.out.println(map);
        TestVo testVo = new TestVo();
        testVo.setApplyUser("蕾米莉亚");
        testVo.setApplyPlace("红魔馆");
        testVo.setApplyPay(25000);
        testVo.setClazz(TestVo.class);
        return Flux.just(testVo);
    }

}
