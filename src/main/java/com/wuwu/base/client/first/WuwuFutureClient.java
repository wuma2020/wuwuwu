package com.wuwu.base.client.first;


import com.sun.org.apache.bcel.internal.generic.RET;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.TreeMap;
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
    private WuwuResponse response = new WuwuResponse();

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

    /**
     * 缓存当前socket对应的缓存数据
     * 初次申请 1024 个Byte
     */
    private ByteBuffer buffer = ByteBuffer.allocate(1024);

    /**
     * 标识当前协议包是否解析完成
     */
    private volatile boolean isFinish = false;

    /**
     * 是否完成读取
     */
    private volatile boolean isReaded = false;

    /**
     * 是否完成写入
     */
    private volatile boolean isWrited = false;


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
        //需要先判断该socket是否是可读状态的，如果是，才读
        //这里需要完成一整个响应完成，才能返回，并且 重置掉 掉这个socket之前对应的解析协议的状态()
        //完成后，需要将socket设置成可写状态，该socket就可以复用了

        //读数据应该是在select监听的读事件里面进行

//        if (isFinish) {
//            isReaded = true;
//            WuwuPipeline pipeline = WuwuApplication.config.getPipeline();
//            pipeline.doHandler(this.getResponse());
//            WuwuResponse wuwuResponse = new WuwuResponse();
//            wuwuResponse.setResult(this.getResponse().getResult());
//            reWrite();
//            return wuwuResponse;
//        } else {
//            while (true) {
//                //暂时先这样
//                Thread.sleep(1000);
//                WuwuResponse wuwuResponse = get();
//                if (wuwuResponse != null) {
//                    return wuwuResponse;
//                }
//            }
//        }

    }

    /**
     * 读取完成后，重新进行写
     * @throws ClosedChannelException
     */
    private void reWrite() throws ClosedChannelException {
        SocketChannel socketChannel = this.getSocketChannel();
        Selector selector = this.getSelector();
        this.getKey().interestOps(SelectionKey.OP_WRITE);
        socketChannel.register(selector,SelectionKey.OP_WRITE,this);
        this.setBuffer(ByteBuffer.allocate(1024));
        this.setFinish(false);
        this.setResponse(new WuwuResponse());
        this.setWrited(true);
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
        // TODO 第二次发送的时候这里一直判断false，后面看一下
        boolean writable = key.isWritable();
        if (writable) {
            this.setFinish(false);
            //在这里直接发送命令，然后注册这个socket到读事件中
            ByteBuffer encode = ProtocolUtil.encode(common);
            if (encode == null) {
                return false;
            }

            socketChannel.write(encode);
            //这里需要把对应的attach对象附件上去
            this.getKey().interestOps(SelectionKey.OP_READ);
            socketChannel.register(selector, SelectionKey.OP_READ, this);

            return true;
        } else {
            return false;
        }

    }

    public WuwuResponse getCommonResponse() throws Exception{
        //需要先判断该socket是否是可读状态的，如果是，才读
        //这里需要完成一整个响应完成，才能返回，并且 重置掉 掉这个socket之前对应的解析协议的状态()
        //完成后，需要将socket设置成可写状态，该socket就可以复用了

        //读数据应该是在select监听的读事件里面进行

        if (isFinish) {
            isReaded = true;
            WuwuPipeline pipeline = WuwuApplication.config.getPipeline();
            pipeline.doHandler(this.getResponse());
            WuwuResponse wuwuResponse = new WuwuResponse();
            wuwuResponse.setResult(this.getResponse().getResult());
            reWrite();
            return wuwuResponse;
        } else {
            while (true) {
                //暂时先这样
                Thread.sleep(1000);
                WuwuResponse wuwuResponse = getCommonResponse();
                if (wuwuResponse != null) {
                    return wuwuResponse;
                }
            }
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

    public WuwuResponse getResponse() {
        return response;
    }

    public void setResponse(WuwuResponse response) {
        this.response = response;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public boolean isFinish() {
        return isFinish;
    }

    public void setFinish(boolean finish) {
        isFinish = finish;
    }

    public boolean isReaded() {
        return isReaded;
    }

    public void setReaded(boolean readed) {
        isReaded = readed;
    }

    public boolean isWrited() {
        return isWrited;
    }

    public void setWrited(boolean writed) {
        isWrited = writed;
    }
}
