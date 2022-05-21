package com.wuwu.base.client;


import com.wuwu.base.client.Handler.ArrayHandler;

/**
 * 主要的启动类
 */
public class WuwuMain {

    public static void main(String[] args) throws Exception {

        WuwuApplication wuwuApplication = new WuwuApplication();
        WuwuConfig wuwuConfig = new WuwuConfig();
        wuwuConfig.setHost("localhost");
        wuwuConfig.setPort(6379);
        WuwuPipeline wuwuPipeline = new WuwuPipeline();
        wuwuPipeline.addHandler(new ArrayHandler());
        wuwuConfig.setPipeline(wuwuPipeline);

        //0. 配置一些配置信息，并启动client
        wuwuApplication.startApplication(wuwuConfig);

        //1.从上下文中获取相应的client实例，用于发送命令，解析命令
        WuwuFutureClient client = wuwuApplication.getClient();

        //2.发送命令
        Boolean aBoolean = client.sendCommon("keys *");
        if (!aBoolean) {
            System.out.println("keys * 发送失败");
        }

        WuwuResponse response = client.getCommonResponse();

        //4.显示结果
        Object result = response.getResult();
        System.out.println(result);

//        client.recycleSocket();

        Thread.sleep(1000 * 1000);

    }
}
