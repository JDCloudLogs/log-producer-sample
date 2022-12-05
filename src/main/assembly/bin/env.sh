#!/bin/bash
LOG_JAVA_HOME=`ls -rd /export/local/jdk1.8* | head -1`
echo "LOG_JAVA_HOME:    $LOG_JAVA_HOME"
if [ -n "$LOG_JAVA_HOME" ] ;then
    export JAVA_HOME=$LOG_JAVA_HOME
fi
if [ ! -d "$JAVA_HOME" ] ;then
    echo "ERROR: Cannot Found JAVA Installed in $JAVA_HOME" >&2
    exit 1
fi

if [ -n "$MAX_JAVA_HEAP" ] ;then
    JAVA_MEM_OPTS="$JAVA_MEM_OPTS -Xms$MAX_JAVA_HEAP -Xmx$MAX_JAVA_HEAP"
fi

LOG_PRODUCER_OPTS=""
if [ -n "$ACCESS_KEY_ID" ] ;then
    LOG_PRODUCER_OPTS="$LOG_PRODUCER_OPTS -DaccessKeyId=$ACCESS_KEY_ID"
fi
if [ -n "$SECRET_ACCESS_KEY" ] ;then
    LOG_PRODUCER_OPTS="$LOG_PRODUCER_OPTS -DsecretAccessKey=$SECRET_ACCESS_KEY"
fi
if [ -n "$LOG_TOPIC" ] ;then
    LOG_PRODUCER_OPTS="$LOG_PRODUCER_OPTS -DlogTopic=$LOG_TOPIC"
fi
if [ -n "$REGION_ID" ] ;then
    LOG_PRODUCER_OPTS="$LOG_PRODUCER_OPTS -DregionId=$REGION_ID"
fi
if [ -n "$ENDPOINT" ] ;then
    LOG_PRODUCER_OPTS="$LOG_PRODUCER_OPTS -Dendpoint=$ENDPOINT"
fi
if [ -n "$PRODUCE_THREADS" ] ;then
    LOG_PRODUCER_OPTS="$LOG_PRODUCER_OPTS -DproduceThreads=$PRODUCE_THREADS"
fi
if [ -n "$PRODUCE_TIMES" ] ;then
    LOG_PRODUCER_OPTS="$LOG_PRODUCER_OPTS -DproduceTimes=$PRODUCE_TIMES"
fi
if [ -n "$SEND_THREADS" ] ;then
    LOG_PRODUCER_OPTS="$LOG_PRODUCER_OPTS -DsendThreads=$SEND_THREADS"
fi
if [ -n "$TOTAL_SIZE_IN_BYTES" ] ;then
    LOG_PRODUCER_OPTS="$LOG_PRODUCER_OPTS -DtotalSizeInBytes=$TOTAL_SIZE_IN_BYTES"
fi
if [ -n "$BATCH_SIZE" ] ;then
    LOG_PRODUCER_OPTS="$LOG_PRODUCER_OPTS -DbatchSize=$BATCH_SIZE"
fi
if [ -n "$BATCH_SIZE_IN_BYTES" ] ;then
    LOG_PRODUCER_OPTS="$LOG_PRODUCER_OPTS -DbatchSizeInBytes=$BATCH_SIZE_IN_BYTES"
fi
if [ -n "$SAMPLE_CLASS" ] ;then
    LOG_PRODUCER_OPTS="$LOG_PRODUCER_OPTS -DsampleClass=$SAMPLE_CLASS"
fi

#MacOS:-agentpath:/Applications/JProfiler.app/Contents/Resources/app/bin/macos/libjprofilerti.jnilib=port=8849
#linux:-agentpath:/export/server/jprofiler13/bin/linux-x64/libjprofilerti.so=port=8849
if [ -n "$JPROFILER_NATIVE_LIBRARY" ] ;then
    JPROFILER_OPTS="-agentpath:${JPROFILER_NATIVE_LIBRARY}=port=${JPROFILER_PORT}"
fi

echo "LOG_PRODUCER_OPTS: 		 $LOG_PRODUCER_OPTS"
echo "JPROFILER_OPTS: 			 $JPROFILER_OPTS"
export JAVA_OPTS="$JAVA_OPTS $JPROFILER_OPTS $LOG_PRODUCER_OPTS"
