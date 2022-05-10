package com.wuwu.base.client;


import com.sun.xml.internal.ws.util.ByteArrayBuffer;
import lombok.Data;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * redis 协议 解析
 *
 *  怎么自定义端口进来，进行连接
 */

@Data
public class Protocol {


    /**
     * CRLF 回车换行
     */
    private final static String CRLF = "\r\n";

    private SocketChannel channel;

    /**
     * selector
     */
    private Selector selector;

    /**
     * 每次读取的缓存数组，不能并发，注意
     */
    private ByteBuffer buffer = ByteBuffer.allocate(1024);

    /**
     * 已经完成解码的字符串
     */
    private StringBuffer sb = new StringBuffer();

    /**
     * 剩余的未完成读取的字节数
     */
    private Integer remainCount = -1;

    /**
     * 请求命令
     */
    private LinkedBlockingQueue<String> commons = new LinkedBlockingQueue<>(10);

    /**
     * 命令结果
     */
    private LinkedBlockingQueue<String> result = new LinkedBlockingQueue<>(10);

    /**
     * redis server ip 地址
     */
    private String remoteIp = "localhost" ;

    /**
     * redis server port 端口号
     */
    private Integer remotePort = 6379;

    /**
     * 是否是一个完成的包  0 不是 1 是
     */
    private Integer isCompletePackage = 1;

    /**
     * 在不是一个完整的包的时候，上一次读取的协议类型
     */
    private Byte type;

    public Protocol(SocketChannel channel, Selector selector) {
        this.channel = channel;
        this.selector = selector;
    }

    public Protocol() {
    }




    /**
     * 解码并处理数据
     */
    public boolean decode() throws IOException {

        boolean finish = false;
        readByteToBuffer();
        byte b = Byte.MIN_VALUE;
        //todo 读取buffer中的数据，并解析

        if(isCompletePackage == 0){
            finish = getRemainInfo();
//            finish = getErrorMessage();
        }else {
            b = buffer.get();

            if(b == '+'){
                //处理单行字符串的情况
                finish = getCommonMessage();
            }else if(b == '$'){
                //处理多行字符串
//                finish = getStringMessage();
                finish = getStringMessageV1();
            }else if(b == '-'){
                //处理错误数据
                finish = getErrorMessage();
            }else if(b == ':'){
                //处理整型数据
                finish = getIntegerMessage();
            }else if(b == '*'){
                //处理数组
//                finish = getArrayMessage();
                finish = getArrayMessageV1();
            }else {
                System.out.println("错误的类型：" + (char)b);
            }
        }

        if(finish){
            isCompletePackage = 1;
        }else {
            isCompletePackage = 0;
            type = b;
        }


        return finish;
    }


    /**
     * 获取剩余的字节数量
     * @return
     */
    private boolean getRemainInfo() {

        boolean cn = false;
        ByteArrayBuffer byteBuffer = new ByteArrayBuffer();

        while (buffer.hasRemaining()){

            // fixme 这里的剩余待取的数据长度和时间的长度 不符，有问题
            if(remainCount == -1){
                cn = true;
                break;
            }

            byteBuffer.write(buffer.get());
            remainCount--;

        }


        if(cn){
            sb.append(byteBuffer.toString());
            result.add(sb.toString());
            System.out.println(sb.toString());
            sb = new StringBuffer();
            buffer.clear();
            return true;
        }else {
            sb.append(byteBuffer.toString());
            buffer.compact();
            return false;
        }
    }


