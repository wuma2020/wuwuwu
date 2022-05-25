package com.wuwu.base.client;

import lombok.Data;

/**
 * 每一个socket对应的结果
 */
public class WuwuResponse {


    //响应的类型值
    // 单行字符串 +
    public static Integer LINE = 1;
    // 错误 -
    public static Integer ERROR = 2;
    // 整数 :
    public static Integer NUMBER = 3;
    // 多行字符串 $
    public static Integer MULTI_LINE = 4;
    // 数组 *
    public static Integer ARRAY = 5;
    // 连接关闭 socket 读取到 eof -1
    public static Integer CLOSE = 6;


    public WuwuResponse(Object result) {
        this.result = result;
    }

    public WuwuResponse() {
    }

    /**
     * 这个response对应的完成的结果
     */
    private Object result;

    /**
     * 这个结果对应的类型
     */
    private Integer type;


    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
