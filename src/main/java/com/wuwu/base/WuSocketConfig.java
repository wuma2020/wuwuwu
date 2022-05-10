package com.wuwu.base;

import java.nio.ByteBuffer;

/**
 * socket 的配置中心
 */
public class WuSocketConfig {


    public static ByteBuffer head = ByteBuffer.allocate(4);

    public static ByteBuffer frame = ByteBuffer.allocate(32);

    public static final String SEPARATOR = "\r\n";



}
