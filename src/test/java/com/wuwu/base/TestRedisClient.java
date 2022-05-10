package com.wuwu.base;

import org.junit.Test;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class TestRedisClient {

    private static final String SEPARATOR = "\r\n";


    @Test
    public void test() throws IOException, InterruptedException {

//        SocketChannel client = SocketChannel.open(new InetSocketAddress("localhost", 6379));

        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("localhost", 6379));
//        client.configureBlocking(true);

        if(socket.isConnected()){
            String crcn = "\r\n";
            String req = "*2\r\n$3"+crcn+"get"+crcn+"$3"+crcn+"mkk" + crcn;
            System.out.println(req);

            String aaa = "*2\\r\\n$3\\r\\nGET\\r\\n$4\\r\\nname\\r\\n";

            StringBuilder cmd = new StringBuilder();
            // 命令加参数个数
            cmd.append("*2").append(SEPARATOR);
            // 当前命令长度
            cmd.append("$3").append(SEPARATOR);
            cmd.append("get").append(SEPARATOR);
            // 命令参数长度
            cmd.append("$4").append(SEPARATOR);
            cmd.append("name").append(SEPARATOR);

            socket.getOutputStream().write(req.toString().getBytes(StandardCharsets.UTF_8));
            socket.getOutputStream().flush();


            byte[] resp = new byte[1024];
            socket.getInputStream().read(resp);
            System.out.println(new String(resp));
            socket.close();
            return;
        }



//        ByteBuffer.wrap(req.getBytes(StandardCharsets.UTF_8)).compact();
//        client.write(ByteBuffer.wrap(req.getBytes(StandardCharsets.UTF_8),0,req.length()));
//
//        ByteBuffer buffer = ByteBuffer.allocate(64);
//
//        ByteBuffer[] byteBuffers = new ByteBuffer[10];
//        client.socket().getInputStream();
//        client.read(byteBuffers,0,50);
//
//        String s = new String(buffer.array(), 0, buffer.limit());
//        System.out.println("读取数据:" +s);
////        Thread.sleep(1000 * 30);
//        client.close();

    }

    @Test
    public void test_2() throws IOException, InterruptedException {

        SocketChannel client = SocketChannel.open(new InetSocketAddress("localhost", 6379));

        client.configureBlocking(true);

        String crcn = "\r\n";
        String req = "*2\r\n$3"+crcn+"get"+crcn+"$3"+crcn+"mkk" + crcn;


        ByteBuffer.wrap(req.getBytes(StandardCharsets.UTF_8)).compact();
        client.write(ByteBuffer.wrap(req.getBytes(StandardCharsets.UTF_8),0,req.length()));

        ByteBuffer buffer = ByteBuffer.allocate(64);
        client.read(buffer);

        String s = new String(buffer.array(), 0, buffer.position());
        System.out.println("读取数据:" +s);
//        Thread.sleep(1000 * 30);
        client.close();

    }



}
