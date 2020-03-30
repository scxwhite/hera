package com.dfire.core.emr;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.emr.model.v20160408.*;
import com.aliyuncs.emr.model.v20160408.CreateClusterV2Request.Config;
import com.aliyuncs.emr.model.v20160408.CreateClusterV2Request.HostGroup;
import com.aliyuncs.emr.model.v20160408.DescribeClusterV2Response.ClusterInfo;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.dfire.common.exception.HeraException;
import com.dfire.common.util.EnvUtils;
import com.dfire.config.HeraGlobalEnv;
import com.dfire.logs.ErrorLog;
import com.dfire.logs.MonitorLog;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * desc:
 *
 * @author scx
 * @create 2019/04/01
 */
public class AliYunEmr extends AbstractEmr {


    public static final String NAME = "aliYun";

    private boolean isShutdown = true;

    private DefaultAcsClient client;


    private List<String> aliveStatus = new ArrayList<>();

    {
        aliveStatus.add("CREATING");
        aliveStatus.add("IDLE");
        aliveStatus.add("RUNNING");
    }

    public static void main(String[] args) {
        AliYunEmr emr = new AliYunEmr();
        emr.init();
    }


    @Override
    protected void init() {
        if (client == null || isShutdown) {
            synchronized (this) {
                if (client == null || isShutdown) {
                    String region = "cn-hangzhou";
                    if (EnvUtils.isDaily()) {
                        region = "cn-shanghai";
                    }
                    IClientProfile profile = DefaultProfile.getProfile(region, HeraGlobalEnv.getAliYunAccessKey(), HeraGlobalEnv.getAliYunAccessSecret());
                    client = new DefaultAcsClient(profile);
                    isShutdown = false;
                }
            }
        }
    }

    @Override
    protected void terminateCluster(String clusterId) {
        ReleaseClusterRequest request = new ReleaseClusterRequest();
        request.setId(clusterId);
        try {
            client.getAcsResponse(request);
            MonitorLog.info("关闭集群" + clusterId + "成功");
        } catch (ClientException e) {
            MonitorLog.error("关闭集群" + clusterId + "失败", e);
        }
    }


    @Override
    protected void shutdown() {
        if (!isShutdown) {
            isShutdown = true;
            client.shutdown();
            client = null;
        }
    }

    @Override
    protected String sendCreateRequest(String owner) {
        try {
            CreateClusterV2Request request = new CreateClusterV2Request();
            request.setName(getClusterName(owner));
            request.setEmrVer("EMR-3.12.0");
            request.setAutoRenew(false);
            request.setChargeType("PostPaid");
            request.setClusterType("HADOOP");
            //使用统一meta数据库 一定要开启
            request.setIsOpenPublicIp(true);
            request.setLogPath("oss://log-xxxx");
            request.setNetType("vpc");
            if (EnvUtils.isDaily()) {
                request.setVpcId("vpc-u");
                request.setVSwitchId("v");
                request.setZoneId("cn-shanghai-f");
                request.setSecurityGroupId("xxxx");
            } else {
                request.setVpcId("vpc-b");
                request.setVSwitchId("vsw-b");
                request.setZoneId("cn-hangzhou-g");
                request.setSecurityGroupId("xxxx");

            }
            //登录emr集群的账号密码
            request.setMasterPwd("xxxxxx");
            request.setIoOptimized(true);
            List<Config> configs = new ArrayList<>();
            if (EnvUtils.isPre()) {
                setCustomHiveMetaDB(configs);
                request.setUseLocalMetaDb(true);
            } else {
                request.setUseLocalMetaDb(false);
            }
            setSparkConf(configs);
            request.setConfigs(configs);
            request.setInstanceGeneration("ecs-2");
            setHostGroups(request);
            setBootStrapAction(request);
            CreateClusterV2Response response = client.getAcsResponse(request);
            String clusterId = response.getClusterId();
            setAutoScaling(clusterId);
            return clusterId;
        } catch (Exception e) {
            ErrorLog.error("aliyun创建集群异常:" + e.getMessage(), e);

        }
        return null;
    }

    /**
     * 引导操作
     *
     * @param request request
     */
    private void setBootStrapAction(CreateClusterV2Request request) {
        List<CreateClusterV2Request.BootstrapAction> actions = new ArrayList<>();
        CreateClusterV2Request.BootstrapAction userAction = new CreateClusterV2Request.BootstrapAction();
        userAction.setName("userInit");
        if (EnvUtils.isDaily()) {
            //emr集群引导操作的oss路径
            userAction.setPath("oss://hera/bootstrap.sh");
            //引导操作脚本的执行命令
            userAction.setArg("-c \"osscmd --id=" + HeraGlobalEnv.getAliYunAccessKey() + " --key=" + HeraGlobalEnv.getAliYunAccessSecret());
        } else if (EnvUtils.isPre()) {
            //emr集群引导操作的oss路径
            userAction.setPath("oss://hera/bootstrap.sh");
            //引导操作脚本的执行命令
            userAction.setArg("-c \"osscmd --id=" + HeraGlobalEnv.getAliYunAccessKey() + " --key=" + HeraGlobalEnv.getAliYunAccessSecret());
        } else if (EnvUtils.isPro()) {
            //emr集群引导操作的oss路径
            userAction.setPath("oss://hera/bootstrap.sh");
            //引导操作脚本的执行命令
            userAction.setArg("-c \"osscmd --id=" + HeraGlobalEnv.getAliYunAccessKey() + " --key=" + HeraGlobalEnv.getAliYunAccessSecret());
        } else {
            throw new IllegalStateException("未识别的环境:" + HeraGlobalEnv.getEnv());
        }
        actions.add(userAction);
        request.setBootstrapActions(actions);
    }

