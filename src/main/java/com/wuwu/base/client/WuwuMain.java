package com.wuwu.base.client;


import com.wuwu.base.client.Handler.ArrayHandler;
import com.wuwu.base.client.cofig.WuwuConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 主要的启动类
 */
public class WuwuMain {

    private static final Logger LOGGER = LogManager.getLogger();


    public static void main(String[] args) throws Exception {

        WuwuApplication wuwuApplication = new WuwuApplication();
        WuwuConfig wuwuConfig = new WuwuConfig();
        wuwuConfig.setSocketNum(2);
        wuwuConfig.setHost("localhost");
        wuwuConfig.setPort(6379);
        wuwuConfig.setPassword("123456");
        WuwuPipeline wuwuPipeline = new WuwuPipeline();
        wuwuPipeline.addHandler(new ArrayHandler());
        wuwuConfig.setPipeline(wuwuPipeline);

        //0. 配置一些配置信息，并启动client
        wuwuApplication.startApplication(wuwuConfig);

        //1.从上下文中获取相应的client实例，用于发送命令，解析命令
        WuwuFutureClient client = wuwuApplication.getClient();

//        String auth = client.auth();
//        System.out.println(auth);
//        String set = client.set("ssss", "中文");
//        System.out.println(set);
//
        Object keys = client.keys();
        LOGGER.info(keys);
//
//        Object info = client.info();

        client.recycleSocket();

        System.out.println("剩余个数: " + WuwuApplication.getClients().size());

        Thread.sleep(1000 * 1000);

    }

    private static void makeMultiThread(WuwuFutureClient client, String name) {
        ExecutorService executorService = Executors.newFixedThreadPool(100);

        for (int i = 1000; i < 5000; i++) {
            int tempInt = i;
            executorService.execute(() -> {
                try {
                    client.set("wu" + tempInt, "wu_value" + tempInt);
                    System.out.println(name + "wu" + tempInt + " | " + "wu_value" + tempInt);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

    }
}
