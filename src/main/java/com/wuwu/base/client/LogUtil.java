package com.wuwu.base.client;

import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Data
public class LogUtil {

    public static Logger log =  LogManager.getLogger();



}
