package com.dfire.controller;

import com.alibaba.fastjson.JSONObject;
import com.dfire.common.constants.Constants;
import com.dfire.common.entity.HeraRerun;
import com.dfire.common.entity.form.HeraRerunForm;
import com.dfire.common.entity.model.JsonResponse;
import com.dfire.common.entity.model.TablePageForm;
import com.dfire.common.entity.model.TableResponse;
import com.dfire.common.entity.vo.HeraRerunVo;
import com.dfire.common.service.HeraJobHistoryService;
import com.dfire.common.service.HeraJobService;
import com.dfire.common.service.HeraRerunService;
import com.dfire.common.util.ActionUtil;
import com.dfire.common.util.Pair;
import com.dfire.config.RunAuth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * desc:
 *
 * @author scx
 * @create 2019/11/25
 */
@RequestMapping("/rerun/")
@Controller
@Api("重跑任务接口")
public class HeraRerunController extends BaseHeraController {


    @Autowired
    private HeraRerunService heraRerunService;

    @Autowired
    @Qualifier("heraJobMemoryService")
    private HeraJobService heraJobService;

    @Autowired
    private HeraJobHistoryService heraJobHistoryService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation("任务重跑列表")
    public TableResponse list(@ApiParam(value = "分页参数", required = true) TablePageForm pageForm,
                              @ApiParam(value = "状态：-1所有，0开启，1结束", required = true) Integer status) {
        Pair<Integer, List<HeraRerunVo>> res = heraRerunService.findByPage(pageForm, status);
        return new TableResponse(res.fst(), 0, res.snd());
    }

