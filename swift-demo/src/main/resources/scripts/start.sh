#!/bin/bash

HOST_NAME=`ifconfig eth0 | grep "inet addr" | cut -f 2 -d ":" | cut -f 1 -d " "`
JMX_PORT=`expr $1 + 500`
#jdk 1.7
#JAVA_OPTS="
# -Xmx2G \
# -Xms2G \
# -Xmn768M \
# -XX:PermSize=64M \
# -XX:MaxPermSize=256M \
# -Xss1M \
# -XX:+DisableExplicitGC \
# -XX:SurvivorRatio=1 \
# -XX:+UseConcMarkSweepGC \
# -XX:+UseParNewGC \
# -XX:+CMSParallelRemarkEnabled \
# -XX:+UseCMSCompactAtFullCollection \
# -XX:CMSFullGCsBeforeCompaction=0 \
# -XX:+CMSClassUnloadingEnabled \
# -XX:LargePageSizeInBytes=128M \
# -XX:+UseFastAccessorMethods \
# -XX:+UseCMSInitiatingOccupancyOnly \
# -XX:CMSInitiatingOccupancyFraction=80 \
# -XX:SoftRefLRUPolicyMSPerMB=0 \
# -XX:+PrintClassHistogram \
# -XX:+PrintGCDetails \
# -XX:+PrintGCTimeStamps \
# -XX:+PrintHeapAtGC \
# -Xloggc:/tmp/gc.log"

#jdk 1.8
JAVA_OPTS="
-server \
-Xms2g \
-Xmx2g \
-Xmn768m \
-XX:MetaspaceSize=64m \
-XX:MaxMetaspaceSize=256m \
-Xss1m \
-XX:+DisableExplicitGC \
-XX:+UseConcMarkSweepGC \
-XX:+CMSParallelRemarkEnabled \
-XX:LargePageSizeInBytes=128m \
-XX:+UseFastAccessorMethods \
-XX:+UseCMSInitiatingOccupancyOnly \
-XX:CMSInitiatingOccupancyFraction=70 \
-XX:SoftRefLRUPolicyMSPerMB=0 \
-XX:+UnlockCommercialFeatures \
-XX:+FlightRecorder \
-Djava.rmi.server.hostname=$HOST_NAME \
-Dcom.sun.management.jmxremote \
-Dcom.sun.management.jmxremote.port=$JMX_PORT \
-Dcom.sun.management.jmxremote.authenticate=false \
-Dcom.sun.management.jmxremote.ssl=false \
-XX:+PrintClassHistogram \
-XX:+PrintGCDetails \
-XX:+PrintGCTimeStamps \
-XX:+PrintHeapAtGC \
-Xloggc:/data/logs/stereo/gc_$1.log
"

SCRIPT_HOME=$(dirname $(readlink -f $0))
PROJECT_HOME=$(dirname ${SCRIPT_HOME})

pid=`ps -eo pid,args | grep $1 | grep java | grep -v grep | awk '{print $1}'`

if [[ -n "${pid}" ]]; then
    kill -3 ${pid}
    sleep 3
    pid2=`ps -eo pid | grep ${pid}`
    if [[  -n "${pid2}" ]]; then
        kill -9 ${pid}
    fi
    echo "-----kill pid: ${pid}-----"
fi

echo "-----project start at $1-----"
java -Dport=$1 ${JAVA_OPTS} -cp ${PROJECT_HOME}/conf:${PROJECT_HOME}/lib/* com.luastar.swift.demo.http.HttpBootstrap $1 > /dev/null 2>&1 &