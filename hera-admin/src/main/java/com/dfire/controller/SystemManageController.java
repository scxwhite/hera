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
import com.dfire.protocol.JobExecuteKind;
import com.google.protobuf.InvalidProtocolBufferException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:52 2018/1/13
 * @desc 系统管理
 */
@Controller
@Api("系统操作接口")
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


    @GetMapping("/userManage")
    @AdminCheck
    public String userManage() throws NoPermissionException {
        return "systemManage/userManage.index";
    }

    @GetMapping("/basicManage")
    @AdminCheck
    public String basicManage() throws NoPermissionException {
        return "systemManage/basicManage.index";
    }

    @RequestMapping("/workManage")
    @GetMapping
    public String workManage() throws NoPermissionException {
        return "systemManage/workManage.index";
    }


    @GetMapping("/hostGroupManage")
    @AdminCheck
    public String hostGroupManage() throws NoPermissionException {
        return "systemManage/hostGroupManage.index";
    }

    @GetMapping("/jobMonitor")
    @AdminCheck
    public String jobMonitor() throws NoPermissionException {
        return "systemManage/jobMonitor.index";
    }


    @GetMapping("/jobDetail")
    public String jobManage() {
        return "jobManage/jobDetail.index";
    }


    @GetMapping("/rerun")
    public String jobRerun() {
        return "jobManage/rerun.index";
    }

    @GetMapping("/jobSearch")
    public String jobSearch() {
        return "jobManage/jobSearch.index";
    }


    @GetMapping("/jobDag")
    public String jobDag() {
        return "jobManage/jobDag.index";
    }

    @GetMapping("/machineInfo")
    public String machineInfo() {
        return "machineInfo";
    }

    @RequestMapping(value = "/workManage/list", method = RequestMethod.GET)
    @ResponseBody
    @AdminCheck
    @ApiOperation("机器组关系列表查询")
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
    @ApiOperation("机器组关系添加")
    public JsonResponse workManageAdd(@ApiParam(value = "机器组管理对象",required = true) HeraHostRelation heraHostRelation) {
        int insert = heraHostRelationService.insert(heraHostRelation);
        if (insert > 0) {
            return new JsonResponse(true, "插入成功");
        }
        return new JsonResponse(false, "插入失败");

    }

    @RequestMapping(value = "/workManage/del", method = RequestMethod.POST)
    @ResponseBody
    @AdminCheck
    @ApiOperation("机器组关系删除")
    public JsonResponse workManageDel(@ApiParam(value = "机器组关系id",required = true)Integer id) {
        int delete = heraHostRelationService.delete(id);
        if (delete > 0) {
            return new JsonResponse(true, "删除成功");
        }
        return new JsonResponse(false, "删除失败");
    }

    @RequestMapping(value = "/workManage/update", method = RequestMethod.POST)
    @ResponseBody
    @AdminCheck
    @ApiOperation("机器组关系更新")
    public JsonResponse workManageUpdate(@ApiParam(value = "机器组管理对象",required = true)HeraHostRelation heraHostRelation) {
        int update = heraHostRelationService.update(heraHostRelation);
        if (update > 0) {
            return new JsonResponse(true, "更新成功");
        }
        return new JsonResponse(false, "更新失败");

    }


    @GetMapping(value = "/jobMonitor/list")
    @ResponseBody
    @ApiOperation("任务监控列表查询")
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
    @ApiOperation("任务监控人添加接口")
    public JsonResponse jobMonitorAdd(@ApiParam(value = "任务id",required = true)Integer jobId,
                                      @ApiParam(value = "监控人hera_sso的id集合,多个,分割",required = true)String monitors) {
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
    @ApiOperation("任务监控人更新接口")
    public JsonResponse jobMonitorUpdate(@ApiParam(value = "任务id",required = true)Integer jobId,
                                         @ApiParam(value = "监控人hera_sso的id集合,多个,分割",required = true)String monitors) {
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
    @ApiOperation("首页任务运行top10")
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
    @ApiOperation("今日所有任务状态，初始化首页饼图")
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
    @ApiOperation("今日任务详情明细，初始化曲线图")
    public JsonResponse findAllJobStatusDetail() {
        return jobManageService.findAllJobStatusDetail();
    }


    @RequestMapping(value = "/homePage/getJobQueueInfo", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation("获取任务在work/master上的等待/执行队列及监控信息")
    public JsonResponse getJobQueueInfo() throws InterruptedException, ExecutionException, InvalidProtocolBufferException, TimeoutException {
        return new JsonResponse(true, workClient.getJobQueueInfoFromWeb());

    }


    @RequestMapping(value = "/homePage/getNotRunJob", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation("查询未执行的任务")
    public JsonResponse getNotRunJob() {
        List<HeraActionVo> scheduleJob = heraJobActionService.getNotRunScheduleJob();
        return new JsonResponse(true, "查询成功", scheduleJob);
    }

    @RequestMapping(value = "/homePage/getFailJob", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation("查询最后一次调度失败的任务")
    public JsonResponse getScheduleFailJob() {
        List<HeraActionVo> failedJob = heraJobActionService.getFailedJob();
        return new JsonResponse(true, "查询成功", failedJob);
    }

    @RequestMapping(value = "/homePage/getAllWorkInfo", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation("查询所有的机器监控信息")
    public JsonResponse getAllWorkInfo() throws InterruptedException, ExecutionException, InvalidProtocolBufferException, TimeoutException {
        return new JsonResponse(true, workClient.getAllWorkInfo());
    }


    @RequestMapping(value = "/isAdmin", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation("是否为admin用户查询")
    public JsonResponse checkAdmin() {
        return new JsonResponse(true, isAdmin());
    }


    @RequestMapping(value = "/admin/generateAllVersion", method = RequestMethod.PUT)
    @ResponseBody
    @AdminCheck
    @ApiOperation("全量版本生成接口")
    public JsonResponse generateAllVersion() throws ExecutionException, InterruptedException, TimeoutException {
        return new JsonResponse(true, workClient.generateActionFromWeb(JobExecuteKind.ExecuteKind.ManualKind, Constants.ALL_JOB_ID));
    }

    @RequestMapping(value = "admin/updateWork", method = RequestMethod.PUT)
    @ResponseBody
    @AdminCheck
    @ApiOperation("更新work信息触发接口")
    public JsonResponse updateWork() throws ExecutionException, InterruptedException, TimeoutException {
        return new JsonResponse(true, workClient.updateWorkFromWeb());
    }
}
