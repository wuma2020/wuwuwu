package com.wuwu.base.client;


import org.apache.logging.log4j.util.Strings;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * 客户端实例
 */
public class WuwuFutureClient {


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


    // ==========================下面为一些基础方法，如发送message 和 get response==============================

    /**
     * 认证方法
     *
     * @return string
     * @throws Exception the exception
     */
    public String auth() throws Exception {

        String password = WuwuApplication.config.getPassword();
        if (password == null || Strings.isBlank(password)) {
            return "未配置redis密码";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("auth").append(" ").append(password);
        this.sendCommon(sb.toString());
        return (String) this.getCommonResponse().getResult();
    }


    /**
     * 根据key获取结果
     *
     * @param key the key
     * @return 该key对应的结果 string
     * @throws Exception the exception
     */
    public String get(String key) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("get").append(" ").append(key);
        this.sendCommon(sb.toString());
        return (String) this.getCommonResponse().getResult();
    }

    /**
     * 设置值
     *
     * @param key   the key
     * @param value the value
     * @return 设置响应 string
     * @throws Exception the exception
     */
    public String set(String key, String value) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("set").append(" ").append(key).append(" ").append(value);
        this.sendCommon(sb.toString());
        return (String) this.getCommonResponse().getResult();
    }

    /**
     * keys * 命令
     *
     * @return object
     * @throws Exception the exception
     */
    public Object keys() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("keys *");
        this.sendCommon(sb.toString());
        return (Object) this.getCommonResponse().getResult();
    }

    /**
     * info
     *
     * @return object
     * @throws Exception the exception
     */
    public Object info() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("info");
        this.sendCommon(sb.toString());
        return (Object) this.getCommonResponse().getResult();
    }


    // ==========================下面为一些基础方法，如发送message 和 get response==============================

    /**
     * 读取完成后，重置一些数据，重新进行写
     */
    private void reWrite() {
        this.setBuffer(ByteBuffer.allocate(1024));
        this.setFinish(false);
        this.setResponse(new WuwuResponse());
        this.setWrited(true);
    }

    /**
     * 发送命令
     *
     * @param common the common
     * @return 返回发送命令是否成功 boolean
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
    public Boolean sendCommon(String common) throws IOException, InterruptedException {

        if (common == null || common.length() == 0) {
            return false;
        }

        boolean writable = false;
        while (true) {
            writable = key.isWritable();
            if (!writable) {
                System.out.println("写 key 一直是false");
                Thread.sleep(1000);
                continue;
            }
            break;
        }

        if (writable) {
            this.setFinish(false);
            //在这里直接发送命令，然后注册这个socket到读事件中
            ByteBuffer encode = ProtocolUtil.encode(common);
            if (encode == null) {
                return false;
            }

            socketChannel.write(encode);
            //这里需要把对应的attach对象附件上去
            socketChannel.register(selector, SelectionKey.OP_READ, this);
            this.setBuffer(ByteBuffer.allocate(1024));
            this.setResponse(new WuwuResponse());
            this.setFinish(false);
            return true;
        } else {
            return false;
        }

    }

    /**
     * 需要先判断该socket是否是可读状态的，如果是，才读
     * 这里需要完成一整个响应完成，才能返回，并且 重置掉 掉这个socket之前对应的解析协议的状态()
     * 完成后，需要将socket设置成可写状态，该socket就可以复用了
     * <p>
     * 读数据应该是在select监听的读事件里面进行
     *
     * @return common response
     * @throws Exception the exception
     */
    public WuwuResponse getCommonResponse() throws Exception {

        if (isFinish) {
            isReaded = true;
            WuwuPipeline pipeline = WuwuApplication.config.getPipeline();
            pipeline.doHandler(this.getResponse());
            WuwuResponse wuwuResponse = new WuwuResponse();
            wuwuResponse.setResult(this.getResponse().getResult());
            wuwuResponse.setType(this.getResponse().getType());
            reWrite();
            return wuwuResponse;
        } else {
            while (true) {
                // TODO 这里可以优化一下
                Thread.sleep(1000);
                WuwuResponse wuwuResponse = getCommonResponse();
                if (wuwuResponse != null) {
                    return wuwuResponse;
                }
            }
        }
    }

    /**
     * 回收这个socket channel
     */
    public void recycleSocket() {
        WuwuApplication.getClients().add(this);
    }


    // ==========================下面为一些set get 方法==============================

    /**
     * Gets socket channel.
     *
     * @return the socket channel
     */
    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    /**
     * Sets socket channel.
     *
     * @param socketChannel the socket channel
     */
    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    /**
     * Gets selector.
     *
     * @return the selector
     */
    public Selector getSelector() {
        return selector;
    }

    /**
     * Sets selector.
     *
     * @param selector the selector
     */
    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    /**
     * Gets key.
     *
     * @return the key
     */
    public SelectionKey getKey() {
        return key;
    }

    /**
     * Sets key.
     *
     * @param key the key
     */
    public void setKey(SelectionKey key) {
        this.key = key;
    }

    /**
     * Gets response.
     *
     * @return the response
     */
    public WuwuResponse getResponse() {
        return response;
    }

    /**
     * Sets response.
     *
     * @param response the response
     */
    public void setResponse(WuwuResponse response) {
        this.response = response;
    }

    /**
     * Gets buffer.
     *
     * @return the buffer
     */
    public ByteBuffer getBuffer() {
        return buffer;
    }

    /**
     * Sets buffer.
     *
     * @param buffer the buffer
     */
    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    /**
     * Is finish boolean.
     *
     * @return the boolean
     */
    public boolean isFinish() {
        return isFinish;
    }

    /**
     * Sets finish.
     *
     * @param finish the finish
     */
    public void setFinish(boolean finish) {
        isFinish = finish;
    }

    /**
     * Is readed boolean.
     *
     * @return the boolean
     */
    public boolean isReaded() {
        return isReaded;
    }

    /**
     * Sets readed.
     *
     * @param readed the readed
     */
    public void setReaded(boolean readed) {
        isReaded = readed;
    }

    /**
     * Is writed boolean.
     *
     * @return the boolean
     */
    public boolean isWrited() {
        return isWrited;
    }

    /**
     * Sets writed.
     *
     * @param writed the writed
     */
    public void setWrited(boolean writed) {
        isWrited = writed;
    }

}
