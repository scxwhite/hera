package com.dfire.controller;

import com.dfire.common.constants.Constants;
import com.dfire.common.entity.*;
import com.dfire.common.entity.model.JsonResponse;
import com.dfire.common.entity.model.TablePageForm;
import com.dfire.common.entity.model.TableResponse;
import com.dfire.common.entity.vo.HeraActionVo;
import com.dfire.common.entity.vo.HeraGroupVo;
import com.dfire.common.entity.vo.HeraJobVo;
import com.dfire.common.entity.vo.PageHelperTimeRange;
import com.dfire.common.enums.*;
import com.dfire.common.exception.NoPermissionException;
import com.dfire.common.service.*;
import com.dfire.common.util.ActionUtil;
import com.dfire.common.util.BeanConvertUtils;
import com.dfire.common.util.PasswordUtils;
import com.dfire.common.util.StringUtil;
import com.dfire.common.vo.GroupTaskVo;
import com.dfire.config.HeraGlobalEnv;
import com.dfire.config.RunAuth;
import com.dfire.config.UnCheckLogin;
import com.dfire.core.netty.worker.WorkClient;
import com.dfire.logs.MonitorLog;
import com.dfire.protocol.JobExecuteKind;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:50 2018/1/13
 * @desc 调度中心视图管理器
 */
@Controller
@Api(value = "调度中心")
@RequestMapping("/scheduleCenter")
public class ScheduleCenterController extends BaseHeraController {

    private final HeraJobService heraJobService;
    private final HeraJobActionService heraJobActionService;
    private final HeraGroupService heraGroupService;
    private final HeraJobHistoryService heraJobHistoryService;
    private final HeraJobMonitorService heraJobMonitorService;
    private final HeraUserService heraUserService;
    private final HeraPermissionService heraPermissionService;
    private final WorkClient workClient;
    private final HeraHostGroupService heraHostGroupService;
    private final HeraAreaService heraAreaService;
    private final HeraSsoService heraSsoService;

