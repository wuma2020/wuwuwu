package com.wuwu.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class TestLog {

    private static final Logger LOGGER = LogManager.getLogger();

    @Test
    public void test() {
        LOGGER.info("测试");
        LOGGER.error("错误");
    }

}
