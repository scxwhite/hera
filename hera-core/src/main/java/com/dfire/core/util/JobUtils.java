package com.dfire.core.util;

import com.dfire.common.constants.RunningJobKeyConstant;
import com.dfire.common.entity.HeraDebugHistory;
import com.dfire.common.entity.model.HeraJobBean;
import com.dfire.common.entity.vo.HeraJobHistoryVo;
import com.dfire.common.entity.vo.HeraProfileVo;
import com.dfire.common.enums.JobRunTypeEnum;
import com.dfire.common.processor.DownProcessor;
import com.dfire.common.processor.JobProcessor;
import com.dfire.common.processor.Processor;
import com.dfire.common.service.HeraFileService;
import com.dfire.common.service.HeraJobActionService;
import com.dfire.common.service.HeraProfileService;
import com.dfire.common.util.BeanConvertUtils;
import com.dfire.common.util.DateUtil;
import com.dfire.common.util.HierarchyProperties;
import com.dfire.common.util.RenderHierarchyProperties;
import com.dfire.core.job.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午12:13 2018/4/26
 * @desc 任务执行体job的创建工具，创建任务依据任务配置创建任务前置处理，核心执行体，后置处理，以及脚本中的变量替换
 */
@Slf4j
public class JobUtils {

    public static final Pattern pattern = Pattern.compile("download\\[(hdfs)://.+]");

    public static Job createDebugJob(JobContext jobContext, HeraDebugHistory heraDebugHistory,
                                     String workDir, ApplicationContext applicationContext) {
        jobContext.setDebugHistory(BeanConvertUtils.convert(heraDebugHistory));
        jobContext.setWorkDir(workDir);
        //todo 脚本中的变量替换
        HierarchyProperties hierarchyProperties = new HierarchyProperties(new HashMap<>());
        String script = heraDebugHistory.getScript();
        List<Map<String, String>> resources = new ArrayList<>();
        script = resolveScriptResource(resources, script, applicationContext);
        jobContext.setResources(resources);
        hierarchyProperties.setProperty(RunningJobKeyConstant.JOB_SCRIPT, script);
        //todo 权限控制判断，暂时不做
        HeraFileService heraFileService = (HeraFileService) applicationContext.getBean("heraFileService");
        String owner = heraFileService.findById(heraDebugHistory.getFileId()).getOwner();
        HeraProfileService heraProfileService = (HeraProfileService) applicationContext.getBean("heraProfileService");
        HeraProfileVo heraProfile = heraProfileService.findByOwner(owner);
        if (heraProfile != null && heraProfile.getHadoopConf() != null) {
            for (String key : heraProfile.getHadoopConf().keySet()) {
                hierarchyProperties.setProperty(key, heraProfile.getHadoopConf().get(key));
            }
        }

        jobContext.setProperties(new RenderHierarchyProperties(hierarchyProperties));
        hierarchyProperties.setProperty("hadoop.mappred.job.hera_id", "hera_debug_" + heraDebugHistory.getId());

        List<Job> pres = new ArrayList<>(1);
        pres.add(new DownLoadJob(jobContext));
        Job core = null;
        if (heraDebugHistory.getRunType().equalsIgnoreCase(JobRunTypeEnum.Shell.toString())) {
            jobContext.putData(RunningJobKeyConstant.JOB_RUN_TYPE, JobRunTypeEnum.Shell.toString());
            core = new HadoopShellJob(jobContext);
        } else if (heraDebugHistory.getRunType().equalsIgnoreCase(JobRunTypeEnum.Hive.toString())) {
            jobContext.putData(RunningJobKeyConstant.JOB_RUN_TYPE, JobRunTypeEnum.Hive.toString());
            core = new HiveJob(jobContext, applicationContext);
        } else if (heraDebugHistory.getRunType().equalsIgnoreCase(JobRunTypeEnum.Spark.toString())) {
            jobContext.putData(RunningJobKeyConstant.JOB_RUN_TYPE, JobRunTypeEnum.Spark.toString());
            core = new SparkJob(jobContext, applicationContext);
        } else if (heraDebugHistory.getRunType().equalsIgnoreCase(JobRunTypeEnum.Spark2.toString())) {
            jobContext.putData(RunningJobKeyConstant.JOB_RUN_TYPE, JobRunTypeEnum.Spark2.toString());
            core = new Spark2Job(jobContext, applicationContext);
        }
        Job job = new ProcessJobContainer(jobContext, pres, new ArrayList<>(), core, applicationContext);
        return job;
    }

