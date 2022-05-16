package com.wuwu.base.client.first;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.concurrent.*;

/**
 * redis 客户端的应用
 */
public class WuwuApplication {


    private WuwuConfig config;


    private ExecutorService workPool = Executors.newFixedThreadPool(1);

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

        CompletableFuture.runAsync(() -> {
            try {
                Selector selector = Selector.open();
                //注册socket
                registerSocketToSelector(selector);
                //处理socket消息
                handleMessage(selector);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("出现异常:" + e.getMessage());
            }
        }, workPool);


    }

    private void handleMessage(Selector selector) throws IOException, InterruptedException {

        int time = 0;
        LocalDateTime now = LocalDateTime.now();
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
                        wuwuFutureClient.setKey(key);
                        key.attach(wuwuFutureClient);
                        //连接完成，注册写事件
                        client.register(selector, SelectionKey.OP_WRITE);
                        clients.add(wuwuFutureClient);
                    }

                    if (key.isReadable()) {
                        WuwuFutureClient futureClient = (WuwuFutureClient) key.attachment();
                        //deal RESP协议
                        ProtocolUtil.decode(futureClient);

                    }

                    iterator.remove();


                }

            }

            time++;
            if (time == 100) {
                LocalDateTime now1 = LocalDateTime.now();
                if (now.plusSeconds(3L).isAfter(now1)) {
                    //出现是类似死循环,记录日志
                    System.out.println("循环---");
                    Thread.sleep(1000);
                    time = 0;
                }
                time = 0;
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
     * <p>
     * 目前没有对该单个client做并发的控制 -- 比如这个引用在多个地方，send 和 read
     *
     * @return 返回可用的客户端连接
     * @throws InterruptedException
     */
    public WuwuFutureClient getClient() throws InterruptedException {
        WuwuFutureClient client = clients.take();
        return client;
    }


}
