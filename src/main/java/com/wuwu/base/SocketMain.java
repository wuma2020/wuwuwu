package com.wuwu.base;


import java.io.IOException;
import java.nio.channels.SelectableChannel;

/**
 * 使用java nio写一个redis客户端，编解码器，swing展示，im系统
 *
 *  封装解码器模块，（通讯相关）消息发送，接受处理，界面这块（java swing），
 *
 *
 *  熟悉的java的nio系统以及jdk一些底层的能力，而不是直接使用netty封装的或者是tomcat，spring那些
 *  对应通讯行业，及时性要求比较高的行业，使用spring太臃肿，不现实
 */


public class SocketMain {

    public static void main(String[] args) {

        //1.新建一个socket监听，使用select，并且后续设置响应的linux的配置
        WuSocketServer wuSocketServer = new WuSocketServer();
        try {
            wuSocketServer.createServer();
            System.out.println("创建服务完成");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //2.对每一一个监听的channel进行处理

        //2.1 新开io处理线程
        //2.2 进行处理具体的io数据，即解码器，进行相应的协议的处理，比如处理redis的简单的协议
        //2.3 处理完成消息提，封装完成后就可以进行相应的业务处理--比如进行界面的显示

        //3. 消息的发送接受



//        new SelectableChannel()

    }

}
