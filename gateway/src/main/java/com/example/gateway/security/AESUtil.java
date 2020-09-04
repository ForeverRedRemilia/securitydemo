package com.example.gateway.security;

import org.springframework.security.crypto.encrypt.Encryptors;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AESUtil {

    public static String encrypt(String str, String aesKey, String salt){
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        byte[] encrypt = Encryptors.standard(aesKey, salt).encrypt(bytes);
        return new String(Base64.getEncoder().encodeToString(encrypt));
    }

    public static String decrypt(String str, String aesKey, String salt){
        byte[] bytes = Base64.getDecoder().decode(str);
        byte[] decrypt = Encryptors.standard(aesKey, salt).decrypt(bytes);
        return new String(decrypt);
    }

}
