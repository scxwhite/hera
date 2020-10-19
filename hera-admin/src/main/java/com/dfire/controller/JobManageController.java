package com.dfire.controller;

import com.alibaba.fastjson.JSONObject;
import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.model.JsonResponse;
import com.dfire.common.service.HeraJobService;
import com.dfire.common.util.StringUtil;
import com.dfire.form.JobSearchForm;
import com.dfire.monitor.service.JobManageService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * desc:
 *
 * @author scx
 * @create 2019/07/10
 */
@Controller
@RequestMapping("job/")
public class JobManageController extends BaseHeraController {


    @Autowired
    private JobManageService jobManageService;

    @Autowired
    @Qualifier("heraJobMemoryService")
    private HeraJobService heraJobService;

    /**
     * 今日任务详情
     *
     * @param status 任务状态
     * @return 历史结果
     */
    @RequestMapping(value = "history", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation("任务执行详情")
    public JsonResponse findJobHistoryByStatus(@RequestParam("status") @ApiParam(value = "任务状态", required = true) String status
            , @RequestParam("begindt") @ApiParam(value = "开始日期", required = true) String begindt
            , @RequestParam("enddt") @ApiParam(value = "结束日志", required = true) String enddt) {
        return jobManageService.findJobHistoryByStatus(status, begindt, enddt);
    }

    @RequestMapping(value = "search", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation("任务搜索")
    public JsonResponse jobSearch(@ApiParam(value = "任务搜索对象", required = true) JobSearchForm searchForm) {
        List<HeraJob> jobList = heraJobService.getAll();

        //全部小写处理
        searchForm.setName(getLowerCase(searchForm.getName()));
        searchForm.setScript(getLowerCase(searchForm.getScript()));
        searchForm.setDescription(getLowerCase(searchForm.getDescription()));
        searchForm.setConfig(getLowerCase(searchForm.getConfig()));
        searchForm.setRunType(getLowerCase(searchForm.getRunType()));

        return new JsonResponse(true, jobList.parallelStream()
                .filter(job -> Optional.ofNullable(searchForm.getAuto())
                        .map(auto -> job.getAuto().equals(auto)).orElse(true)
                        && Optional.ofNullable(searchForm.getRunType())
                        .map(runTypeEnum -> runTypeEnum.equals(job.getRunType())).orElse(true)
                        && Optional.ofNullable(searchForm.getName())
                        .map(name -> job.getName().toLowerCase().contains(name))
                        .orElse(true)
                        && Optional.ofNullable(searchForm.getDescription())
                        .map(desc -> StringUtils.isNotBlank(job.getDescription())
                                && job.getDescription().toLowerCase().contains(searchForm.getDescription()))
                        .orElse(true)
                        && Optional.ofNullable(searchForm.getConfig())
                        .map(confName -> StringUtils.isNotBlank(job.getConfigs()) &&
                                job.getConfigs().toLowerCase().contains(confName))
                        .orElse(true)
                        && Optional.ofNullable(searchForm.getScript())
                        .map(script -> StringUtils.isNotBlank(job.getScript())
                                && job.getScript().toLowerCase().contains(script)).orElse(true))
                .map(job -> {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", job.getId());
                    jsonObject.put("name", markAsRed(job.getName(), searchForm.getName()));
                    Map<String, String> configMap = StringUtil.convertStringToMap(markAsRed(job.getConfigs(), searchForm.getConfig()));
                    jsonObject.put("config", mapToString(configMap));
                    jsonObject.put("configLine", configMap.size());
                    if (StringUtils.isBlank(job.getScript())) {
                        jsonObject.put("script", "");
                    } else {
                        Pair<String, Integer> pair = replaceLine(markAsRed(job.getScript(), searchForm.getScript()));
                        jsonObject.put("script", pair.getLeft());
                        jsonObject.put("scriptLine", pair.getRight());
                    }
                    jsonObject.put("runType", job.getRunType());
                    jsonObject.put("description", markAsRed(job.getDescription(), searchForm.getDescription()));
                    return jsonObject;
                })
                .collect(Collectors.toList()));
    }


    private String mapToString(Map<String, String> map) {
        StringBuilder builder = new StringBuilder();
        map.forEach((key, value) -> {
            if (value.toLowerCase().contains("password")) {
                value = "******";
            }
            builder.append(key).append("=").append(value).append("<br>");
        });
        return builder.toString();
    }

    private Pair<String, Integer> replaceLine(String script) {
        Matcher pattern = Pattern.compile("\n+").matcher(script);
        StringBuilder builder = new StringBuilder();
        int start = 0, end = script.length(), line = 1;
        while (pattern.find()) {
            builder.append(script, start, pattern.start());
            builder.append("<br>");
            start = pattern.end();
            line++;
        }
        builder.append(script, start, end);
        return new MutablePair<>(builder.toString(), line);
    }


    private String markAsRed(String source, String word) {
        if (StringUtils.isBlank(word)) {
            return source;
        }
        Matcher matcher = Pattern.compile(word, Pattern.CASE_INSENSITIVE).matcher(source);
        StringBuilder builder = new StringBuilder();
        int start = 0, end = source.length();
        while (matcher.find()) {
            builder.append(source, start, matcher.start());
            builder.append("<font color='").append("red'>").append(matcher.group()).append("</font>");
            start = matcher.end();
        }

        builder.append(source, start, end);
        return builder.toString();
    }


    private String getLowerCase(String val) {
        if (StringUtils.isBlank(val)) {
            return null;
        }
        return val.toLowerCase();
    }


}
