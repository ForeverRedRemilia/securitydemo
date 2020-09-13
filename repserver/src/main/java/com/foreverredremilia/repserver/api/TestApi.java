package com.foreverredremilia.repserver.api;

import com.foreverredremilia.repserver.bean.vo.TestVo;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestApi {

    @RequestMapping("/test")
    public Flux<TestVo> test(@RequestBody Map<String,Object> map) {
        System.out.println(map);
        TestVo testVo = new TestVo();
        testVo.setApplyMsg("申请成功，请保管好个人");
        testVo.setApplyCode("288700");
        testVo.setApplyStatus("500");
        testVo.setClazz(TestVo.class);
        return Flux.just(testVo);
    }

}
