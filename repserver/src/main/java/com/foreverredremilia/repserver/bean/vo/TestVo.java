package com.foreverredremilia.repserver.bean.vo;

import com.foreverredremilia.repserver.security.Crypt;

public class TestVo {

    @Crypt
    private String applyUser;

    private String applyPlace;

    @Crypt
    private Integer applyPay;

    private Class<?> clazz;

    public String getApplyUser() {
        return applyUser;
    }

    public void setApplyUser(String applyUser) {
        this.applyUser = applyUser;
    }

    public String getApplyPlace() {
        return applyPlace;
    }

    public void setApplyPlace(String applyPlace) {
        this.applyPlace = applyPlace;
    }

    public Integer getApplyPay() {
        return applyPay;
    }

    public void setApplyPay(Integer applyPay) {
        this.applyPay = applyPay;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }
}