    /**
     * fixme 处理多行协议， info 这种复杂的场景
     * @return
     * @throws IOException
     */
    private boolean getStringMessageV1() throws IOException {

        boolean cn = false;
        ByteArrayBuffer byteBuffer = new ByteArrayBuffer();

        int count = 0;
        int time = -1;

        //"$ 2222\r\n  aaaaaaaaaa\r\n"


        while (buffer.hasRemaining()){

            //string 的长度
            byte b = buffer.get();

            if (b == '\r' && time == -1) {
                time++;
                continue;
            }
            if(b == '\n' && time == 0){
                time++;
                continue;
            }


            //没有数据，返回$-1\r\n
            if (count == 0 && b == '-' && time == -1) {
                byte b1 = buffer.get();
                if (b1 == '1') {
                    buffer.get();
                    buffer.get();
                }
                byteBuffer.write("数据不存在".getBytes(StandardCharsets.UTF_8));
                cn = true;
                break;
            }


            //"$0\r\n\r\n" 空数据
            if(count == 0 && b == '0' && time == -1){
                //获取2个换行符
                buffer.getInt();
                byteBuffer.write("空字符串数据".getBytes(StandardCharsets.UTF_8));
                cn = true;
                break;
            }

            //"$6666\r\n  foobard...dd  \r\n"  有数据，很多的
            if(time == -1){
                count = count * 10 + b - '0';
                continue;
            }

            //处理最末尾的两个字节 crcn
            if(count == -1){
                cn = true;
                break;
            }

            byteBuffer.write(b);
            count--;
        }

        if(cn){
            result.add(byteBuffer.toString());
            System.out.println(byteBuffer.toString());
            sb = new StringBuffer();
            buffer.clear();
            return true;
        }else {
            sb.append(byteBuffer.toString());
            buffer.compact();
            remainCount = count;
            return false;
        }
    }

    /**
     *
     * keys *
     * *5
     * $3
     * mkk
     * $8
     * xiaoming
     * $4
     * xiao
     * $8
     * makaikai
     * $6
     * hongse
     *
     * @return
     * @throws IOException
     */
    private boolean getArrayMessageV1() throws IOException {

        boolean cn = false;
        ByteArrayBuffer byteBuffer = new ByteArrayBuffer();

        int count = 0;
        int time = -1;

        while (buffer.hasRemaining()){

            byte b = buffer.get();

            //处理头的大小
            if(time == -1 && b == '\r' ){
                time++;
            }

            if(time == -1){
                count = count * 10 + b - '0';
//                time ++;
                continue;
            }


            if(count == 0){
                //读取一个回车换行符
                if(b == '\r'){
                    continue;
                }

                if(b == '\n'){
                    // 读取完成
                    byteBuffer.write("空数据".getBytes(StandardCharsets.UTF_8));
                    cn = true;
                }

            }else if (count > 0){


                //读取长度大小后的第一个CRLF
                if( (b == '\r' || b == '\n') && time == 0){
                    byteBuffer.write(b);
                    continue;
                }


                if(b == ':'){
                    //解析数字
                    boolean isOver = getArrayNumber(byteBuffer);
                    if(isOver){
                        time++;
                    }else {
                        // TODO 考虑素组元素情况下的拆包问题
                        System.out.println("后面需要考虑下拆包的问题");
                    }
                }else if(b == '$'){
                    //解析多个行字符串
                    // fixme 这里还需要处理一下，在 使用 info 命令时候，解析有问题
                    boolean isOver = getArrayString(byteBuffer);
                    if(isOver){
                        time++;
                    }else {
                        // TODO 考虑素组元素情况下的拆包问题
                        System.out.println("后面需要考虑下拆包的问题");
                    }
                }

                if(count == time){
                    cn = true;
                    break;
                }
            }

        }


        if(cn){
            result.add(byteBuffer.toString());
            System.out.println(byteBuffer.toString());
            sb = new StringBuffer();
            buffer.clear();
            return true;
        }else {
            sb.append(byteBuffer.toString());
            buffer.compact();
            return false;
        }

    }


