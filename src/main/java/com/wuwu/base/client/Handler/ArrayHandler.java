package com.wuwu.base.client.Handler;

import com.wuwu.base.client.MessageHandler;
import com.wuwu.base.client.WuwuResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 对数组类型的消息进行处理
 */
public class ArrayHandler implements MessageHandler {
    @Override
    public void handle(WuwuResponse response) {
        Object result = response.getResult();
        if (result instanceof List) {
            List<String> change = ((List<?>) result).stream().map(one -> "【 " + one + " 】").collect(Collectors.toList());
            response.setResult(change);
        }
    }

    @Override
    public boolean match() {
        return false;
    }
}