    @Override
    protected String getAliveId(String owner) {
        ListClustersRequest request = new ListClustersRequest();
        request.setStatusLists(aliveStatus);
        request.setPageNumber(1);
        request.setPageSize(200);
        try {
            ListClustersResponse response = client.getAcsResponse(request);
            List<ListClustersResponse.ClusterInfo> clusters = response.getClusters();
            for (ListClustersResponse.ClusterInfo cluster : clusters) {
                if (cluster.getName().startsWith(buildClusterName(owner))) {
                    return cluster.getId();
                }
            }
        } catch (ClientException e) {
            ErrorLog.error("读取emr存活集群失败", e);
        }
        return null;
    }

    @Override
    protected boolean isCompletion(String clusterId) throws HeraException {
        ClusterInfo clusterInfo = getClusterInfo(clusterId);
        if (clusterInfo == null) {
            return false;
        }
        MonitorLog.info("当前集群状态为:{},{},{}", clusterId, clusterInfo.getName(), clusterInfo.getStatus());
        return isReady(clusterInfo.getStatus());
    }

    private boolean isReady(String status) throws HeraException {
        if ("CREATING".equals(status) || "BOOTSTRAPPING".equals(status)) {
            return false;
        }
        if ("RELEASED".equals(status)) {
            throw new HeraException("集群已关闭");
        }
        return aliveStatus.contains(status);
    }

    @Override
    protected List<String> getMasterIp(String clusterId) {
        ClusterInfo clusterInfo = getClusterInfo(clusterId);
        if (clusterInfo == null) {
            return null;
        }
        List<String> ipList = new ArrayList<>();
        List<ClusterInfo.HostGroup> groupList = clusterInfo.getHostGroupList();
        for (ClusterInfo.HostGroup hostGroup : groupList) {
            if ("CORE".equals(hostGroup.getHostGroupType())) {
                for (ClusterInfo.HostGroup.Node node : hostGroup.getNodes()) {
                    if ("NORMAL".equals(node.getStatus())) {
                        ipList.add(node.getInnerIp());
                    }
                }
            }
        }
        return ipList;
    }

    @Override
    protected boolean checkAlive(String cacheClusterId) {
        if (StringUtils.isBlank(cacheClusterId)) {
            return false;
        }
        ClusterInfo clusterInfo = getClusterInfo(cacheClusterId);
        if (clusterInfo == null) {
            return false;
        }
        MonitorLog.info("{}集群状态为:{}", cacheClusterId, clusterInfo.getStatus());
        return aliveStatus.contains(clusterInfo.getStatus());
    }

    private ClusterInfo getClusterInfo(String clusterId) {
        DescribeClusterV2Request request = new DescribeClusterV2Request();
        request.setId(clusterId);
        ClusterInfo clusterInfo = null;
        try {
            DescribeClusterV2Response acsResponse = client.getAcsResponse(request);
            clusterInfo = acsResponse.getClusterInfo();
        } catch (ClientException e) {
            ErrorLog.error("获取集群信息失败", e);
        }
        return clusterInfo;
    }


    private void setHostGroups(CreateClusterV2Request request) {
        List<HostGroup> hostGroups = new ArrayList<>();

        HostGroup masterGroup = new HostGroup();
        masterGroup.setNodeCount(1);
        masterGroup.setHostGroupName("MASTER");
        masterGroup.setHostGroupType("MASTER");
        masterGroup.setInstanceType("ecs.r5.xlarge");
        masterGroup.setDiskType("cloud_efficiency");
        masterGroup.setDiskCapacity(80);
        masterGroup.setDiskCount(1);
        hostGroups.add(masterGroup);

        HostGroup coreGroup = new HostGroup();

        if (EnvUtils.isDaily()) {
            coreGroup.setNodeCount(2);
            coreGroup.setInstanceType("ecs.r5.xlarge");
        } else {
            if (EnvUtils.isPre()) {
                coreGroup.setInstanceType("ecs.r5.2xlarge");
                coreGroup.setNodeCount(2);
            } else {
                coreGroup.setInstanceType("ecs.r5.2xlarge");
                coreGroup.setNodeCount(4);
            }
        }
        coreGroup.setHostGroupName("CORE");
        coreGroup.setHostGroupType("CORE");
        coreGroup.setDiskType("cloud_efficiency");
        coreGroup.setDiskCapacity(80);
        coreGroup.setDiskCount(4);
        hostGroups.add(coreGroup);

        request.setHostGroups(hostGroups);
    }

