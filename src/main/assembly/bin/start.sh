#!/bin/bash
#
# Usage: start.sh [debug]
#
[  -e `dirname $0`/env_app.sh ] && . `dirname $0`/env_app.sh
[  -e `dirname $0`/env.sh ] && . `dirname $0`/env.sh
[  -e `dirname $0`/prepare.sh ] && . `dirname $0`/prepare.sh

if [ ! -d "$APP_LOG_HOME" ] ;then
    mkdir -p $APP_LOG_HOME
fi

if [ -n "$PID" ]; then
    echo "ERROR: The $APP_NAME already started!"
    echo "PID: $PID"
    exit 1
fi

echo "Starting the $APP_NAME ..."
echo "JAVA_HOME: $JAVA_HOME"
echo "APP_HOME: $APP_HOME"
echo "APP_LOG_HOME: $APP_LOG_HOME"
echo "STDOUT_FILE: $STDOUT_FILE"

if [ -d "$APP_HOME" ]; then
    APP_LAUNCHER_JAR=`ls $APP_HOME | grep .jar`
    if [ -n "$APP_LAUNCHER_JAR" ]; then
        APP_LAUNCHER_JAR="$APP_HOME/$APP_LAUNCHER_JAR"
    fi
fi
echo "Using APP_LAUNCHER_JAR:     $APP_LAUNCHER_JAR"

nohup $JAVA_HOME/bin/java $JAVA_OPTS $JAVA_MEM_OPTS -cp $APP_LAUNCHER_JAR -jar $APP_LAUNCHER_JAR > $STDOUT_FILE 2>&1 &

COUNT=0
while [ $COUNT -lt 1 ]; do
    sleep 1
    COUNT=`ps -ef | grep java | grep "$APP_HOME" | awk '{print $2}' | wc -l`
    echo "ps check count[$COUNT]"
    if [ $COUNT -gt 0 ]; then
        break
    fi
done

echo "OK!"
PID=`ps -ef | grep java | grep "$APP_HOME" | awk '{print $2}'`
echo "PID: $PID"
