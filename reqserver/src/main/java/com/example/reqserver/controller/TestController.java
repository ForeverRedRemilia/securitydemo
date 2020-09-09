package com.example.reqserver.controller;

import com.example.reqserver.bean.dto.TestDto;
import com.example.reqserver.security.AESUtil;
import com.example.reqserver.security.GetCryptAnnotation;
import com.example.reqserver.security.KeyConstant;
import com.example.reqserver.security.RSAUtil;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@RequestMapping("/test")
@RestController
public class TestController {

    private static final Logger log = LoggerFactory.getLogger(TestController.class);

    private final Gson gson = new Gson();

    @RequestMapping("/test")
    public void test() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders header = getGatewayHeader();
        //请求测试数据
        TestDto testDto = new TestDto();
        testDto.setApplyUser("蕾米莉亚");
        testDto.setApplyPlace("红魔馆");
        testDto.setApplyDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        testDto.setApplyPay(25000000);
        log.info(gson.toJson(header));
        log.info(gson.toJson(testDto));
        log.info(TestDto.class.getName());
        String body = getBodyContent(gson.fromJson(gson.toJson(testDto), HashMap.class),TestDto.class);
        log.info(body);
    }

    /**
     * 获取加密后的 Body
     * @param map 由 json字符串转换的 map
     * @param clazz json字符串转换之前对应的 dto类
     * @return
     */
    private String getBodyContent(Map<String, Object> map, Class<?> clazz) {
        List<String> crypts = null;
        try {
            //得到需要加密的属性名称
            crypts = GetCryptAnnotation.getCrypt(clazz);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取加密DTO类别失败");
        }
        if (null != crypts) {
            for (String key : crypts) {
                //使用响应端的公钥加密敏感数据
                String encrypt = RSAUtil.encrypt(String.valueOf(map.get(key)), KeyConstant.REP_PUB_KEY);
                map.put(key, encrypt);
            }
        }
        String json = gson.toJson(map);
        log.info(json);
        return AESUtil.encrypt(json, KeyConstant.AES_KEY, KeyConstant.SALT);
    }

    //自定义获取请求头部
    private HttpHeaders getGatewayHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //使用Gateway的公钥加密token和timestamp，并添加到请求头部
        try {
            headers.add("token"
                    , RSAUtil.encrypt(UUID.randomUUID().toString(), KeyConstant.GATE_PUB_KEY));
            headers.add("timestamp"
                    , RSAUtil.encrypt(String.valueOf(System.currentTimeMillis()), KeyConstant.GATE_PUB_KEY));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("服务请求段对Gateway进行RSA加密失败");
        }
        return headers;
    }
}