    @RequestMapping(value = "/failed", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation("重跑失败列表查询")
    public TableResponse failedList(@ApiParam(value = "分页参数", required = true) TablePageForm pageForm, @ApiParam(value = "重跑id", required = true) Integer rerunId) {
        HeraRerunVo heraRerun = heraRerunService.findVoById(rerunId);
        Pair<Integer, List<JSONObject>> res = heraJobHistoryService.findRerunFailed(heraRerun.getJobId()
                , String.valueOf(rerunId)
                , ActionUtil.getActionByDateStr(heraRerun.getStartTime()) - 1, pageForm);
        return new TableResponse(res.fst(), 0, res.snd());
    }

    @RequestMapping(value = "/failed", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation("重跑所有失败记录")
    public JsonResponse rerunFailed(@ApiParam(value = "重跑id", required = true) Integer rerunId) {
        HeraRerunVo lastRerun = heraRerunService.findVoById(rerunId);
        String rerunFailedCount = lastRerun.getExtra().getOrDefault(Constants.ACTION_FAILED_NUM, "0");
        if ("0".equals(rerunFailedCount)) {
            return new JsonResponse(false, "无失败记录，无需重跑");
        }
        Integer realFailedCount = heraJobHistoryService.findRerunFailedCount(lastRerun.getJobId(), String.valueOf(lastRerun.getId()), ActionUtil.getActionByDateStr(lastRerun.getStartTime()) - 1);

        if (realFailedCount != Integer.parseInt(rerunFailedCount)) {
            return new JsonResponse(false, "执行记录可能被删除，无法重跑失败");
        }

        HeraRerunVo newRerun = new HeraRerunVo();
        newRerun.setStartTime(lastRerun.getStartTime());
        newRerun.setEndTime(lastRerun.getEndTime());
        newRerun.setGmtCreate(ActionUtil.getTodayString());
        newRerun.setName("重跑失败-" + lastRerun.getName());
        newRerun.setJobId(lastRerun.getJobId());
        newRerun.setSsoName(lastRerun.getSsoName());
        Map<String, String> extra = new HashMap<>(2);
        extra.put(Constants.RERUN_THREAD, lastRerun.getExtra().getOrDefault(Constants.RERUN_THREAD, "1"));
        extra.put(Constants.RERUN_FAILED, Boolean.TRUE.toString());
        extra.put(Constants.ACTION_DONE, Boolean.TRUE.toString());
        extra.put(Constants.ACTION_PROCESS_NUM, "0");
        extra.put(Constants.ACTION_ALL_NUM, rerunFailedCount);
        extra.put(Constants.LAST_RERUN_ID, String.valueOf(lastRerun.getId()));
        newRerun.setExtra(extra);
        heraRerunService.add(newRerun);
        return new JsonResponse(true, "添加重跑任务成功");
    }


    @RequestMapping(value = "/status", method = RequestMethod.PUT)
    @ResponseBody
    @ApiOperation("重跑状态更改")
    public JsonResponse update(@ApiParam(value = "重跑id", required = true) Integer id,
                               @ApiParam(value = "状态：0开启，1结束", required = true) Integer isEnd) {

        if (isEnd == 0) {
            HeraRerunVo heraRerun = heraRerunService.findVoById(id);

            if (Integer.parseInt(heraRerun.getExtra().getOrDefault(Constants.ACTION_ALL_NUM, String.valueOf(Integer.MAX_VALUE))) <= Integer.parseInt(heraRerun.getExtra().getOrDefault(Constants.ACTION_PROCESS_NUM, "0"))) {
                return new JsonResponse(false, "重跑任务已经结束,无法开启");
            }

            Integer cntByJob = heraRerunService.findCntByJob(heraRerun.getJobId(), 0);

            if (cntByJob > 0) {
                return new JsonResponse(false, "存在正在执行的重跑任务，任务ID[" + heraRerun.getJobId() + "],请先关闭之前的重跑任务");
            }

            if (!isAdmin() && !heraRerun.getSsoName().trim().equals(getSsoName())) {
                return new JsonResponse(false, "无权限关闭该重跑任务,请联系[" + heraRerun.getSsoName() + "]关闭");
            }

        }

        boolean res = heraRerunService.updateById(HeraRerun.builder()
                .id(id)
                .isEnd(isEnd)
                .build());
        return new JsonResponse(res, res ? (isEnd == 0 ? "开启成功" : "关闭成功") : (isEnd == 0 ? "开启失败" : "关闭失败"));
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    @RunAuth
    @Transactional(rollbackFor = Exception.class)
    @ApiOperation("重跑任务添加")
    public JsonResponse add(@ApiParam(value = "任务id", required = false) Integer jobId
            , @ApiParam(value = "重跑json对象，see：HeraRerunForm", required = true) String rerunJson) {

        JSONObject rerunObj = JSONObject.parseObject(rerunJson);

        HeraRerunForm rerunForm = new HeraRerunForm();

        rerunForm.setName(rerunObj.getString("name"));

        rerunForm.setJobId(rerunObj.getInteger("jobId"));

        rerunForm.setThreads(rerunObj.getString("threads"));

        if (StringUtils.isBlank(rerunForm.getName()) || rerunForm.getName().length() < 4) {
            return new JsonResponse(false, "重跑名称不可少，且最短4个字");
        }

        if (rerunForm.getJobId() == null || heraJobService.findMemById(rerunForm.getJobId()) == null) {
            return new JsonResponse(false, "任务ID不存在");
        }
        Map<String, String> timeMap = new HashMap<>(rerunObj.size());


        for (Map.Entry<String, Object> entry : rerunObj.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key.startsWith(Constants.RERUN_START_TIME) || key.startsWith(Constants.RERUN_END_TIME)) {
                if (value == null || StringUtils.isBlank(entry.getValue().toString())) {
                    return new JsonResponse(false, "重跑时间不允许为空");
                }
                timeMap.put(key, String.valueOf(value));
            }

        }
        int dateSize = timeMap.size() / 2;

        for (int i = 0; i < dateSize; i++) {
            String startTime = timeMap.get(Constants.RERUN_START_TIME + i);
            String endTime = timeMap.get(Constants.RERUN_END_TIME + i);
            heraRerunService.add(HeraRerunVo.builder()
                    .jobId(rerunForm.getJobId())
                    .name(rerunForm.getName())
                    .startTime(startTime)
                    .endTime(endTime)
                    .gmtCreate(ActionUtil.getTodayString())
                    .ssoName(getSsoName())
                    .extra(Collections.singletonMap(Constants.RERUN_THREAD, rerunForm.getThreads()))
                    .build());

        }

        return new JsonResponse(true, "添加成功");
    }


}