    /**
     * 获取
     * @return
     */
    private boolean getArrayMessage() throws IOException {
        boolean cn = false;
        ByteArrayBuffer byteBuffer = new ByteArrayBuffer();

        int count = -1;
        int time = -1;

        while (buffer.hasRemaining()){

            byte b = buffer.get();

            if(time == -1){
                count = b - '0';
                time ++;
                continue;
            }


            if(count == 0){
                //读取一个回车换行符
                if(b == '\r'){
                    continue;
                }

                if(b == '\n'){
                    // 读取完成
                    byteBuffer.write("空数据".getBytes(StandardCharsets.UTF_8));
                    cn = true;
                }

            }else if (count > 0){


                //读取多个数组
                //读取一个回车换行符
                if(b == '\r'){
                    continue;
                }

                if(b == '\n' && time == 0){
//                    time ++;
                    continue;
                }

                if(b == ':'){
                    //解析数字
                    boolean isOver = getArrayNumber(byteBuffer);
                    if(isOver){
                        time++;
                    }else {
                        // TODO 考虑素组元素情况下的拆包问题
                        System.out.println("后面需要考虑下拆包的问题");
                    }
                }else if(b == '$'){
                    //解析多个行字符串
                    // fixme 这里还需要处理一下，在 使用 info 命令时候，解析有问题
                    boolean isOver = getArrayString(byteBuffer);
                    if(isOver){
                        time++;
                    }else {
                        // TODO 考虑素组元素情况下的拆包问题
                        System.out.println("后面需要考虑下拆包的问题");
                    }
                }

                if(count == time){
                    cn = true;
                    break;
                }
            }

        }


        if(cn){
            result.add(byteBuffer.toString());
            System.out.println(byteBuffer.toString());
            sb = new StringBuffer();
            buffer.clear();
            return true;
        }else {
            sb.append(byteBuffer.toString());
            buffer.compact();
            return false;
        }

    }

    /**
     * 获取数组元素中的多行字符串类型 $
     * @return  是否完成了一次解析，后面需要考虑到拆包的问题
     * @param byteBuffer
     */
    private boolean getArrayString(ByteArrayBuffer byteBuffer) throws IOException {

        boolean isOver = false;
        int time = 0;
        while (buffer.hasRemaining()) {
            //string 的长度
            byte b = buffer.get();

            //没有数据，返回$-1\r\n
            if (b == '-') {
                byte b1 = buffer.get();
                if (b1 == '1') {
                    buffer.get();
                    buffer.get();
                }
                byteBuffer.write("空数据".getBytes(StandardCharsets.UTF_8));
                isOver = true;
                break;
            }

            if (b == '\r') {
                continue;
            }

            if (b == '\n') {
                time++;
                if (time == 2) {
                    isOver = true;
                    byteBuffer.write("  ".getBytes(StandardCharsets.UTF_8));
                    break;
                }
                continue;
            }
            if (time == 1) {
                byteBuffer.write(b);
            }
        }

        return isOver;
    }
    /**
     * 获取素数组元素中的数字类型 :
     * @param byteBuffer
     */
    private boolean getArrayNumber(ByteArrayBuffer byteBuffer) {

        Boolean isOver = false;
        while (buffer.hasRemaining()){

            byte b = buffer.get();
            if(b == '\r'){
                continue;
            }
            if(b == '\n'){
                isOver = true;
                break;
            }

            byteBuffer.write(b);

        }

        return isOver;
    }

    /**
     * 整型数据类型
     * @return
     */
    private boolean getIntegerMessage() {
        boolean cn = false;
        ByteArrayBuffer byteBuffer = new ByteArrayBuffer();

        while (buffer.hasRemaining()){

            byte b = buffer.get();
            if(b == '\r'){
                continue;
            }

            if(b == '\n'){
                cn = true;
                break;
            }

            byteBuffer.write(b);
        }


        if(cn){
            result.add(byteBuffer.toString());
            System.out.println(byteBuffer.toString());
            sb = new StringBuffer();
            buffer.clear();
            return true;
        }else {
            sb.append(byteBuffer.toString());
            buffer.compact();
            return false;
        }

    }


    /**
     * 处理错误信息
     * @return
     */
    private boolean getErrorMessage() {

        boolean cn = false;
        ByteArrayBuffer byteBuffer = new ByteArrayBuffer();

        while (buffer.hasRemaining()){

            byte b = buffer.get();
            if(b == '\r'){
                continue;
            }

            if(b == '\n'){
                cn = true;
                break;
            }

            byteBuffer.write(b);
        }


        if(cn){
            //处理大包的数据
            sb.append(byteBuffer.toString());
            result.add(sb.toString());
            System.out.println(sb.toString());
            sb = new StringBuffer();
            buffer.clear();
            return true;
        }else {
            sb.append(byteBuffer.toString());
            buffer.compact();
            return false;
        }
    }

