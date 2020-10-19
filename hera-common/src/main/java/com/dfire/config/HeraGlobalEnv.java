package com.dfire.config;

import com.dfire.common.constants.Constants;
import com.dfire.common.enums.OperatorSystemEnum;
import com.dfire.logs.HeraLog;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xiaosuda
 * @date 2018/4/16
 */
@Configuration
public class HeraGlobalEnv {

    @Getter
    public static String excludeFile;

    public static int defaultWorkerGroup;

    public static Integer preemptionMasterGroup;

    @Getter
    private static long requestTimeout = 60 * 1000L;

    @Getter
    private static long channelTimeout = 1000L;

    @Getter
    private static int jobCacheDay;

    @Getter
    private static String loadBalance;

    @Getter
    private static String env;
    @Getter
    private static boolean sudoUser;

    @Getter
    private static int warmUpCheck;

    @Getter
    private static Float maxMemRate;
    @Getter
    private static Float maxCpuLoadPerCore;
    @Getter
    private static Float perTaskUseMem;
    @Getter
    private static Float systemMemUsed;
    @Getter
    private static Integer scanRate;
    @Getter
    private static Integer connectPort;
    @Getter
    private static String workDir;
    @Getter
    private static Integer maxParallelNum;
    @Getter
    private static Integer maxRerunParallelNum;
    @Getter
    private static Integer rerunStartTime;
    @Getter
    private static Integer rerunEndTime;
    @Getter
    private static Integer heartBeat;
    @Getter
    private static String admin;
    @Getter
    private static String area;
    @Getter
    private static Integer taskTimeout;
    @Getter
    private static String sparkAddress;
    @Getter
    private static String sparkDriver;
    @Getter
    private static String sparkUser;
    @Getter
    private static String sparkPassword;
    @Getter
    private static String sparkMaster;
    @Getter
    private static String sparkDriverMemory;
    @Getter
    private static String sparkDriverCores;
    @Getter
    private static String sparkExecutorMemory;
    @Getter
    private static String sparkExecutorCores;
    @Getter
    private static String hdfsUploadPath;
    @Getter
    private static String jobShellBin;
    @Getter
    private static String jobHiveBin;
    @Getter
    private static String jobSparkSqlBin;
    @Getter
    private static boolean emrJob;
    @Getter
    private static boolean scriptEcho;
    @Getter
    private static String emrCluster;
    @Getter
    private static String keyPath;

    @Getter
    private static String kerberosKeytabPath;
    @Getter
    private static String kerberosPrincipal;

    @Getter
    private static Integer webSessionExpire;

    @Getter
    private static Integer webLogHeadCount;

    @Getter
    private static Integer webLogTailCount;

    @Getter
    private static String aliYunAccessKey;
    @Getter
    private static String indiaAccessKey;

    @Getter
    private static String aliYunAccessSecret;
    @Getter
    private static String indiaAccessSecret;

    @Getter
    private static String mailPort;
    @Getter
    private static String mailProtocol;
    @Getter
    private static String mailHost;
    @Getter
    private static String mailUser;
    @Getter
    private static String mailPassword;
    @Getter
    private static Set<String> alarmEnvSet;
    @Getter
    public static String emrFixedHost;
    @Getter
    private static Set<String> emrGroups;

    @Getter
    private static String awsEmrType;
    @Getter
    public static String monitorUsers;
    @Getter
    public static String monitorEmails;



    @Value("${hera.sudoUser}")
    public void setSudoUser(boolean sudoUser) {
        HeraGlobalEnv.sudoUser = sudoUser;
    }



    @Value("${hera.emr_groups}")
    public void setEmrGroups(String groups) {
        if (StringUtils.isNotBlank(groups)) {
            HeraGlobalEnv.emrGroups = Arrays.stream(groups.split(Constants.SEMICOLON)).collect(Collectors.toSet());
        } else {
            HeraGlobalEnv.emrGroups = new HashSet<>(0);
        }
        if (HeraGlobalEnv.emrGroups.size() == 0) {
            HeraLog.warn("the hera.emr.groups is null, all user will use same emr cluster");
        }
    }


    @Value("${hera.job.script-echo}")
    public void setScriptEcho(boolean scriptEcho) {
        HeraGlobalEnv.scriptEcho = scriptEcho;
    }

