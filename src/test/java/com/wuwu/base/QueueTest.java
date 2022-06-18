package com.wuwu.base;

import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.LinkedBlockingQueue;

@Ignore
public class QueueTest {

    @Test
    public void test() throws InterruptedException {

        LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();


        queue.add("aaa");
        queue.add("bbb");
        queue.add("ccc");
        queue.add("ddd");


        System.out.println(queue.take());
        System.out.println(queue);

    }


}
