package com.dfire.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dfire.common.constants.Constants;
import com.dfire.common.entity.*;
import com.dfire.common.entity.model.HeraJobBean;
import com.dfire.common.entity.model.JsonResponse;
import com.dfire.common.entity.vo.HeraGroupVo;
import com.dfire.common.entity.vo.HeraJobVo;
import com.dfire.common.enums.RecordTypeEnum;
import com.dfire.common.enums.RunAuthType;
import com.dfire.common.enums.StatusEnum;
import com.dfire.common.enums.TriggerTypeEnum;
import com.dfire.common.exception.HeraException;
import com.dfire.common.exception.NoPermissionException;
import com.dfire.common.service.*;
import com.dfire.common.util.*;
import com.dfire.config.HeraGlobalEnv;
import com.dfire.config.RunAuth;
import com.dfire.config.UnCheckLogin;
import com.dfire.core.netty.worker.WorkClient;
import com.dfire.core.util.JobUtils;
import com.dfire.logs.ErrorLog;
import com.dfire.logs.MonitorLog;
import com.dfire.protocol.JobExecuteKind;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.WebAsyncTask;
import springfox.documentation.annotations.ApiIgnore;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * desc:
 *
 * @author scx
 * @create 2020/06/19
 */
@Controller
@Api("调度中心操作")
@RequestMapping("/scheduleCenter")
public class ScheduleOperatorController extends BaseHeraController {


    private final HeraJobService heraJobService;
    private final HeraJobActionService heraJobActionService;
    private final HeraGroupService heraGroupService;
    private final HeraJobHistoryService heraJobHistoryService;
    private final HeraUserService heraUserService;
    private final HeraPermissionService heraPermissionService;
    private final WorkClient workClient;
    private final HeraHostGroupService heraHostGroupService;
    private final HeraSsoService heraSsoService;

    private final Set<Long> cancelSet = new HashSet<>();
    private final HeraJobMonitorService jobMonitorService;

    public ScheduleOperatorController(HeraJobMonitorService jobMonitorService, HeraJobActionService heraJobActionService, @Qualifier("heraJobMemoryService") HeraJobService heraJobService, @Qualifier("heraGroupMemoryService") HeraGroupService heraGroupService,  HeraJobHistoryService heraJobHistoryService,  HeraUserService heraUserService, HeraPermissionService heraPermissionService, WorkClient workClient, HeraHostGroupService heraHostGroupService, HeraSsoService heraSsoService) {
        this.heraJobActionService = heraJobActionService;
        this.heraJobService = heraJobService;
        this.heraGroupService = heraGroupService;
        this.heraJobHistoryService = heraJobHistoryService;
        this.heraUserService = heraUserService;
        this.heraPermissionService = heraPermissionService;
        this.workClient = workClient;
        this.heraHostGroupService = heraHostGroupService;
        this.heraSsoService = heraSsoService;
        this.jobMonitorService = jobMonitorService;
    }

    @PostMapping("/moveNodes")
    @ResponseBody
    @ApiOperation("任务批量移动接口")
    public JsonResponse moveNodes(@ApiParam(value = "任务id集合，用,分割",required = true)String ids
            ,@ApiParam(value = "之前的所在组目录",required = true) String oldParent
            ,@ApiParam(value = "新的所在组目录",required = true) String newParent) {
        if (ids != null) {
            for (String id : ids.split(Constants.COMMA)) {
                moveNode(id, newParent, oldParent);
            }
        }
        return new JsonResponse(true, "成功");
    }