    @Value("${hera.monitorUsers}")
    public void setMonitorUsers(String monitorUsers) {
        HeraGlobalEnv.monitorUsers = monitorUsers;
    }

    @Value("${hera.monitorEmails}")
    public void setMonitorEmails(String monitorEmails) {
        HeraGlobalEnv.monitorEmails = monitorEmails;
    }

    @Value("${india.accessKey}")
    public void setIndiaAccessKey(String indiaAccessKey) {
        HeraGlobalEnv.indiaAccessKey = indiaAccessKey;
    }

    @Value("${india.accessSecret}")
    public void setIndiaAccessSecret(String indiaAccessSecret) {
        HeraGlobalEnv.indiaAccessSecret = indiaAccessSecret;
    }

    @Value("${aliYun.accessKey}")
    public void setAliYunAccessKey(String aliYunAccessKey) {
        HeraGlobalEnv.aliYunAccessKey = aliYunAccessKey;
    }

    @Value("${aliYun.accessSecret}")
    public void setAliYunAccessSecret(String aliYunAccessSecret) {
        HeraGlobalEnv.aliYunAccessSecret = aliYunAccessSecret;
    }

    @Value("${hera.excludeFile}")
    public void setExcludeFile(String excludeFile) {
        HeraGlobalEnv.excludeFile = excludeFile;
    }

    @Value("${hera.defaultWorkerGroup}")
    public void setDefaultWorkerGroup(int defaultWorkerGroup) {
        HeraGlobalEnv.defaultWorkerGroup = defaultWorkerGroup;
    }

    @Value("${hera.preemptionMasterGroup}")
    public void setPreemptionMasterGroup(Integer preemptionMasterGroup) {
        HeraGlobalEnv.preemptionMasterGroup = preemptionMasterGroup;
    }

    @Value("${hera.kerberos.keytabpath}")
    public void setKerberosKeytabPath(String kerberosKeytabPath) {
        HeraGlobalEnv.kerberosKeytabPath = kerberosKeytabPath.trim();
    }
    @Value("${hera.kerberos.principal}")
    public void setKerberosPrincipal(String kerberosPrincipal) {
        HeraGlobalEnv.kerberosPrincipal = kerberosPrincipal.trim();
    }

    @Value("${hera.env}")
    public void setEnv(String env) {
        if (env.contains("_")) {
            HeraGlobalEnv.env = env.split("_")[0];
        } else {
            HeraGlobalEnv.env = env;
        }
    }

    @Value("${hera.maxMemRate}")
    public void setMaxMemRate(Float maxMemRate) {
        HeraGlobalEnv.maxMemRate = maxMemRate;
    }

    @Value("${hera.maxCpuLoadPerCore}")
    public void setCpuLoadPerCore(Float maxCpuLoadPerCore) {
        HeraGlobalEnv.maxCpuLoadPerCore = maxCpuLoadPerCore;
    }

    @Value("${hera.hdfsUploadPath}")
    public void setHdfsUploadPath(String hdfsUploadPath) {
        HeraGlobalEnv.hdfsUploadPath = hdfsUploadPath;
    }

    @Value("${hera.scanRate}")
    public void setScanRate(Integer scanRate) {
        HeraGlobalEnv.scanRate = scanRate;
    }

    @Value("${hera.connectPort}")
    public void setConnectPort(Integer connectPort) {
        HeraGlobalEnv.connectPort = connectPort;
    }

    @Value("${hera.workDir}")
    public void setWorkDir(String workDir) {
        File file = new File(workDir);
        if (!file.exists()) {
            HeraGlobalEnv.workDir = System.getProperty("user.dir");
            HeraLog.warn("配置的工作路径" + workDir + "不存在，将使用默认路径:" + HeraGlobalEnv.workDir);
        } else {
            HeraGlobalEnv.workDir = workDir;
        }
    }

    @Value("${hera.maxParallelNum}")
    public void setMaxParallelNum(Integer maxParallelNum) {
        HeraGlobalEnv.maxParallelNum = maxParallelNum;
    }

    @Value("${hera.rerun.maxParallelNum}")
    public void setMaxRerunParallelNum(Integer maxRerunParallelNum) {
        HeraGlobalEnv.maxRerunParallelNum = maxRerunParallelNum;
    }

