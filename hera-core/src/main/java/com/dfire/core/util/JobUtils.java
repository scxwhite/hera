package com.dfire.core.util;

import com.dfire.common.constants.Constants;
import com.dfire.common.constants.RunningJobKeyConstant;
import com.dfire.common.entity.HeraDebugHistory;
import com.dfire.common.entity.model.HeraJobBean;
import com.dfire.common.entity.vo.HeraJobHistoryVo;
import com.dfire.common.entity.vo.HeraProfileVo;
import com.dfire.common.enums.JobRunTypeEnum;
import com.dfire.common.exception.HeraException;
import com.dfire.common.util.BeanConvertUtils;
import com.dfire.common.util.HierarchyProperties;
import com.dfire.common.util.PasswordUtils;
import com.dfire.common.util.RenderHierarchyProperties;
import com.dfire.config.HeraGlobalEnv;
import com.dfire.core.job.*;
import com.dfire.core.netty.worker.WorkContext;
import com.dfire.logs.HeraLog;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

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
public class JobUtils {

    private static final Pattern downloadPatt = Pattern.compile("download\\[.*://.+]");


    private static final Pattern valPatt = Pattern.compile("\\$\\{([^}{$])*\\}");


    public static Job createDebugJob(JobContext jobContext, HeraDebugHistory heraDebugHistory, HeraJobBean heraJobBean,
                                     String workDir, WorkContext workContext) throws HeraException {
        jobContext.setDebugHistory(BeanConvertUtils.convert(heraDebugHistory));
        jobContext.setWorkDir(workDir);
        if (heraJobBean != null) {
            jobContext.setProperties(new RenderHierarchyProperties(heraJobBean.getHierarchyProperties()));
        }

        HierarchyProperties hierarchyProperties = new HierarchyProperties(new HashMap<>());
        String script = heraDebugHistory.getScript();
        List<Map<String, String>> resources = new ArrayList<>();
        script = resolveScriptResource(resources, script);
        script = replace(jobContext.getProperties().getAllProperties(), script);
        script = RenderHierarchyProperties.render(script, null);
        jobContext.setResources(resources);
        hierarchyProperties.setProperty(RunningJobKeyConstant.JOB_SCRIPT, script);

        String owner = workContext.getHeraFileService().findById(heraDebugHistory.getFileId()).getOwner();
        HeraProfileVo heraProfile = workContext.getHeraProfileService().findByOwner(owner);
        if (heraProfile != null && heraProfile.getHadoopConf() != null) {
            for (String key : heraProfile.getHadoopConf().keySet()) {
                hierarchyProperties.setProperty(key, heraProfile.getHadoopConf().get(key));
            }
        }

        jobContext.setProperties(new RenderHierarchyProperties(hierarchyProperties));
        hierarchyProperties.setProperty("hadoop.mappred.job.hera_id", "hera_debug_" + heraDebugHistory.getId());

        List<Job> pres = buildPreJob(jobContext);

        Job core = null;

        if (heraDebugHistory.getRunType().equalsIgnoreCase(JobRunTypeEnum.Shell.toString())) {
            jobContext.putData(RunningJobKeyConstant.JOB_RUN_TYPE, JobRunTypeEnum.Shell.toString());
            core = new HadoopShellJob(jobContext);
        } else if (heraDebugHistory.getRunType().equalsIgnoreCase(JobRunTypeEnum.Hive.toString())) {
            jobContext.putData(RunningJobKeyConstant.JOB_RUN_TYPE, JobRunTypeEnum.Hive.toString());
            core = new HiveJob(jobContext);
        } else if (heraDebugHistory.getRunType().equalsIgnoreCase(JobRunTypeEnum.Spark.toString())) {
            jobContext.putData(RunningJobKeyConstant.JOB_RUN_TYPE, JobRunTypeEnum.Spark.toString());
            core = new SparkJob(jobContext);
        } else if (heraDebugHistory.getRunType().equalsIgnoreCase(JobRunTypeEnum.Spark2.toString())) {
            jobContext.putData(RunningJobKeyConstant.JOB_RUN_TYPE, JobRunTypeEnum.Spark2.toString());
            core = new Spark2Job(jobContext);
        }
        return new ProcessJobContainer(jobContext, pres, new ArrayList<>(), core);
    }


