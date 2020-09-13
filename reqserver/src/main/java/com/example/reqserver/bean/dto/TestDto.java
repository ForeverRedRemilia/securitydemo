package com.example.reqserver.bean.dto;

import com.example.reqserver.security.Crypt;

public class TestDto {
    @Crypt
    private String applyUser; //申请用户

    private String applyPlace; //申请地点

    @Crypt
    private String applyDate; //申请时间

    @Crypt
    private String applyPay; //申请金额

    private String applyStyle; //支付方式

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

    public String getApplyDate() {
        return applyDate;
    }

    public void setApplyDate(String applyDate) {
        this.applyDate = applyDate;
    }

    public String getApplyPay() {
        return applyPay;
    }

    public void setApplyPay(String applyPay) {
        this.applyPay = applyPay;
    }

    public String getApplyStyle() {
        return applyStyle;
    }

    public void setApplyStyle(String applyStyle) {
        this.applyStyle = applyStyle;
    }
}