    @Value("${hera.rerun.timeRange}")
    public void setRerunTimeRange(String rerunTimeRange) {
        int splitIndex;
        if (rerunTimeRange != null && (splitIndex = rerunTimeRange.indexOf("-")) != -1) {
            HeraGlobalEnv.rerunStartTime = Integer.parseInt(rerunTimeRange.substring(0, splitIndex));
            HeraGlobalEnv.rerunEndTime = Integer.parseInt(rerunTimeRange.substring(splitIndex + 1));
        }
    }

    @Value("${hera.jobCacheDay}")
    public void setJobCacheDay(int jobCacheDay) {
        HeraGlobalEnv.jobCacheDay = jobCacheDay;
    }


    @Value("${hera.admin}")
    public void setAdmin(String admin) {
        HeraGlobalEnv.admin = admin;
    }

    @Value("${hera.taskTimeout}")
    public void setTaskTimeout(Integer taskTimeout) {
        HeraGlobalEnv.taskTimeout = taskTimeout;
    }

    @Value("${hera.heartBeat}")
    public void setHeartBeat(Integer heartBeat) {
        HeraGlobalEnv.heartBeat = heartBeat;
    }

    @Value("${hera.perTaskUseMem}")
    public void setPerTaskUseMem(Float perTaskUseMem) {
        HeraGlobalEnv.perTaskUseMem = perTaskUseMem;
    }

    @Value("${hera.systemMemUsed}")
    public void setSystemMemUsed(Float systemMemUsed) {
        HeraGlobalEnv.systemMemUsed = systemMemUsed;
    }

    @Value("${hera.maxCpuLoadPerCore}")
    public void setMaxCpuLoadPerCore(Float maxCpuLoadPerCore) {
        HeraGlobalEnv.maxCpuLoadPerCore = maxCpuLoadPerCore;
    }

    @Value("${hera.loadBalance}")
    public void setLoadBalance(String loadBalance) {
        HeraGlobalEnv.loadBalance = loadBalance;
    }

    @Value("${hera.warmUpCheck}")
    public void setWarmUpCheck(int warmUpCheck) {
        HeraGlobalEnv.warmUpCheck = warmUpCheck;
    }

    @Value("${hera.requestTimeout}")
    public void setTimeout(Long requestTimeout) {
        HeraGlobalEnv.requestTimeout = requestTimeout;
    }

    @Value("${hera.channelTimeout}")
    public void setChannelTimeout(Long channelTimeout) {
        HeraGlobalEnv.channelTimeout = channelTimeout;
    }

    @Value("${spark.address}")
    public void setSparkAddress(String sparkAddress) {
        HeraGlobalEnv.sparkAddress = sparkAddress;
    }

    @Value("${spark.driver}")
    public void setSparkDriver(String sparkDriver) {
        HeraGlobalEnv.sparkDriver = sparkDriver;
    }

    @Value("${spark.username}")
    public void setSparkUser(String sparkUser) {
        HeraGlobalEnv.sparkUser = sparkUser;
    }

    @Value("${spark.password}")
    public void setSparkPassword(String sparkPassword) {
        HeraGlobalEnv.sparkPassword = sparkPassword;
    }

    @Value("${spark.master}")
    public void setSparkMaster(String sparkMaster) {
        HeraGlobalEnv.sparkMaster = sparkMaster;
    }

    @Value("${spark.driver-memory}")
    public void setSparkDriverMemory(String sparkDriverMemory) {
        HeraGlobalEnv.sparkDriverMemory = sparkDriverMemory;
    }

    @Value("${spark.driver-cores}")
    public void setSparkDriverCores(String sparkDriverCores) {
        HeraGlobalEnv.sparkDriverCores = sparkDriverCores;
    }

    @Value("${spark.executor-memory}")
    public void setSparkExecutorMemory(String sparkExecutorMemory) {
        HeraGlobalEnv.sparkExecutorMemory = sparkExecutorMemory;
    }

    @Value("${spark.executor-cores}")
    public void setSparkExecutorCores(String sparkExecutorCores) {
        HeraGlobalEnv.sparkExecutorCores = sparkExecutorCores;
    }

    @Value("${hera.job.shell.bin}")
    public void setJobShellBin(String jobShellBin) {
        HeraGlobalEnv.jobShellBin = jobShellBin + Constants.BLANK_SPACE;
    }

    @Value("${hera.job.hive.bin}")
    public void setJobHiveBin(String jobHiveBin) {
        HeraGlobalEnv.jobHiveBin = jobHiveBin + Constants.BLANK_SPACE;
    }

