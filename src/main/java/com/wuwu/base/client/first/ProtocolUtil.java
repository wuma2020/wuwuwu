package com.wuwu.base.client.first;

import java.nio.ByteBuffer;
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

        StringBuffer sb = new StringBuffer();
        sb.append("*").append(args.length).append(CRLF);

        for (String arg : args) {
            sb.append("$")
                    .append(arg.length())
                    .append(CRLF)
                    .append(arg)
                    .append(CRLF);
        }

        return StandardCharsets.UTF_8.encode(sb.toString());

    }


    /**
     * 根据resp协议来进行解码
     *
     * @param futureClient
     */
    public static void decode(WuwuFutureClient futureClient) {
        // TODO 最难写的地方就在这了，



    }
}