    @RequestMapping(value = "/moveNode", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation("任务单个移动接口")
    public JsonResponse moveNode(@ApiParam(value = "任务id", required = true) String id
            , @ApiParam(value = "新的所在组目录", required = true) String parent
            , @ApiParam(value = "之前的所在组目录", required = true) String lastParent) throws NoPermissionException {
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

    @RequestMapping(value = "/updatePermission", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    @RunAuth(typeIndex = 1)
    @ApiOperation("权限更新接口")
    public JsonResponse updatePermission(@RequestParam("id") @ApiParam(value = "任务id", required = true) String id,
                                         @RequestParam("type") @ApiParam(value = "类型：job,group", required = true) RunAuthType type,
                                         @RequestParam("uIdS") @ApiParam(value = "ssoid集合", required = true) String names) {

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

    @RequestMapping(value = "/check", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation("权限检测接口")
    public JsonResponse check(@ApiParam(value = "任务id", required = true) String id) {
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
    @ApiIgnore
    public JsonResponse getJobImpact(Integer jobId, Integer type, Integer auto) throws NoPermissionException, HeraException {
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
    @ApiOperation("取消任务")
    public WebAsyncTask<JsonResponse> cancelJob(@ApiParam(value = "执行记录id", required = true) Long historyId, @ApiParam(value = "任务id", required = true) String jobId) {
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
                addJobRecord(Integer.parseInt(jobId), "", RecordTypeEnum.CANCEL, ssoName, ownerId);
                try {
                    res = workClient.cancelJobFromWeb(JobExecuteKind.ExecuteKind.ScheduleKind, historyId);
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

    @RequestMapping(value = "/updateSwitch", method = RequestMethod.POST)
    @ResponseBody
    @RunAuth
    @ApiOperation("开启/关闭任务")
    public JsonResponse updateSwitch(@ApiParam(value = "任务id", required = true) Integer id, @ApiParam(value = "切换状态，0：关闭，1开启", required = true) Integer status) throws HeraException {
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

    private void updateJobToMaster(boolean result, Integer id) {
        if (result) {
            doAsync(() -> {
                try {
                    workClient.updateJobFromWeb(String.valueOf(id));
                } catch (ExecutionException | InterruptedException | TimeoutException e) {
                    ErrorLog.error("更新异常", e);
                }
            });
        }
    }

    @RequestMapping(value = "/execute", method = RequestMethod.GET)
    @ResponseBody
    @UnCheckLogin
    @ApiOperation("http外部调用，执行任务")
    public JsonResponse publicExecute(@RequestParam @ApiParam(value = "map参数类型，替换任务的配置信息", required = true) Map<String, String> params) throws ExecutionException, InterruptedException, NoPermissionException, HeraException, TimeoutException {
        String secret = params.get("secret");
        String decrypt = PasswordUtils.aesDecrypt(secret);
        if (decrypt == null) {
            return new JsonResponse(false, "解密失败，请询问管理员");
        }
        String[] split = decrypt.split(";");
        if (split.length != 2) {
            return new JsonResponse(false, "解密失败，请询问管理员");
        }
        HeraAction action = heraJobActionService.findLatestByJobId(Long.parseLong((split[0])));
        if (action == null) {
            return new JsonResponse(false, "找不到版本");
        }
        addJobRecord(Integer.parseInt(split[0]), "远程执行任务", RecordTypeEnum.REMOTE, getIp() + ":" + split[1], String.valueOf(heraUserService.findByName(split[1]).getId()));
        MonitorLog.info("远程调用:{}", JSONObject.toJSONString(params));
        HeraJob heraJob = heraJobService.findById(Integer.parseInt(split[0]));
        Map<String, String> configs = StringUtil.convertStringToMap(heraJob.getConfigs());
        configs.putAll(params);
        heraJob.setConfigs(StringUtil.convertMapToString(configs));
        heraJobService.update(heraJob);
        return execute(action.getId(), 2, split[1]);
    }


    /**
     * 手动执行任务
     *
     * @param actionId
     * @return
     */
    @RequestMapping(value = "/manual", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation("手动执行接口")
    public JsonResponse execute(@JsonSerialize(using = ToStringSerializer.class) @ApiParam(value = "版本id", required = true) Long actionId
            , @ApiParam(value = "触发类型，2手动执行，3手动恢复，6超级恢复", required = true) Integer triggerType,
                                @RequestParam(required = false) @ApiParam(value = "任务执行组", required = false) String execUser) throws InterruptedException, ExecutionException, HeraException, TimeoutException {
        if (actionId == null) {
            return new JsonResponse(false, "请先生成版本再执行");
        }
        if (execUser == null) {
            checkPermission(ActionUtil.getJobId(actionId), RunAuthType.JOB);
        }
        TriggerTypeEnum triggerTypeEnum = TriggerTypeEnum.parser(triggerType);
        if (triggerTypeEnum == null) {
            return new JsonResponse(false, " 无法识别的触发类型，请联系管理员");
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
        return new JsonResponse(true, String.valueOf(actionId));
    }

    @RequestMapping(value = "/updateJobMessage", method = RequestMethod.POST)
    @ResponseBody
    @RunAuth(idIndex = -1)
    @ApiOperation("更新任务信息")
    public JsonResponse updateJobMessage(@ApiParam(value = "任务vo对象", required = true) HeraJobVo heraJobVo) {
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
        int maxTimeOut = HeraGlobalEnv.getTaskTimeout() * 60;
        if (newJob.getMustEndMinute() > maxTimeOut) {
            return new JsonResponse(false, "超出最大超时限制,最大为:" + maxTimeOut);
        } else if (newJob.getMustEndMinute() == 0) {
            newJob.setMustEndMinute(60);
        }
        if (StringUtils.isNotBlank(newJob.getDependencies())) {
            if (!newJob.getDependencies().equals(memJob.getDependencies())) {
                List<HeraJob> relation = heraJobService.getAllJobDependencies();

                DagLoopUtil dagLoopUtil = new DagLoopUtil(heraJobService.selectMaxId());

                for (HeraJob job : relation) {
                    String dependencies;
                    if (job.getId() == newJob.getId()) {
                        dependencies = newJob.getDependencies();
                    } else {
                        dependencies = job.getDependencies();
                    }
                    if (StringUtils.isNotBlank(dependencies)) {
                        String[] split = dependencies.split(",");
                        for (String s : split) {
                            HeraJob memById = heraJobService.findMemById(Integer.parseInt(s));
                            if (memById == null) {
                                return new JsonResponse(false, "依赖任务:" + s + "不存在");
                            }
                            dagLoopUtil.addEdge(job.getId(), Integer.parseInt(s));
                        }
                    }
                }

                if (dagLoopUtil.isLoop()) {
                    return new JsonResponse(false, "出现环形依赖，请检测依赖关系:" + dagLoopUtil.getLoop());
                }
            }
        }


        newJob.setAuto(memJob.getAuto());

        String ssoName = getSsoName();
        String ownerId = getOwnerId();
        Integer update = heraJobService.update(newJob);
        if (update == null || update == 0) {
            return new JsonResponse(false, "更新失败");
        }
        doAsync(() -> {
            //脚本更新
            if (!newJob.getScript().equals(memJob.getScript())) {
                addJobRecord(newJob.getId(), memJob.getScript(), RecordTypeEnum.SCRIPT, ssoName, ownerId);
            }
            //依赖任务更新
            if (newJob.getDependencies() != null && !newJob.getDependencies().equals(memJob.getDependencies())) {
                addJobRecord(newJob.getId(), memJob.getDependencies(), RecordTypeEnum.DEPEND, ssoName, ownerId);
            }
            //定时表达式更新
            if (newJob.getCronExpression() != null && !newJob.getCronExpression().equals(memJob.getCronExpression())) {
                addJobRecord(newJob.getId(), memJob.getCronExpression(), RecordTypeEnum.CRON, ssoName, ownerId);
            }
            //执行区域更新
            if (newJob.getAreaId() != null && !newJob.getAreaId().equals(memJob.getAreaId())) {
                addJobRecord(newJob.getId(), memJob.getAreaId(), RecordTypeEnum.AREA, ssoName, ownerId);
            }
            //脚本配置项变化
            if (newJob.getConfigs() != null && !newJob.getConfigs().equals(memJob.getConfigs())) {
                addJobRecord(newJob.getId(), memJob.getConfigs(), RecordTypeEnum.CONFIG, ssoName, ownerId);
            }
            if (newJob.getRunType() != null && !newJob.getRunType().equals(memJob.getRunType())) {
                addJobRecord(newJob.getId(), memJob.getRunType(), RecordTypeEnum.RUN_TYPE, ssoName, ownerId);
            }
        });

        return new JsonResponse(true, "更新成功");
    }




    @GetMapping(value = "previewJob")
    @ResponseBody
    @ApiOperation("预览任务接口")
    public JsonResponse previewJobScript(@ApiParam(value = "任务版本id", required = true) Long actionId) throws HeraException {
        return new JsonResponse("", true, getRenderScript(actionId));
    }

    private String getRenderScript(Long actionId) throws HeraException {
        return this.getRenderScript(actionId, null);
    }

    private String getRenderScript(Long actionId, String script) throws HeraException {
        Integer jobId = ActionUtil.getJobId(String.valueOf(actionId));
        HeraJobBean jobBean = heraGroupService.getUpstreamJobBean(jobId);
        if (script == null) {
            script = jobBean.getHeraJob().getScript();
        }

        RenderHierarchyProperties renderHierarchyProperties = new RenderHierarchyProperties(jobBean.getHierarchyProperties());
        script = JobUtils.previewScript(renderHierarchyProperties.getAllProperties(), script);
        script = RenderHierarchyProperties.render(script, String.valueOf(actionId).substring(0, 12));
        return script;
    }

    @RequestMapping(value = "/updateGroupMessage", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation("更新组信息")
    @RunAuth(authType = RunAuthType.GROUP, idIndex = 1)
    public JsonResponse updateGroupMessage(@ApiParam(value = "组信息对象", required = true) HeraGroupVo groupVo, @ApiParam(value = "组id", required = true) String groupId) {
        groupVo.setId(StringUtil.getGroupId(groupId));

        Map<String, String> configMap = StringUtil.configsToMap(groupVo.getSelfConfigs());
        configEncry(configMap);

        groupVo.setSelfConfigs(StringUtil.mapToConfigs(configMap));
        HeraGroup heraGroup = BeanConvertUtils.convert(groupVo);

        String ownerId = getOwnerId();
        String ssoName = getSsoName();
        doAsync(() -> {
            HeraGroup lastGroup = heraGroupService.findById(heraGroup.getId());
            if (lastGroup.getConfigs() != null && !lastGroup.getConfigs().equals(heraGroup.getConfigs())) {
                addGroupRecord(heraGroup.getId(), lastGroup.getConfigs(), RecordTypeEnum.CONFIG, ssoName, ownerId);
            }
        });

        boolean res = heraGroupService.update(heraGroup) > 0;
        return new JsonResponse(res, res ? "更新成功" : "系统异常,请联系管理员");
    }

    private void configEncry(Map<String, String> config) {
        Optional.ofNullable(config)
                .ifPresent(cxf -> cxf.entrySet()
                        .stream()
                        .filter(pair -> pair.getKey().toLowerCase().contains(Constants.SECRET_PREFIX))
                        .forEach(entry -> entry.setValue(PasswordUtils.aesEncryption(entry.getValue()))));
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

    @RequestMapping(value = "/deleteJob", method = RequestMethod.POST)
    @ResponseBody
    @RunAuth(typeIndex = 1)
    @ApiOperation("删除任务接口")
    public JsonResponse deleteJob(@ApiParam(value = "任务id", required = true) String id,
                                  @ApiParam(value = "类型：job、group", required = true) RunAuthType type) throws NoPermissionException {
        Integer xId = StringUtil.getGroupId(id);
        boolean res;
        String check = heraJobService.checkDependencies(xId, type);
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

}
