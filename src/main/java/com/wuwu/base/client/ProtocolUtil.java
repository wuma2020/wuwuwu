package com.wuwu.base.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

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
     * @param common redis 命令
     * @return redis发送的二进制缓存 byte buffer
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
     * @param futureClient 一个redis的client
     * @throws IOException io异常
     */
    public static void decode(WuwuFutureClient futureClient) throws IOException {

        SocketChannel socketChannel = futureClient.getSocketChannel();
        ByteBuffer buffer = futureClient.getBuffer();
        ByteBuffer newBuffer = MemoryCalculator.calculator_add(buffer);
        futureClient.setBuffer(newBuffer);
        int read = socketChannel.read(newBuffer);

        if (read >= 0) {
            decode0(newBuffer, futureClient);
        } else {
            //说明读取到了结束符
            futureClient.setFinish(true);
            futureClient.getResponse().setResult("连接已经关闭!");
            futureClient.getResponse().setType(WuwuResponse.CLOSE);
        }


    }

    /**
     * 解析方法
     *
     * @param buffer socket read 出来的buffer
     * @param wuwuFutureClient 一个redis的client
     * @throws  ClosedChannelException 异常
     */
    private static void decode0(ByteBuffer buffer, WuwuFutureClient wuwuFutureClient) throws ClosedChannelException {

        //转换成读取模式
        buffer.flip();

        byte type = buffer.get();
        Object res = decodeByType(type, buffer, null);
        if (res != null) {
            //在这里返程注册新的写的数据
            Selector selector = wuwuFutureClient.getSelector();
            SocketChannel client = wuwuFutureClient.getSocketChannel();
            client.register(selector, SelectionKey.OP_WRITE, wuwuFutureClient);
            wuwuFutureClient.getResponse().setResult(res);
            wuwuFutureClient.getResponse().setType(getResponseTypeByByte(type));
            wuwuFutureClient.setFinish(true);
        }

    }

    /**
     * 根据响应的类型，设置响应的相应类型
     *
     * @param type RESP协议的类型
     * @return 类型的常量
     */
    private static Integer getResponseTypeByByte(byte type) {
        switch (type) {
            case '+':
                return WuwuResponse.LINE;
            case '-':
                return WuwuResponse.ERROR;
            case ':':
                return WuwuResponse.NUMBER;
            case '$':
                return WuwuResponse.MULTI_LINE;
            case '*':
                return WuwuResponse.ARRAY;
            default:
                return WuwuResponse.CLOSE;
        }
    }


    /**
     * 根据协议的具体类型来进行解析该协议的内容
     *
     * @param type         the type
     * @param ds           此时的ds为读模式下
     * @param notNeedClear 是否需要清除ds
     * @return 返回实际的类型数据 如 单行字符串 错误字符串 整数类型
     */
    public static Object decodeByType(byte type, ByteBuffer ds, Boolean notNeedClear) {

        if (type == '+') {
            //处理单行字符串的情况如果不够，则需要下次继续读取
            String res = dealOneLine(ds);
            if (res == null) {
                //切换读模式到写模式
                changeReadToWrite(ds);
                return null;
            }
            if (notNeedClear == null || !notNeedClear) {
                ds.clear();
            }

            return res;
        } else if (type == '$') {
            //处理多行字符串
            String res = dealMultiLine(ds);
            if (res == null) {
                //切换读模式到写模式
                changeReadToWrite(ds);
                return null;
            }
            if (notNeedClear == null || !notNeedClear) {
                ds.clear();
            }
            return res;
        } else if (type == '-') {
            //处理错误数据
            String res = dealOneLine(ds);
            if (res == null) {
                //切换读模式到写模式
                changeReadToWrite(ds);
                return null;
            }
            if (notNeedClear == null || !notNeedClear) {
                ds.clear();
            }
            return res;
        } else if (type == ':') {
            //处理整型数据
            String res = dealOneLine(ds);
            if (res == null) {
                //切换读模式到写模式
                changeReadToWrite(ds);
                return null;
            }
            if (notNeedClear == null || !notNeedClear) {
                ds.clear();
            }
            long l = Long.parseLong(res);
            return l;
        } else if (type == '*') {
            LinkedList<Object> res = dealArrayLine(ds);
            if (null == res) {
                //切换读模式到写模式
                changeReadToWrite(ds);
                return null;
            }
            if (notNeedClear == null || !notNeedClear) {
                ds.clear();
            }
            return res;
        } else {
            return null;
        }

    }

    /**
     * 处理数组的获取
     * "*2\r\n$3\r\nfoo\r\n$3\r\nbar\r\n"
     * 如果*后面是 10，如，*10，那么就应该是读取对应的字符串
     * <p>
     * https://www.redis.com.cn/topics/protocol.html
     *
     * @param ds 需要解码的buffer
     */
    private static LinkedList<Object> dealArrayLine(ByteBuffer ds) {

        Integer count = getInteger(ds);
        if (count == null) return null;

        if (count == 0) {
            System.out.println("空数组");
            return new LinkedList<>();
        } else if (count == -1) {
            System.out.println("命令超时");
            return new LinkedList<>();
        } else {
            LinkedList<Object> res = new LinkedList<>();
            for (int i = 0; i < count; i++) {
                byte type = ds.get();
                Object o = decodeByType(type, ds, true);
                if (o == null) {
                    return null;
                } else {
                    res.add(o);
                }
            }
            return res;
        }

    }

    /**
     *
     * @param ds 获取长度的buffer
     * @return 某个类型下的字节长度
     */
    private static Integer getInteger(ByteBuffer ds) {
        //读取数组的个数
        ds.mark();
        StringBuilder countTemp = new StringBuilder();
        boolean isCountFinish = false;
        Integer count = 0;

        while (ds.hasRemaining()) {
            byte b = ds.get();
            if (b == '\r' || b == '\n') {
                if (b == '\n') {
                    isCountFinish = true;
                    break;
                }
            } else {
                countTemp.append((char) b);
            }
        }

        if (isCountFinish) {
            //读取完成，继续处理
            count = Integer.parseInt(countTemp.toString());
        } else {
            //没有读取完成
            ds.reset();
            return null;
        }
        return count;
    }

    /**
     * "$6\r\nfoobar\r\n"
     * number = ds.get()
     * pos : 1  number = 6
     * pos + 2 + 6(number) = res
     * <p>
     * limit = res + 2
     *
     * @param ds 多行字符串的buffer
     * @return 字符串
     */
    private static String dealMultiLine(ByteBuffer ds) {

        Integer count = getInteger(ds);
        if (count == null) return null;

        //处理极端情况下的多行标识
        if (count == 0) {
            return "空字符串";
        } else if (count < 0) {
            return "不存在的值";
        }

        int limit = ds.limit();
        if (limit < (ds.position() + count + 2)) {
            return null;
        }

        //读取完成，进行解析
        byte[] context = new byte[count + 2];
        ds.get(context);
        String res = new String(context);
        return res;
    }

    /**
     * 切换读取模式到写模式
     *
     * @param ds 需要切换的buffer
     */
    private static void changeReadToWrite(ByteBuffer ds) {
        //重置
        ds.rewind();

        //切换写模式
        ds.compact();
    }

    /**
     * 单行字符串 +
     * 以 回车换行符 作为结束，不会显示指定读取的字节数
     *
     * @param ds 读模式下
     * @return null 标识该ds不够组成一个完整的包，不为null则为解析完成且返回解析结果
     */
    private static String dealOneLine(ByteBuffer ds) {

        StringBuilder sb = new StringBuilder();

        boolean isOver = false;

        while (ds.hasRemaining()) {
            byte b = ds.get();
            sb.append((char) b);

            if (b == '\n') {
                isOver = true;
            }
        }

        if (isOver) {
            return sb.toString();
        } else {
            //如果没有完成就返回null，标识该ds中不够一个完成的包
            return null;
        }
    }


}
