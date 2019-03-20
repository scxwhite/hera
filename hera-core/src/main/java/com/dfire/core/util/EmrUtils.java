package com.dfire.core.util;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClient;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClientBuilder;
import com.amazonaws.services.elasticmapreduce.model.*;
import com.dfire.common.constants.Constants;
import com.dfire.common.entity.EmrConf;
import org.apache.commons.lang.StringUtils;

import java.util.*;
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

    private static final int SUCCESS_HTTP_CODE = 200;

    private static volatile AmazonElasticMapReduceClient emr;

    private static volatile boolean isShutdown = true;

    public static void main(String[] args) {
        createCluster();
    }


    private static String getIp(String clusterId) {
        init();
        DescribeClusterResult result = emr.describeCluster(new DescribeClusterRequest().withClusterId(clusterId));
        if (result.getSdkHttpMetadata().getHttpStatusCode() != SUCCESS_HTTP_CODE) {
            return null;
        }
        return result.getCluster().getMasterPublicDnsName();
    }


    private static void createCluster() {
        init();
        //TODO 如果非动态扩展 自己衡量 如果动态扩展，判断集群是否关闭
        RunJobFlowResult result = createClient(EmrConf.builder()
                .loginURl("s3://aws-logs-636856355690-ap-south-1/elasticmapreduce/")
                .clusterName("bigdata-moye-auto-scale")
                .masterInstanceType("m5.2xlarge")
                .numCoresNodes(4)
                .coreInstanceType("m5.2xlarge")
                .emrManagedMasterSecurityGroup("sg-0d9414a5e40b236c2")
                .emrManagedSlaveSecurityGroup("sg-04c3e127cbaf17ed1")
                .additionalMasterSecurityGroups("sg-0fc3efb3cc2f75dd0")
                .additionalSlaveSecurityGroups("sg-0fc3efb3cc2f75dd0")
                .serviceAccessSecurityGroup("sg-088fe656e6c50a6fc")
                .ec2SubnetId("subnet-0032203fab5879af0")
                .build());
        int statusCode = result.getSdkHttpMetadata().getHttpStatusCode();
        String clusterId = result.getJobFlowId();
        System.out.println("clusterId is:" + clusterId);
        if (statusCode == SUCCESS_HTTP_CODE) {
            waitClusterCompletion(clusterId);
        } else {
            System.out.println("创建失败,退出码:" + statusCode);
        }
        System.out.println("集群创建完成,可以执行任务了.集群ID为：" + clusterId + "集群IP为:" + getIp(clusterId));
        showCluster();
    }


    /**
     * 循环检测 ，等待集群创建完成
     *
     * @param clusterId 集群ID
     */
    private static void waitClusterCompletion(String clusterId) {
        long start = System.currentTimeMillis();
        long sleepTime = 15 * 1000 * 1000000L;
        do {
            LockSupport.parkNanos(sleepTime);
            System.out.println("检测集群是否创建完成:" + clusterId);
        } while (!checkCompletion(clusterId));

        System.out.println("创建集群:" + clusterId + "耗时:" + (System.currentTimeMillis() - start) + "ms");
    }

    /**
     * 检测集群是否创建完成逻辑
     *
     * @param clusterId 集群ID
     * @return 创建结果
     */
    private static boolean checkCompletion(String clusterId) {
        ListClustersResult waiting = emr.listClusters(new ListClustersRequest().withClusterStates("WAITING"));
        if (waiting.getSdkHttpMetadata().getHttpStatusCode() != SUCCESS_HTTP_CODE) {
            System.out.println("请求失败,http退出码为:" + waiting.getSdkHttpMetadata().getHttpStatusCode());
            return false;
        }
        List<ClusterSummary> clusters = waiting.getClusters();
        for (ClusterSummary cluster : clusters) {
            if (cluster.getId().equals(clusterId)) {
                return true;
            }
        }
        return false;

    }

    /**
     * 关闭集群
     *
     * @param jobFlowIds 集群id
     */
    private static void terminateJob(String... jobFlowIds) {
        init();
        for (String jobFlowId : jobFlowIds) {
            emr.setTerminationProtection(new SetTerminationProtectionRequest().withJobFlowIds(jobFlowId).withTerminationProtected(false));
            TerminateJobFlowsResult terminateResult = emr.terminateJobFlows(new TerminateJobFlowsRequest().withJobFlowIds(jobFlowId));
            if (terminateResult.getSdkHttpMetadata().getHttpStatusCode() == SUCCESS_HTTP_CODE) {
                System.out.println("关闭成功" + jobFlowId);
            } else {
                System.out.println("关闭失败" + jobFlowId);
            }
        }
    }

    /**
     * 输出最近创建的集群
     */
    private static void showCluster() {
        init();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 14);
        ListClustersResult running = emr.listClusters(new ListClustersRequest().withCreatedAfter(calendar.getTime()));
        System.out.println("集群个数" + running.getClusters().size());
        for (ClusterSummary cluster : running.getClusters()) {
            System.out.println(cluster.toString());
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
        }
    }

    public static RunJobFlowResult createClient(EmrConf emrConf) {
        RunJobFlowRequest request = new RunJobFlowRequest()
                .withApplications(getApps())
                .withReleaseLabel("emr-5.13.0")
                .withLogUri(emrConf.getLoginURl())
                .withName(emrConf.getClusterName())
                .withInstances(buildInstances(emrConf))
                .withConfigurations(buildConfigurations())
                .withAutoScalingRole("EMR_AutoScaling_DefaultRole")
                .withVisibleToAllUsers(true)
                .withJobFlowRole("EMR_EC2_DefaultRole")
                .withServiceRole("EMR_DefaultRole")
                .withScaleDownBehavior("TERMINATE_AT_TASK_COMPLETION");
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

    private static AutoScalingPolicy buildAutoScalingPolicy() {

        ScalingRule scaleOut = new ScalingRule()
                .withName("YARNMemoryAvailablePercentage")
                .withDescription("scale-out by yarn resources")
                .withAction(new ScalingAction()
                        .withSimpleScalingPolicyConfiguration(new SimpleScalingPolicyConfiguration()
                                .withScalingAdjustment(10)
                                .withAdjustmentType("PERCENT_CHANGE_IN_CAPACITY")
                                .withCoolDown(300)))
                .withTrigger(new ScalingTrigger()
                        .withCloudWatchAlarmDefinition(new CloudWatchAlarmDefinition()
                                .withComparisonOperator("LESS_THAN_OR_EQUAL")
                                .withEvaluationPeriods(1)
                                .withPeriod(60)
                                .withMetricName("YARNMemoryAvailablePercentage")
                                .withNamespace("AWS/ElasticMapReduce")
                                .withThreshold(10d)
                                .withStatistic("AVERAGE")
                                .withUnit("PERCENT")
                                .withDimensions(new MetricDimension()
                                        .withKey("JobFlowId")
                                        .withValue("${emr.clusterId}"))));

        ScalingRule scaleIn = new ScalingRule()
                .withName("YARNMemoryAvailablePercentage")
                .withDescription("scale-in by yarn resources")
                .withAction(new ScalingAction()
                        .withSimpleScalingPolicyConfiguration(new SimpleScalingPolicyConfiguration()
                                .withScalingAdjustment(-10)
                                .withAdjustmentType("PERCENT_CHANGE_IN_CAPACITY")
                                .withCoolDown(300)))
                .withTrigger(new ScalingTrigger()
                        .withCloudWatchAlarmDefinition(new CloudWatchAlarmDefinition()
                                .withComparisonOperator("GREATER_THAN_OR_EQUAL")
                                .withEvaluationPeriods(1)
                                .withPeriod(60)
                                .withMetricName("YARNMemoryAvailablePercentage")
                                .withNamespace("AWS/ElasticMapReduce")
                                .withThreshold(30d)
                                .withStatistic("AVERAGE")
                                .withUnit("PERCENT")
                                .withDimensions(new MetricDimension()
                                        .withKey("JobFlowId")
                                        .withValue("${emr.clusterId}"))));
        return new AutoScalingPolicy()
                .withConstraints(new ScalingConstraints()
                        .withMinCapacity(1)
                        .withMaxCapacity(10))
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
        hivePro.put("hive.metastore.glue.datacatalog.enabled", "true");
        configs.add(new Configuration()
                .withClassification("presto-connector-hive")
                .withProperties(prestoPro)
                .withConfigurations());

        //spark-hive 配置
        Map<String, String> sparkPro = new HashMap<>(1);
        hivePro.put("hive.metastore.client.factory.class", "com.amazonaws.glue.catalog.metastore.AWSGlueDataCatalogHiveClientFactory");
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
