package com.wuwu.base.client;

import lombok.Data;

/**
 * 每一个socket对应的结果
 */
public class WuwuResponse {


    public WuwuResponse(Object result) {
        this.result = result;
    }

    public WuwuResponse() {
    }

    /**
     * 这个response对应的完成的结果
     */
    private Object result;


    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
