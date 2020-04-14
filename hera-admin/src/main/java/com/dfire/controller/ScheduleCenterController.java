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
import com.dfire.common.entity.vo.PageHelperTimeRange;
import com.dfire.common.enums.*;
import com.dfire.common.exception.NoPermissionException;
import com.dfire.common.service.*;
import com.dfire.common.util.*;
import com.dfire.common.vo.GroupTaskVo;
import com.dfire.config.AdminCheck;
import com.dfire.config.HeraGlobalEnv;
import com.dfire.config.RunAuth;
import com.dfire.config.UnCheckLogin;
import com.dfire.core.netty.worker.WorkClient;
import com.dfire.logs.ErrorLog;
import com.dfire.logs.MonitorLog;
import com.dfire.protocol.JobExecuteKind;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.WebAsyncTask;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
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

    private Set<Long> cancelSet = new HashSet<>();


    @RequestMapping()
    public String login() {
        return "scheduleCenter/scheduleCenter.index";
    }

    @RequestMapping(value = "/init", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse initJobTree() {
        return new JsonResponse(true, Optional.of(heraJobService.buildJobTree(getOwner())).get());
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
        heraJobVo.setUIdS(getuIds(jobId, RunAuthType.JOB));
        heraJobVo.setFocusUser(focusUsers.toString());
        heraJobVo.setAlarmLevel(AlarmLevel.getName(job.getOffset()));
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


    private void configDecry(Map<String, String> config) {
        Optional.ofNullable(config)
                .ifPresent(cxf -> cxf.entrySet()
                        .stream()
                        .filter(pair -> pair.getKey().toLowerCase().contains(Constants.SECRET_PREFIX))
                        .forEach(entry -> entry.setValue(PasswordUtils.aesDecrypt(entry.getValue()))));
    }

    private void configEncry(Map<String, String> config) {
        Optional.ofNullable(config)
                .ifPresent(cxf -> cxf.entrySet()
                        .stream()
                        .filter(pair -> pair.getKey().toLowerCase().contains(Constants.SECRET_PREFIX))
                        .forEach(entry -> entry.setValue(PasswordUtils.aesEncryption(entry.getValue()))));
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


    @RequestMapping(value = "/updatePermission", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    @RunAuth(typeIndex = 1)
    public JsonResponse updatePermission(@RequestParam("id") String id,
                                         @RequestParam("type") RunAuthType type,
                                         @RequestParam("uIdS") String names) {

        Integer newId = StringUtil.getGroupId(id);
        JSONArray parseId = JSONArray.parseArray(names);
        Set<String> uIdS;
        if (parseId == null) {
            uIdS = new HashSet<>(0);
        } else {
            uIdS = parseId.stream().map(uid -> (String) uid).collect(Collectors.toSet());
        }
        String typeStr = type.getName();
        Optional.ofNullable(heraPermissionService.findByTargetId(newId, typeStr, 1)).ifPresent(perms -> perms.forEach(perm -> {
            if (!uIdS.contains(perm.getUid())) {
                heraPermissionService.updateByUid(newId, typeStr, 0, perm.getUid());
            } else {
                uIdS.remove(perm.getUid());
            }
        }));
        //经过第一轮的筛选后，如果还剩下，继续处理
        if (uIdS.size() > 0) {
            List<HeraPermission> perms = heraPermissionService.findByTargetId(newId, typeStr, 0);
            //把以前设置为无效的、这次加入管理的重新设置为有效
            if (perms != null) {
                perms.stream().filter(perm -> uIdS.contains(perm.getUid())).forEach(perm -> {
                    uIdS.remove(perm.getUid());
                    heraPermissionService.updateByUid(newId, typeStr, 1, perm.getUid());
                });
            }
            if (uIdS.size() > 0) {
                //余下的都是需要新增的
                Long targetId = Long.parseLong(String.valueOf(newId));
                uIdS.forEach(uid -> heraPermissionService.insert(HeraPermission
                        .builder()
                        .type(typeStr)
                        .targetId(targetId)
                        .uid(uid)
                        .build())
                );
            }
        }
        return new JsonResponse(true, "修改成功");
    }


    @RequestMapping(value = "/getJobOperator", method = RequestMethod.GET)
    @ResponseBody
    @RunAuth(typeIndex = 1)
    public JsonResponse getJobOperator(String jobId, RunAuthType type) {
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

    /**
     * 手动执行任务
     *
     * @param actionId
     * @return
     */
    @RequestMapping(value = "/manual", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse execute(@JsonSerialize(using = ToStringSerializer.class) Long actionId, Integer triggerType, @RequestParam(required = false) String execUser) throws InterruptedException, ExecutionException {
        if (execUser == null) {
            checkPermission(ActionUtil.getJobId(actionId), RunAuthType.JOB);
        }
        TriggerTypeEnum triggerTypeEnum;
        if (triggerType == 3) {
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
        actionHistory.setActionId(heraAction.getId());
        actionHistory.setTriggerType(triggerTypeEnum.getId());
        actionHistory.setOperator(heraJob.getOwner());
        actionHistory.setIllustrate(execUser);
        actionHistory.setStatus(StatusEnum.RUNNING.toString());
        actionHistory.setStatisticEndTime(heraAction.getStatisticEndTime());
        actionHistory.setHostGroupId(heraAction.getHostGroupId());
        actionHistory.setProperties(configs);
        actionHistory.setBatchId(heraAction.getBatchId());
        actionHistory.setBizLabel(heraJob.getBizLabel());
        heraJobHistoryService.insert(actionHistory);
        heraAction.setScript(heraJob.getScript());
        heraAction.setHistoryId(actionHistory.getId());
        heraAction.setConfigs(configs);
        heraAction.setAuto(heraJob.getAuto());
        heraAction.setHostGroupId(heraJob.getHostGroupId());
        heraJobActionService.update(heraAction);
        workClient.executeJobFromWeb(JobExecuteKind.ExecuteKind.ManualKind, actionHistory.getId());

        String ownerId = getOwnerId();
        if (ownerId == null) {
            ownerId = "0";
        }
        addJobRecord(heraJob.getId(), String.valueOf(actionId), RecordTypeEnum.Execute, execUser, ownerId);

        return new JsonResponse(true, actionId);
    }

    @RequestMapping(value = "/getJobVersion", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse getJobVersion(Long jobId) {
        return new JsonResponse(true, heraJobActionService.getActionVersionByJobId(jobId)
                .stream()
                .map(id -> HeraActionVo.builder().id(id).build())
                .collect(Collectors.toList()));
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
        HeraJob memJob = heraJobService.findById(heraJobVo.getId());
        Map<String, String> configMap = StringUtil.configsToMap(heraJobVo.getSelfConfigs());
        configEncry(configMap);
        heraJobVo.setSelfConfigs(StringUtil.mapToConfigs(configMap));
        HeraJob newJob = BeanConvertUtils.convertToHeraJob(heraJobVo);
        if (StringUtils.isNotBlank(newJob.getDependencies())) {
            if (!newJob.getDependencies().equals(memJob.getDependencies())) {
                List<HeraJob> relation = heraJobService.getAllJobDependencies();

                DagLoopUtil dagLoopUtil = new DagLoopUtil(heraJobService.selectMaxId());
                relation.forEach(x -> {
                    String dependencies;
                    if (x.getId() == newJob.getId()) {
                        dependencies = newJob.getDependencies();
                    } else {
                        dependencies = x.getDependencies();
                    }
                    if (StringUtils.isNotBlank(dependencies)) {
                        String[] split = dependencies.split(",");
                        for (String s : split) {
                            dagLoopUtil.addEdge(x.getId(), Integer.parseInt(s));
                        }
                    }
                });

                if (dagLoopUtil.isLoop()) {
                    return new JsonResponse(false, "出现环形依赖，请检测依赖关系:" + dagLoopUtil.getLoop());
                }
            }
        }


        Integer update = heraJobService.update(newJob);

        if (update == null || update == 0) {
            return new JsonResponse(false, "更新失败");
        }
        String ssoName = getSsoName();
        String ownerId = getOwnerId();
        doAsync(() -> {
            //脚本更新
            if (!memJob.getScript().equals(newJob.getScript())) {
                addJobRecord(newJob.getId(), memJob.getScript(), RecordTypeEnum.SCRIPT, ssoName, ownerId);
            }
            //依赖任务更新
            if (!memJob.getDependencies().equals(newJob.getDependencies())) {
                addJobRecord(newJob.getId(), memJob.getDependencies(), RecordTypeEnum.DEPEND, ssoName, ownerId);
            }
            //定时表达式更新
            if (!memJob.getCronExpression().equals(newJob.getCronExpression())) {
                addJobRecord(newJob.getId(), memJob.getScript(), RecordTypeEnum.CRON, ssoName, ownerId);
            }
            //执行区域更新
            if (!memJob.getAreaId().equals(newJob.getAreaId())) {
                addJobRecord(newJob.getId(), memJob.getAreaId(), RecordTypeEnum.AREA, ssoName, ownerId);
            }
            //脚本配置项变化
            if (!memJob.getConfigs().equals(newJob.getConfigs())) {
                addJobRecord(newJob.getId(), memJob.getConfigs(), RecordTypeEnum.CONFIG, ssoName, ownerId);
            }
            if (!memJob.getRunType().equals(newJob.getRunType())) {
                addJobRecord(newJob.getId(), memJob.getRunType(), RecordTypeEnum.RUN_TYPE, ssoName, ownerId);
            }
        });
        return new JsonResponse(true, "更新成功");
    }


    @RequestMapping(value = "/updateGroupMessage", method = RequestMethod.POST)
    @ResponseBody
    @RunAuth(authType = RunAuthType.GROUP, idIndex = 1)
    public JsonResponse updateGroupMessage(HeraGroupVo groupVo, String groupId) {
        groupVo.setId(StringUtil.getGroupId(groupId));

        Map<String, String> configMap = StringUtil.configsToMap(groupVo.getSelfConfigs());
        configEncry(configMap);

        groupVo.setSelfConfigs(StringUtil.mapToConfigs(configMap));

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
        String ssoName = getSsoName();
        String ownerId = getOwnerId();
        if (type == RunAuthType.GROUP) {
            res = heraGroupService.delete(xId) > 0;
            MonitorLog.info("{}【删除】组{}成功", ssoName, xId);
            doAsync(() -> addGroupRecord(xId, null, RecordTypeEnum.DELETE, ssoName, ownerId));
            return new JsonResponse(res, res ? "删除成功" : "系统异常,请联系管理员");
        }
        res = heraJobService.delete(xId) > 0;
        MonitorLog.info("{}【删除】任务{}成功", getOwner(), xId);
        updateJobToMaster(res, xId);
        doAsync(() -> addJobRecord(xId, null, RecordTypeEnum.DELETE, ssoName, ownerId));
        return new JsonResponse(res, res ? "删除成功" : "系统异常,请联系管理员");
    }

    @RequestMapping(value = "/addJob", method = RequestMethod.POST)
    @ResponseBody
    @RunAuth(authType = RunAuthType.GROUP, idIndex = 1)
    public JsonResponse addJob(HeraJob heraJob, String parentId) {
        heraJob.setGroupId(StringUtil.getGroupId(parentId));
        heraJob.setHostGroupId(HeraGlobalEnv.defaultWorkerGroup);
        heraJob.setOwner(getOwner());
        heraJob.setScheduleType(JobScheduleTypeEnum.Independent.getType());
        int insert = heraJobService.insert(heraJob);
        if (insert > 0) {
            String ssoName = getSsoName();
            String ownerId = getOwnerId();
            MonitorLog.info("{}[{}]【添加】任务{}成功", heraJob.getOwner(), ssoName, heraJob.getId());
            updateMonitor(heraJob.getId());
            doAsync(() -> addJobRecord(heraJob.getId(), heraJob.getName(), RecordTypeEnum.Add, ssoName, ownerId));
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
            MonitorLog.info("{}【关注】任务{}成功", getSsoName(), id);
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
            MonitorLog.info("{}【取关】任务{}成功", getSsoName(), id);
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
            MonitorLog.info("{}【添加】组{}成功", getSsoName(), heraGroup.getId());
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
            MonitorLog.info("{}【切换】任务{}状态{}成功", getSsoName(), status == 1 ? Constants.OPEN_STATUS : status == 0 ? "关闭" : "失效");
        }

        String ssoName = getSsoName();
        String ownerId = getOwnerId();
        doAsync(() -> addJobRecord(id, String.valueOf(status), RecordTypeEnum.SWITCH, ssoName, ownerId));
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
    public JsonResponse generateVersion(Long jobId) throws ExecutionException, InterruptedException {
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
     * @return
     */
    @RequestMapping(value = "/getJobHistory", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse getJobHistory(PageHelperTimeRange pageHelperTimeRange) {
        return new JsonResponse(true, heraJobHistoryService.findLogByPage(pageHelperTimeRange));
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
    public WebAsyncTask<JsonResponse> cancelJob(Long historyId, String jobId) {
        MonitorLog.info("{}取消任务{}", getOwner(), jobId);
        if (cancelSet.contains(historyId)) {
            return new WebAsyncTask<>(() -> new JsonResponse(true, "任务正在取消中，请稍后"));
        }
        String ssoName = getSsoName();
        String ownerId = getOwnerId();
        WebAsyncTask<JsonResponse> response = new WebAsyncTask<>(() -> {
            String res = null;
            try {
                cancelSet.add(historyId);
                HeraJobHistory history = heraJobHistoryService.findById(historyId);
                addJobRecord(Integer.parseInt(jobId), "", RecordTypeEnum.CANCEL, ssoName, ownerId);
                JobExecuteKind.ExecuteKind kind;
                if (TriggerTypeEnum.parser(history.getTriggerType()) == TriggerTypeEnum.MANUAL) {
                    kind = JobExecuteKind.ExecuteKind.ManualKind;
                } else {
                    kind = JobExecuteKind.ExecuteKind.ScheduleKind;
                }
                try {
                    res = workClient.cancelJobFromWeb(kind, historyId);
                } catch (ExecutionException | InterruptedException e) {
                    ErrorLog.error("取消任务异常", e);
                }
                return new JsonResponse(true, res);
            } finally {
                cancelSet.remove(historyId);
                MonitorLog.info("取消任务{}结果为:{}", jobId, res);
            }
        });
        response.onTimeout(() -> new JsonResponse(false, "任务正在取消,请稍后"));
        return response;
    }

    @RequestMapping(value = "getLog", method = RequestMethod.GET)
    @ResponseBody
    @RunAuth(idIndex = 1)
    public JsonResponse getJobLog(Integer id, Integer jobId) {
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
        List<HeraAction> actions = heraJobActionService.findByJobId(Long.parseLong((split[0])));
        if (actions == null || actions.size() == 0) {
            return new JsonResponse(false, "找不到版本");
        }
        doAsync(() -> addJobRecord(Integer.parseInt(split[0]), "远程执行任务", RecordTypeEnum.REMOTE, getIp() + ":" + split[1], String.valueOf(heraUserService.findByName(split[1]).getId())));
        MonitorLog.info("远程调用:{}", JSONObject.toJSONString(params));
        HeraJob heraJob = heraJobService.findById(Integer.parseInt(split[0]));
        Map<String, String> configs = StringUtil.convertStringToMap(heraJob.getConfigs());
        configs.putAll(params);
        heraJob.setConfigs(StringUtil.convertMapToString(configs));
        heraJobService.update(heraJob);
        return execute(actions.get(actions.size() - 1).getId(), 2, split[1]);
    }

    @RequestMapping(value = "/status/{jobId}", method = RequestMethod.GET)
    @ResponseBody
    @UnCheckLogin
    public JsonResponse getStatus(@PathVariable("jobId") Long jobId, @RequestParam("time") long time) {
        HeraJobHistory history = heraJobHistoryService.findNewest(jobId);
        if (history == null) {
            return new JsonResponse(false, "无执行记录");
        }
        //此时可能正在创建动态集群 或者发送netty消息的路上
        if (history.getStartTime() == null && history.getGmtCreate().getTime() >= time) {
            return new JsonResponse(true, StatusEnum.RUNNING.toString());
        }

        if (history.getStartTime().getTime() < time) {
            return new JsonResponse(false, "无执行记录");
        }
        return new JsonResponse(true, Optional.ofNullable(history.getStatus()).orElse(StatusEnum.RUNNING.toString()));
    }

    private void updateJobToMaster(boolean result, Integer id) {
        if (result) {
            doAsync(() -> {
                try {
                    workClient.updateJobFromWeb(String.valueOf(id));
                } catch (ExecutionException | InterruptedException e) {
                    ErrorLog.error("更新异常", e);
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


    private String getuIds(Integer id, RunAuthType type) {
        List<HeraPermission> permissions = heraPermissionService.findByTargetId(id, type.getName(), 1);
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
                List<HeraJob> jobList = heraJobService.findByPid(id).stream()
                        .filter(job -> job.getIsValid() == 1)
                        .collect(Collectors.toList());
                if (jobList.size() == 0) {
                    return null;
                }
                StringBuilder openJob = new StringBuilder("无法删除存在任务的目录:[ ");
                for (HeraJob job : jobList) {
                    openJob.append(job.getId()).append(" ");
                }
                openJob.append("]");
                return openJob.toString();
            } else {
                //如果是大目录
                List<HeraGroup> parent = heraGroupService.findByParent(id).stream()
                        .filter(group -> group.getExisted() == 1)
                        .collect(Collectors.toList());
                if (parent.size() == 0) {
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
    public JsonResponse getJobImpact(Integer jobId, Integer type, Integer auto) throws NoPermissionException {
        List<Integer> jobList = heraJobService.findJobImpact(jobId, type);
        if (jobList == null) {
            return new JsonResponse(false, "当前任务不存在");
        }
        int size = jobList.size();
        JsonResponse response;
        if ((type == 0 && auto == 1) || (type == 1 && auto != 1)) {
            for (int i = size - 1; i >= 0; i--) {
                response = updateSwitch(jobList.get(i), auto);
                if (!response.isSuccess()) {
                    return response;
                }
            }
        } else if ((type == 1 && auto == 1) || (type == 0 && auto != 1)) {
            for (int i = 0; i < size; i++) {
                response = updateSwitch(jobList.get(i), auto);
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
        return new JsonResponse(true, "成功", Optional.of(heraAreaService.findAll()).get());
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
        String ssoName = getSsoName();
        String ownerId = getOwnerId();
        if (id.startsWith(Constants.GROUP_PREFIX)) {
            newId = StringUtil.getGroupId(id);
            checkPermission(newId, RunAuthType.GROUP);
            boolean result = heraGroupService.changeParent(newId, newParent);
            doAsync(() -> addGroupRecord(newId, lastParent + "=>" + newParent, RecordTypeEnum.MOVE, ssoName, ownerId));
            MonitorLog.info("组{}:发生移动 {}  --->  {}", newId, lastParent, newParent);
            return new JsonResponse(result, result ? "处理成功" : "移动失败");
        } else {
            newId = Integer.parseInt(id);
            checkPermission(newId, RunAuthType.JOB);
            boolean result = heraJobService.changeParent(newId, newParent);
            doAsync(() -> addJobRecord(newId, lastParent + "=>" + newParent, RecordTypeEnum.MOVE, ssoName, ownerId));
            MonitorLog.info("任务{}:发生移动{}  --->  {}", newId, lastParent, newParent);
            return new JsonResponse(result, result ? "处理成功" : "移动失败");
        }
    }


    @RequestMapping(value = "/copyJobFromExistsJob", method = RequestMethod.POST)
    @ResponseBody
    @RunAuth
    public JsonResponse copyJobFromExists(Integer jobId) {
        HeraJob job = heraJobService.copyJobFromExistsJob(jobId);
        if (job == null) {
            return new JsonResponse(false, "复制任务失败！");
        } else {
            return new JsonResponse(true, "复制任务成功[新任务位于同目录下,名称=" + job.getName() + "_copy]！");
        }
    }


    /**
     * @param jobHisId
     * @param status
     * @return
     */
    @RequestMapping(value = "/forceJobHisStatus", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse forceJobHisStatus(Long jobHisId, String status) {

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
        return null;
    }


}
