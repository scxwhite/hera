#!/bin/bash
workDir=$(cd "$(dirname $0)";pwd)
if [[ ! -f "$workDir/stop.sh" ]];then
	echo "找不到关闭hera的脚本stop.sh，请确保${workDir}目录下有stop.sh脚本"
	exit 1
fi
if [[ ! -f "$workDir/start.sh" ]];then
	echo "找不到启动hera的脚本start.sh，请确保${workDir}目录下有start.sh脚本"
	exit 1
fi

sh $workDir/stop.sh

sh $workDir/start.sh

echo "---------重启hera成功----------"
