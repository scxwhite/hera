package com.dfire.core.util;

import com.dfire.common.constant.JobRunType;
import com.dfire.common.entity.HeraDebugHistory;
import com.dfire.common.entity.HeraFile;
import com.dfire.common.entity.vo.HeraProfileVo;
import com.dfire.common.service.HeraFileService;
import com.dfire.common.service.HeraProfileService;
import com.dfire.common.util.HierarchyProperties;
import com.dfire.common.util.RenderHierarchyProperties;
import com.dfire.core.job.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午12:13 2018/4/26
 * @desc
 */
public class JobUtils {

    public static final Pattern pattern = Pattern.compile("download\\[(doc|hdfs|http)://.+]");

    public static Job createDebugJob(JobContext jobContext, HeraDebugHistory heraDebugHistory,
                                     String workDir, ApplicationContext applicationContext) {
        jobContext.setDebugHistory(heraDebugHistory);
        jobContext.setWorkDir(workDir);
        //脚本中的变量替换，暂时不做
        HierarchyProperties hierarchyProperties = new HierarchyProperties(new HashMap<>());
        String script = heraDebugHistory.getScript();
        List<Map<String, String>> resources = new ArrayList<>();
        script = resolveScriptResource(resources, script, applicationContext);
        jobContext.setResources(resources);
        hierarchyProperties.setProperty("job.script", script);
        //权限控制判断，暂时不做
        HeraFileService heraFileService = (HeraFileService) applicationContext.getBean("heraFileService");
        String owner = heraFileService.getHeraFile(heraDebugHistory.getId()).getOwner();
        HeraProfileService heraProfileService = (HeraProfileService) applicationContext.getBean("profileService");
        HeraProfileVo heraProfile = heraProfileService.findByOwner(owner);
        if(heraProfile != null && heraProfile.getHadoopConf() != null) {
            for(String key : heraProfile.getHadoopConf().keySet())
            hierarchyProperties.setProperty(key, heraProfile.getHadoopConf().get(key));
        }

        jobContext.setProperties(new RenderHierarchyProperties(hierarchyProperties));
        hierarchyProperties.setProperty("hadoop.mappred.job.hera_id", "hera_debug_" + heraDebugHistory.getId());

        List<Job> pres = new ArrayList<>(1);
        pres.add(new DownLoadJob(jobContext));
        Job core = null;
        if (heraDebugHistory.getRunType().equalsIgnoreCase(JobRunType.Shell.toString())) {
            core = new ShellJob(jobContext);
        } else if(heraDebugHistory.getRunType().equalsIgnoreCase(JobRunType.Hive.toString())) {
            core = new HadoopShellJob(jobContext);
        }
        Job job = new WithProcessJob(jobContext, pres, new ArrayList<>(), core, applicationContext);
        return job;
    }

    private static String resolveScriptResource(List<Map<String, String>> resources, String script, ApplicationContext applicationContext) {
        Matcher matcher = pattern.matcher(script);
        while (matcher.find()) {
            String group = matcher.group();
            group = group.substring(group.indexOf("[") + 1, group.indexOf("]"));
            String[] url = StringUtils.split(group, ".");
            String uri = url[0];
            String name = "";
            String referScript = null;
            String path = uri.substring(uri.lastIndexOf('/') + 1);
            Map<String, String> map = new HashMap<>(2);
            if (uri.startsWith("doc://")) {
                HeraFileService fileService = (HeraFileService) applicationContext.getBean("fileService");
                HeraFile heraFile = fileService.getHeraFile(path);
                name = heraFile.getName();
                referScript = heraFile.getContent();
            }

            if (url.length > 1) {
                name = "";
                for (int i = 0; i < url.length; i++) {
                    if (i > 1) {
                        name += "_";
                    }
                    name += url[i];
                }
            } else if (url.length == 1) {
                if (uri.startsWith("hdfs://")) {
                    if (uri.endsWith("/")) {
                        continue;
                    }
                    name = path;
                }
            }
            boolean exist = false;
            for(Map<String, String> env : resources) {
                if(env.get("name").equals(name)) {
                    exist = true;
                    break;
                }
            }
            if(!exist) {
                map.put("uri", uri);
                map.put("name", name);
                resources.add(map);
                if(uri.startsWith("doc://") && referScript != null) {
                    map.put("hera-doc-" + path, resolveScriptResource(resources, referScript, applicationContext));
                }
            }
        }
        script = matcher.replaceAll("");
        return script;
    }


}
