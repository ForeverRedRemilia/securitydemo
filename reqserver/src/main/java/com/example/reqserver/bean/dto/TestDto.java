package com.example.reqserver.bean.dto;

import com.example.reqserver.security.Crypt;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

public class TestDto {
    @Crypt
    private String applyUser; //申请用户

    private String applyPlace; //申请地点

    @Crypt
    private String applyDate; //申请时间

    @Crypt
    private Integer applyPay; //申请金额

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

    public Integer getApplyPay() {
        return applyPay;
    }

    public void setApplyPay(Integer applyPay) {
        this.applyPay = applyPay;
    }

}
