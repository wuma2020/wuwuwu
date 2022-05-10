package com.wuwu.base.client.first;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * redis 客户端的应用
 */
public class WuwuApplication {


    private WuwuConfig config;

    private LinkedBlockingQueue<WuwuFutureClient> clients = new LinkedBlockingQueue<>();

    /**
     * 启动应用
     */
    public void startApplication(WuwuConfig config) throws Exception {
        this.config = config;
        //这里启动整个应用
        start();
    }

    private void start() throws IOException {

        Selector selector = Selector.open();

        //注册socket
        registerSocketToSelector(selector);

        //处理socket消息
        handleMessage(selector);

    }

    private void handleMessage(Selector selector) throws IOException {

        while (true) {

            int readyNum = selector.select();
            if (readyNum > 0) {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {

                    SelectionKey key = iterator.next();
                    SocketChannel channel = (SocketChannel) key.channel();

                    //搜集连接的socket
                    if (key.isConnectable() && channel.finishConnect()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        WuwuFutureClient wuwuFutureClient = new WuwuFutureClient();
                        wuwuFutureClient.setSocketChannel(client);
                        wuwuFutureClient.setSelector(selector);
                        clients.add(wuwuFutureClient);
                        key.attach(wuwuFutureClient);
                    }

                    if (key.isReadable()) {
                        WuwuFutureClient futureClient = (WuwuFutureClient) key.attachment();
                        //deal RESP协议


                        //如果完成了读操作，需要重置这个client中的一些状态信息

                    }

                    if (key.isWritable()) {

                        WuwuFutureClient futureClient = (WuwuFutureClient) key.attachment();
                        //deal write RESP协议内容即可

                    }

                    iterator.remove();


                }

            }
        }
    }

    private void registerSocketToSelector(Selector selector) throws IOException {
        Integer socketNum = config.getSocketNum();
        for (int i = 0; i < socketNum; i++) {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
            socketChannel.connect(new InetSocketAddress(config.getHost(), config.getPort()));

        }
    }


    /**
     * 获取一个客户端连接的future
     * <p>
     * 理论上来说，应该是给多个空闲的连接中一个
     * 如果连接不够了就等待有空闲的连接
     *
     * 目前没有对该单个client做并发的控制 -- 比如这个引用在多个地方，send 和 read
     * @return 返回可用的客户端连接
     * @throws InterruptedException
     */
    public WuwuFutureClient getClient() throws InterruptedException {
        WuwuFutureClient client = clients.take();
        return client;
    }


}