    public static Job createScheduleJob(JobContext jobContext, HeraJobBean jobBean,
                                        HeraJobHistoryVo history, String workDir, ApplicationContext applicationContext) {
        jobContext.setHeraJobHistory(history);
        jobContext.setWorkDir(workDir);
        jobContext.getProperties().setProperty("hera.encode", "utf-8");
        HierarchyProperties hierarchyProperties = jobBean.getHierarchyProperties();
        Map<String, String> configs = history.getProperties();
        if (configs != null && !configs.isEmpty()) {
            history.getLog().appendHera("this job has configs");
            for (String key : configs.keySet()) {
                hierarchyProperties.setProperty(key, configs.get(key));
                history.getLog().appendHera(key + "=" + configs.get(key));
            }
        }
        jobContext.setProperties(new RenderHierarchyProperties(hierarchyProperties));
        List<Map<String, String>> resource = jobBean.getHierarchyResources();
        HeraJobActionService heraJobActionService = (HeraJobActionService) applicationContext.getBean("heraJobActionService");

        String jobId = jobBean.getHeraActionVo().getId();
        String script = heraJobActionService.findHeraActionVo(jobId).getSource().getScript();
        String actionDate = history.getActionId().substring(0, 12) + "00";
        if (StringUtils.isNotBlank(actionDate) && actionDate.length() == 14) {
            script = RenderHierarchyProperties.render(script, actionDate);
        }
        if (jobBean.getHeraActionVo().getRunType().equals(JobRunTypeEnum.Shell)
                || jobBean.getHeraActionVo().getRunType().equals(JobRunTypeEnum.Hive)
                || jobBean.getHeraActionVo().getRunType().equals(JobRunTypeEnum.Spark)) {
            script = resolveScriptResource(resource, script, applicationContext);
        }
        jobContext.setResources(resource);
        if (actionDate != null && actionDate.length() == 14) {
            script = replace(jobContext.getProperties().getAllProperties(actionDate), script);
        } else {
            script = replace(jobContext.getProperties().getAllProperties(), script);
        }

        hierarchyProperties.setProperty(RunningJobKeyConstant.JOB_SCRIPT, script);

        List<Job> pres = new ArrayList<>();
        pres.add(new DownLoadJob(jobContext));
        List<Job> posts = new ArrayList<>();

        Job core = null;
        if (jobBean.getHeraActionVo().getRunType() == JobRunTypeEnum.Shell) {
            core = new HadoopShellJob(jobContext);
        } else if (jobBean.getHeraActionVo().getRunType() == JobRunTypeEnum.Hive) {
            core = new HiveJob(jobContext, applicationContext);
        } else if (jobBean.getHeraActionVo().getRunType() == JobRunTypeEnum.Spark) {
            core = new SparkJob(jobContext, applicationContext);
        } else if (jobBean.getHeraActionVo().getRunType() == JobRunTypeEnum.Spark2) {
            core = new Spark2Job(jobContext, applicationContext);
        }
        return new ProcessJobContainer(jobContext, pres, posts, core, applicationContext);

    }

