package com.dfire.controller;

import com.dfire.common.constants.Constants;
import com.dfire.common.entity.HeraDebugHistory;
import com.dfire.common.entity.HeraFile;
import com.dfire.common.entity.model.JsonResponse;
import com.dfire.common.entity.vo.HeraFileTreeNodeVo;
import com.dfire.common.service.HeraDebugHistoryService;
import com.dfire.common.service.HeraFileService;
import com.dfire.core.config.HeraGlobalEnvironment;
import com.dfire.core.netty.worker.WorkClient;
import com.dfire.protocol.JobExecuteKind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.WebAsyncTask;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:34 2018/1/13
 * @desc 开发中心页面控制器
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
    public List<HeraFileTreeNodeVo> initFileTree() {
        String owner = getOwner();
        return heraFileService.buildFileTree(owner);
    }

    @RequestMapping(value = "/addFile", method = RequestMethod.GET)
    @ResponseBody
    public Integer addFileAndFolder(HeraFile heraFile) {
        Integer parent = heraFile.getParent();
        HeraFile parentFile = heraFileService.findById(parent);
        if (Constants.FILE_ALL_NAME.equals(parentFile.getOwner())) {
            heraFile.setOwner(Constants.FILE_ALL_NAME);
        } else {
            heraFile.setOwner(getOwner());
        }
        heraFile.setHostGroupId(HeraGlobalEnvironment.defaultWorkerGroup);
        return heraFileService.insert(heraFile);
    }

    @RequestMapping(value = "/find", method = RequestMethod.GET)
    @ResponseBody
    public HeraFile getHeraFile(HeraFile heraFile) {
        return heraFileService.findById(heraFile.getId());
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
        return new JsonResponse(res, res ? "删除成功" : "删除失败");
    }

    @RequestMapping(value = "/rename", method = RequestMethod.GET)
    @ResponseBody
    public String rename(HeraFile heraFile) {
        return heraFileService.updateFileName(heraFile) > 0 ? "更新成功" : "更新失败";
    }


    /**
     * 手动执行脚本
     *
     * @param heraFile
     * @return
     */
    @RequestMapping(value = "/debug", method = RequestMethod.POST)
    @ResponseBody
    public WebAsyncTask<JsonResponse> debug(@RequestBody HeraFile heraFile) {


        String owner = getOwner();
        return new WebAsyncTask<>(10000, () -> {
            Map<String, Object> res = new HashMap<>(2);
            HeraFile file = heraFileService.findById(heraFile.getId());
            if (file == null) {
                return new JsonResponse(false, "脚本已被删除");
            }
            String name = file.getName();
            String runType;
            file.setContent(heraFile.getContent());
            heraFileService.updateContent(heraFile);
            HeraDebugHistory history = HeraDebugHistory.builder()
                    .fileId(file.getId())
                    .script(heraFile.getContent())
                    .startTime(new Date())
                    .owner(Constants.FILE_ALL_NAME.equals(file.getOwner()) ? owner : file.getOwner())
                    .hostGroupId(file.getHostGroupId() == 0 ? HeraGlobalEnvironment.defaultWorkerGroup : file.getHostGroupId())
                    .build();
            int suffixIndex = name.lastIndexOf(Constants.POINT);
            if (suffixIndex == -1) {
                return new JsonResponse(false, "无后缀名,请设置支持的后缀名[.sh .hive .spark]");
            }
            String suffix = name.substring(suffixIndex);
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
            workClient.executeJobFromWeb(JobExecuteKind.ExecuteKind.DebugKind, newId);
            res.put("fileId", file.getId());
            res.put("debugId", newId);
            return new JsonResponse(true, "执行成功", res);
        });
    }

    /**
     * 手动执行选中的代码
     *
     * @param heraFile
     * @return
     */
    @RequestMapping(value = "/debugSelectCode", method = RequestMethod.POST)
    @ResponseBody
    public WebAsyncTask<JsonResponse> debugSelectCode(@RequestBody HeraFile heraFile) {

        String owner = getOwner();
        return new WebAsyncTask<JsonResponse>(HeraGlobalEnvironment.getRequestTimeout(), () -> {
            Map<String, Object> res = new HashMap<>(2);
            HeraFile file = heraFileService.findById(heraFile.getId());
            file.setContent(heraFile.getContent());
            String name = file.getName();
            String runType;
            HeraDebugHistory history = HeraDebugHistory.builder()
                    .fileId(file.getId())
                    .script(heraFile.getContent())
                    .startTime(new Date())
                    .owner(owner)
                    .hostGroupId(file.getHostGroupId() == 0 ? HeraGlobalEnvironment.defaultWorkerGroup : file.getHostGroupId())
                    .build();

            int suffixIndex = name.lastIndexOf(Constants.POINT);
            if (suffixIndex == -1) {
                return new JsonResponse(false, "无后缀名,请设置支持的后缀名[.sh .hive .spark]");
            }
            String suffix = name.substring(suffixIndex);
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
            workClient.executeJobFromWeb(JobExecuteKind.ExecuteKind.DebugKind, newId);
            res.put("fileId", file.getId());
            res.put("debugId", newId);
            return new JsonResponse(true, "执行成功", res);
        });
    }

    /**
     * 获取脚本执行历史
     *
     * @param fileId
     * @return
     */
    @RequestMapping(value = "findDebugHistory", method = RequestMethod.GET)
    @ResponseBody
    public List<HeraDebugHistory> findDebugHistory(Integer fileId) {
        return debugHistoryService.findByFileId(fileId);
    }

    /**
     * 取消执行脚本
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/cancelJob", method = RequestMethod.GET)
    @ResponseBody
    public WebAsyncTask<String> cancelJob(String id) {
        return new WebAsyncTask<>(3000, () ->
                workClient.cancelJobFromWeb(JobExecuteKind.ExecuteKind.DebugKind, id));

    }


    @RequestMapping(value = "/getLog", method = RequestMethod.GET)
    @ResponseBody
    public HeraDebugHistory getJobLog(Integer id) {
        return debugHistoryService.findLogById(id);
    }

    @RequestMapping(value = "saveScript", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse saveScript(@RequestBody HeraFile heraFile) {
        boolean result = heraFileService.updateContent(heraFile) > 0;

        return new JsonResponse(result, result ? "保存成功" : "保存失败");
    }
}
