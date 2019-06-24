package com.dfire.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dfire.common.constants.Constants;
import com.dfire.common.entity.*;
import com.dfire.common.entity.model.JsonResponse;
import com.dfire.common.entity.model.TablePageForm;
import com.dfire.common.entity.model.TableResponse;
import com.dfire.common.entity.vo.HeraActionVo;
import com.dfire.common.entity.vo.HeraGroupVo;
import com.dfire.common.entity.vo.HeraJobVo;
import com.dfire.common.entity.vo.PageHelper;
import com.dfire.common.enums.*;
import com.dfire.common.exception.NoPermissionException;
import com.dfire.common.service.*;
import com.dfire.common.util.*;
import com.dfire.common.vo.GroupTaskVo;
import com.dfire.config.AdminCheck;
import com.dfire.config.HeraGlobalEnvironment;
import com.dfire.config.RunAuth;
import com.dfire.config.UnCheckLogin;
import com.dfire.core.netty.worker.WorkClient;
import com.dfire.logs.MonitorLog;
import com.dfire.protocol.JobExecuteKind;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:50 2018/1/13
 * @desc 调度中心视图管理器
 */
@Controller
@RequestMapping("/scheduleCenter")
public class ScheduleCenterController extends BaseHeraController {

    @Autowired
    @Qualifier("heraJobMemoryService")
    private HeraJobService heraJobService;
    @Autowired
    private HeraJobActionService heraJobActionService;
    @Autowired
    @Qualifier("heraGroupMemoryService")
    private HeraGroupService heraGroupService;
    @Autowired
    private HeraJobHistoryService heraJobHistoryService;
    @Autowired
    private HeraJobMonitorService heraJobMonitorService;
    @Autowired
    private HeraUserService heraUserService;
    @Autowired
    private HeraPermissionService heraPermissionService;
    @Autowired
    private WorkClient workClient;
    @Autowired
    private HeraHostGroupService heraHostGroupService;
    @Autowired
    private HeraAreaService heraAreaService;
    @Autowired
    private HeraSsoService heraSsoService;

    private Set<String> cancelSet = new HashSet<>();

    private ThreadPoolExecutor poolExecutor;

    {
        poolExecutor = new ThreadPoolExecutor(
                1, Runtime.getRuntime().availableProcessors() * 4, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new NamedThreadFactory("updateJobThread"), new ThreadPoolExecutor.AbortPolicy());
        poolExecutor.allowCoreThreadTimeOut(true);
    }

    @RequestMapping()
    public String login() {
        return "scheduleCenter/scheduleCenter.index";
    }

