package com.jdcloud.logs.sample.utils;

import com.jdcloud.logs.api.common.LogContent;
import com.jdcloud.logs.api.common.LogItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Utils
 *
 * @author liubai
 * @date 2022/12/1
 */
public class LogUtils {
    public static LogItem buildLogItem(int seq) {
        LogItem logItem = new LogItem(System.currentTimeMillis());
        logItem.addContent("level", "INFO_" + seq);
        logItem.addContent("thread", "pool-1-thread-2_" + seq);
        logItem.addContent("location", "com.jdcloud.logs.producer.core.BatchSender.sendBatch(BatchSender.java:117)_" + seq);
        logItem.addContent("message",
                "0测试日志_____abcdefghijklmnopqrstuvwxyz~!@#$%^&*()_0123456789_" + seq
                        + ",1测试日志_____abcdefghijklmnopqrstuvwxyz~!@#$%^&*()_0123456789_" + seq
                        + ",2测试日志_____abcdefghijklmnopqrstuvwxyz~!@#$%^&*()_0123456789_" + seq
                        + ",3测试日志_____abcdefghijklmnopqrstuvwxyz~!@#$%^&*()_0123456789_" + seq
                        + ",4测试日志_____abcdefghijklmnopqrstuvwxyz~!@#$%^&*()_0123456789_" + seq
                        + ",5测试日志_____abcdefghijklmnopqrstuvwxyz~!@#$%^&*()_0123456789_" + seq);
        return logItem;
    }

    public static List<LogItem> buildLogItems(int size) {
        List<LogItem> logItems = new ArrayList<LogItem>();
        for (int i = 0; i < size; i++) {
            logItems.add(buildLogItem(i));
        }
        return logItems;
    }

    public static int calculate(LogItem logItem) {
        int sizeInBytes = 8;
        for (LogContent content : logItem.getContents()) {
            if (content.getKey() != null) {
                sizeInBytes += content.getKey().length();
            }
            if (content.getValue() != null) {
                sizeInBytes += content.getValue().length();
            }
        }
        return sizeInBytes;
    }
}
