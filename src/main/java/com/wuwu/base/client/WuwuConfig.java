package com.wuwu.base.client;

import lombok.Data;

/**
 * 配置相关
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
     * 处理的pipeline
     */
    private WuwuPipeline pipeline = new WuwuPipeline();

}
