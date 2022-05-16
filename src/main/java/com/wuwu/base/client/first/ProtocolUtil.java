package com.wuwu.base.client.first;

import java.io.IOException;
import java.nio.ByteBuffer;
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
    public static void decode(WuwuFutureClient futureClient) throws IOException {
        // TODO 最难写的地方就在这了，
        SocketChannel socketChannel = futureClient.getSocketChannel();

        // TODO 1.抽象出每一种类型单独的解析对象的方法，并返回解析结果
        //  如果解析完成，则直接返回
        //  如果解析未完成，则需要记录未完成，并记录上一次的解析位置，一直已经完成的解析内容，下一次再进来的时候，先判断是否完成解析，
        //  如果没有，从上次未解析的地方解析
        //  我每次只读取1024字节
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        int read = socketChannel.read(buffer);
        if (read >= 0) {
            decode0(buffer, futureClient);

        } else {
            //说明读取到了结束符


        }


    }

    /**
     * 解析方法
     *
     * @param buffer
     * @param wuwuFutureClient
     */
    private static void decode0(ByteBuffer buffer, WuwuFutureClient wuwuFutureClient) {

        buffer.flip();

        int position = buffer.position();
        byte[] bytes = new byte[position - 1];
        buffer.get(bytes);


    }


    /**
     * 根据协议的具体类型来进行解析该协议的内容
     *
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
            return res;
        } else if (type == '-') {
            //处理错误数据
            String res = dealOneLine(ds);
            if (res == null) {
                //切换读模式到写模式
                changeReadToWrite(ds);
                return null;
            }
            ds.clear();
            return res;
        } else if (type == ':') {
            //处理整型数据
            String res = dealOneLine(ds);
            if (res == null) {
                //切换读模式到写模式
                changeReadToWrite(ds);
                return null;
            }
            ds.clear();
            long l = Long.parseLong(res);
            return l;
        } else if (type == '*') {
            LinkedList<Object> res = dealArrayLine(ds);
            if (null == res) {
                //切换读模式到写模式
                changeReadToWrite(ds);
                return null;
            }
            ds.clear();
            return res;
        } else {
            return null;
        }

    }

    /**
     * 处理数组的获取
     * "*2\r\n$3\r\nfoo\r\n$3\r\nbar\r\n"
     *
     * @param ds
     */
    private static LinkedList<Object> dealArrayLine(ByteBuffer ds) {
        byte b = ds.get();
        Integer count = b - '0';
        if (count == 0) {
            if (ds.limit() < 1) {
                return null;
            } else {
                ds.getChar();
                return new LinkedList<>();
            }
        } else {
            if (ds.limit() < 1) {
                return null;
            } else if (ds.limit() == 1) {
                ds.getChar();
                return null;
            } else {
                LinkedList<Object> res = new LinkedList<>();
                ds.getChar();
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


    }

    /**
     * "$6\r\nfoobar\r\n"
     * number = ds.get()
     * pos : 1  number = 6
     * pos + 2 + 6(number) = res
     * <p>
     * limit = res + 2
     *
     * @param ds
     * @return
     */
    private static String dealMultiLine(ByteBuffer ds) {
        char number = (char) ds.get();
        //处理极端情况下的多行标识
        if (number - '0' == 0) {
            return "空字符串";
        } else if (number == '-') {
            return "不存在的值";
        }

        int limit = ds.limit();
        if (limit < (1 + 2 + (number - '0') + 2)) {
            return null;
        }

        //读取完成，进行解析
        char aChar = ds.getChar();
        byte[] context = new byte[number];
        ds.get(context);
        String s = String.valueOf(context);
        return s;
    }

    /**
     * 切换读取模式到写模式
     *
     * @param ds
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
            if (b != '\r' && b != '\n') {
                sb.append((char) b);
            } else if (b == '\n') {
                isOver = true;
                return sb.toString();
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
