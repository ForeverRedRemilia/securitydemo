package com.example.reqserver.bean.vo;

import com.example.reqserver.security.Crypt;

public class TestVo {
    @Crypt
    private String applyMsg; //申请结果
    @Crypt
    private String applyStatus; //申请状态
    @Crypt
    private String applyCode; //申请操作码

    public String getApplyMsg() {
        return applyMsg;
    }

    public void setApplyMsg(String applyMsg) {
        this.applyMsg = applyMsg;
    }

    public String getApplyStatus() {
        return applyStatus;
    }

    public void setApplyStatus(String applyStatus) {
        this.applyStatus = applyStatus;
    }

    public String getApplyCode() {
        return applyCode;
    }

    public void setApplyCode(String applyCode) {
        this.applyCode = applyCode;
    }
}
