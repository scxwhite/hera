package com.dfire.controller;

import com.dfire.common.constants.Constants;
import com.dfire.common.entity.HeraRerun;
import com.dfire.common.entity.form.HeraRerunForm;
import com.dfire.common.entity.model.JsonResponse;
import com.dfire.common.entity.model.TablePageForm;
import com.dfire.common.entity.model.TableResponse;
import com.dfire.common.entity.vo.HeraRerunVo;
import com.dfire.common.service.HeraJobService;
import com.dfire.common.service.HeraRerunService;
import com.dfire.common.util.ActionUtil;
import com.dfire.common.util.Pair;
import com.dfire.config.RunAuth;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;

/**
 * desc:
 *
 * @author scx
 * @create 2019/11/25
 */
@RequestMapping("/rerun/")
@Controller
public class HeraRerunController extends BaseHeraController {


    @Autowired
    private HeraRerunService heraRerunService;

    @Autowired
    @Qualifier("heraJobMemoryService")
    private HeraJobService heraJobService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public TableResponse list(TablePageForm pageForm) {
        Pair<Integer, List<HeraRerunVo>> res = heraRerunService.findByPage(pageForm);
        return new TableResponse(res.fst(), 0, res.snd());
    }

    @RequestMapping(value = "/status", method = RequestMethod.PUT)
    @ResponseBody
    public JsonResponse update(Integer id, Integer isEnd) {

        if (isEnd == 0) {
            HeraRerunVo heraRerun = heraRerunService.findVoById(id);


            if (Integer.parseInt(heraRerun.getExtra().get(Constants.ACTION_ALL_NUM)) <= Integer.parseInt(heraRerun.getExtra().get(Constants.ACTION_PROCESS_NUM))) {
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
    public JsonResponse add(Integer jobId, HeraRerunForm rerunForm) {

        if (StringUtils.isBlank(rerunForm.getName()) || rerunForm.getName().length() < 4) {
            return new JsonResponse(false, "重跑名称不可少，且最短4个字");
        }

        if (heraJobService.findMemById(jobId) == null) {
            return new JsonResponse(false, "任务ID不存在");
        }

        Integer count = heraRerunService.findCntByJob(rerunForm.getJobId(), 0);


        if (count > 0) {
            return new JsonResponse(false, "已经存在正在执行的重跑任务，请等待重跑完毕或者手动停止后新建 ");
        }

        boolean res;
        return new JsonResponse(res = heraRerunService.add(HeraRerunVo.builder()
                .jobId(rerunForm.getJobId())
                .name(rerunForm.getName())
                .startTime(rerunForm.getStartTime())
                .endTime(rerunForm.getEndTime())
                .gmtCreate(ActionUtil.getTodayString())
                .ssoName(getSsoName())
                .extra(Collections.singletonMap(Constants.RERUN_THREAD, rerunForm.getThreads()))
                .build()), res ? "添加成功" : "添加失败");
    }


}
