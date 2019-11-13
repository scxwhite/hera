#!/bin/bash
workDir=`pwd`
if [[ ! -f "$workDir/stop.sh" ]];then
	echo "找不到关闭hera的脚本stop.sh，请确保本目录下为hera/bin的目录"
	exit 1
fi
if [[ ! -f "$workDir/start.sh" ]];then
	echo "找不到启动hera的脚本start.sh，请确保本目录下为hera/bin的目录"
	exit 1
fi

sh stop.sh

sh start.sh

echo "---------重启hera成功----------"
