package com.wuwu.base.client;

import java.util.LinkedList;
import java.util.List;

/**
 * 处理消息的Pipeline
 */
public class WuwuPipeline {

    /**
     * 处理具体的message handle
     */
    List<MessageHandler> handlers = new LinkedList<>();

    /**
     * 添加处理的handle
     *
     * @param handler the handler
     */
    public void addHandler(MessageHandler handler) {
        handlers.add(handler);
    }


    /**
     * 执行处理器
     *
     * @param response the response
     */
    public void doHandler(WuwuResponse response) {
        handlers.forEach(handler -> {
            handler.handle(response);
        });
    }


}
