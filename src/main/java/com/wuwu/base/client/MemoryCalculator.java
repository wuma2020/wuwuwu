package com.wuwu.base.client;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * 内存计算分配器
 *
 */
public class MemoryCalculator {


    /**
     * 根据入参 计算出 是否需要扩大bytebuffer的大小，并释放原先的内存
     * 扩容规则：
     *  因为每次都是读取1024个字节，所以每次扩大2kb，暂时先定成这样，后面可以单独的一块一块的解决
     *  此时的buffer还是写状态
     *  翻转，然后读取到新的buffer中
     * @param oldBuffer
     * @return
     */
    public static ByteBuffer calculator(ByteBuffer oldBuffer){

        int remaining = oldBuffer.remaining();
        if(remaining < 1024){

            oldBuffer.flip();
            //当前的有效字节数
            int limit = oldBuffer.limit();

            ByteBuffer newBuffer = ByteBuffer.allocate(limit + 2048);
            while (oldBuffer.hasRemaining()){
                newBuffer.put(oldBuffer.get());
            }
            oldBuffer = null;
            return newBuffer;
        }else {
            return oldBuffer;
        }
    }


    public static void main(String[] args) {
        ByteBuffer buffer1 = ByteBuffer.allocate(1024);
        buffer1.put("123".getBytes(StandardCharsets.UTF_8));
//        ByteBuffer bufferuffer = ByteBuffer.wrap("123".getBytes(StandardCharsets.UTF_8));

        ByteBuffer calculator = calculator(buffer1);
        System.out.println(calculator);


    }

}
