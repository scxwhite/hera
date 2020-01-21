[TOC]



## 日志详情功能

### 日志详情

- 之前网页端只保留1000行左右的日志，若超过，就不展示；

- 本Feature增加功能：在worker机器的脚本运行目录上--生成日志文件

  - 日志文件清单

  ```shell
  #示例
  /opt/logs/spring-boot/2019-12-29/manual-4564
  
  -rwxrwxrwx 1 hera hera  105 Dec 29 15:25 1577604332970.sh
  -rw-r--r-- 1 hera hera 8917 Dec 29 15:25 3912efed-c508-498a-9610-ee545a0c9c87.log
  -rwxrwxrwx 1 hera hera  517 Dec 29 15:25 tmp.sh
  
  ```

  - tmp.sh

  ```shell
  curDir=$(cd `dirname $0`; pwd)
  scriptName=`basename $0`
  cd ${curDir}
  log_file=78e32f90-16ca-481d-b55f-a6c82f0e63a7.log
  echo "调度作业的日志文件:[${curDir}/${log_file}]"
  runtime=`date '+%Y-%m-%d %H:%M:%S'`
  echo "作业执行开始,时间[$runtime]"
  
  bash /opt/logs/spring-boot/2019-12-30/manual-4657/1577673901459.sh  2>&1|tee -a ${log_file}
  if [ ${PIPESTATUS[0]}  != 0 ]
  then
      runtime=`date '+%Y-%m-%d %H:%M:%S'`
      echo "作业执行失败,时间[$runtime]"
      exit -1
  else
      runtime=`date '+%Y-%m-%d %H:%M:%S'`
      #'此处可设置web或FTP服务,如上传日志文件，以达到网页端可查看完成日志功能'
      echo "作业执行成功,时间[$runtime]"
  fi
  ```

  - 网页端的日志示例

    ```shell
    HERA# 本地执行任务
    2019-12-30 10:45:11 开始运行
    HERA# ==================开始输出脚本内容==================
    
    sleep 234
    HERA# ==================结束输出脚本内容==================
    HERA# 开始执行前置处理单元DownLoadJob
    HERA# 前置处理单元DownLoadJob处理完毕
    HERA# 开始执行核心job
    HERA# dos2unix file:/opt/logs/spring-boot/2019-12-30/manual-4658/1577673912031.sh
    CONSOLE# dos2unix: converting file /opt/logs/spring-boot/2019-12-30/manual-4658/1577673912031.sh to Unix format ...
    CONSOLE# 调度作业的日志文件:[/opt/logs/spring-boot/2019-12-30/manual-4658/343ac3e9-7243-407e-bfd3-aa0c666f9fa6.log]
    CONSOLE# 作业执行开始,时间[2019-12-30 10:45:12]
    HERA# 核心job处理完毕
    HERA# exitCode = 0
    ```




### 下一步计划

  - 在网页端提供一个“下载”完整日志






