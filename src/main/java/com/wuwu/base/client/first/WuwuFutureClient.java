package com.wuwu.base.client.first;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
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

    /**
     * 这个 selector 还是需要使用的
     */
    private Selector selector;

    /**
     * 当前socket对应的selectionKey
     */
    private SelectionKey key;


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

        //需要先判断该socket是否是可读状态的，如果是，才读
        //这里需要完成一整个响应完成，才能返回，并且 重置掉 掉这个socket之前对应的解析协议的状态()
        //完成后，需要将socket设置成可写状态，该socket就可以复用了

        //读数据应该是在select监听的读事件里面进行，

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
     * @throws Exception 可能会报错一些异常
     */
    public Boolean sendCommon(String common) throws IOException {

        if (common == null || common.length() == 0) {
            return false;
        }

        //先判断这个socket是否是可写状态的，是可写的才进行写数据
        boolean writable = key.isWritable();
        if (writable) {
            //在这里直接发送命令，然后注册这个socket到读事件中
            ByteBuffer encode = ProtocolUtil.encode(common);
            if (encode == null) {
                return false;
            }

            socketChannel.write(encode);
            socketChannel.register(selector, SelectionKey.OP_READ);

            return true;
        } else {
            return false;
        }

    }


    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public Selector getSelector() {
        return selector;
    }

    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    public SelectionKey getKey() {
        return key;
    }

    public void setKey(SelectionKey key) {
        this.key = key;
    }
}
