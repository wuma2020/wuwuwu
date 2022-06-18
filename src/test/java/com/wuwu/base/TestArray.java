package com.wuwu.base;

import org.junit.Ignore;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

@Ignore
public class TestArray {


    @Test
    public void test(){

        StringBuffer sb = new StringBuffer();

        String ss  = "重恩";

        byte[] bytes = ss.getBytes(StandardCharsets.UTF_8);

        for (byte b : bytes) {
            sb.append( (char) b );
        }

        System.out.println(sb.toString());

    }

}
