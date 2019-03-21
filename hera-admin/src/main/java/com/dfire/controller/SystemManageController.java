package com.dfire.controller;

import com.dfire.common.entity.HeraHostRelation;
import com.dfire.common.entity.model.JsonResponse;
import com.dfire.common.entity.model.TableResponse;
import com.dfire.common.entity.vo.HeraActionVo;
import com.dfire.common.service.HeraHostRelationService;
import com.dfire.common.service.HeraJobActionService;
import com.dfire.config.HeraGlobalEnvironment;
import com.dfire.core.netty.worker.WorkClient;
import com.dfire.logs.ErrorLog;
import com.dfire.monitor.service.JobManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.WebAsyncTask;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:52 2018/1/13
 * @desc 系统管理
 */
@Controller
public class SystemManageController extends BaseHeraController {

    @Autowired
    private JobManageService jobManageService;

    @Autowired
    private HeraJobActionService heraJobActionService;

    @Autowired
    private HeraHostRelationService heraHostRelationService;

    @Autowired
    private WorkClient workClient;

    @RequestMapping("/userManage")
    public String userManage() {
        if (checkAdmin()) {
            return "systemManage/userManage.index";
        }
        return "home";
    }

    @RequestMapping("/workManage")
    public String workManage() {
        if (checkAdmin()) {
            return "systemManage/workManage.index";
        }
        return "home";
    }

    @RequestMapping("/hostGroupManage")
    public String hostGroupManage() {
        if (checkAdmin()) {
            return "systemManage/hostGroupManage.index";
        }
        return "home";
    }

    @RequestMapping("/jobDetail")
    public String jobManage() {
        return "jobManage/jobDetail.index";
    }

    @RequestMapping("/jobDag")
    public String jobDag() {
        return "jobManage/jobDag.index";
    }

    @RequestMapping("/machineInfo")
    public String machineInfo() {
        return "machineInfo";
    }

    @RequestMapping(value = "/workManage/list", method = RequestMethod.GET)
    @ResponseBody
    public TableResponse<List<HeraHostRelation>> workManageList() {
        List<HeraHostRelation> hostRelations = heraHostRelationService.getAll();
        if (hostRelations == null) {
            return new TableResponse<>(-1, "查询失败");
        }
        return new TableResponse<>(hostRelations.size(), 0, hostRelations);
    }

    @RequestMapping(value = "/workManage/add", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse workManageAdd(HeraHostRelation heraHostRelation) {
        int insert = heraHostRelationService.insert(heraHostRelation);
        if (insert > 0) {
            return new JsonResponse(true, "插入成功");
        }
        return new JsonResponse(false, "插入失败");

    }

    @RequestMapping(value = "/workManage/del", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse workManageDel(Integer id) {
        int delete = heraHostRelationService.delete(id);
        if (delete > 0) {
            return new JsonResponse(true, "删除成功");
        }
        return new JsonResponse(false, "删除失败");

    }

    @RequestMapping(value = "/workManage/update", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse workManageUpdate(HeraHostRelation heraHostRelation) {
        int update = heraHostRelationService.update(heraHostRelation);
        if (update > 0) {
            return new JsonResponse(true, "更新成功");
        }
        return new JsonResponse(false, "更新失败");

    }

    /**
     * 任务管理页面今日任务详情
     *
     * @param status
     * @return
     */
    @RequestMapping(value = "/jobManage/findJobHistoryByStatus", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse findJobHistoryByStatus(@RequestParam("status") String status) {
        return jobManageService.findJobHistoryByStatus(status);
    }

    /**
     * 首页任务运行top10
     *
     * @return
     */
    @RequestMapping(value = "/homePage/findJobRunTimeTop10", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse findJobRunTimeTop10() {
        return jobManageService.findJobRunTimeTop10();
    }


    /**
     * 今日所有任务状态，初始化首页饼图
     *
     * @return
     */
    @RequestMapping(value = "/homePage/findAllJobStatus", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse findAllJobStatus() {
        return jobManageService.findAllJobStatus();
    }


    /**
     * 今日所有任务状态明细，线形图初始化
     *
     * @return
     */
    @RequestMapping(value = "/homePage/findAllJobStatusDetail", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse findAllJobStatusDetail() {
        return jobManageService.findAllJobStatusDetail();
    }

    /**
     * 今日所有任务状态明细，线形图初始化
     *
     * @return
     */
    @RequestMapping(value = "/homePage/getJobQueueInfo", method = RequestMethod.GET)
    @ResponseBody
    public WebAsyncTask getJobQueueInfo() {

        return new WebAsyncTask<>(HeraGlobalEnvironment.getRequestTimeout(), () -> {
            try {
                return workClient.getJobQueueInfoFromWeb();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    /**
     * 今日所有任务状态明细，线形图初始化
     *
     * @return
     */
    @RequestMapping(value = "/homePage/getNotRunJob", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse getNotRunJob() {
        List<HeraActionVo> scheduleJob = heraJobActionService.getNotRunScheduleJob();
        return new JsonResponse(true, "查询成功", scheduleJob);
    }

    /**
     * 今日所有任务状态明细，线形图初始化
     *
     * @return
     */
    @RequestMapping(value = "/homePage/getFailJob", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse getScheduleFailJob() {
        List<HeraActionVo> failedJob = heraJobActionService.getFailedJob();
        return new JsonResponse(true, "查询成功", failedJob);
    }

    @RequestMapping(value = "/homePage/getAllWorkInfo", method = RequestMethod.GET)
    @ResponseBody
    public WebAsyncTask getAllWorkInfo() {

        WebAsyncTask webAsyncTask = new WebAsyncTask<>(HeraGlobalEnvironment.getRequestTimeout(), () -> workClient.getAllWorkInfo());

        webAsyncTask.onTimeout(() -> {
            ErrorLog.warn("获取work信息超时");
            return null;
        });
        return webAsyncTask;
    }


    @RequestMapping(value = "/isAdmin", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse isAdmin() {
        boolean isAdmin = checkAdmin();
        return new JsonResponse(true, isAdmin ? "是" : "否", isAdmin);
    }

    private boolean checkAdmin() {
        return getOwner().equals(HeraGlobalEnvironment.getAdmin());
    }


}
