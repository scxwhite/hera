package com.dfire.controller;

import com.dfire.common.constants.Constants;
import com.dfire.common.entity.HeraDebugHistory;
import com.dfire.common.entity.HeraFile;
import com.dfire.common.entity.model.JsonResponse;
import com.dfire.common.enums.RecordTypeEnum;
import com.dfire.common.exception.HeraException;
import com.dfire.common.service.HeraDebugHistoryService;
import com.dfire.common.service.HeraFileService;
import com.dfire.config.HeraGlobalEnv;
import com.dfire.core.netty.worker.WorkClient;
import com.dfire.logs.MonitorLog;
import com.dfire.protocol.JobExecuteKind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:34 2018/1/13
 * @desc 开发中心
 */
@Controller
@RequestMapping("/developCenter")
public class DevelopCenterController extends BaseHeraController {

    @Autowired
    @Qualifier("heraFileMemoryService")
    private HeraFileService heraFileService;
    @Autowired
    private HeraDebugHistoryService debugHistoryService;
    @Autowired
    private WorkClient workClient;


    @RequestMapping
    public String dev() {
        return "developCenter/developCenter.index";
    }

    @RequestMapping(value = "/init", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse initFileTree() {
        return new JsonResponse(true, heraFileService.buildFileTree(getOwner()));
    }

    @RequestMapping(value = "/addFile", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse addFileAndFolder(HeraFile heraFile) {
        Integer parent = heraFile.getParent();
        HeraFile parentFile = heraFileService.findById(parent);
        if (Constants.FILE_ALL_NAME.equals(parentFile.getOwner())) {
            heraFile.setOwner(Constants.FILE_ALL_NAME);
        } else {
            heraFile.setOwner(getOwner());
        }
        Integer id = heraFileService.insert(heraFile);

        addDebugRecord(id, heraFile.getName(), RecordTypeEnum.Add, getSsoName(), getOwnerId());
        return new JsonResponse(true, id);
    }

    @RequestMapping(value = "/find", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse getHeraFile(HeraFile heraFile) {
        return new JsonResponse(true, heraFileService.findById(heraFile.getId()));
    }

    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse delete(HeraFile heraFile) {
        HeraFile file = heraFileService.findById(heraFile.getId());
        if (Constants.FILE_SELF.equals(file.getName())) {
            return new JsonResponse(false, "无法删除个人文档");
        } else if (Constants.FILE_ALL.equals(file.getName())) {
            return new JsonResponse(false, "无法删除共享文档");
        }
        boolean res = heraFileService.delete(heraFile.getId()) > 0;
        addDebugRecord(heraFile.getId(), heraFile.getName(), RecordTypeEnum.DELETE, getSsoName(), getOwnerId());
        return new JsonResponse(res, res ? "删除成功" : "删除失败");
    }

    @RequestMapping(value = "/rename", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse rename(HeraFile heraFile) {
        return new JsonResponse(true, heraFileService.updateFileName(heraFile) > 0 ? "更新成功" : "更新失败");
    }


    /**
     * 手动执行脚本
     *
     * @param heraFile
     * @return
     */
    @RequestMapping(value = "/debug", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse debug(@RequestBody HeraFile heraFile) throws ExecutionException, InterruptedException {
        String owner = getOwner();
        HeraFile file = heraFileService.findById(heraFile.getId());
        if (file == null) {
            return new JsonResponse(false, "脚本已被删除");
        }
        String name = file.getName();
        file.setContent(heraFile.getContent());
        heraFileService.updateContent(heraFile);
        HeraDebugHistory history = HeraDebugHistory.builder()
                .fileId(file.getId())
                .script(heraFile.getContent())
                .startTime(new Date())
                .owner(Constants.FILE_ALL_NAME.equals(file.getOwner()) ? owner : file.getOwner())
                .hostGroupId(file.getHostGroupId() == 0 ? HeraGlobalEnv.defaultWorkerGroup : file.getHostGroupId())
                .build();
        return executeJob(name, history, getSsoName());
    }

    /**
     * 手动执行选中的代码
     *
     * @param heraFile
     * @return
     */
    @RequestMapping(value = "/debugSelectCode", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse debugSelectCode(@RequestBody HeraFile heraFile) throws HeraException, ExecutionException, InterruptedException {
        String owner = getOwner();
        HeraFile file = heraFileService.findById(heraFile.getId());
        file.setContent(heraFile.getContent());
        String name = file.getName();
        HeraDebugHistory history = HeraDebugHistory.builder()
                .fileId(file.getId())
                .script(heraFile.getContent())
                .startTime(new Date())
                .owner(owner)
                .hostGroupId(file.getHostGroupId() == 0 ? HeraGlobalEnv.defaultWorkerGroup : file.getHostGroupId())
                .build();

        return executeJob(name, history, getSsoName());
    }

    private JsonResponse executeJob(String name, HeraDebugHistory history, String ssoName) throws ExecutionException, InterruptedException {
        int suffixIndex = name.lastIndexOf(Constants.POINT);
        if (suffixIndex == -1) {
            return new JsonResponse(false, "无后缀名,请设置支持的后缀名[.sh .hive .spark]");
        }
        String suffix = name.substring(suffixIndex);
        String runType;
        if ((Constants.HIVE_SUFFIX).equalsIgnoreCase(suffix)) {
            runType = Constants.HIVE_FILE;
        } else if ((Constants.SHELL_SUFFIX).equalsIgnoreCase(suffix)) {
            runType = Constants.SHELL_FILE;
        } else if ((Constants.SPARK_SUFFIX).equalsIgnoreCase(suffix)) {
            runType = Constants.SPARK_FILE;
        } else {
            return new JsonResponse(false, "暂未支持的后缀名[" + suffix + "],请设置支持的后缀名[.sh .hive .spark]");
        }
        history.setRunType(runType);
        String newId = debugHistoryService.insert(history);
        String ownerId = getOwnerId();
        doAsync(() -> addDebugRecord(history.getFileId(), history.getId(), RecordTypeEnum.Execute, ssoName, ownerId));
        workClient.executeJobFromWeb(JobExecuteKind.ExecuteKind.DebugKind, newId);
        Map<String, Object> res = new HashMap<>(2);
        res.put("fileId", history.getFileId());
        res.put("debugId", newId);
        return new JsonResponse(true, "执行成功", res);
    }

    /**
     * 获取脚本执行历史
     *
     * @param fileId
     * @return
     */
    @RequestMapping(value = "findDebugHistory", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse findDebugHistory(Integer fileId) {
        return new JsonResponse(true, debugHistoryService.findByFileId(fileId));
    }

    /**
     * 取消执行脚本
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/cancelJob", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse cancelJob(String id) throws ExecutionException, InterruptedException {
        return new JsonResponse(true, workClient.cancelJobFromWeb(JobExecuteKind.ExecuteKind.DebugKind, id));
    }


    @RequestMapping(value = "/getLog", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse getJobLog(Integer id) {
        return new JsonResponse(true, debugHistoryService.findLogById(id));
    }


    @RequestMapping(value = "/check", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse check(Integer id) {
        if (checkPermission(id)) {
            return new JsonResponse(true, "查询成功", true);
        } else {
            return new JsonResponse(true, "无权限", false);
        }
    }

    @RequestMapping(value = "/moveNode", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse moveNode(Integer id, Integer parent, Integer lastParent) {
        if (!checkPermission(id)) {
            return new JsonResponse(false, "无权限，移动失败");
        }
        boolean res = heraFileService.updateParentById(id, parent);
        if (res) {
            addDebugRecord(id, lastParent + "=>" + parent, RecordTypeEnum.MOVE, getSsoName(), getOwnerId());
            MonitorLog.info("开发中心任务{}【移动】:{} ----> {}", id, lastParent, parent);
            return new JsonResponse(true, "移动成功");
        } else {
            return new JsonResponse(false, "移动失败,请联系管理员");
        }
    }

    private boolean checkPermission(Integer id) {
        if (HeraGlobalEnv.getAdmin().equals(getOwner())) {
            return true;
        }
        HeraFile heraFile = heraFileService.findById(id);
        return heraFile != null && heraFile.getOwner().equals(getOwner());
    }

    @RequestMapping(value = "saveScript", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse saveScript(@RequestBody HeraFile heraFile) {
        boolean result = heraFileService.updateContent(heraFile) > 0;
        return new JsonResponse(result, result ? "保存成功" : "保存失败");
    }

}
