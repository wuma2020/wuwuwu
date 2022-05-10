package com.wuwu.base;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * 开启监听服务
 */
public class WuSocketServer {


    public WuSocketConfig wuSocketConfig = new WuSocketConfig();

    /**
     * 开启监听服务
     *
     * tcp
     */
    public void createServer() throws IOException {

        ServerSocketChannel server = ServerSocketChannel.open();
        server.configureBlocking(false);
        server.bind(new InetSocketAddress("localhost", 8080));

        Selector selector = Selector.open();
        server.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("创建监听完成");
        while (true){

            if (isException(selector)) break;

            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();

            while (iterator.hasNext()){
                SelectionKey selectKey = iterator.next();
                dealWithClientRequest(selector, selectKey);
            }

        }




    }

    private void dealWithClientRequest(Selector selector, SelectionKey selectKey) throws IOException {

        SocketChannel client = null;
        ServerSocketChannel serverSocket = null;

        try {
            clientWell(selector, selectKey,client,serverSocket);
        }catch (Exception e){

            if(client != null){
                client.close();
            }
            if(serverSocket != null){
                serverSocket.close();
            }
            //需要关闭连接不然业务异常就会死循环
            e.printStackTrace();
        }

    }

    private void clientWell(Selector selector, SelectionKey selectKey,
                            SocketChannel client,
                            ServerSocketChannel serverSocketChannel) throws IOException {
        if(selectKey.isAcceptable()){
            //1.新建连接，注册读事件
            serverSocketChannel = (ServerSocketChannel) selectKey.channel();
            client = serverSocketChannel.accept();
            if(client == null){
                return;
            }
            client.configureBlocking(false);
            client.register(selector,SelectionKey.OP_READ,SelectionKey.OP_WRITE);
            System.out.println("新建连接并且注册到select中");

        }else if(selectKey.isReadable()){
            client = (SocketChannel) selectKey.channel();
            selectKey.attach(ByteBuffer.allocate(64));
            client.configureBlocking(false);
            handReadFrame(client,selectKey);
        }else if(selectKey.isWritable()){
            client = (SocketChannel) selectKey.channel();
            client.configureBlocking(false);
            client.write(new ByteBuffer[]{ByteBuffer.wrap(new String("我是aa").getBytes(StandardCharsets.UTF_8))});
            System.out.println("写事件完成");
        }
    }

    private void handReadFrame(SocketChannel client, SelectionKey selectKey) throws IOException {

        //1.先读取integer 大小的报文长度
        ByteBuffer buffer = (ByteBuffer)selectKey.attachment();

        if(buffer == null){
            buffer = ByteBuffer.allocate(64);
        }

        int read = client.read(buffer);
        if(read < 0){
            client.close();
            throw  new RuntimeException("对方关闭连接");
        }else {
            //拆分buffer
            getInfo(buffer,client);
            selectKey.attach(buffer);
        }

    }

    /**
     * 业务数据的拆分
     * @param buffer
     * @param client
     */
    private void getInfo(ByteBuffer buffer, SocketChannel client) throws IOException {

        //切换到读模式  position -> 0   limit -> position
        buffer.flip();
        buffer.mark();

        if(buffer.limit() < 4){
            buffer.reset();
            buffer.compact();
            return;
        }

        int count = buffer.getInt();

        if( (buffer.limit() + 1 - 4) < count){
            buffer.reset();
            buffer.compact();
            return;
        }

        //获取一个整体报文
        byte[] bb = new byte[count];
        // fixme 这里读取字节的操作有问题，不能读数据到字节数组中去
        //读取剩下的buffer，重新放回到key中
        buffer.get(bb, 0, count);

        buffer.compact();

        String bodyInfo = new String(bb,StandardCharsets.UTF_8);

        if("xiaoming".equals(bodyInfo)){
            client.write(ByteBuffer.wrap("hello world".getBytes(StandardCharsets.UTF_8)));
        }

    }

    private boolean isException(Selector selector) {
        //尝试先获取channel，如果异常，直接脱出循环
        try {
            selector.select();
        }catch (Exception e){
            e.printStackTrace();
            return true;
        }
        return false;
    }

}
