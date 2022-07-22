package com.jdcloud.logs.sample;

import com.jdcloud.logs.api.common.LogContent;
import com.jdcloud.logs.api.common.LogItem;
import com.jdcloud.logs.producer.LogProducer;
import com.jdcloud.logs.producer.Producer;
import com.jdcloud.logs.producer.config.ProducerConfig;
import com.jdcloud.logs.producer.config.RegionConfig;
import com.jdcloud.logs.producer.errors.ProducerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 性能测试示例
 *
 * @author liubai
 * @date 2022/7/16
 */
public class PerformanceSample {

    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceSample.class);

    public static void main(String[] args) throws InterruptedException {
        String accessKeyId = System.getProperty("accessKeyId");
        String secretAccessKey = System.getProperty("secretAccessKey");
        final String logTopic = System.getProperty("logTopic");
        final String regionId = System.getProperty("regionId");
        String endpoint = System.getProperty("endpoint");
        int produceThreads = Integer.parseInt(System.getProperty("produceThreads"));
        final int produceTimes = Integer.parseInt(System.getProperty("produceTimes"));
        int sendThreads = Integer.parseInt(System.getProperty("sendThreads"));
        int totalSizeInBytes = Integer.parseInt(System.getProperty("totalSizeInBytes"));
        int batchSize = Integer.parseInt(System.getProperty("batchSize"));
        int batchSizeInBytes = Integer.parseInt(System.getProperty("batchSizeInBytes"));

        LOGGER.info("Ready to create producer, logTopic={}, regionId={}, endpoint={}, produceThreads={}, produceTimes={}, "
                        + "sendThreads={}, totalSizeInBytes={}, batchSize={}, batchSizeInBytes={}", logTopic, regionId,
                endpoint, produceThreads, produceTimes, sendThreads, totalSizeInBytes, batchSize, batchSizeInBytes);

        ProducerConfig producerConfig = new ProducerConfig();
        producerConfig.setSendThreads(sendThreads);
        producerConfig.setTotalSizeInBytes(totalSizeInBytes);
        producerConfig.setBatchSize(batchSize);
        producerConfig.setBatchSizeInBytes(batchSizeInBytes);

        RegionConfig regionConfig = new RegionConfig(accessKeyId, secretAccessKey, regionId, endpoint);
        final Producer producer = new LogProducer(producerConfig);
        producer.putRegionConfig(regionConfig);

        final AtomicInteger logId = new AtomicInteger(0);
        ExecutorService executorService = Executors.newFixedThreadPool(produceThreads);
        final CountDownLatch latch = new CountDownLatch(produceThreads);

        LOGGER.info("Sample started");
        long start = System.currentTimeMillis();
        for (int i = 0; i < produceThreads; ++i) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int i = 0; i < produceTimes; ++i) {
                            producer.send(regionId, logTopic, buildLogItem(logId.getAndIncrement()));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    latch.countDown();
                }
            });
        }
        latch.await();

        LOGGER.info("Produce end, producer logCount={}, availableMemoryInBytes={}", producer.getLogCount(),
                producer.availableMemoryInBytes());

        while (true) {
            if (producer.getLogCount() == 0) {
                break;
            }
            Thread.sleep(100);
        }

        long end = System.currentTimeMillis();
        long cost = end - start;
        LOGGER.info("Sample end, cost {} millis", cost);
        // 压缩比在100以上
        int compressionRatio = 73;
        int totalCount = produceThreads * produceTimes;
        int singleBytes = calculate(buildLogItem(0));
        long totalBytes = (long) singleBytes * totalCount;
        long compressedBytes = totalBytes / compressionRatio;
        long throughput = totalBytes / (cost / 1000);
        long compressedThroughput = compressedBytes / (cost / 1000);
        LOGGER.info("Total count={}, single bytes={}, total Bytes={}, total compressed bytes={}, " +
                        "throughput={}/s, compressed throughput={}/s", totalCount, singleBytes, totalBytes, compressedBytes,
                throughput, compressedThroughput);

        try {
            producer.close();
        } catch (ProducerException e) {
            LOGGER.warn("Close producer error", e);
        }
        long producerEnd = System.currentTimeMillis();
        LOGGER.info("Close producer end, total cost {} millis", producerEnd- start);
        executorService.shutdown();
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

    public static LogItem buildLogItem(int seq) {
        LogItem logItem = new LogItem(System.currentTimeMillis());
        logItem.addContent("level", "INFO");
        logItem.addContent("thread", "pool-1-thread-2");
        logItem.addContent("location", "com.jdcloud.logs.producer.core.BatchSender.sendBatch(BatchSender.java:117)");
        logItem.addContent("message", seq + "This is a test message,"
                + "测试日志_____abcdefghijklmnopqrstuvwxyz~!@#$%^&*()_0123456789,"
                + "测试日志_____abcdefghijklmnopqrstuvwxyz~!@#$%^&*()_0123456789,"
                + "测试日志_____abcdefghijklmnopqrstuvwxyz~!@#$%^&*()_0123456789,"
                + "测试日志_____abcdefghijklmnopqrstuvwxyz~!@#$%^&*()_0123456789,"
                + "测试日志_____abcdefghijklmnopqrstuvwxyz~!@#$%^&*()_0123456789,"
                + "测试日志_____abcdefghijklmnopqrstuvwxyz~!@#$%^&*()_0123456789");
        return logItem;
    }
}
