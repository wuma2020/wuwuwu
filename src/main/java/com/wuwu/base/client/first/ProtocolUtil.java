package com.wuwu.base.client.first;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * 协议解析
 */
public class ProtocolUtil {

    /**
     * CRLF 回车换行
     */
    private final static String CRLF = "\r\n";

    /**
     * 解析redis 发送消息
     *
     * @param common
     * @return
     */
    public static ByteBuffer encode(String common) {

        if (common == null || common.length() == 0) {
            return null;
        }

        String[] args = common.split(" ");

        StringBuffer buffer = new StringBuffer();
        buffer.append("*").append(args.length).append(CRLF);

        for (String arg : args) {
            if (!arg.equals(" ")) {
                buffer.append("$")
                        .append(arg.length())
                        .append(CRLF)
                        .append(arg)
                        .append(CRLF);
            }
        }

        return StandardCharsets.UTF_8.encode(buffer.toString());

    }


    /**
     * 根据resp协议来进行解码
     *
     * @param futureClient
     */
    public static void decode(WuwuFutureClient futureClient) {
        // TODO 最难写的地方就在这了，
        SocketChannel socketChannel = futureClient.getSocketChannel();

        // TODO 1.抽象出每一种类型单独的解析对象的方法，并返回解析结果
        //  如果解析完成，则直接返回
        //  如果解析未完成，则需要记录未完成，并记录上一次的解析位置，一直已经完成的解析内容，下一次再进来的时候，先判断是否完成解析，
        //  如果没有，从上次未解析的地方解析


    }
}
