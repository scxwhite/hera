package com.dfire.core.emr;

import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClient;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClientBuilder;
import com.amazonaws.services.elasticmapreduce.model.*;
import com.dfire.common.constants.Constants;
import com.dfire.common.entity.EmrConf;
import com.dfire.common.exception.HeraException;
import com.dfire.common.util.EnvUtils;
import com.dfire.config.HeraGlobalEnv;
import com.dfire.logs.ErrorLog;
import com.dfire.logs.MonitorLog;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * desc:
 * 亚马逊emr集群
 *
 * @author scx
 * @create 2019/04/01
 */
public class AmazonEmr extends AbstractEmr {

    public static final String NAME = "amazon";


    private final String[] APP_NAMES = {"hadoop", "spark", "hive", "livy", "zeppelin", "sqoop"};

    private volatile long lastCheckTime = 0L;

    private volatile boolean lastCheckStatus = false;

    /**
     * httpCode 正常退出码
     */
    protected final int SUCCESS_HTTP_CODE = 200;
    /**
     * emr 客户端
     */
    private volatile AmazonElasticMapReduceClient emr;

    /**
     * emr 客户端是否管不
     */
    private volatile boolean isShutdown = true;


    private ClusterState[] aliveStatus = {ClusterState.STARTING, ClusterState.BOOTSTRAPPING, ClusterState.RUNNING, ClusterState.WAITING};


