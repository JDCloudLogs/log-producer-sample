# JDCloud log producer Java sample

JDCloud log producer Java 示例代码

## 运行步骤

### 打包
```
mvn clean package
```

### 将配置写入环境变量
请将配置中的 xxx 替换为真实的值
```
export ACCESS_KEY_ID=xxx
export SECRET_ACCESS_KEY=xxx
export LOG_TOPIC=xxx
export REGION_ID=xxx
export ENDPOINT=xxx
export PRODUCE_THREADS=10
export PRODUCE_TIMES=20000000
export SEND_THREADS=4
export TOTAL_SIZE_IN_BYTES=104857600
export BATCH_SIZE=32768
export BATCH_SIZE_IN_BYTES=2097152
#如果需要用Jprofiler监控请打开下面的配置并替换为Jprofiler真实路径和端口
#export JPROFILER_NATIVE_LIBRARY=/Applications/JProfiler.app/Contents/Resources/app/bin/macos/libjprofilerti.jnilib
#export JPROFILER_PORT=8849
#jvm内存大小，-Xms4096m -Xmx4096m
export MAX_JAVA_HEAP=4096m
#日志级别
export LOG_LEVEL=DEBUG
```
### 运行
运行以下命令会向目标 logTopic 写入约 108 GB 的数据，在非性能测试场景下谨慎运行。
```
./bin/start.sh
```