    public static Job createScheduleJob(JobContext jobContext, HeraJobBean jobBean,
                                        HeraJobHistoryVo history, String workDir) throws HeraException {
        jobContext.setHeraJobHistory(history);
        jobContext.setWorkDir(workDir);
        jobContext.getProperties().setProperty("hera.encode", "utf-8");
        HierarchyProperties hierarchyProperties = jobBean.getHierarchyProperties();

        jobContext.setProperties(new RenderHierarchyProperties(hierarchyProperties));
        List<Map<String, String>> resource = jobBean.getHierarchyResources();
        String script = jobBean.getHeraJob().getScript();
        if (jobBean.getHeraJob().getRunType().equals(JobRunTypeEnum.Shell.toString())
                || jobBean.getHeraJob().getRunType().equals(JobRunTypeEnum.Hive.toString())
                || jobBean.getHeraJob().getRunType().equals(JobRunTypeEnum.Spark.toString())) {
            script = resolveScriptResource(resource, script);
        }
        jobContext.setResources(resource);
        script = replace(jobContext.getProperties().getAllProperties(), script);
        script = RenderHierarchyProperties.render(script, String.valueOf(history.getActionId()).substring(0, 12));
        hierarchyProperties.setProperty(RunningJobKeyConstant.JOB_SCRIPT, script);
//        List<Job> pres = buildPreJob(jobContext);
        List<Job> pres = Lists.newArrayList(new DownLoadJob(jobContext), new EmptyJob(jobContext));
        List<Job> posts = new ArrayList<>();
        Job core = null;
        JobRunTypeEnum runType = JobRunTypeEnum.parser(jobBean.getHeraJob().getRunType());
        if (runType == JobRunTypeEnum.Shell) {
            core = new HadoopShellJob(jobContext);
        } else if (runType == JobRunTypeEnum.Hive) {
            core = new HiveJob(jobContext);
        } else if (runType == JobRunTypeEnum.Spark) {
            core = new SparkJob(jobContext);
        } else if (runType == JobRunTypeEnum.Spark2) {
            core = new Spark2Job(jobContext);
        }
        return new ProcessJobContainer(jobContext, pres, posts, core);

    }

    private static List<Job> buildPreJob(JobContext jobContext) {
        List<Job> pres = new ArrayList<>(1);
        pres.add(new DownLoadJob(jobContext));
        return pres;
    }

    private static String replace(Map<String, String> allProperties, String script, boolean dbReplace) {
        if (script == null) {
            return null;
        }
        Map<String, String> varMap = new HashMap<>(allProperties.size());
        if (dbReplace && WorkContext.cacheDBMap != null && WorkContext.cacheDBMap.size() != 0) {
            varMap.putAll(WorkContext.cacheDBMap);
        }
        String key;
        String areaSuffix = HeraGlobalEnv.getArea() + ".";
        for (Map.Entry<String, String> entry : allProperties.entrySet()) {
            key = entry.getKey();
            varMap.put(key, entry.getValue());
            if (key.startsWith(areaSuffix)) {
                varMap.putIfAbsent(key.replaceFirst(areaSuffix, ""), entry.getValue());
            }
            if (key.toLowerCase().startsWith(Constants.SECRET_PREFIX)) {
                varMap.put(key, PasswordUtils.aesDecrypt(entry.getValue()));
            }
        }
        return replaceVal(script, varMap);
    }

    private static String replace(Map<String, String> allProperties, String script) {
        return replace(allProperties, script, true);
    }

    public static String previewScript(Map<String, String> allProperties, String script) {
        return replace(allProperties, script, false);
    }

    /**
     * 解析脚本中download开头的脚本，解析后存储在jobContext的resources中，在生成ProcessJobContainer时，根据属性生成preProcess,postProcess
     *
     * @param resources 资源下载
     * @param script    脚本内容
     * @return
     */
    private static String resolveScriptResource(List<Map<String, String>> resources, String script) {
        Matcher matcher = downloadPatt.matcher(script);
        while (matcher.find()) {
            String group = matcher.group();
            group = group.substring(group.indexOf("[") + 1, group.indexOf("]"));
            String[] url = StringUtils.split(group, " ");
            String uri = null;
            String name = null;
            if (url.length == 1) {
                uri = url[0];
                HeraLog.warn("can not found download name,will use default name,{}", group);
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
            //TODO 脚本中不存在 就不用下载了
            if (!exist) {
                map.put("uri", uri);
                map.put("name", name);
                resources.add(map);
            }
        }
        script = matcher.replaceAll("");
        return script;
    }

    private static String replaceVal(String script, Map<String, String> confs) {
        Matcher matcher = valPatt.matcher(script);
        StringBuilder newScript = new StringBuilder();
        int start = 0, end;

        while (matcher.find()) {
            String var = matcher.group();
            end = matcher.start();
            newScript.append(script, start, end);
            String val;
            while (!(val = confs.getOrDefault(var.substring(2, var.length() - 1), var)).equals(var) && val.matches(valPatt.pattern())) {
                var = val;
            }
            newScript.append(val);
            start = matcher.end();
        }
        newScript.append(script, start, script.length());
        return newScript.toString();
    }

}
