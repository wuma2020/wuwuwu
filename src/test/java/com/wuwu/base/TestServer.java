package com.wuwu.base;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

@Ignore
public class TestServer {

    @Test
    public void testServer() throws IOException, InterruptedException {

        SocketChannel client = SocketChannel.open(new InetSocketAddress("localhost", 8080));

        client.configureBlocking(true);

        ByteBuffer head = ByteBuffer.allocate(4);
        head.putInt(0,8);
        client.write(head);

        String ss = "xiaoming hexiaohong";
        client.write(ByteBuffer.wrap(ss.getBytes(StandardCharsets.UTF_8),0,ss.length()));
        System.out.println("输出完成22");
        ByteBuffer allocate = ByteBuffer.allocate(64);
        client.read(allocate);
        System.out.println(new String(allocate.array(),0,allocate.limit()));
        System.out.println("输出完成1");
        client.close();

        System.out.println("输出完成");
//        Thread.sleep(1000 * 1000);


    }


    @Test
    public void parseInteger(){

        ByteBuffer allocate = ByteBuffer.allocate(4);
        allocate.putInt(0,4);

        System.out.println(new String(String.valueOf(allocate.getInt(0))));

    }

    @Test
    public void parseInteger2(){

        ByteBuffer allocate = ByteBuffer.allocate(4);
        allocate.putInt(0,4);

        System.out.println(new String(String.valueOf(allocate.getInt(0))));

    }


    @Test
    public void test_byteBuffer(){

        // TODO ByteBuffer 的基本使用步骤 1、写入数据 2、flip 切换到读模式 3、读取指定字节 4、compact 切换回写模式

        ByteBuffer allocate = ByteBuffer.allocate(64);

        allocate.put("1".getBytes(StandardCharsets.UTF_8));

        allocate.put("2".getBytes(StandardCharsets.UTF_8));

        System.out.println(allocate);


    }


}
