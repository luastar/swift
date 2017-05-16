#!/bin/bash
#启2个服务
num=0
if [[ -z "$1" ]]; then
    echo "-----请输入端口号-----"
else
    SCRIPT_HOME=$(dirname $(readlink -f $0))
    for i in $(seq 0 ${num})
    do
        port=`expr $1 + $i`
        sh ${SCRIPT_HOME}/start.sh ${port}
    done
fi