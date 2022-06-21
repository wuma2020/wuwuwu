package com.wuwu.base.client.cofig;

import com.wuwu.base.client.WuwuPipeline;
import lombok.Data;

/**
 * redis配置和应用程序相关配置
 */
@Data
public class WuwuConfig {


    /**
     * 最大支持的socket的数量
     */
    private Integer socketNum = 1;

    /**
     * host 地址
     */
    private String host = "localhost";

    /**
     * 端口号
     */
    private Integer port = 6379;

    /**
     * redis 密码
     */
    private String password = "";


    /**
     * 处理的pipeline
     */
    private WuwuPipeline pipeline = new WuwuPipeline();

}
