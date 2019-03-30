package com.dfire.core.util;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClient;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClientBuilder;
import com.amazonaws.services.elasticmapreduce.model.*;
import com.dfire.common.constants.Constants;
import com.dfire.common.entity.EmrConf;
import com.dfire.common.util.ActionUtil;
import com.dfire.common.util.NamedThreadFactory;
import com.dfire.logs.MonitorLog;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

/**
 * desc:
 * emr连接工具
 *
 * @author scx
 * @create 2019/03/18
 */
public class EmrUtils {

    private static final String[] APP_NAMES = {"hadoop", "spark", "hive", "livy", "zeppelin", "sqoop"};

    /**
     * httpCode 正常退出码
     */
    private static final int SUCCESS_HTTP_CODE = 200;

    /**
     * emr 客户端
     */
    private static volatile AmazonElasticMapReduceClient emr;

    /**
     * emr 客户端是否管不
     */
    private static volatile boolean isShutdown = true;

    /**
     * 集群是否已经关闭字段
     */
    private static volatile boolean clusterTerminate = true;

    /**
     * 缓存的集群IP
     */
    private static volatile String cacheIp = null;

    /**
     * 缓存的集群Id
     */
    private static String cacheClusterId;

    /**
     * 任务数
     */
    private static AtomicInteger taskRunning;

    /**
     * 任务计数器
     */
    private static AtomicLong taskNum;

    private static long cacheTaskNum;

    private static ScheduledExecutorService pool;

    /**
     * check 集群是否需要关闭返回的future
     */
    private static ScheduledFuture<?> clusterWatchFuture;

    private static void closeCluster(String clusterId) {
        init();
        cacheClusterId = clusterId;
        terminateJob();
    }

    public static void main(String[] args) {


    }

    private static String getSystemProperty(String name) {
        String val = System.getenv(name);
        if (StringUtils.isNotBlank(name)) {
            return val;
        }
        val = System.getProperty(name);
        if (StringUtils.isBlank(name)) {
            MonitorLog.error("找不到的环境变量:" + name);
        }
        return val;
    }

    public static void addJob() {
        createCluster();
        taskRunning.incrementAndGet();
        taskNum.incrementAndGet();
    }

    public static synchronized void removeJob() {
        if (taskRunning != null) {
            taskRunning.decrementAndGet();
        }
    }

    /**
     * 获得集群IP
     *
     * @return clusterIp
     */
    public static String getIp() {
        if (cacheIp == null) {
            synchronized (EmrUtils.class) {
                if (cacheIp == null) {
                    createCluster();
                    DescribeClusterResult result = emr.describeCluster(new DescribeClusterRequest().withClusterId(cacheClusterId));
                    if (result.getSdkHttpMetadata().getHttpStatusCode() != SUCCESS_HTTP_CODE) {
                        cacheIp = null;
                    } else {
                        cacheIp = result.getCluster().getMasterPublicDnsName();
                    }
                }
            }
        }
        return cacheIp;
    }


    private static boolean notAlive(String clusterName) {
        ListClustersResult clusters = emr.listClusters(new ListClustersRequest()
                .withClusterStates(ClusterState.STARTING, ClusterState.BOOTSTRAPPING, ClusterState.RUNNING, ClusterState.WAITING));
        List<ClusterSummary> summaries = clusters.getClusters();
        if (summaries != null && summaries.size() > 0) {
            for (ClusterSummary summary : summaries) {
                if (summary.getName().startsWith(clusterName)) {
                    cacheClusterId = summary.getId();
                    MonitorLog.info("emr集群已经启动过，无需再次启动 :" + cacheClusterId);
                    return false;
                }
            }
        }
        return true;
    }


    private static void createCluster() {
        init();
        String clusterName = "hera-schedule-";
        if (clusterTerminate) {
            synchronized (EmrUtils.class) {
                if (clusterTerminate) {
                    if (notAlive(clusterName)) {
                        clusterName += ActionUtil.getCurrDate();
                        RunJobFlowResult result = createClient(EmrConf.builder()
                                .loginURl("s3://aws-logs-636856355690-ap-south-1/elasticmapreduce/")
                                .clusterName(clusterName)
                                .masterInstanceType("m5.2xlarge")
                                .numCoresNodes(1)
                                .coreInstanceType("m5.2xlarge")
                                .emrManagedMasterSecurityGroup("sg-0d9414a5e40b236c2")
                                .emrManagedSlaveSecurityGroup("sg-04c3e127cbaf17ed1")
                                .additionalMasterSecurityGroups("sg-0fc3efb3cc2f75dd0")
                                .additionalSlaveSecurityGroups("sg-0fc3efb3cc2f75dd0")
                                .serviceAccessSecurityGroup("sg-088fe656e6c50a6fc")
                                .ec2SubnetId("subnet-0032203fab5879af0")
                                .build());
                        int statusCode = result.getSdkHttpMetadata().getHttpStatusCode();
                        if (statusCode == SUCCESS_HTTP_CODE) {
                            cacheClusterId = result.getJobFlowId();
                        } else {
                            MonitorLog.error("创建集群失败,退出码:" + statusCode);
                        }
                    }
                    waitClusterCompletion(cacheClusterId);
                    clusterTerminate = false;
                    showCluster();
                    taskRunning = new AtomicInteger(0);
                    taskNum = new AtomicLong(0);
                    submitClusterWatch();
                    MonitorLog.info("集群创建完成,可以执行任务了.集群ID为：" + cacheClusterId + ".集群IP为:" + getIp());
                }
            }
        } else {
            if (notAlive(clusterName)) {
                destroyCluster();
                createCluster();
            }
        }
    }