    @Value("${hera.job.spark-sql.bin}")
    public void setJobSparkSqlBin(String jobSparkSqlBin) {
        HeraGlobalEnv.jobSparkSqlBin = jobSparkSqlBin + Constants.BLANK_SPACE;
    }


    @Value("${hera.area}")
    public void setArea(String area) {
        if (StringUtils.isBlank(area)) {
            throw new RuntimeException("请设置hera要执行的任务区域:" + area);
        } else {
            HeraGlobalEnv.area = area.trim();
        }
    }

    @Value("${mail.port}")
    public void setMailPort(String mailPort) {
        HeraGlobalEnv.mailPort = mailPort;
    }

    @Value("${mail.protocol}")
    public void setMailProtocol(String mailProtocol) {
        HeraGlobalEnv.mailProtocol = mailProtocol;
    }

    @Value("${mail.host}")
    public void setMailHost(String mailHost) {
        HeraGlobalEnv.mailHost = mailHost;
    }

    @Value("${mail.user}")
    public void setMailUser(String mailUser) {
        HeraGlobalEnv.mailUser = mailUser;
    }

    @Value("${mail.password}")
    public void setMailPassword(String mailPassword) {
        HeraGlobalEnv.mailPassword = mailPassword;
    }


    @Value("${hera.webLogHeadCount}")
    public void setWebLogHeadCount(Integer webLogHeadCount) {
        HeraGlobalEnv.webLogHeadCount = webLogHeadCount;
    }

    @Value("${hera.webLogTailCount}")
    public void setWebLogTailCount(Integer webLogTailCount) {
        HeraGlobalEnv.webLogTailCount = webLogTailCount;
    }


    @Value("${hera.webSessionExpire}")
    public void setWebSessionExpire(Integer webSessionExpire) {
        HeraGlobalEnv.webSessionExpire = webSessionExpire;
    }


    @Value("${hera.alarmEnv}")
    public void setAlarmEnvSet(String mailEnv) {
        if (StringUtils.isBlank(mailEnv)) {
            mailEnv = Constants.PUB_ENV;
        }
        HeraGlobalEnv.alarmEnvSet = new HashSet<>();
        alarmEnvSet.addAll(Arrays.asList(mailEnv.split(Constants.COMMA)));
    }

    @Value("${hera.emr_fixed_host}")
    public void setEmrFixedHost(String emrFixedHost) {
        HeraGlobalEnv.emrFixedHost = emrFixedHost;
    }

    @Value("${hera.aws_emr_type}")
    public void setAwsEmrType(String awsEmrType) {
        HeraGlobalEnv.awsEmrType = awsEmrType;
    }

    @Value("${hera.emrJob}")
    public void setEmrJob(String emrJob) {
        if (Boolean.FALSE.toString().equals(emrJob)) {
            HeraGlobalEnv.emrJob = false;
        } else {
            HeraGlobalEnv.emrJob = true;

            String[] emrCluster = emrJob.split(":");
            if (emrCluster.length == 0 || emrCluster.length == 1) {
                throw new RuntimeException("emrJob参数设置错误:" + emrJob);
            }
            HeraGlobalEnv.emrCluster = emrCluster[1];
        }
    }



    @Value("${hera.keyPath}")
    public void setKeyPath(String keyPath) {
        HeraGlobalEnv.keyPath = keyPath;
    }


    @Getter
    private static OperatorSystemEnum systemEnum;

    /**
     * 用户环境变量
     */
    public static Map<String, String> userEnvMap = new HashMap<>();

    static {
        String os = System.getProperty("os.name");
        if (os != null) {
            if (os.toLowerCase().startsWith("win")) {
                systemEnum = OperatorSystemEnum.WIN;
            } else if (os.toLowerCase().startsWith("mac")) {
                systemEnum = OperatorSystemEnum.MAC;
            } else {
                systemEnum = OperatorSystemEnum.LINUX;
            }
        }
        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
            userEnvMap.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
        userEnvMap.putAll(System.getenv());
        // 全局配置，支持中文不乱
        userEnvMap.put("LANG", "zh_CN.UTF-8");
    }

    public static boolean isLinuxSystem() {
        return OperatorSystemEnum.isLinux(systemEnum);
    }

    public static boolean isMacOS() {
        return OperatorSystemEnum.isMac(systemEnum);
    }


}
