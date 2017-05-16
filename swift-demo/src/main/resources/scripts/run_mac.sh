#!/bin/bash
#启2个服务
num=1
if [[ -z "$1" ]]; then
    echo "-----请输入端口号-----"
else
    #need install greadlink (brew install coreutils)
	SCRIPT_HOME=$(dirname $(greadlink -f $0))
    for i in $(seq 0 ${num})
    do
        port=`expr $1 + $i`
        sh ${SCRIPT_HOME}/start_mac.sh ${port}
    done
fi