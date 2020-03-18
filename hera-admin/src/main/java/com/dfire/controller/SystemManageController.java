package com.dfire.controller;

import com.dfire.common.constants.Constants;
import com.dfire.common.entity.HeraHostRelation;
import com.dfire.common.entity.HeraJobMonitor;
import com.dfire.common.entity.model.JsonResponse;
import com.dfire.common.entity.model.TableResponse;
import com.dfire.common.entity.vo.HeraActionVo;
import com.dfire.common.entity.vo.HeraJobMonitorVo;
import com.dfire.common.entity.vo.HeraSsoVo;
import com.dfire.common.exception.NoPermissionException;
import com.dfire.common.service.HeraHostRelationService;
import com.dfire.common.service.HeraJobActionService;
import com.dfire.common.service.HeraJobMonitorService;
import com.dfire.common.service.HeraSsoService;
import com.dfire.config.AdminCheck;
import com.dfire.core.netty.worker.WorkClient;
import com.dfire.monitor.service.JobManageService;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;
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

    @Autowired
    private HeraJobMonitorService heraJobMonitorService;

    @Autowired
    private HeraSsoService heraSsoService;

    @RequestMapping("/userManage")
    @AdminCheck
    public String userManage() throws NoPermissionException {
        return "systemManage/userManage.index";
    }

    @RequestMapping("/workManage")
    @AdminCheck
    public String workManage() throws NoPermissionException {
        return "systemManage/workManage.index";
    }


    @RequestMapping("/hostGroupManage")
    @AdminCheck
    public String hostGroupManage() throws NoPermissionException {
        return "systemManage/hostGroupManage.index";
    }

    @RequestMapping("/jobMonitor")
    @AdminCheck
    public String jobMonitor() throws NoPermissionException {
        return "systemManage/jobMonitor.index";
    }


    @RequestMapping("/jobDetail")
    public String jobManage() {
        return "jobManage/jobDetail.index";
    }


    @RequestMapping("/jobInstLog")
    public String jobInstLog() {
        return "jobManage/jobInstLog.index";
    }

    @RequestMapping("/rerun")
    public String jobRerun() {
        return "jobManage/rerun.index";
    }

    @RequestMapping("/jobSearch")
    public String jobSearch() {
        return "jobManage/jobSearch.index";
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
    @AdminCheck
    public TableResponse workManageList() {
        List<HeraHostRelation> hostRelations = heraHostRelationService.getAll();
        if (hostRelations == null) {
            return new TableResponse(-1, "查询失败");
        }
        return new TableResponse(hostRelations.size(), 0, hostRelations);
    }

    @RequestMapping(value = "/workManage/add", method = RequestMethod.POST)
    @ResponseBody
    @AdminCheck
    public JsonResponse workManageAdd(HeraHostRelation heraHostRelation) {
        int insert = heraHostRelationService.insert(heraHostRelation);
        if (insert > 0) {
            return new JsonResponse(true, "插入成功");
        }
        return new JsonResponse(false, "插入失败");

    }

    @RequestMapping(value = "/workManage/del", method = RequestMethod.POST)
    @ResponseBody
    @AdminCheck
    public JsonResponse workManageDel(Integer id) {
        int delete = heraHostRelationService.delete(id);
        if (delete > 0) {
            return new JsonResponse(true, "删除成功");
        }
        return new JsonResponse(false, "删除失败");
    }

    @RequestMapping(value = "/workManage/update", method = RequestMethod.POST)
    @ResponseBody
    @AdminCheck
    public JsonResponse workManageUpdate(HeraHostRelation heraHostRelation) {
        int update = heraHostRelationService.update(heraHostRelation);
        if (update > 0) {
            return new JsonResponse(true, "更新成功");
        }
        return new JsonResponse(false, "更新失败");

    }


    @GetMapping(value = "/jobMonitor/list")
    @ResponseBody
    public TableResponse jobMonitorList() {
        List<HeraJobMonitorVo> monitors = heraJobMonitorService.findAllVo();
        if (monitors == null || monitors.size() == 0) {
            return new TableResponse(-1, "无监控任务");
        }
        Map<String, HeraSsoVo> cacheSso = new HashMap<>();
        monitors.forEach(monitor -> {
            if (!StringUtils.isBlank(monitor.getUserIds())) {
                List<HeraSsoVo> ssoVos = new ArrayList<>();
                Arrays.stream(monitor.getUserIds().split(Constants.COMMA)).filter(StringUtils::isNotBlank).distinct().forEach(id -> {
                    HeraSsoVo ssoVo = cacheSso.get(id);
                    if (ssoVo == null) {
                        ssoVo = heraSsoService.findSsoVoById(Integer.parseInt(id));
                        cacheSso.put(id, ssoVo);
                    }
                    ssoVos.add(ssoVo);
                });
                monitor.setMonitors(ssoVos);
            } else {
                monitor.setMonitors(new ArrayList<>(0));
            }
            Optional.ofNullable(monitor.getUserIds()).ifPresent(userIds -> {
                if (userIds.endsWith(Constants.COMMA)) {
                    monitor.setUserIds(userIds.substring(0, userIds.length() - 1));
                }
            });
        });
        cacheSso.clear();
        return new TableResponse(monitors.size(), 0, monitors);
    }

    @PostMapping(value = "/jobMonitor/add")
    @ResponseBody
    @AdminCheck
    public JsonResponse jobMonitorAdd(Integer jobId, String monitors) {
        HeraJobMonitor monitor = heraJobMonitorService.findByJobId(jobId);
        if (monitor != null) {
            return new JsonResponse(false, "该监控任务已经存在,请直接编辑该任务");
        }
        boolean res = heraJobMonitorService.addMonitor(monitors, jobId);
        return new JsonResponse(res, res ? "添加监控成功" : "添加监控失败");
    }

    @PostMapping(value = "/jobMonitor/update")
    @ResponseBody
    @AdminCheck
    public JsonResponse jobMonitorUpdate(Integer jobId, String monitors) {
        boolean res = heraJobMonitorService.updateMonitor(monitors, jobId);
        return new JsonResponse(res, res ? "添加监控成功" : "添加监控失败");
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
    public JsonResponse getJobQueueInfo() throws InterruptedException, ExecutionException, InvalidProtocolBufferException {
        return new JsonResponse(true, workClient.getJobQueueInfoFromWeb());

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
    public JsonResponse getAllWorkInfo() throws InterruptedException, ExecutionException, InvalidProtocolBufferException {
        return new JsonResponse(true, workClient.getAllWorkInfo());
    }


    @RequestMapping(value = "/isAdmin", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse checkAdmin() {
        return new JsonResponse(true, isAdmin());
    }


}