    /**
     * 设置自定义的hive 元数据地址
     *
     * @param configs
     */
    private void setCustomHiveMetaDB(List<Config> configs) {
        Config metaConf = new Config();
        setHiveConf(metaConf, "init.meta.db", "false");
        configs.add(metaConf);

        Config usernameConf = new Config();
        setHiveConf(usernameConf, "javax.jdo.option.ConnectionUserName", "xxxx");
        configs.add(usernameConf);

        Config passwordConf = new Config();
        setHiveConf(passwordConf, "javax.jdo.option.ConnectionPassword", "xxxx");
        configs.add(passwordConf);

        Config driverConf = new Config();
        setHiveConf(driverConf, "javax.jdo.option.ConnectionDriverName", "com.mysql.jdbc.Driver");
        configs.add(driverConf);

        Config urlConf = new Config();
        setHiveConf(urlConf, "javax.jdo.option.ConnectionURL", "jdbc:mysql://rds:3306/hivemeta_pre?createDatabaseIfNotExist=true");
        configs.add(urlConf);

    }

    private void setSparkConf(List<Config> configs) {
        Config retryConf = new Config();
        setSparkConf(retryConf, "spark.port.maxRetries", "256");
        configs.add(retryConf);
    }


    private void setHiveConf(Config config, String key, String val) {
        config.setServiceName("HIVE");
        config.setFileName("hive-site");
        config.setConfigKey(key);
        config.setConfigValue(val);

    }


    private void setSparkConf(Config config, String key, String val) {
        config.setServiceName("SPARK");
        config.setFileName("spark-defaults");
        config.setConfigKey(key);
        config.setConfigValue(val);
    }


    /**
     * 设置弹性伸缩  阿里云暂时不支持api模式
     *
     * @param clusterId clusterId
     */
    private void setAutoScaling(String clusterId) {

        CreateScalingTaskGroupRequest scaleRequest = new CreateScalingTaskGroupRequest();

        scaleRequest.setClusterId(clusterId);
        //10
        scaleRequest.setMaxSize(getMaxCapacity());
        //2
        scaleRequest.setMinSize(getMinCapacity());
        //300
        scaleRequest.setDefaultCooldown(getCoolDown());
        List<CreateScalingTaskGroupRequest.ScalingRule> rules = new ArrayList<>(2);

        CreateScalingTaskGroupRequest.ScalingRule scaleOut = new CreateScalingTaskGroupRequest.ScalingRule();
        scaleOut.setAdjustmentType("PercentChangeInCapacity");
        //10
        scaleOut.setAdjustmentValue(getScalePercent());
        scaleOut.setRuleName("hera-scale-out");
        //300
        scaleOut.setCooldown(getCoolDown());


        CreateScalingTaskGroupRequest.ScalingRule.CloudWatchTrigger scaleOutTrigger = new CreateScalingTaskGroupRequest.ScalingRule.CloudWatchTrigger();
        scaleOutTrigger.setComparisonOperator("LessThanOrEqualToThreshold");
        scaleOutTrigger.setPeriod(getCoolDown());
        scaleOutTrigger.setStatistics("Average");
        scaleOutTrigger.setThreshold(String.valueOf(1024 * 4));
        scaleOutTrigger.setMetricName("YARN.AvailableMemory");

        scaleOut.setCloudWatchTriggers(Collections.singletonList(scaleOutTrigger));

        rules.add(scaleOut);

        scaleRequest.setScalingRules(rules);
        scaleRequest.setInstanceTypeLists(Collections.singletonList("ecs.r5.xlarge"));
        scaleRequest.setPayType("Postpaid");
        scaleRequest.setDataDiskCategory("CLOUD_SSD");
        scaleRequest.setDataDiskSize(80);
        scaleRequest.setDataDiskCount(4);

        try {
            CreateScalingTaskGroupResponse response = client.getAcsResponse(scaleRequest);
            MonitorLog.info("ScalingTaskGroup's hostGroupId is :" + response.getHostGroupId());
        } catch (ClientException e) {
            MonitorLog.error("create scaling exception", e);
        }

        /*

        CreateScalingTaskGroupRequest.ScalingRule scaleIn = new CreateScalingTaskGroupRequest.ScalingRule();
        scaleIn.setAdjustmentType("PercentChangeInCapacity");
        scaleIn.setAdjustmentValue(-getScalePercent());
        scaleIn.setRuleName("hera-scale-in");
        scaleIn.setCooldown(getCoolDown());

        CreateScalingTaskGroupRequest.ScalingRule.CloudWatchTrigger scaleInTrigger = new CreateScalingTaskGroupRequest.ScalingRule.CloudWatchTrigger();
        scaleInTrigger.setComparisonOperator("GreaterThanOrEqualToThreshold");
        scaleInTrigger.setPeriod(getCoolDown());
        scaleInTrigger.setStatistics("Average");
        scaleInTrigger.setThreshold(String.valueOf(1024 * 6));
        scaleInTrigger.setMetricName("YARN.AvailableMemory");

        scaleIn.setCloudWatchTriggers(Collections.singletonList(scaleInTrigger));
        rules.add(scaleIn);*/
    }


}
