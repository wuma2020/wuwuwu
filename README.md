# wuwuwu

基于java nio的redis客户端

#### 介绍

0. 基于java nio的redis客户端
1. RESP协议的解析
2. 内存分配的管理
3. pipeline的handle的处理
4. 重复使用client
5. 提供常用的api封装

#### 使用demo

1. 先添加pom依赖

```xml

<dependency>
    <groupId>io.github.wuma2020</groupId>
    <artifactId>wuwuwu</artifactId>
    <version>0.0.1</version>
</dependency>

```

2. 使用demo

```java

public class testWuwuwu {

    @Test
    public void test() throws Exception {

        WuwuApplication wuwuApplication = new WuwuApplication();
        WuwuConfig wuwuConfig = new WuwuConfig();
        wuwuConfig.setHost("localhost");
        wuwuConfig.setPort(6379);
        wuwuConfig.setPassword("123456");

        wuwuApplication.startApplication(wuwuConfig);

        WuwuFutureClient client = wuwuApplication.getClient();

        Object info = client.keys();
        System.out.println(info);

    }
}

````

#### 特性


**0.0.2 正式版本  doing**
1.连接的自动获取以及自动释放，不用手动调用client.recycleSocket()
2.优化发送命令和获取结果时候等阻塞的处理
3.添加string，hash类型的完整的api

**0.0.1 正式版本  2022/06/21**
已经完成
1. RESP协议的解析
2. 内存分配的管理
3. pipeline的handle的处理（过滤器连处理每次从redis获取的返回值，进行加工后返回给使用者）
4. 重复使用client(没有实现自动获取连接以及自动释放，还需要显示的调用client.recycleSocket()进行释放，下一个版本进行自动获取client，用完后自动释放)
5. 提供常用的api封装(目前只完成了 get set info keys 命令的实现，后续增加更多api，包括对pipeline，pubsub的支持，未来也许支持集群模式，甚至redis-search)