    /**
     *
     * 处理多行字符串
     * @return
     * @throws IOException
     */
    private boolean getStringMessage() throws IOException {

        boolean cn = false;
        ByteArrayBuffer byteBuffer = new ByteArrayBuffer();

        int time = 0;
        int count = -1;
        while (buffer.hasRemaining()){

            //string 的长度
            byte b = buffer.get();

            //没有数据，返回$-1\r\n
            if (b == '-') {
                byte b1 = buffer.get();
                if (b1 == '1') {
                    buffer.get();
                    buffer.get();
                }
                byteBuffer.write("空数据".getBytes(StandardCharsets.UTF_8));
                cn = true;
                break;
            }

            if (b == '\r') {
                continue;
            }

            if (b == '\n') {
                time++;
                if (time == 2) {
                    cn = true;
                    byteBuffer.write("  ".getBytes(StandardCharsets.UTF_8));
                    break;
                }
                continue;
            }
            if (time == 1) {
                byteBuffer.write(b);
            }

//
//
//            //string 的长度
//            byte b = buffer.get();
//
//            //没有数据，返回$-1\r\n
//            if(b == '-'){
//                byte b1 = buffer.get();
//                if(b1 == '1'){
//                    buffer.get();
//                    buffer.get();
//                }
//                byteBuffer.write("空数据".getBytes(StandardCharsets.UTF_8));
//                cn = true;
//                break;
//            }
//
//            /**
//             *
//                info 的命令的解析，需要处理下
////            if(time == -1){
////                count = b - '0';
////                time++;
////                continue;
////            }
////
////            byteBuffer.write(b);
////
////            if(b == '\n'){
////                time++;
////                if(time == count){
////                    cn = true;
////                    break;
////                }
////            }
//             */
//
//            if(b == '\r'){
//                byteBuffer.write(b);
//                continue;
//            }
//
//            if(b == '\n'){
//                byteBuffer.write(b);
//                time ++;
//                if(time == 2){
//                    cn = true;
//                    break;
//                }
//                continue;
//            }
//            if(time == 1){
//                byteBuffer.write(b);
//            }
        }

        if(cn){
            result.add(byteBuffer.toString());
            System.out.println(byteBuffer.toString());
            sb = new StringBuffer();
            buffer.clear();
            return true;
        }else {
            sb.append(byteBuffer.toString());
            buffer.compact();
            return false;
        }

    }

    /**
     * 单行字符串解析
     * @return
     */
    private boolean getCommonMessage() {

        boolean cr = false;
        boolean cn = false;
        ByteArrayBuffer byteBuffer = new ByteArrayBuffer();

        while (buffer.hasRemaining()){
            //假设一次响应只会有一个命令的结果
            byte b = buffer.get();
            if((char)b == '\r'){
                continue;
            }

            if((char)b == '\n' ){
                cn = true;
                break;
            }
            byteBuffer.write(b);
        }

        if(cn){
            result.add(byteBuffer.toString());
            System.out.println(byteBuffer.toString());
            sb = new StringBuffer();
            buffer.clear();
            return true;
        }else {
            sb.append(byteBuffer.toString());
            buffer.compact();
            return false;
        }


    }


    private void readByteToBuffer() throws IOException {
        int read = channel.read(buffer);
        if(read == -1){
//            channel.close();
            System.out.println("连接关闭");
            this.getResult().add("连接关闭");
        }
        buffer.flip();
    }


    /**
     *  编码并发送数据
     * @throws Exception
     */
    public void encode() throws Exception {

        String take = commons.take();

        String[] args = take.split(" ");

        StringBuffer sb = new StringBuffer();
        sb.append("*");
        sb.append(args.length + CRLF);

        for (String arg : args){
            sb.append("$"+ arg.length() + CRLF);
            sb.append(arg + CRLF);
        }


        channel.write(Charset.forName("UTF-8").encode(sb.toString()));
        channel.register(selector, SelectionKey.OP_READ,this);
    }
}
