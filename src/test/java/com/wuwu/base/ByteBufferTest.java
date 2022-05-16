package com.wuwu.base;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ByteBufferTest {

    /**
     * 结果如下L
     * buffer.put:java.nio.HeapByteBuffer[pos=3 lim=10 cap=10]
     * buffer.flip():java.nio.HeapByteBuffer[pos=0 lim=3 cap=10]
     * 1
     * 2
     * 3
     * 1
     * 2
     * 3
     * 4
     * 5
     * 6
     * java.nio.HeapByteBuffer[pos=6 lim=6 cap=10]
     */
    @Test
    public void test() {

        ByteBuffer buffer = ByteBuffer.allocate(10);

        buffer.put("123".getBytes(StandardCharsets.UTF_8));
        System.out.println("buffer.put:" + buffer);

        //写模式 -> 切换成读模式
        buffer.flip();
        System.out.println("buffer.flip():" + buffer);

        //读模式 -> 读取数据
        byte a = buffer.get();
        System.out.println(String.valueOf((char) a));

        byte b = buffer.get();
        System.out.println(String.valueOf((char) b));
        //读模式 -> 读取数据
        byte c = buffer.get();
        System.out.println(String.valueOf((char) c));

        //读模式 -> 重置读取，从0开始重新读取
        buffer.rewind();

        //读模式 -> 切换成写模式  pos 切换到当前可用的数据的index值，limit 设置成最大值 cap，开始冲pos位置继续写入
        buffer.compact();
        //写数据
        buffer.put("456".getBytes(StandardCharsets.UTF_8));

        //写数据 -> 切换成读数据模式
        buffer.flip();

        //读模式 -> 读取数据
        byte d = buffer.get();
        System.out.println(String.valueOf((char) d));

        byte e = buffer.get();
        System.out.println(String.valueOf((char) e));
        //读模式 -> 读取数据
        byte f = buffer.get();
        System.out.println(String.valueOf((char) f));

        byte h = buffer.get();
        System.out.println(String.valueOf((char) h));

        byte i = buffer.get();
        System.out.println(String.valueOf((char) i));
        //读模式 -> 读取数据
        byte j = buffer.get();
        System.out.println(String.valueOf((char) j));

        System.out.println(buffer);


    }
}
