package com.foreverredremilia.repserver.security;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class GetCryptAnnotation {

    public static List<String> getCrypt(Class<?> clazz) {
        try {
            Object o = clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Field[] fields = clazz.getDeclaredFields();
        List<String> list = new ArrayList<>();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (null != field.getAnnotation(Crypt.class) && field.getAnnotation(Crypt.class).isCrypt()) {
                list.add(field.getName());
            }
        }
        return list;
    }

}
