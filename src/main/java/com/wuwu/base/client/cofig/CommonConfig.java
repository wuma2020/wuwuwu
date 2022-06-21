package com.wuwu.base.client.cofig;

/**
 * Copyright (C), 2022-2022, wuma2020
 * Author: wuma2020
 * Date: 2022/6/21 10:34 下午
 * FileName: CommonConfig
 * Description: 代码使用到的配置
 */
public class CommonConfig {

    /**
     * 发送和接受tcp消息的等待睡眠时间
     */
    public static Long TIME_SLEEP = 10L;

    /**
     * select 空轮训时候睡眠时间
     */
    public static Long SELECT_TIME_SLEEP = 100L;

    /**
     * 空轮训持续时长 3S
     */
    public static Long SELECTOR_CIRCULATION_SLEEP = 3L;




}
