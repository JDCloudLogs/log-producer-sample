#!/bin/bash
cd `dirname $0`
export APP_BIN=`pwd`
cd ..
export APP_HOME=`pwd`
export APP_HOME_REAL=`readlink -f $APP_HOME`
export APP_LOG_HOME=$APP_HOME/log
export STDOUT_FILE=$APP_LOG_HOME/console.log
export PID_FILE="$APP_HOME/pid"
PID=`ps -ef | grep java | grep "$APP_HOME" | awk '{print $2}'`
export PID