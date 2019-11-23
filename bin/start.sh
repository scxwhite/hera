#!/bin/bash
workDir=$(cd "$(dirname $0)";cd ..;pwd)
JAVA_OPTS="-server -Xms4G -Xmx4G -Xmn2G -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:+CMSParallelRemarkEnabled -XX:CMSFullGCsBeforeCompaction=5 -XX:+CMSParallelInitialMarkEnabled -XX:CMSInitiatingOccupancyFraction=80  -verbose:gc -XX:+PrintGCTimeStamps -XX:+PrintGCDetails -Xloggc:/opt/logs/spring-boot/gc.log -XX:MetaspaceSize=128m -XX:+UseCMSCompactAtFullCollection -XX:MaxMetaspaceSize=128m -XX:+CMSPermGenSweepingEnabled -XX:+CMSClassUnloadingEnabled -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${workDir}/dump"
log_dir="${workDir}/logs"
log_file="${log_dir}/all.log"


#日志文件夹不存在，则创建
if [[ ! -d "${log_dir}" ]]; then
    echo "创建日志目录:${log_dir}"
    mkdir -p "${log_dir}"
    echo "创建日志目录完成:${log_dir}"
fi

jar_file=`find ${workDir} -maxdepth 1 -name "hera-*.jar"`

echo ${jar_file}

#父目录下jar文件存在
if [[ -f "${jar_file}" ]]; then
    #启动jar包 错误输出的error 标准输出的log
    nohup java ${JAVA_OPTS} -jar ${jar_file} 1>"${log_file}" 2>"${log_dir}"/error.log &
    echo "启动完成,日志路径:${log_dir}"
    exit 0
else
    echo -e "\033[31m启动失败！！！无法在${workDir}目录找不到hera启动jar文件！\033[0m"
    exit 1
fi
