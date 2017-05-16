#!/bin/bash

#JMX_PORT=`expr $1 + 500`
JAVA_OPTS="-server -Xms1024m -Xmx1024m -Xmn384m -XX:MaxPermSize=128m \
-Xss1m -XX:+UseConcMarkSweepGC \
-XX:+UseParNewGC -XX:CMSFullGCsBeforeCompaction=5 \
-XX:+UseCMSCompactAtFullCollection \
-XX:+PrintGC -Xloggc:/data/logs/${APP}/gc_$1.log"
#-Djava.rmi.server.hostname=114.215.143.54 \
#-Dcom.sun.management.jmxremote \
#-Dcom.sun.management.jmxremote.port=$JMX_PORT \
#-Dcom.sun.management.jmxremote.authenticate=false \
#-Dcom.sun.management.jmxremote.ssl=false"

#need install greadlink (brew install coreutils)
SCRIPT_HOME=$(dirname $(greadlink -f $0))
PROJECT_HOME=$(dirname ${SCRIPT_HOME})

pid=`ps -eo pid,args | grep $1 | grep java | grep -v grep | awk '{print $1}'`

if [ -n "$pid" ]
then
    kill -3 ${pid}
    kill ${pid} && sleep 3
    if [  -n "`ps -eo pid | grep $pid`" ]
    then
        kill -9 ${pid}
    fi
    echo "-----kill pid: ${pid}-----"
fi

echo "-----project start at $1-----"
java -Dport=$1 ${JAVA_OPTS} -cp ${PROJECT_HOME}/conf:${PROJECT_HOME}/lib/* com.luastar.swift.demo.http.HttpBootstrap $1 > /dev/null 2>&1 &