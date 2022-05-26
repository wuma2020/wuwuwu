# wuwuwu

基于java nio的redis客户端

#### 介绍

1. RESP协议的解析
2. 内存分配的管理
3. pipeline的handle的处理
4. 重复使用client

#### 使用demo

1. 先添加pom依赖

```xml

<dependency>
    <groupId>wuma2080</groupId>
    <artifactId>wuwuwu</artifactId>
    <version>0.0.1-SNAPSHOT</version>
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