    /**
     * createCluster 方法已经同步过
     */
    private static void submitClusterWatch() {
        if (clusterWatchFuture == null) {
            if (pool == null) {
                pool = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("cluster-destroy-watch", false));
            }
            cacheTaskNum = taskNum.get();
            clusterWatchFuture = pool.scheduleWithFixedDelay(() -> {
                MonitorLog.info("正在emr集群运行的任务个数:{},十分钟前运行的总任务个数:{},现在运行的总任务个数:{}", taskRunning.get(), cacheTaskNum, taskNum.get());
                if (taskRunning.get() == 0 && cacheTaskNum == taskNum.get()) {
                    terminateJob();
                    clusterWatchFuture.cancel(true);
                } else {
                    cacheTaskNum = taskNum.get();
                }
            }, 10, 10, TimeUnit.MINUTES);
        }
    }

    /**
     * 循环检测 ，等待集群创建完成
     *
     * @param clusterId 集群ID
     */
    private static void waitClusterCompletion(String clusterId) {
        long start = System.currentTimeMillis();
        long sleepTime = 15 * 1000 * 1000000L;
        while (!checkCompletion(clusterId)) {
            LockSupport.parkNanos(sleepTime);
        }
        MonitorLog.info("创建集群:" + clusterId + "耗时:" + (System.currentTimeMillis() - start) + "ms");
    }

    /**
     * 检测集群是否创建完成逻辑
     *
     * @param clusterId 集群ID
     * @return 创建结果
     */
    private static boolean checkCompletion(String clusterId) {
        try {
            MonitorLog.info("检测集群是否创建完成:" + clusterId);
            ListClustersResult waiting = emr.listClusters(new ListClustersRequest().withClusterStates("WAITING"));
            if (waiting.getSdkHttpMetadata().getHttpStatusCode() != SUCCESS_HTTP_CODE) {
                MonitorLog.info("检测集群创建完成的请求失败,http退出码为:" + waiting.getSdkHttpMetadata().getHttpStatusCode());
                return false;
            }
            List<ClusterSummary> clusters = waiting.getClusters();
            for (ClusterSummary cluster : clusters) {
                if (cluster.getId().equals(clusterId)) {
                    return true;
                }
            }
        } catch (Exception e) {
            MonitorLog.error(e.getMessage(), e);
            return false;
        }
        return false;

    }

    /**
     * 关闭集群
     */
    private static void terminateJob() {
        emr.setTerminationProtection(new SetTerminationProtectionRequest().withJobFlowIds(cacheClusterId).withTerminationProtected(false));
        TerminateJobFlowsResult terminateResult = emr.terminateJobFlows(new TerminateJobFlowsRequest().withJobFlowIds(cacheClusterId));
        if (terminateResult.getSdkHttpMetadata().getHttpStatusCode() == SUCCESS_HTTP_CODE) {
            MonitorLog.info("集群:" + cacheClusterId + "关闭成功,执行任务数为:" + taskRunning.get());
            destroyCluster();
        } else {
            MonitorLog.error("集群关闭失败" + cacheClusterId);
        }
    }

    private static void destroyCluster() {
        clusterTerminate = true;
        cacheIp = null;
        cacheClusterId = null;
        clusterWatchFuture = null;
        shutdown();
    }

    /**
     * 输出最近创建的集群
     */
    private static void showCluster() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, -1);
        ListClustersResult running = emr.listClusters(new ListClustersRequest().withCreatedAfter(calendar.getTime()));
        MonitorLog.info("集群个数" + running.getClusters().size());
        for (ClusterSummary cluster : running.getClusters()) {
            MonitorLog.info(cluster.toString());
        }
    }

    /**
     * emr client 初始化
     */
    private static void init() {
        if (emr == null || isShutdown) {
            synchronized (EmrUtils.class) {
                if (emr == null || isShutdown) {
                    AWSCredentials credentials = new BasicAWSCredentials("AKIAIDNAUCQ2GWA34WAA", "OCb6tZJfqBWo2tNJbwjw3Cx81siCuvVrnTKveCD8");
                    emr = (AmazonElasticMapReduceClient) AmazonElasticMapReduceClientBuilder.standard().withRegion("ap-south-1").withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
                    isShutdown = false;
                }
            }
        }
    }

    private static synchronized void shutdown() {
        if (!isShutdown) {
            emr.shutdown();
            isShutdown = true;
            emr = null;
        }
    }

    private static RunJobFlowResult createClient(EmrConf emrConf) {
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
                .withScaleDownBehavior("TERMINATE_AT_TASK_COMPLETION");
        MonitorLog.info("准备创建集群...");
        return emr.runJobFlow(request);
    }


    /**
     * 新建实例
     *
     * @param emrConf EmrConf
     * @return JobFlowInstancesConfig
     */
    private static JobFlowInstancesConfig buildInstances(EmrConf emrConf) {

        InstanceGroupConfig masterInstance = new InstanceGroupConfig()
                .withInstanceCount(1)
                .withEbsConfiguration(new EbsConfiguration()
                        .withEbsBlockDeviceConfigs(new EbsBlockDeviceConfig()
                                .withVolumeSpecification(new VolumeSpecification()
                                        .withSizeInGB(100)
                                        .withVolumeType("gp2"))
                                .withVolumesPerInstance(1)))
                .withInstanceRole("MASTER")
                .withInstanceType(emrConf.getMasterInstanceType())
                .withName("主实例 - 1");


        InstanceGroupConfig coreInstance = new InstanceGroupConfig()
                .withInstanceCount(emrConf.getNumCoresNodes())
                .withEbsConfiguration(new EbsConfiguration()
                        .withEbsBlockDeviceConfigs(new EbsBlockDeviceConfig()
                                .withVolumeSpecification(new VolumeSpecification()
                                        .withSizeInGB(100)
                                        .withVolumeType("gp2"))
                                .withVolumesPerInstance(2)))
                .withInstanceRole("CORE")
                .withInstanceType(emrConf.getCoreInstanceType())
                .withAutoScalingPolicy(buildAutoScalingPolicy())
                .withName("核心实例组 - 2");

        JobFlowInstancesConfig instancesConfig = new JobFlowInstancesConfig()
                .withInstanceGroups(masterInstance, coreInstance)
                .withEc2KeyName("bigdata")
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
    private static AutoScalingPolicy buildAutoScalingPolicy() {

        int coolDown = 300;
        int scalePercent = 10;
        int minCapacity = 1;
        int maxCapacity = 30;
        double increaseThreshold = 10d;
        double decreaseThreshold = 30d;

        ScalingRule scaleOut = new ScalingRule()
                .withName("scale-out by yarn resource")
                .withDescription("scale-out by yarn resources")
                .withAction(new ScalingAction()
                        .withSimpleScalingPolicyConfiguration(new SimpleScalingPolicyConfiguration()
                                .withScalingAdjustment(scalePercent)
                                .withAdjustmentType("PERCENT_CHANGE_IN_CAPACITY")
                                .withCoolDown(coolDown)))
                .withTrigger(new ScalingTrigger()
                        .withCloudWatchAlarmDefinition(new CloudWatchAlarmDefinition()
                                .withComparisonOperator("LESS_THAN_OR_EQUAL")
                                .withEvaluationPeriods(1)
                                .withPeriod(coolDown)
                                .withMetricName("YARNMemoryAvailablePercentage")
                                .withNamespace("AWS/ElasticMapReduce")
                                .withThreshold(increaseThreshold)
                                .withStatistic("AVERAGE")
                                .withUnit("PERCENT")
                                .withDimensions(new MetricDimension()
                                        .withKey("JobFlowId")
                                        .withValue("${emr.clusterId}"))));

        ScalingRule scaleIn = new ScalingRule()
                .withName("scale-in by yarn resource")
                .withDescription("scale-in by yarn resources")
                .withAction(new ScalingAction()
                        .withSimpleScalingPolicyConfiguration(new SimpleScalingPolicyConfiguration()
                                .withScalingAdjustment(-scalePercent)
                                .withAdjustmentType("PERCENT_CHANGE_IN_CAPACITY")
                                .withCoolDown(coolDown)))
                .withTrigger(new ScalingTrigger()
                        .withCloudWatchAlarmDefinition(new CloudWatchAlarmDefinition()
                                .withComparisonOperator("GREATER_THAN_OR_EQUAL")
                                .withEvaluationPeriods(1)
                                .withPeriod(coolDown)
                                .withMetricName("YARNMemoryAvailablePercentage")
                                .withNamespace("AWS/ElasticMapReduce")
                                .withThreshold(decreaseThreshold)
                                .withStatistic("AVERAGE")
                                .withUnit("PERCENT")
                                .withDimensions(new MetricDimension()
                                        .withKey("JobFlowId")
                                        .withValue("${emr.clusterId}"))));
        return new AutoScalingPolicy()
                .withConstraints(new ScalingConstraints()
                        .withMinCapacity(minCapacity)
                        .withMaxCapacity(maxCapacity))
                .withRules(scaleOut, scaleIn);
    }

    /**
     * 初始化配置信息
     *
     * @return configurations
     */
    private static List<Configuration> buildConfigurations() {

        List<Configuration> configs = new ArrayList<>();

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

        return configs;
    }


    /**
     * 初始化所有要创建的集群
     *
     * @return applications
     */
    private static List<Application> getApps() {
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
    private static Application buildApp(String name) {
        return new Application().withName(name);
    }

}
