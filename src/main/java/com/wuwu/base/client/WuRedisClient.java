package com.wuwu.base.client;

import com.wuwu.base.WuSocketConfig;
import lombok.Data;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

/**
 * 设置redis的客户端
 *
 */

@Data
public class WuRedisClient {

    Protocol response = new Protocol();

    public WuRedisClient(){
        runClient();
    }


    public static void main(String[] args) throws Exception {
        new WuRedisClient();
    }


    /**
     * 开启线程
     * @return
     */
    public void runClient(){
        CompletableFuture.runAsync(() -> {
            try {
                startClient(this.getResponse());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, Executors.newSingleThreadExecutor());
    };




    /**
     * 开启客户端
     */
    public synchronized void startClient(Protocol param) throws Exception {
        Selector selector = Selector.open();
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector,SelectionKey.OP_CONNECT);

        //先注册再连接
        socketChannel.connect(new InetSocketAddress("localhost", 6379));

        while (true){
            int select = selector.select();
            if(select > 0){

                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()){
                    SelectionKey key = iterator.next();

                    SocketChannel channel = (SocketChannel)key.channel();
                    if(key.isConnectable() && channel.finishConnect()){
                        SocketChannel client = (SocketChannel)key.channel();

                        if(key.attachment() == null){
                            param.setChannel(client);
                            param.setSelector(selector);
                            key.attach(param);
                        }

                        Protocol protocol = (Protocol)key.attachment();
                        protocol.encode();

                    }
                    if(key.isReadable()){

                        Protocol protocol = (Protocol) key.attachment();

                        boolean over = protocol.decode();
                        if(over){
                            //写数据，等待从输入框汇总输入命令，并解析命令，发送命令，然后继续监听读事假
                            protocol.encode();
                        }
                    }

                    iterator.remove();
                }

            }


        }
    }

    private ByteBuffer getMassage(String ping) {
        ByteBuffer encode = Charset.forName("UTF-8").encode(ping);
        return encode;
    }


    //处理读取事件
    private static boolean doRead(SelectionKey key) throws IOException {

        System.out.println("处理读取事件");
        SocketChannel channel = (SocketChannel)key.channel();

        ByteBuffer buffer = ByteBuffer.allocate(64);
        int read = 0;
        read = channel.read(buffer);

        StringBuffer sb = new StringBuffer();

        if(read == -1){
            System.out.println("连接已经关闭");
            channel.close();
        }

        while (read > 0){

            // TODO 是否需要flip一下
            CharBuffer decode = Charset.forName("UTF-8").decode(buffer);
            sb.append(decode.toString());
            buffer.clear();
            read = channel.read(buffer);
            if(read == -1){
                System.out.println("连接已经关闭");
                channel.close();
                break;
            }else if(read == 0) {
                break;
            }

        }

        System.out.println("响应: " + sb.toString());
        return true;
    }


    /**
     * 处理写事件
     * @param key
     */
    private static void doWrite(SelectionKey key) throws IOException {

        SocketChannel channel = (SocketChannel)key.channel();

        StringBuilder cmd = new StringBuilder();
        // 命令加参数个数
        cmd.append("*2").append(WuSocketConfig.SEPARATOR);
        // 当前命令长度
        cmd.append("$3").append(WuSocketConfig.SEPARATOR);
        cmd.append("get").append(WuSocketConfig.SEPARATOR);
        // 命令参数长度
        cmd.append("$4").append(WuSocketConfig.SEPARATOR);
        cmd.append("mkk").append(WuSocketConfig.SEPARATOR);

        channel.write(ByteBuffer.wrap(cmd.toString().getBytes(StandardCharsets.UTF_8)));
        System.out.println("处理写事件");
    }


}