    public ScheduleCenterController(HeraJobMonitorService heraJobMonitorService, @Qualifier("heraJobMemoryService") HeraJobService heraJobService, HeraJobActionService heraJobActionService, @Qualifier("heraGroupMemoryService") HeraGroupService heraGroupService, HeraJobHistoryService heraJobHistoryService, HeraUserService heraUserService, HeraPermissionService heraPermissionService, WorkClient workClient, HeraHostGroupService heraHostGroupService, HeraAreaService heraAreaService, HeraSsoService heraSsoService) {
        this.heraJobMonitorService = heraJobMonitorService;
        this.heraJobService = heraJobService;
        this.heraJobActionService = heraJobActionService;
        this.heraGroupService = heraGroupService;
        this.heraJobHistoryService = heraJobHistoryService;
        this.heraUserService = heraUserService;
        this.heraPermissionService = heraPermissionService;
        this.workClient = workClient;
        this.heraHostGroupService = heraHostGroupService;
        this.heraAreaService = heraAreaService;
        this.heraSsoService = heraSsoService;
    }


    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "调度中心页面跳转")
    public String login() {
        return "scheduleCenter/scheduleCenter.index";
    }

    @RequestMapping(value = "/init", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "获取任务树,包含我的任务和全部任务两个集合")
    public JsonResponse initJobTree() {
        return new JsonResponse(true, Optional.of(heraJobService.buildJobTree(getOwner(), Integer.parseInt(getSsoId()))).get());
    }

    @RequestMapping(value = "/getJobMessage", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "获取任务信息")
    public JsonResponse getJobMessage(@ApiParam(value = "任务ID", required = true) Integer jobId) {
        HeraJob job = heraJobService.findById(jobId);
        HeraJobVo heraJobVo = BeanConvertUtils.convert(job);
        heraJobVo.setInheritConfig(getInheritConfig(job.getGroupId()));
        HeraJobMonitor monitor = heraJobMonitorService.findByJobId(jobId);
        StringBuilder focusUsers = new StringBuilder("[ ");
        Optional.ofNullable(monitor).ifPresent(m -> {
            if (StringUtils.isNotBlank(m.getUserIds())) {
                String ssoId = getSsoId();
                Arrays.stream(monitor.getUserIds().split(Constants.COMMA)).filter(StringUtils::isNotBlank).distinct().forEach(id -> {
                    if (ssoId.equals(id)) {
                        heraJobVo.setFocus(true);
                        focusUsers.append(Constants.BLANK_SPACE).append(getSsoName());
                    } else {
                        Optional.ofNullable(heraSsoService.findSsoById(Integer.parseInt(id)))
                                .ifPresent(sso -> focusUsers.append(Constants.BLANK_SPACE).append(sso.getName()));
                    }
                });
            }
        });
        focusUsers.append(" ]");
        Optional.ofNullable(heraHostGroupService.findById(job.getHostGroupId()))
                .ifPresent(group -> heraJobVo.setHostGroupName(group.getName()));
        heraJobVo.setUIdS(getuIds(jobId, RunAuthType.JOB));
        heraJobVo.setFocusUser(focusUsers.toString());
        heraJobVo.setAlarmLevel(AlarmLevel.getName(job.getOffset()));
        heraJobVo.setCycle(CycleEnum.parse(job.getCycle()).getDesc());
        configDecry(heraJobVo.getConfigs());
        configDecry(heraJobVo.getInheritConfig());
        //如果无权限，进行变量加密
        if (heraJobVo.getConfigs().keySet().stream().anyMatch(key -> key.toLowerCase().contains(Constants.PASSWORD_WORD))
                || heraJobVo.getInheritConfig().keySet().stream().anyMatch(key -> key.toLowerCase().contains(Constants.PASSWORD_WORD))) {
            try {
                checkPermission(jobId, RunAuthType.JOB);
            } catch (NoPermissionException e) {
                encryption(heraJobVo.getConfigs());
                encryption(heraJobVo.getInheritConfig());
            }
        }
        return new JsonResponse(true, heraJobVo);
    }


    @RunAuth(typeIndex = 1)
    @GetMapping("/checkPermission")
    @ResponseBody
    @ApiOperation(value = "权限检测接口")
    public JsonResponse doAspectAuth(@ApiParam(value = "任务ID", required = true) Integer jobId
            , @ApiParam(value = "检测类型", required = true) RunAuthType type) {
        return new JsonResponse(true, true);
    }


    private void configDecry(Map<String, String> config) {
        Optional.ofNullable(config)
                .ifPresent(cxf -> cxf.entrySet()
                        .stream()
                        .filter(pair -> pair.getKey().toLowerCase().contains(Constants.SECRET_PREFIX))
                        .forEach(entry -> entry.setValue(PasswordUtils.aesDecrypt(entry.getValue()))));
    }


    private void encryption(Map<String, String> config) {
        Optional.ofNullable(config)
                .ifPresent(cxf -> cxf.entrySet()
                        .stream()
                        .filter(pair -> pair.getKey().toLowerCase().contains(Constants.PASSWORD_WORD))
                        .forEach(entry -> entry.setValue("******")));
    }

    /**
     * 组下搜索任务
     *
     * @param groupId  groupId
     * @param status   all:全部;
     * @param pageForm layui table分页参数
     * @return 结果
     */
    @RequestMapping(value = "/getGroupTask", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "某组下搜索任务")
    public TableResponse getGroupTask(@ApiParam(value = "组Id", required = true) String groupId,
                                      @ApiParam(value = "任务状态") String status,
                                      @ApiParam(value = "日期", required = true) String dt,
                                      @ApiParam(value = "分页参数", required = true) TablePageForm pageForm) {

        List<HeraGroup> group = heraGroupService.findDownStreamGroup(StringUtil.getGroupId(groupId));

        Set<Integer> groupSet = group.stream().map(HeraGroup::getId).collect(Collectors.toSet());
        List<HeraJob> jobList = heraJobService.getAll();
        Set<Integer> jobIdSet = jobList.stream().filter(job -> groupSet.contains(job.getGroupId())).map(HeraJob::getId).collect(Collectors.toSet());
        SimpleDateFormat format = new SimpleDateFormat("yyMMdd");
        Calendar calendar = Calendar.getInstance();
        String startDate;
        Date start;
        if (StringUtils.isBlank(dt)) {
            start = new Date();
        } else {
            try {
                start = format.parse(dt);
            } catch (ParseException e) {
                start = new Date();
            }
        }

        calendar.setTime(start);
        startDate = ActionUtil.getFormatterDate("yyyyMMdd", calendar.getTime());
        calendar.add(Calendar.DAY_OF_YEAR, +1);
        String endDate = ActionUtil.getFormatterDate("yyyyMMdd", calendar.getTime());
        List<GroupTaskVo> taskVos = heraJobActionService.findByJobIds(new ArrayList<>(jobIdSet), startDate, endDate, pageForm, status);
        return new TableResponse(pageForm.getCount(), 0, taskVos);
    }

    @RequestMapping(value = "/getGroupMessage", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "获取组信息")
    public JsonResponse getGroupMessage(@ApiParam(value = "组ID", required = true) String groupId) {
        Integer id = StringUtil.getGroupId(groupId);
        HeraGroup group = heraGroupService.findById(id);
        HeraGroupVo groupVo = BeanConvertUtils.convert(group);
        groupVo.setInheritConfig(getInheritConfig(groupVo.getParent()));
        groupVo.setUIdS(getuIds(id, RunAuthType.GROUP));
        configDecry(groupVo.getConfigs());
        configDecry(groupVo.getInheritConfig());
        if (groupVo.getConfigs().keySet().stream().anyMatch(key -> key.toLowerCase().contains(Constants.PASSWORD_WORD))
                || groupVo.getInheritConfig().keySet().stream().anyMatch(key -> key.toLowerCase().contains(Constants.PASSWORD_WORD))) {
            try {
                checkPermission(id, RunAuthType.GROUP);
            } catch (NoPermissionException e) {
                encryption(groupVo.getConfigs());
                encryption(groupVo.getInheritConfig());
            }
        }
        return new JsonResponse(true, groupVo);
    }


    @RequestMapping(value = "/getJobOperator", method = RequestMethod.GET)
    @ResponseBody
    @RunAuth(typeIndex = 1)
    @ApiOperation("获取任务管理员，admin表示目前有权限操作的用户")
    public JsonResponse getJobOperator(@ApiParam(value = "任务/组ID", required = true) String jobId,
                                       @ApiParam(value = "任务类型", required = true) RunAuthType type) {
        Integer groupId = StringUtil.getGroupId(jobId);
        List<HeraPermission> permissions = heraPermissionService.findByTargetId(groupId, type.getName(), 1);
        List<HeraUser> all = heraUserService.findAllName();
        if (all == null || permissions == null) {
            return new JsonResponse(false, "发生错误，请联系管理员");
        }
        Map<String, Object> res = new HashMap<>(2);
        res.put("allUser", all);
        res.put("admin", permissions);
        return new JsonResponse(true, "查询成功", res);
    }

    @RequestMapping(value = "/getJobVersion", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation("获取任务的所有版本")
    public JsonResponse getJobVersion(@ApiParam(value = "任务ID", required = true) Long jobId) {
        return new JsonResponse(true, heraJobActionService.getActionVersionByJobId(jobId)
                .stream()
                .map(id -> HeraActionVo.builder().id(id).build())
                .collect(Collectors.toList()));
    }


    @RequestMapping(value = "/addJob", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation("新增任务")
    @RunAuth(authType = RunAuthType.GROUP, idIndex = 1)
    public JsonResponse addJob(@ApiParam(value = "任务信息", required = true) HeraJob heraJob,
                               @ApiParam(value = "所在组目录", required = true) String parentId) {
        heraJob.setGroupId(StringUtil.getGroupId(parentId));
        heraJob.setHostGroupId(HeraGlobalEnv.defaultWorkerGroup);
        heraJob.setOwner(getOwner());
        heraJob.setScheduleType(JobScheduleTypeEnum.Independent.getType());
        int insert = heraJobService.insert(heraJob);
        if (insert > 0) {
            addJobRecord(heraJob);
            return new JsonResponse(true, String.valueOf(heraJob.getId()));
        } else {
            return new JsonResponse(false, "新增失败");
        }
    }

    @PostMapping(value = "/copyJob")
    @ResponseBody
    @RunAuth(authType = RunAuthType.JOB)
    @ApiOperation("复制任务接口")
    public JsonResponse copyJob(
            @ApiParam(value = "复制的任务ID", required = true) int jobId) {
        HeraJob copyJob = heraJobService.findById(jobId);
        copyJob.setName(copyJob.getName() + "_copy");
        copyJob.setOwner(getOwner());
        copyJob.setScheduleType(JobScheduleTypeEnum.Independent.getType());
        copyJob.setId(0);
        copyJob.setIsValid(0);
        int insert = heraJobService.insert(copyJob);
        if (insert > 0) {
            addJobRecord(copyJob);
            return new JsonResponse(true, String.valueOf(copyJob.getId()));
        } else {
            return new JsonResponse(false, "新增失败");
        }
    }


    private void addJobRecord(HeraJob heraJob) {
        String ssoName = getSsoName();
        String ownerId = getOwnerId();
        MonitorLog.info("{}[{}]【添加】任务{}成功", heraJob.getOwner(), ssoName, heraJob.getId());
        updateMonitor(heraJob.getId());
        doAsync(() -> addJobRecord(heraJob.getId(), heraJob.getName(), RecordTypeEnum.Add, ssoName, ownerId));
    }

    @RequestMapping(value = "/addMonitor", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation("任务添加监控")
    public JsonResponse updateMonitor(@ApiParam(value = "任务ID", required = true) Integer id) {
        String ssoId = getSsoId();
        if (Constants.DEFAULT_ID.equals(ssoId)) {
            return new JsonResponse(false, "组账户不支持监控任务");
        }
        boolean res = heraJobMonitorService.addMonitor(ssoId, id);
        if (res) {
            MonitorLog.info("{}【关注】任务{}成功", getSsoName(), id);
            return new JsonResponse(true, "关注成功");
        } else {
            return new JsonResponse(false, "系统异常，请联系管理员");
        }
    }

    @RequestMapping(value = "/delMonitor", method = RequestMethod.POST)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation("删除任务关注人")
    public JsonResponse deleteMonitor(@ApiParam(value = "任务ID", required = true) Integer id) {
        String ssoId = getSsoId();
        HeraJobMonitor monitor = heraJobMonitorService.findByJobId(id);
        if (monitor != null) {
            if (monitor.getUserIds().split(Constants.COMMA).length == 1) {
                return new JsonResponse(false, "至少有一个监控人");
            }
        }
        boolean res = heraJobMonitorService.removeMonitor(ssoId, id);
        if (res) {
            MonitorLog.info("{}【取关】任务{}成功", getSsoName(), id);
            return new JsonResponse(true, "取关成功");
        } else {
            return new JsonResponse(false, "系统异常，请联系管理员");
        }
    }

    @RequestMapping(value = "/addGroup", method = RequestMethod.POST)
    @ResponseBody
    @RunAuth(authType = RunAuthType.GROUP, idIndex = 1)
    @ApiOperation("添加组")
    public JsonResponse addJob(@ApiParam(value = "组信息", required = true) HeraGroup heraGroup,
                               @ApiParam(value = "所在组id", required = true) String parentId) {
        heraGroup.setParent(StringUtil.getGroupId(parentId));
        heraGroup.setOwner(getOwner());
        heraGroup.setExisted(1);
        int insert = heraGroupService.insert(heraGroup);
        if (insert > 0) {
            MonitorLog.info("{}【添加】组{}成功", getSsoName(), heraGroup.getId());
            return new JsonResponse(true, Constants.GROUP_PREFIX + heraGroup.getId());
        } else {
            return new JsonResponse(false, String.valueOf(-1));

        }
    }


    @RequestMapping(value = "/generateVersion", method = RequestMethod.POST)
    @ResponseBody
    @RunAuth
    @ApiOperation("全量版本生成/单个版本生成")
    public JsonResponse generateVersion(@ApiParam("任务ID") Long jobId) throws ExecutionException, InterruptedException, TimeoutException {
        return new JsonResponse(true, workClient.generateActionFromWeb(JobExecuteKind.ExecuteKind.ManualKind, jobId));
    }


    /**
     * 获取任务历史版本
     *
     * @param pageHelper
     * @return
     */
    @RequestMapping(value = "/getJobHistory", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation("任务历史记录")
    public JsonResponse getJobHistory(@ApiParam(value = "分页", required = true) PageHelperTimeRange pageHelper) {
        return new JsonResponse(true, heraJobHistoryService.findLogByPage(pageHelper));
    }

    @RequestMapping(value = "/getHostGroupIds", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation("获取机器组Id")
    public JsonResponse getHostGroupIds() {
        return new JsonResponse(true, heraHostGroupService.getAll());
    }

    @RequestMapping(value = "getLog", method = RequestMethod.GET)
    @ResponseBody
    @RunAuth()
    @ApiOperation("获取任务日志接口")
    public JsonResponse getJobLog(@ApiParam(value = "任务ID", required = true) Integer id) {
        return new JsonResponse(true, heraJobHistoryService.findLogById(id));
    }

    @RequestMapping(value = "jobInstLog", method = RequestMethod.GET)
    @ResponseBody
    @RunAuth()
    @ApiOperation("获取任务日志接口")
    public String jobInstLog(@ApiParam(value = "任务ID", required = true) Integer id,Integer hisId) {
        return  heraJobHistoryService.findLogById(hisId).getLog();
    }


    @RequestMapping(value = "/status/{jobId}", method = RequestMethod.GET)
    @ResponseBody
    @UnCheckLogin
    @ApiOperation("开放接口,查询任务状态")
    public JsonResponse getStatus(@PathVariable("jobId") @ApiParam(value = "任务ID", required = true) Integer jobId
            , @RequestParam("time") @ApiParam(value = "时间戳，只查询该时间戳之后的记录", required = true) long time) {
        HeraJobHistory history = heraJobHistoryService.findNewest(jobId);
        if (history == null) {
            return new JsonResponse(false, "无执行记录");
        }
        //此时可能正在创建动态集群 或者发送netty消息的路上
        if (history.getStartTime() == null && history.getGmtCreate().getTime() >= time) {
            return new JsonResponse(true, StatusEnum.RUNNING.toString());
        }

        if (history.getStartTime() != null && history.getStartTime().getTime() < time) {
            return new JsonResponse(false, "无执行记录");
        }
        return new JsonResponse(true, Optional.ofNullable(history.getStatus()).orElse(StatusEnum.RUNNING.toString()));
    }


    private Map<String, String> getInheritConfig(Integer groupId) {
        HeraGroup group = heraGroupService.findConfigById(groupId);
        Map<String, String> configMap = new TreeMap<>();
        while (group != null && groupId != null && groupId != 0) {
            Map<String, String> map = StringUtil.convertStringToMap(group.getConfigs());
            // 多重继承相同变量，以第一个的为准
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();
                if (!configMap.containsKey(key)) {
                    configMap.put(key, entry.getValue());
                }
            }
            groupId = group.getParent();
            group = heraGroupService.findConfigById(groupId);
        }
        return configMap;
    }


    private String getuIds(Integer id, RunAuthType type) {
        List<HeraPermission> permissions = heraPermissionService.findByTargetId(id, type.getName(), 1);
        StringBuilder uids = new StringBuilder("[ ");
        if (permissions != null && permissions.size() > 0) {
            permissions.forEach(x -> uids.append(x.getUid()).append(" "));
        }
        uids.append("]");

        return uids.toString();
    }


    @RequestMapping(value = "/getJobImpactOrProgress", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation("查询任务依赖关系接口")
    public JsonResponse getJobImpactOrProgress(@ApiParam(value = "任务ID", required = true) Integer jobId
            , @ApiParam(value = "0:上游/1:下游", required = true) Integer type) {
        Map<String, Object> graph = heraJobService.findCurrentJobGraph(jobId, type);
        if (graph == null) {
            return new JsonResponse(false, "当前任务不存在");
        }
        return new JsonResponse(true, "成功", graph);
    }

    @RequestMapping(value = "/getAllArea", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation("获取所有区域")
    public JsonResponse getAllArea() {
        return new JsonResponse(true, "成功", Optional.of(heraAreaService.findAll()).get());
    }


    /**
     * @param jobHisId
     * @param status
     * @return
     */
    @RequestMapping(value = "/forceJobHisStatus", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation("强制设置任务状态")
    public JsonResponse forceJobHisStatus(@ApiParam(value = "执行记录id", required = true) Long jobHisId
            , @ApiParam(value = "设置的状态", required = true) String status) {

        String info = "";
        if (status.equals(StatusEnum.WAIT.toString())) {
            info = "手动强制等待状态";
        } else if (status.equals(StatusEnum.FAILED.toString())) {
            info = "手动强制失败状态";
        } else if (status.equals(StatusEnum.SUCCESS.toString())) {
            info = "手动强制成功状态";
        } else if (status.equals(StatusEnum.RUNNING.toString())) {
            info = "手动强制运行中状态";
        }

        String illustrate = heraJobHistoryService.findById(jobHisId).getIllustrate();
        if (StringUtils.isNotBlank(illustrate)) {
            illustrate += ";" + info;
        } else {
            illustrate = info;
        }
        heraJobHistoryService.updateStatusAndIllustrate(jobHisId, status, illustrate, new Date());
        return new JsonResponse(true, "修改状态成功");
    }
}
