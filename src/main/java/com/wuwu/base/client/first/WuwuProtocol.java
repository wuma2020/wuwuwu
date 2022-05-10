package com.wuwu.base.client.first;

import java.nio.channels.SocketChannel;

/**
 * 解析每一个socket对应的消息的协议实现类
 */
public class WuwuProtocol {


    /**
     * 该socket
     */
    private SocketChannel client;

    /**
     * 该socket对应的结果信息
     */
    private WuwuResponse response;


    /**
     * 解析当前client的结果
     */
    public void decode(){};


}