    @RequestMapping(value = "/init", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse initJobTree() {
        return new JsonResponse(true, heraJobService.buildJobTree(getOwner()));
    }

    @RequestMapping(value = "/getJobMessage", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse getJobMessage(Integer jobId) {
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
        heraJobVo.setUIdS(getuIds(jobId));
        heraJobVo.setFocusUser(focusUsers.toString());
        heraJobVo.setAlarmLevel(AlarmLevel.getName(job.getOffset()));
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


    private void checkPermission(Integer jobId, RunAuthType type) {
        ScheduleCenterController schedule = (ScheduleCenterController) Optional.ofNullable(AopContext.currentProxy()).orElse(this);
        schedule.doAspectAuth(jobId, type);
    }

    @RunAuth(typeIndex = 1)
    @GetMapping("/checkPermission")
    @ResponseBody
    public JsonResponse doAspectAuth(Integer jobId, RunAuthType type) {
        return new JsonResponse(true, true);
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
    public TableResponse getGroupTask(String groupId, String status, String dt, TablePageForm pageForm) {


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
    public JsonResponse getGroupMessage(String groupId) {
        Integer id = StringUtil.getGroupId(groupId);
        HeraGroup group = heraGroupService.findById(id);
        HeraGroupVo groupVo = BeanConvertUtils.convert(group);
        groupVo.setInheritConfig(getInheritConfig(groupVo.getParent()));
        groupVo.setUIdS(getuIds(id));
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


    @RequestMapping(value = "/updatePermission", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    @RunAuth(typeIndex = 1)
    public JsonResponse updatePermission(@RequestParam("id") String id,
                                         @RequestParam("type") RunAuthType type,
                                         @RequestParam("uIdS") String names) {
        Integer newId = StringUtil.getGroupId(id);
        JSONArray uIdS = JSONArray.parseArray(names);
        Integer integer = heraPermissionService.deleteByTargetId(newId);
        if (integer == null) {
            return new JsonResponse(false, "修改失败");
        }
        if (uIdS != null && uIdS.size() > 0) {
            String typeStr = type.getName();
            Long targetId = Long.parseLong(String.valueOf(newId));
            List<HeraPermission> permissions = new ArrayList<>(uIdS.size());
            Date date = new Date();
            for (Object uId : uIdS) {
                HeraPermission heraPermission = new HeraPermission();
                heraPermission.setType(typeStr);
                heraPermission.setGmtModified(date);
                heraPermission.setGmtCreate(date);
                heraPermission.setTargetId(targetId);
                heraPermission.setUid((String) uId);
                permissions.add(heraPermission);
            }
            Integer res = heraPermissionService.insertList(permissions);
            if (res == null || res != uIdS.size()) {
                return new JsonResponse(false, "修改失败");
            }
        }

        return new JsonResponse(true, "修改成功");
    }


    @RequestMapping(value = "/getJobOperator", method = RequestMethod.GET)
    @ResponseBody
    @RunAuth(typeIndex = 1)
    public JsonResponse getJobOperator(String jobId, RunAuthType type) {
        Integer groupId = StringUtil.getGroupId(jobId);
        List<HeraPermission> permissions = heraPermissionService.findByTargetId(groupId);
        List<HeraUser> all = heraUserService.findAllName();
        if (all == null || permissions == null) {
            return new JsonResponse(false, "发生错误，请联系管理员");
        }
        Map<String, Object> res = new HashMap<>(2);
        res.put("allUser", all);
        res.put("admin", permissions);
        return new JsonResponse(true, "查询成功", res);
    }

    /**
     * 手动执行任务
     *
     * @param actionId
     * @return
     */
    @RequestMapping(value = "/manual", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse execute(String actionId, Integer triggerType, @RequestParam(required = false) String execUser) throws InterruptedException, ExecutionException {
        if (execUser == null) {
            checkPermission(Integer.parseInt(actionId.substring(actionId.length() - 4)), RunAuthType.JOB);
        }
        TriggerTypeEnum triggerTypeEnum;
        if (triggerType == 2) {
            triggerTypeEnum = TriggerTypeEnum.MANUAL_RECOVER;
        } else {
            triggerTypeEnum = TriggerTypeEnum.MANUAL;
        }
        HeraAction heraAction = heraJobActionService.findById(actionId);
        HeraJob heraJob = heraJobService.findById(heraAction.getJobId());

        if (execUser == null) {
            execUser = super.getSsoName();
        }
        if (execUser == null) {
            return new JsonResponse(false, "任务执行人为空");
        }
        String configs = heraJob.getConfigs();
        HeraJobHistory actionHistory = HeraJobHistory.builder().build();
        actionHistory.setJobId(heraAction.getJobId());
        actionHistory.setActionId(heraAction.getId().toString());
        actionHistory.setTriggerType(triggerTypeEnum.getId());
        actionHistory.setOperator(heraJob.getOwner());
        actionHistory.setIllustrate(execUser);
        actionHistory.setStatus(StatusEnum.RUNNING.toString());
        actionHistory.setStatisticEndTime(heraAction.getStatisticEndTime());
        actionHistory.setHostGroupId(heraAction.getHostGroupId());
        actionHistory.setProperties(configs);
        heraJobHistoryService.insert(actionHistory);
        heraAction.setScript(heraJob.getScript());
        heraAction.setHistoryId(actionHistory.getId());
        heraAction.setConfigs(configs);
        heraAction.setAuto(heraJob.getAuto());
        heraAction.setHostGroupId(heraJob.getHostGroupId());
        heraJobActionService.update(heraAction);
        workClient.executeJobFromWeb(JobExecuteKind.ExecuteKind.ManualKind, actionHistory.getId());
        return new JsonResponse(true, actionId);
    }

    @RequestMapping(value = "/getJobVersion", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse getJobVersion(String jobId) {
        return new JsonResponse(true, heraJobActionService.getActionVersionByJobId(Long.parseLong(jobId))
                .stream()
                .map(id -> HeraActionVo.builder().id(id).build())
                .collect(Collectors.toList()));
    }

    @RequestMapping(value = "/updateScript", method = RequestMethod.POST)
    @ResponseBody
    @RunAuth
    public JsonResponse updateScript(Integer id, String script) {
        Integer update = heraJobService.updateScript(id, script);
        if (update != null && update > 0) {
            return new JsonResponse(true, "更新脚本成功");
        }
        return new JsonResponse(false, "更新脚本失败");
    }

    @RequestMapping(value = "/updateJobMessage", method = RequestMethod.POST)
    @ResponseBody
    @RunAuth(idIndex = -1)
    public JsonResponse updateJobMessage(HeraJobVo heraJobVo) {
        if (StringUtils.isBlank(heraJobVo.getDescription())) {
            return new JsonResponse(false, "描述不能为空");
        }
        try {
            new CronExpression(heraJobVo.getCronExpression());
        } catch (ParseException e) {
            return new JsonResponse(false, "定时表达式不准确，请核实后再保存");
        }

        HeraHostGroup hostGroup = heraHostGroupService.findById(heraJobVo.getHostGroupId());
        if (hostGroup == null) {
            return new JsonResponse(false, "机器组不存在，请选择一个机器组");
        }

        if (StringUtils.isBlank(heraJobVo.getAreaId())) {
            return new JsonResponse(false, "至少选择一个任务所在区域");
        }

        //如果是依赖任务
        if (heraJobVo.getScheduleType() == 1) {
            String dependencies = heraJobVo.getDependencies();
            if (StringUtils.isNotBlank(dependencies)) {
                String[] jobs = dependencies.split(Constants.COMMA);
                HeraJob heraJob;
                boolean jobAuto = true;
                StringBuilder sb = null;
                for (String job : jobs) {
                    heraJob = heraJobService.findById(Integer.parseInt(job));
                    if (heraJob == null) {
                        return new JsonResponse(false, "任务:" + job + "为空");
                    }
                    if (heraJob.getAuto() != 1) {
                        if (jobAuto) {
                            jobAuto = false;
                            sb = new StringBuilder();
                            sb.append(job);
                        } else {
                            sb.append(",").append(job);
                        }
                    }
                }
                if (!jobAuto) {
                    return new JsonResponse(false, "不允许依赖关闭状态的任务:" + sb.toString());
                }
            } else {
                return new JsonResponse(false, "请勾选你要依赖的任务");
            }
        } else if (heraJobVo.getScheduleType() == 0) {
            heraJobVo.setDependencies("");
        } else {
            return new JsonResponse(false, "无法识别的调度类型");
        }
        return heraJobService.checkAndUpdate(BeanConvertUtils.convertToHeraJob(heraJobVo));
    }

    @RequestMapping(value = "/updateGroupMessage", method = RequestMethod.POST)
    @ResponseBody
    @RunAuth(authType = RunAuthType.GROUP, idIndex = 1)
    public JsonResponse updateGroupMessage(HeraGroupVo groupVo, String groupId) {
        groupVo.setId(StringUtil.getGroupId(groupId));
        HeraGroup heraGroup = BeanConvertUtils.convert(groupVo);
        boolean res = heraGroupService.update(heraGroup) > 0;
        return new JsonResponse(res, res ? "更新成功" : "系统异常,请联系管理员");
    }

    @RequestMapping(value = "/deleteJob", method = RequestMethod.POST)
    @ResponseBody
    @RunAuth(typeIndex = 1)
    public JsonResponse deleteJob(String id, RunAuthType type) throws NoPermissionException {
        Integer xId = StringUtil.getGroupId(id);
        boolean res;
        String check = checkDependencies(xId, type);
        if (StringUtils.isNotBlank(check)) {
            return new JsonResponse(false, check);
        }
        if (type == RunAuthType.GROUP) {
            res = heraGroupService.delete(xId) > 0;
            MonitorLog.info("{}【删除】组{}成功", getOwner(), xId);
            return new JsonResponse(res, res ? "删除成功" : "系统异常,请联系管理员");

        }
        res = heraJobService.delete(xId) > 0;
        MonitorLog.info("{}【删除】任务{}成功", getOwner(), xId);
        updateJobToMaster(res, xId);
        return new JsonResponse(res, res ? "删除成功" : "系统异常,请联系管理员");
    }

    @RequestMapping(value = "/addJob", method = RequestMethod.POST)
    @ResponseBody
    @RunAuth(authType = RunAuthType.GROUP, idIndex = 1)
    public JsonResponse addJob(HeraJob heraJob, String parentId) {
        heraJob.setGroupId(StringUtil.getGroupId(parentId));
        heraJob.setHostGroupId(HeraGlobalEnvironment.defaultWorkerGroup);
        heraJob.setOwner(getOwner());
        heraJob.setScheduleType(JobScheduleTypeEnum.Independent.getType());
        int insert = heraJobService.insert(heraJob);
        if (insert > 0) {
            MonitorLog.info("{}【添加】任务{}成功", heraJob.getOwner(), heraJob.getId());
            updateMonitor(heraJob.getId());
            return new JsonResponse(true, String.valueOf(heraJob.getId()));
        } else {
            return new JsonResponse(false, "新增失败");
        }
    }

    @RequestMapping(value = "/addMonitor", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse updateMonitor(Integer id) {
        String ssoId = getSsoId();
        if (Constants.DEFAULT_ID.equals(ssoId)) {
            return new JsonResponse(false, "组账户不支持监控任务");
        }
        boolean res = heraJobMonitorService.addMonitor(ssoId, id);
        if (res) {
            MonitorLog.info("{}【关注】任务{}成功", ssoId, id);
            return new JsonResponse(true, "关注成功");
        } else {
            return new JsonResponse(false, "系统异常，请联系管理员");
        }
    }

    @RequestMapping(value = "/delMonitor", method = RequestMethod.POST)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public JsonResponse deleteMonitor(Integer id) {
        String ssoId = getSsoId();
        HeraJobMonitor monitor = heraJobMonitorService.findByJobId(id);
        if (monitor != null) {
            if (monitor.getUserIds().split(Constants.COMMA).length == 1) {
                return new JsonResponse(false, "至少有一个监控人");
            }
        }
        boolean res = heraJobMonitorService.removeMonitor(ssoId, id);
        if (res) {
            MonitorLog.info("{}【取关】任务{}成功", ssoId, id);
            return new JsonResponse(true, "取关成功");
        } else {
            return new JsonResponse(false, "系统异常，请联系管理员");
        }
    }

    @RequestMapping(value = "/addGroup", method = RequestMethod.POST)
    @ResponseBody
    @RunAuth(authType = RunAuthType.GROUP, idIndex = 1)
    public JsonResponse addJob(HeraGroup heraGroup, String parentId) {
        heraGroup.setParent(StringUtil.getGroupId(parentId));
        heraGroup.setOwner(getOwner());
        heraGroup.setExisted(1);
        int insert = heraGroupService.insert(heraGroup);
        if (insert > 0) {
            MonitorLog.info("{}【添加】组{}成功", getOwner(), heraGroup.getId());
            return new JsonResponse(true, String.valueOf(heraGroup.getId()));
        } else {
            return new JsonResponse(false, String.valueOf(-1));

        }
    }

    @RequestMapping(value = "/updateSwitch", method = RequestMethod.POST)
    @ResponseBody
    @RunAuth
    public JsonResponse updateSwitch(Integer id, Integer status) {
        HeraJob heraJob = heraJobService.findById(id);
        if (status.equals(heraJob.getAuto())) {
            return new JsonResponse(true, "操作成功");
        }
        //TODO 上下游任务检测时需要优化  任务链路复杂时 导致关闭/开启耗时较久
        //关闭动作 上游关闭时需要判断下游是否有开启任务，如果有，则不允许关闭
        if (status != 1) {
            String errorMsg;
            if ((errorMsg = getJobFromAuto(heraJobService.findDownStreamJob(id), 1)) != null) {
                return new JsonResponse(false, id + "下游存在开启状态任务:" + errorMsg);
            }
        } else { //开启动作 如果有上游任务，上游任务不能为关闭状态
            String errorMsg;
            if ((errorMsg = getJobFromAuto(heraJobService.findUpStreamJob(id), 0)) != null) {
                return new JsonResponse(false, id + "上游存在关闭状态任务:" + errorMsg);
            }
        }
        boolean result = heraJobService.changeSwitch(id, status);

        if (result) {
            MonitorLog.info("{}【切换】任务{}状态{}成功", id, status == 1 ? Constants.OPEN_STATUS : status == 0 ? "关闭" : "失效");
        }
        if (status == 1) {
            updateJobToMaster(result, id);
            return new JsonResponse(result, result ? "开启成功" : "开启失败");
        } else if (status == 0) {
            return new JsonResponse(result, result ? "关闭成功" : "关闭失败");
        } else {
            return new JsonResponse(result, result ? "成功设置为失效状态" : "设置状态失败");
        }

    }


    private String getJobFromAuto(List<HeraJob> streamJob, Integer auto) {
        boolean has = false;
        StringBuilder filterJob = null;
        for (HeraJob job : streamJob) {
            if (job.getAuto().equals(auto)) {
                if (!has) {
                    has = true;
                    filterJob = new StringBuilder();
                    filterJob.append(job.getId());
                } else {
                    filterJob.append(",").append(job.getId());
                }
            }
        }
        if (has) {
            return filterJob.toString();
        }
        return null;
    }

    @RequestMapping(value = "/generateVersion", method = RequestMethod.POST)
    @ResponseBody
    @RunAuth
    public JsonResponse generateVersion(String jobId) throws ExecutionException, InterruptedException {
        return new JsonResponse(true, workClient.generateActionFromWeb(JobExecuteKind.ExecuteKind.ManualKind, jobId));
    }


    @RequestMapping(value = "/generateAllVersion", method = RequestMethod.GET)
    @ResponseBody
    @AdminCheck
    public JsonResponse generateAllVersion() throws ExecutionException, InterruptedException {
        return new JsonResponse(true, workClient.generateActionFromWeb(JobExecuteKind.ExecuteKind.ManualKind, Constants.ALL_JOB_ID));
    }

    /**
     * 获取任务历史版本
     *
     * @param pageHelper
     * @return
     */
    @RequestMapping(value = "/getJobHistory", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse getJobHistory(PageHelper pageHelper) {
        return new JsonResponse(true, heraJobHistoryService.findLogByPage(pageHelper));
    }

    @RequestMapping(value = "/getHostGroupIds", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse getHostGroupIds() {
        return new JsonResponse(true, heraHostGroupService.getAll());
    }

    /**
     * 取消正在执行的任务
     *
     * @param jobId
     * @param historyId
     * @return
     */
    @RequestMapping(value = "/cancelJob", method = RequestMethod.GET)
    @ResponseBody
    @RunAuth(idIndex = 1)
    public JsonResponse cancelJob(String historyId, String jobId) throws ExecutionException, InterruptedException {
        if (cancelSet.contains(historyId)) {
            return new JsonResponse(true, "任务正在取消中，请稍后");
        }
        String res;
        try {
            cancelSet.add(historyId);
            HeraJobHistory history = heraJobHistoryService.findById(historyId);
            JobExecuteKind.ExecuteKind kind;
            if (TriggerTypeEnum.parser(history.getTriggerType()) == TriggerTypeEnum.MANUAL) {
                kind = JobExecuteKind.ExecuteKind.ManualKind;
            } else {
                kind = JobExecuteKind.ExecuteKind.ScheduleKind;
            }
            res = workClient.cancelJobFromWeb(kind, historyId);
        } finally {
            cancelSet.remove(historyId);
        }
        return new JsonResponse(true, res);
    }

    @RequestMapping(value = "getLog", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse getJobLog(Integer id) {
        return new JsonResponse(true, heraJobHistoryService.findLogById(id));
    }


    @RequestMapping(value = "/execute", method = RequestMethod.GET)
    @ResponseBody
    @UnCheckLogin
    public JsonResponse publicExecute(@RequestParam Map<String, String> params) throws ExecutionException, InterruptedException, NoPermissionException {
        String secret = params.get("secret");
        String decrypt = PasswordUtils.aesDecrypt(secret);
        if (decrypt == null) {
            return new JsonResponse(false, "解密失败，请询问管理员");
        }
        String[] split = decrypt.split(";");
        if (split.length != 2) {
            return new JsonResponse(false, "解密失败，请询问管理员");
        }
        List<HeraAction> actions = heraJobActionService.findByJobId(split[0]);
        if (actions == null || actions.size() == 0) {
            return new JsonResponse(false, "找不到版本");
        }
        MonitorLog.info("远程调用:{}", JSONObject.toJSONString(params));
        HeraJob heraJob = heraJobService.findById(Integer.parseInt(split[0]));
        Map<String, String> configs = StringUtil.convertStringToMap(heraJob.getConfigs());
        configs.putAll(params);
        heraJob.setConfigs(StringUtil.convertMapToString(configs));
        heraJobService.update(heraJob);
        return execute(actions.get(actions.size() - 1).getId().toString(), 2, split[1]);
    }


    private void updateJobToMaster(boolean result, Integer id) {
        if (result) {
            poolExecutor.execute(() -> {
                try {
                    workClient.updateJobFromWeb(String.valueOf(id));
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
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


    private String getuIds(Integer id) {
        List<HeraPermission> permissions = heraPermissionService.findByTargetId(id);
        StringBuilder uids = new StringBuilder("[ ");
        if (permissions != null && permissions.size() > 0) {
            permissions.forEach(x -> uids.append(x.getUid()).append(" "));
        }
        uids.append("]");

        return uids.toString();
    }

    private String checkDependencies(Integer id, RunAuthType type) {
        List<HeraJob> allJobs = heraJobService.getAllJobDependencies();
        if (type == RunAuthType.GROUP) {

            HeraGroup heraGroup = heraGroupService.findById(id);
            if (heraGroup == null) {
                return "组不存在";
            } else if (heraGroup.getDirectory() == 1) {
                //如果是小目录
                List<HeraJob> jobList = heraJobService.findByPid(id);
                StringBuilder openJob = new StringBuilder("无法删除存在任务的目录:[ ");
                for (HeraJob job : jobList) {
                    openJob.append(job.getId()).append(" ");
                }
                openJob.append("]");
                if (jobList.size() > 0) {
                    return openJob.toString();
                }
                return null;
            } else {
                //如果是大目录
                List<HeraGroup> parent = heraGroupService.findByParent(id);

                if (parent == null || parent.size() == 0) {
                    return null;
                }
                StringBuilder openGroup = new StringBuilder("无法删除存在目录的目录:[ ");
                for (HeraGroup group : parent) {
                    if (group.getExisted() == 1) {
                        openGroup.append(group.getId()).append(" ");
                    }
                }
                openGroup.append("]");
                return openGroup.toString();
            }

        } else {
            HeraJob job = heraJobService.findById(id);
            if (job.getAuto() == 1) {
                return "无法删除正在开启的任务";
            }
            boolean canDelete = true;
            boolean isFirst = true;
            String deleteJob = String.valueOf(job.getId());
            StringBuilder dependenceJob = new StringBuilder("任务依赖: ");
            String[] dependenceJobs;
            for (HeraJob allJob : allJobs) {
                if (StringUtils.isNotBlank(allJob.getDependencies())) {
                    dependenceJobs = allJob.getDependencies().split(",");
                    for (String jobId : dependenceJobs) {
                        if (jobId.equals(deleteJob)) {
                            if (canDelete) {
                                canDelete = false;
                            }
                            if (isFirst) {
                                isFirst = false;
                                dependenceJob.append("[").append(job.getId()).append(" -> ").append(allJob.getId()).append(" ");
                            } else {
                                dependenceJob.append(allJob.getId()).append(" ");
                            }
                            break;
                        }
                    }
                }
            }
            dependenceJob.append("]").append("\n");
            if (!canDelete) {
                return dependenceJob.toString();
            }
            return null;
        }
    }

    /**
     * 一键开启/关闭/失效 某job 的上游/下游的所有任务
     *
     * @param jobId jobId
     * @param type  0:上游  1:下游
     * @param auto  0:关闭  1:开启  2:失效
     * @return
     */
    @RequestMapping(value = "/switchAll", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse getJobImpact(Integer jobId, Integer type, Integer auto) {
        List<Integer> jobList = heraJobService.findJobImpact(jobId, type);
        if (jobList == null) {
            return new JsonResponse(false, "当前任务不存在");
        }
        int size = jobList.size();
        JsonResponse response;
        if ((type == 0 && auto == 1) || (type == 1 && auto != 1)) {
            for (int i = size - 1; i >= 0; i--) {
                response = this.updateSwitch(jobList.get(i), auto);
                if (!response.isSuccess()) {
                    return response;
                }
            }
        } else if ((type == 1 && auto == 1) || (type == 0 && auto != 1)) {
            for (int i = 0; i < size; i++) {
                response = this.updateSwitch(jobList.get(i), auto);
                if (!response.isSuccess()) {
                    return response;
                }
            }
        } else {
            return new JsonResponse(false, "未知的type:" + type);
        }

        return new JsonResponse(true, "全部处理成功", jobList);
    }


    @RequestMapping(value = "/getJobImpactOrProgress", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse getJobImpactOrProgress(Integer jobId, Integer type) {
        Map<String, Object> graph = heraJobService.findCurrentJobGraph(jobId, type);
        if (graph == null) {
            return new JsonResponse(false, "当前任务不存在");
        }
        return new JsonResponse(true, "成功", graph);
    }

    @RequestMapping(value = "/getAllArea", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse getAllArea() {
        List<HeraArea> heraAreas = heraAreaService.findAll();
        if (heraAreas == null) {
            return new JsonResponse(false, "查询异常");
        }
        return new JsonResponse(true, "成功", heraAreas);
    }


    @RequestMapping(value = "/check", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse check(String id) {
        if (id == null) {
            return new JsonResponse(true, "查询成功", false);
        }
        boolean auth = true;
        if (id.startsWith(Constants.GROUP_PREFIX)) {
            try {
                checkPermission(StringUtil.getGroupId(id), RunAuthType.GROUP);
            } catch (NoPermissionException e) {
                auth = false;
            }
            return new JsonResponse(true, "查询成功", auth);
        } else {
            try {
                checkPermission(Integer.parseInt(id), RunAuthType.JOB);
            } catch (NoPermissionException e) {
                auth = false;
            }
            return new JsonResponse(true, "查询成功", auth);
        }
    }

    @RequestMapping(value = "/moveNode", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse moveNode(String id, String parent, String lastParent) throws NoPermissionException {
        Integer newParent = StringUtil.getGroupId(parent);
        Integer newId;
        if (id.startsWith(Constants.GROUP_PREFIX)) {
            newId = StringUtil.getGroupId(id);
            checkPermission(newId, RunAuthType.GROUP);
            boolean result = heraGroupService.changeParent(newId, newParent);
            MonitorLog.info("组{}:发生移动 {}  --->  {}", newId, lastParent, newParent);
            return new JsonResponse(result, result ? "处理成功" : "移动失败");
        } else {
            newId = Integer.parseInt(id);
            checkPermission(newId, RunAuthType.JOB);
            boolean result = heraJobService.changeParent(newId, newParent);
            MonitorLog.info("任务{}:发生移动{}  --->  {}", newId, lastParent, newParent);
            return new JsonResponse(result, result ? "处理成功" : "移动失败");
        }

    }


}
