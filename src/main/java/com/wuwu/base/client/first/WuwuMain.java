package com.wuwu.base.client.first;

import java.util.concurrent.ExecutionException;

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
        wuwuPipeline.addHandler(new MessageHandler() {
            @Override
            public void handle(WuwuResponse response) {
                System.out.println("处理器处理:" + response);
            }

            @Override
            public boolean match() {

                return false;
            }
        });

        wuwuConfig.setPipeline(wuwuPipeline);
        //0. 配置一些配置信息，并启动client
        wuwuApplication.startApplication(wuwuConfig);


        //1.从上下文中获取相应的client实例，用于发送命令，解析命令
        WuwuFutureClient client = wuwuApplication.getClient();

        //2.发送命令
        client.sendCommon("keys *");

        //3.获取结果命令，这里会阻塞，需要整个响应数据封装完成，才能返回结果
        //  理论上，实际 可以使用 isDone 方法来判断，是否完成解析，这样主线程可以用来执行其他命令（不阻塞用户主线程），
        //  等到必须需要的时候，才get
        WuwuResponse response = (WuwuResponse) client.get();


        //4.显示结果
        Object result = response.getResult();
        System.out.println(result);


        Thread.sleep(1000 * 1000);

    }
}
