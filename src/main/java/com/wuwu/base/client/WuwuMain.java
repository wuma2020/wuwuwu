package com.wuwu.base.client;


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
        Boolean aBoolean = client.sendCommon("keys *");
        if(!aBoolean){
            System.out.println("keys * 发送失败");
        }

        WuwuResponse response = (WuwuResponse) client.getCommonResponse();

        //4.显示结果
        Object result = response.getResult();
        System.out.println(result);

        Boolean sendSuccess = client.sendCommon("set xiaoxiao xxxx");
        if(!sendSuccess){
            System.out.println("发送 info 不成功");
        }
        WuwuResponse wuwuResponse = client.getCommonResponse();
        System.out.println(wuwuResponse.getResult());


        Thread.sleep(1000 * 1000);

    }
}