    private static List<Job> parseJobs(JobContext jobContext, ApplicationContext applicationContext, HeraJobBean jobBean,
                                       List<Processor> processors, HeraJobHistoryVo history, String workDir) {
        List<Job> jobs = new ArrayList<>();
        Map<String, String> map = jobContext.getProperties().getAllProperties();
        Map<String, String> varMap = new HashMap<>(16);
        try {
            for (String key : map.keySet()) {
                String value = map.get(key);

                if (StringUtils.isBlank(value)) {
                    if (history.getStatisticsEndTime() != null && history.getTimezone() != null) {
                        value = value.replace("${j_set}", history.getStatisticsEndTime().toString());
                        value = value.replace("${j_est}", DateUtil.string2Timestamp(history.getStatisticsEndTime().toString(), history.getTimezone()) / 1000 + "");
                        varMap.put(key, value);
                    }
                }
            }
        } catch (ParseException e) {
            log.error("parse end time error");
        }
        for (Processor processor : processors) {
            String config = processor.getConfig();
            if (StringUtils.isNotBlank(config)) {
                for (String key : map.keySet()) {
                    String old = "";
                    do {
                        old = config;
                        String value = varMap.get(key).replace("\"", "\\\"");
                        config = config.replace(key, value);

                    } while (!old.equals(config));
                }
                processor.parse(config);
            }
            if (processor instanceof DownProcessor) {
                jobs.add(new DownLoadJob(jobContext));
            } else if (processor instanceof JobProcessor) {
                Integer depth = (Integer) jobContext.getData("depth");
                if (depth == null) {
                    depth = 0;
                }
                if (depth < 2) {
                    JobProcessor jobProcessor = (JobProcessor) processor;
                    Map<String, String> configs = jobProcessor.getKvConfig();
                    for (String key : configs.keySet()) {
                        if (configs.get(key) != null) {
                            jobBean.getHeraActionVo().getConfigs().put(key, map.get(key));
                        }
                    }
                    File directory = new File(workDir + File.separator + "job-processor-" + jobProcessor.getJobId());
                    if (!directory.exists()) {
                        directory.mkdirs();
                    }
                    JobContext subJobContext = new JobContext(jobContext.getRunType());
                    subJobContext.putData("depth", ++depth);
                    Job job = createScheduleJob(subJobContext, jobBean, history, directory.getAbsolutePath(), applicationContext);
                    jobs.add(job);
                } else {
                    jobContext.getHeraJobHistory().getLog().appendHera("递归的JobProcessor处理单元深度过大，停止递归");
                }
            }
        }
        return jobs;
    }


    public static String replace(Map<String, String> allProperties, String script) {
        if (script == null) {
            return null;
        }
        Map<String, String> varMap = new HashMap<>(16);
        for (String key : allProperties.keySet()) {
            if (allProperties.get(key) != null) {
                varMap.put("${" + key + "}", allProperties.get(key));
            }
        }
        for (String key : varMap.keySet()) {
            String old = "";
            do {
                old = script;
                script = script.replace(key, varMap.get(key));
            } while (!old.equals(script));
        }
        return script;
    }

    /**
     * 解析脚本中download开头的脚本，解析后存储在jobContext的resources中，在生成ProcessJobContainer时，根据属性生成preProcess,postProcess
     *
     * @param resources
     * @param script
     * @param applicationContext
     * @return
     */
    public static String resolveScriptResource(List<Map<String, String>> resources, String script, ApplicationContext applicationContext) {
        Matcher matcher = pattern.matcher(script);
        while (matcher.find()) {
            String group = matcher.group();
            group = group.substring(group.indexOf("[") + 1, group.indexOf("]"));
            String[] url = StringUtils.split(group, " ");
            String uri = null;
            String name = null;
            if (url.length == 1) {
                uri = url[0];
                log.warn("can not found download name,will use default name,{}", group);
            } else if (url.length == 2) {
                uri = url[0];
                name = url[1];
            }
            Map<String, String> map = new HashMap<>(2);
            boolean exist = false;
            for (Map<String, String> env : resources) {
                if (env.get("name").equals(name)) {
                    exist = true;
                    break;
                }
            }
            if (!exist) {
                map.put("uri", uri);
                map.put("name", name);
                resources.add(map);
            }
        }
        script = matcher.replaceAll("");
        return script;
    }


}