    @Override
    protected String getAliveId(String owner) {
        String clusterId = null;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -2);
        ListClustersResult clusters = emr.listClusters(new ListClustersRequest().withCreatedAfter(calendar.getTime())
                .withClusterStates(aliveStatus));
        List<ClusterSummary> summaries = clusters.getClusters();
        if (summaries != null && summaries.size() > 0) {
            for (ClusterSummary summary : summaries) {
                if (summary.getName().startsWith(buildClusterName(owner))) {
                    clusterId = summary.getId();
                    MonitorLog.info("emr集群已经启动过，无需再次启动 :" + clusterId);
                }
            }
        }
        return clusterId;
    }


    @Override
    protected boolean checkAlive(String cacheClusterId) {
        if (StringUtils.isBlank(cacheClusterId)) {
            return false;
        }
        //60秒检测一次
        long time = 30000L;
        long now = System.currentTimeMillis();
        if (now - lastCheckTime >= time) {
            synchronized (this) {
                if (now - lastCheckTime >= time) {
                    Cluster clusterInfo = getClusterInfo(cacheClusterId);
                    ClusterStatus status = clusterInfo.getStatus();
                    lastCheckStatus = false;
                    for (ClusterState clusterState : aliveStatus) {
                        if (clusterState.toString().equals(status.getState())) {
                            lastCheckStatus = true;
                            break;
                        }
                    }
                    lastCheckTime = System.currentTimeMillis();
                }
            }
        }
        return lastCheckStatus;
    }

    /**
     * emr client 初始化
     */
    @Override
    protected void init() {
        if (emr == null || isShutdown) {
            synchronized (this) {
                if (emr == null || isShutdown) {
                    String regionName = "ap-south-1";
                    if (EnvUtils.isEurope()) {
                        regionName = "eu-central-1";
                    } else if (EnvUtils.isUs()) {
                        regionName = "us-west-2";
                    }
                    emr = (AmazonElasticMapReduceClient) AmazonElasticMapReduceClientBuilder.standard().withRegion(regionName).build();
                    isShutdown = false;
                }
            }
        }
    }


    @Override
    protected String sendCreateRequest(String owner) {
        try {
            EmrConf emrConf = new EmrConf();
            emrConf.setClusterName(getClusterName(owner));
            emrConf.setMasterInstanceType("m5.4xlarge");
            emrConf.setCoreInstanceType("m5.xlarge");
            emrConf.setTaskInstanceType("m5.4xlarge");
            emrConf.setNumCoreNodes(3);
            emrConf.setNumTaskNodes(1);
            if (EnvUtils.isEurope()) {
                //欧洲区域
                emrConf.setLoginURl("s3n://xxx");
                emrConf.setEmrManagedSlaveSecurityGroup("xxx");
                emrConf.setEmrManagedMasterSecurityGroup("xxx");
                emrConf.setServiceAccessSecurityGroup("xxx");
                emrConf.setEc2SubnetId("xxx");
                //初始化的公钥key
                emrConf.setKeyPairName("key_eu");
            } else if (EnvUtils.isUs()) {
                //美国区域
                emrConf.setLoginURl("s3n://xxx");
                emrConf.setEmrManagedSlaveSecurityGroup("yyyy");
                emrConf.setEmrManagedMasterSecurityGroup("yyyy");
                emrConf.setServiceAccessSecurityGroup("yyyy");
                emrConf.setEc2SubnetId("yyyy");
                //初始化的公钥key
                emrConf.setKeyPairName("key_us");
            } else if (EnvUtils.isIndia()) {
                //印度区域
                emrConf.setLoginURl("s3n://xxx");
                emrConf.setEmrManagedSlaveSecurityGroup("zzz");
                emrConf.setEmrManagedMasterSecurityGroup("zzz");
                emrConf.setServiceAccessSecurityGroup("zzz");
                emrConf.setEc2SubnetId("zzz");
                //初始化的公钥key
                emrConf.setKeyPairName("key_ind");
            }
            RunJobFlowResult result = createClient(emrConf, owner);
            int statusCode = result.getSdkHttpMetadata().getHttpStatusCode();
            if (statusCode == SUCCESS_HTTP_CODE) {
                return result.getJobFlowId();
            } else {
                ErrorLog.error("创建集群异常:" + statusCode);
            }
        } catch (Exception e) {
            ErrorLog.error("amazon创建集群异常:" + e.getMessage(), e);
        }
        return null;
    }


    /**
     * 检测集群是否创建完成逻辑
     *
     * @param clusterId 集群ID
     * @return 创建结果
     */
    @Override
    protected boolean isCompletion(String clusterId) throws HeraException {
        try {
            if (clusterId == null) {
                throw new HeraException("clusterId不能为null");
            }
            MonitorLog.info("检测集群是否创建完成:" + clusterId);
            Cluster clusterInfo = getClusterInfo(clusterId);
            ClusterStatus status = clusterInfo.getStatus();
            if (ClusterState.TERMINATED.toString().equals(status.getState()) || ClusterState.TERMINATED_WITH_ERRORS.toString().equals(status.getState()) || ClusterState.TERMINATING.toString().equals(status.getState())) {
                throw new HeraException("集群异常关闭");
            }
            MonitorLog.info(clusterId + "当前集群状态:" + status.getState());
            if (status.getState().equals(ClusterState.WAITING.toString()) || status.getState().equals(ClusterState.RUNNING.toString())) {
                return true;
            }
        } catch (Exception e) {
            throw new HeraException("检测amazon集群创建是否完成失败:" + e.getMessage(), e);
        }
        return false;

    }

    private Cluster getClusterInfo(String clusterId) {
        DescribeClusterResult result = emr.describeCluster(new DescribeClusterRequest().withClusterId(clusterId));
        if (result.getSdkHttpMetadata().getHttpStatusCode() != SUCCESS_HTTP_CODE) {
            MonitorLog.info("检测集群创建完成的请求失败,http退出码为:" + result.getSdkHttpMetadata().getHttpStatusCode());
        }
        return result.getCluster();
    }

    /**
     * 关闭集群
     */
    @Override
    protected void terminateCluster(String clusterId) {
        emr.setTerminationProtection(new SetTerminationProtectionRequest().withJobFlowIds(clusterId).withTerminationProtected(false));
        TerminateJobFlowsResult terminateResult = emr.terminateJobFlows(new TerminateJobFlowsRequest().withJobFlowIds(clusterId));
        if (terminateResult.getSdkHttpMetadata().getHttpStatusCode() != SUCCESS_HTTP_CODE) {
            MonitorLog.error("集群关闭失败" + clusterId);
        }
    }


    /**
     * 输出最近创建的集群
     */
    private void showCluster() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, -1);
        ListClustersResult running = emr.listClusters(new ListClustersRequest().withCreatedAfter(calendar.getTime()));
        MonitorLog.info("集群个数" + running.getClusters().size());
        for (ClusterSummary cluster : running.getClusters()) {
            MonitorLog.info(cluster.toString());
        }
    }

    @Override
    protected List<String> getMasterIp(String clusterId) {
        DescribeClusterResult result = emr.describeCluster(new DescribeClusterRequest().withClusterId(clusterId));
        if (result.getSdkHttpMetadata().getHttpStatusCode() != SUCCESS_HTTP_CODE) {
            return null;
        } else {
            List<String> ipList = new ArrayList<>(1);
            ipList.add(result.getCluster().getMasterPublicDnsName());
            return ipList;
        }
    }


    @Override
    protected synchronized void shutdown() {
        if (!isShutdown) {
            emr.shutdown();
            isShutdown = true;
            emr = null;
        }
    }

    private RunJobFlowResult createClient(EmrConf emrConf, String owner) {
        RunJobFlowRequest request = new RunJobFlowRequest()
                .withApplications(getApps())
                .withReleaseLabel("emr-5.21.0")
                .withLogUri(emrConf.getLoginURl())
                .withName(emrConf.getClusterName())
                .withInstances(buildInstances(emrConf))
                .withConfigurations(buildConfigurations())
                .withAutoScalingRole("EMR_AutoScaling_DefaultRole")
                .withVisibleToAllUsers(true)
                .withJobFlowRole("EMR_EC2_DefaultRole")
                .withServiceRole("EMR_DefaultRole")
                .withBootstrapActions(buildBootstrapActions())
                .withTags(new Tag("EMR", HeraGlobalEnv.getArea() + "-" + HeraGlobalEnv.getEnv() + "-" + owner + "-hera-schedule"))
                .withScaleDownBehavior("TERMINATE_AT_TASK_COMPLETION");
        MonitorLog.info("准备创建集群...");
        return emr.runJobFlow(request);
    }

    private Collection<BootstrapActionConfig> buildBootstrapActions() {
        Collection<BootstrapActionConfig> actionConfigs = new ArrayList<>();
        //设置创建集群的引导操作
        if (EnvUtils.isIndia()) {
            actionConfigs.add(new BootstrapActionConfig()
                    .withName("userinit")
                    .withScriptBootstrapAction(new ScriptBootstrapActionConfig()
                            .withPath("s3://xx/bootstrap.sh")
                            .withArgs(Arrays.asList("-caws s3 cp", ""))));
        } else if (EnvUtils.isUs() || EnvUtils.isEurope()) {
            actionConfigs.add(new BootstrapActionConfig()
                    .withName("userinit")
                    .withScriptBootstrapAction(new ScriptBootstrapActionConfig()
                            .withPath("s3://xx/bootstrap.sh")
                            .withArgs(Arrays.asList("-caws s3 cp", ""))));
        }

        return actionConfigs;
    }


    /**
     * 新建实例
     *
     * @param emrConf EmrConf
     * @return JobFlowInstancesConfig
     */
    private JobFlowInstancesConfig buildInstances(EmrConf emrConf) {

        InstanceGroupConfig masterInstance = new InstanceGroupConfig()
                .withInstanceCount(1)
                .withEbsConfiguration(new EbsConfiguration()
                        .withEbsBlockDeviceConfigs(new EbsBlockDeviceConfig()
                                .withVolumeSpecification(new VolumeSpecification()
                                        .withSizeInGB(100)
                                        .withVolumeType("standard"))
                                .withVolumesPerInstance(1)))
                .withInstanceRole("MASTER")
                .withInstanceType(emrConf.getMasterInstanceType())
                .withName("主实例 - 1");


        /**
         * core节点一般为3个节点
         */
        InstanceGroupConfig coreInstance = new InstanceGroupConfig()
                .withInstanceCount(emrConf.getNumCoreNodes())
                .withMarket(MarketType.ON_DEMAND)
                .withEbsConfiguration(new EbsConfiguration()
                        .withEbsBlockDeviceConfigs(new EbsBlockDeviceConfig()
                                .withVolumeSpecification(new VolumeSpecification()
                                        .withSizeInGB(100)
                                        .withVolumeType("standard"))
                                .withVolumesPerInstance(2)))
                .withInstanceRole("CORE")
                .withInstanceType(emrConf.getCoreInstanceType())
                .withName("核心实例组 - 1");


        /**
         * task节点弹性伸缩
         */
        InstanceGroupConfig taskInstance = new InstanceGroupConfig()
                .withInstanceCount(emrConf.getNumTaskNodes())
                .withMarket(MarketType.fromValue(HeraGlobalEnv.getAwsEmrType()))
                .withEbsConfiguration(new EbsConfiguration()
                        .withEbsBlockDeviceConfigs(new EbsBlockDeviceConfig()
                                .withVolumeSpecification(new VolumeSpecification()
                                        .withSizeInGB(100)
                                        .withVolumeType("standard"))
                                .withVolumesPerInstance(2)))
                .withInstanceRole("TASK")
                .withInstanceType(emrConf.getTaskInstanceType())
                .withAutoScalingPolicy(buildAutoScalingPolicy())
                .withName("核心实例组 - 2");

        JobFlowInstancesConfig instancesConfig = new JobFlowInstancesConfig()
                .withInstanceGroups(masterInstance, coreInstance, taskInstance)
                .withEc2KeyName(emrConf.getKeyPairName())
                .withKeepJobFlowAliveWhenNoSteps(true)
                .withEmrManagedMasterSecurityGroup(emrConf.getEmrManagedMasterSecurityGroup())
                .withEmrManagedSlaveSecurityGroup(emrConf.getEmrManagedSlaveSecurityGroup())
                .withServiceAccessSecurityGroup(emrConf.getServiceAccessSecurityGroup())
                .withTerminationProtected(true)
                .withEc2SubnetId(emrConf.getEc2SubnetId());


        if (StringUtils.isNotBlank(emrConf.getAdditionalMasterSecurityGroups())) {
            instancesConfig.setAdditionalMasterSecurityGroups(Arrays.asList(emrConf.getAdditionalMasterSecurityGroups().split(Constants.COMMA)));
        }

        if (StringUtils.isNotBlank(emrConf.getAdditionalSlaveSecurityGroups())) {
            instancesConfig.setAdditionalSlaveSecurityGroups(Arrays.asList(emrConf.getAdditionalSlaveSecurityGroups().split(Constants.COMMA)));
        }
        return instancesConfig;
    }

    /**
     * emr集群自动扩展策略，可以自己定义
     *
     * @return AutoScalingPolicy
     */
    private AutoScalingPolicy buildAutoScalingPolicy() {


        // yarn 资源可用百分比低于10% 扩容
        double increaseThreshold = 20d;
        // yarn 资源可用百分比高于30% 缩容
        double decreaseThreshold = 30d;

        ScalingRule scaleOutByMemPer = new ScalingRule()
                .withName("scale-out by yarn resource Per")
                .withDescription("scale-out by yarn resources Per")
                .withAction(new ScalingAction()
                        .withSimpleScalingPolicyConfiguration(new SimpleScalingPolicyConfiguration()
                                .withScalingAdjustment(getScalePercent())
                                .withAdjustmentType("PERCENT_CHANGE_IN_CAPACITY")
                                .withCoolDown(getCoolDown())))
                .withTrigger(new ScalingTrigger()
                        .withCloudWatchAlarmDefinition(new CloudWatchAlarmDefinition()
                                .withComparisonOperator("LESS_THAN_OR_EQUAL")
                                .withEvaluationPeriods(1)
                                .withPeriod(getCoolDown())
                                .withMetricName("YARNMemoryAvailablePercentage")
                                .withNamespace("AWS/ElasticMapReduce")
                                .withThreshold(increaseThreshold)
                                .withStatistic("AVERAGE")
                                .withUnit("PERCENT")
                                .withDimensions(new MetricDimension()
                                        .withKey("JobFlowId")
                                        .withValue("${emr.clusterId}"))));


        ScalingRule scaleOutByMem = new ScalingRule()
                .withName("scale-out by yarn resource")
                .withDescription("scale-out by yarn resources")
                .withAction(new ScalingAction()
                        .withSimpleScalingPolicyConfiguration(new SimpleScalingPolicyConfiguration()
                                .withScalingAdjustment(getScalePercent())
                                .withAdjustmentType("PERCENT_CHANGE_IN_CAPACITY")
                                .withCoolDown(getCoolDown())))
                .withTrigger(new ScalingTrigger()
                        .withCloudWatchAlarmDefinition(new CloudWatchAlarmDefinition()
                                .withComparisonOperator("LESS_THAN_OR_EQUAL")
                                .withEvaluationPeriods(1)
                                .withPeriod(getCoolDown())
                                .withMetricName("MemoryAvailableMB")
                                .withNamespace("AWS/ElasticMapReduce")
                                .withThreshold(increaseThreshold * 2048)
                                .withStatistic("AVERAGE")
                                .withUnit("COUNT")
                                .withDimensions(new MetricDimension()
                                        .withKey("JobFlowId")
                                        .withValue("${emr.clusterId}"))));

        ScalingRule scaleInByMemPer = new ScalingRule()
                .withName("scale-in by yarn resource per")
                .withDescription("scale-in by yarn resources per")
                .withAction(new ScalingAction()
                        .withSimpleScalingPolicyConfiguration(new SimpleScalingPolicyConfiguration()
                                .withScalingAdjustment(-getScalePercent())
                                .withAdjustmentType("PERCENT_CHANGE_IN_CAPACITY")
                                .withCoolDown(getCoolDown())))
                .withTrigger(new ScalingTrigger()
                        .withCloudWatchAlarmDefinition(new CloudWatchAlarmDefinition()
                                .withComparisonOperator("GREATER_THAN_OR_EQUAL")
                                .withEvaluationPeriods(1)
                                .withPeriod(getCoolDown())
                                .withMetricName("YARNMemoryAvailablePercentage")
                                .withNamespace("AWS/ElasticMapReduce")
                                .withThreshold(decreaseThreshold)
                                .withStatistic("AVERAGE")
                                .withUnit("PERCENT")
                                .withDimensions(new MetricDimension()
                                        .withKey("JobFlowId")
                                        .withValue("${emr.clusterId}"))));


        ScalingRule scaleInByMem = new ScalingRule()
                .withName("scale-in by yarn resource")
                .withDescription("scale-in by yarn resources")
                .withAction(new ScalingAction()
                        .withSimpleScalingPolicyConfiguration(new SimpleScalingPolicyConfiguration()
                                .withScalingAdjustment(-getScalePercent())
                                .withAdjustmentType("PERCENT_CHANGE_IN_CAPACITY")
                                .withCoolDown(getCoolDown())))
                .withTrigger(new ScalingTrigger()
                        .withCloudWatchAlarmDefinition(new CloudWatchAlarmDefinition()
                                .withComparisonOperator("GREATER_THAN_OR_EQUAL")
                                .withEvaluationPeriods(1)
                                .withPeriod(getCoolDown())
                                .withMetricName("MemoryAvailableMB")
                                .withNamespace("AWS/ElasticMapReduce")
                                .withThreshold(decreaseThreshold * 2048)
                                .withStatistic("AVERAGE")
                                .withUnit("COUNT")
                                .withDimensions(new MetricDimension()
                                        .withKey("JobFlowId")
                                        .withValue("${emr.clusterId}"))));
        return new AutoScalingPolicy()
                .withConstraints(new ScalingConstraints()
                        .withMinCapacity(getMinCapacity())
                        .withMaxCapacity(getMaxCapacity()))
                .withRules(scaleInByMem, scaleInByMemPer, scaleOutByMem, scaleOutByMemPer);
    }

    /**
     * 初始化配置信息
     *
     * @return configurations
     */
    private List<Configuration> buildConfigurations() {

        List<Configuration> configs = new ArrayList<>();

        if (EnvUtils.isPre() && !EnvUtils.isIndia()) {
            Map<String, String> hivePro = new HashMap<>(1);
            hivePro.put("javax.jdo.option.ConnectionUserName", "username");
            hivePro.put("javax.jdo.option.ConnectionDriverName", "org.mariadb.jdbc.Driver");
            hivePro.put("javax.jdo.option.ConnectionPassword", "password");
            String url = "jdbc:mysql://localhost:3306/hivemeta_pre?createDatabaseIfNotExist=true";
            if (EnvUtils.isEurope()) {
                url = "jdbc:mysql://localhost:3306/hivemeta_pre?createDatabaseIfNotExist=true";
            }
            hivePro.put("javax.jdo.option.ConnectionURL", url);
            configs.add(new Configuration()
                    .withClassification("hive-site")
                    .withProperties(hivePro)
                    .withConfigurations());
        } else {
            //hive 配置
            Map<String, String> hivePro = new HashMap<>(1);
            hivePro.put("hive.metastore.client.factory.class", "com.amazonaws.glue.catalog.metastore.AWSGlueDataCatalogHiveClientFactory");
            configs.add(new Configuration()
                    .withClassification("hive-site")
                    .withProperties(hivePro)
                    .withConfigurations());


            //presto-hive 配置
            Map<String, String> prestoPro = new HashMap<>(1);
            prestoPro.put("hive.metastore.glue.datacatalog.enabled", "true");
            configs.add(new Configuration()
                    .withClassification("presto-connector-hive")
                    .withProperties(prestoPro)
                    .withConfigurations());

            //spark-hive 配置
            Map<String, String> sparkPro = new HashMap<>(1);
            sparkPro.put("hive.metastore.client.factory.class", "com.amazonaws.glue.catalog.metastore.AWSGlueDataCatalogHiveClientFactory");
            configs.add(new Configuration()
                    .withClassification("spark-hive-site")
                    .withProperties(sparkPro)
                    .withConfigurations());
        }
        configs.add(new Configuration()
                .withClassification("spark-defaults")
                .withProperties(Collections.singletonMap("spark.port.maxRetries", "256"))
                .withConfigurations());
        return configs;
    }


    /**
     * 初始化所有要创建的集群
     *
     * @return applications
     */
    private List<Application> getApps() {
        List<Application> apps = new ArrayList<>(APP_NAMES.length);
        for (String appName : APP_NAMES) {
            apps.add(buildApp(appName));
        }
        return apps;
    }

    /**
     * 新建集群
     *
     * @param name 集群名称
     * @return application
     */
    private Application buildApp(String name) {
        return new Application().withName(name);
    }

}
