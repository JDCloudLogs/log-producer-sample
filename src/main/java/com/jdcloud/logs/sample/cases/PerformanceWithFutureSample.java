package com.jdcloud.logs.sample.cases;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.jdcloud.logs.producer.LogProducer;
import com.jdcloud.logs.producer.Producer;
import com.jdcloud.logs.producer.config.ProducerConfig;
import com.jdcloud.logs.producer.config.RegionConfig;
import com.jdcloud.logs.producer.errors.ProducerException;
import com.jdcloud.logs.producer.errors.SendFailureException;
import com.jdcloud.logs.producer.res.Response;
import com.jdcloud.logs.sample.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 带异步回调的性能测试
 *
 * @author liubai
 * @date 2022/11/29
 */
public class PerformanceWithFutureSample implements Sample {

    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceWithFutureSample.class);

    @Override
    public void execute() throws InterruptedException {
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

        final Producer producer = new LogProducer(producerConfig);
        RegionConfig regionConfig = new RegionConfig(accessKeyId, secretAccessKey, regionId, endpoint);
        producer.putRegionConfig(regionConfig);

        final ExecutorService sendPool = Executors.newFixedThreadPool(produceThreads);
        final ExecutorService resultPool = Executors.newFixedThreadPool(1);
        final CountDownLatch latch = new CountDownLatch(produceThreads * produceTimes);
        final FutureCallback<Response> futureCallback = new LogFutureCallback(latch);

        LOGGER.info("Sample started");
        final Random random = new Random();
        long start = System.currentTimeMillis();
        for (int i = 0; i < produceThreads; ++i) {
            sendPool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int i = 0; i < produceTimes; ++i) {
                            int seq = random.nextInt(produceTimes);
                            ListenableFuture<Response> future = producer.send(regionId, logTopic,
                                    LogUtils.buildLogItem(seq));
                            Futures.addCallback(future, futureCallback, resultPool);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
        int compressionRatio = 9;
        int totalCount = produceThreads * produceTimes;
        int singleBytes = LogUtils.calculate(LogUtils.buildLogItem(0));
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
        LOGGER.info("Close producer end, total cost {} millis", producerEnd - start);
        sendPool.shutdown();
        resultPool.shutdown();
    }

    static class LogFutureCallback implements FutureCallback<Response> {

        final CountDownLatch latch;

        LogFutureCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(Response response) {
            LOGGER.info("Receive response: {}", response);
            latch.countDown();
        }

        @Override
        public void onFailure(Throwable t) {
            if (t instanceof SendFailureException) {
                SendFailureException ex = (SendFailureException) t;
                LOGGER.error("Found error, errorCode: {}, errorMessage: {}, attemptCount: {}", ex.getErrorCode(),
                        ex.getErrorMessage(), ex.getAttemptCount());
            } else {
                LOGGER.error("Found error: {}", t.getMessage(), t);
            }
            latch.countDown();
        }
    }

}
