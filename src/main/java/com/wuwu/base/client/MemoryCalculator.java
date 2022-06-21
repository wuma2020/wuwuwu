package com.wuwu.base.client;


import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * 内存计算分配器
 * 使用的时候，可以开两个内存计算的实体类，然后通过wuwuConfig来配置响应的开关，
 * 看启用哪一个内存处理器
 */
public class MemoryCalculator {

    /**
     * 最大20M的内存
     */
    public static Integer MAX_BUFFER_SIZE = 1024 * 1024 * 20;

    /**
     * 根据入参 计算出 是否需要扩大bytebuffer的大小，并释放原先的内存
     * 扩容规则：
     * 因为每次都是读取1024个字节，所以每次扩大2kb，暂时先定成这样，后面可以单独的一块一块的解决
     * 此时的buffer还是写状态
     * 翻转，然后读取到新的buffer中
     *
     * @param oldBuffer 老的缓存
     * @return  新的缓存对象
     */
    public static ByteBuffer calculator_add(ByteBuffer oldBuffer) {

        int remaining = oldBuffer.remaining();
        if (remaining < 1024) {

            oldBuffer.flip();
            //当前的有效字节数
            int limit = oldBuffer.limit();

            ByteBuffer newBuffer = ByteBuffer.allocate(limit + 2048);
            while (oldBuffer.hasRemaining()) {
                newBuffer.put(oldBuffer.get());
            }
            oldBuffer = null;
            return newBuffer;
        } else {
            return oldBuffer;
        }
    }


    /**
     * 计算新的缓冲区
     *
     * @param oldBuffer 老的缓存对象
     * @return 新的缓存对象
     */
    public static ByteBuffer calculator_double(ByteBuffer oldBuffer) {

        int remaining = oldBuffer.remaining();
        if (remaining < 1024) {

            oldBuffer.flip();

            //此时最大的大小
            int limit = oldBuffer.limit();
            ByteBuffer newBuffer = null;
            if (limit * 2 <= MAX_BUFFER_SIZE) {
                if(limit * 2 < 1024){
                    newBuffer = ByteBuffer.allocate(1024);
                }else {
                    newBuffer = ByteBuffer.allocate(limit * 2);
                }

            } else {
                newBuffer = ByteBuffer.allocate(MAX_BUFFER_SIZE);
            }

            while (oldBuffer.hasRemaining()) {
                newBuffer.put(oldBuffer.get());
            }
            oldBuffer = null;
            return newBuffer;
        } else {
            return oldBuffer;
        }

    }


    public static void main(String[] args) {
        ByteBuffer buffer1 = ByteBuffer.allocate(1024);
        buffer1.put("123".getBytes(StandardCharsets.UTF_8));
//        ByteBuffer bufferuffer = ByteBuffer.wrap("123".getBytes(StandardCharsets.UTF_8));

        ByteBuffer calculator = calculator_add(buffer1);
        System.out.println(calculator);


        ByteBuffer buffer2 = ByteBuffer.allocate(1024);
        buffer2.put("01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789".getBytes(StandardCharsets.UTF_8));
//        ByteBuffer bufferuffer = ByteBuffer.wrap("123".getBytes(StandardCharsets.UTF_8));

        ByteBuffer newBuffer = calculator_double(buffer2);
        System.out.println(newBuffer);


    }

}
