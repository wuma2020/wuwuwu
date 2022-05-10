package com.wuwu.base.client.first;


import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 客户端实例
 */
public class WuwuFutureClient implements Future<WuwuResponse> {


    /**
     * 获取的该FutureClient对应的socket对应的读取的信息
     */
    private WuwuResponse response;

    /**
     * 一个客户端对应的连接
     */
    private SocketChannel socketChannel;


    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public WuwuResponse get() throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public WuwuResponse get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }


    /**
     * 发送命令
     *
     * @return 返回发送命令是否成功
     * @throws Exception  可能会报错一些异常
     */
    public Boolean sendCommon(String common) {

        return false;
    }


    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }
}
