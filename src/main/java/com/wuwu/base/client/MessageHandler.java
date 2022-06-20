package com.wuwu.base.client;

/**
 * 处理响应消息的类
 */
public interface MessageHandler {

    /**
     * 完成消息的封装之后
     * 加工处理消息处理响应
     *
     * @param response the response
     */
    void handle(WuwuResponse response);


    /**
     * 判断当前是否为可以执行handle
     * 先就处理响应，写死，后面在扩展成inBound 和 outBound，使用match方法
     *
     * @return 空 boolean
     */
    boolean match();
}
