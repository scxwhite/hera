package com.dfire.controller;

import com.dfire.common.entity.HeraDebugHistory;
import com.dfire.common.entity.HeraFile;
import com.dfire.common.entity.vo.HeraFileTreeNodeVo;
import com.dfire.common.service.HeraDebugHistoryService;
import com.dfire.common.service.HeraFileService;
import com.dfire.common.vo.RestfulResponse;
import com.dfire.core.config.HeraGlobalEnvironment;
import com.dfire.core.message.Protocol.ExecuteKind;
import com.dfire.core.netty.worker.WorkClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.WebAsyncTask;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:34 2018/1/13
 * @desc 开发中心页面控制器
 */
@Slf4j
@Controller
@RequestMapping("/developCenter")
public class DevelopCenterController extends BaseHeraController {

    @Autowired
    HeraFileService heraFileService;
    @Autowired
    HeraDebugHistoryService debugHistoryService;
    @Autowired
    WorkClient workClient;


    @RequestMapping
    public String dev() {
        return "developCenter/developCenter.index";
    }

    @RequestMapping(value = "/init", method = RequestMethod.POST)
    @ResponseBody
    public List<HeraFileTreeNodeVo> initFileTree() {
        String owner = getOwner();
        List<HeraFileTreeNodeVo> list = heraFileService.buildFileTree(owner);
        return list;
    }

    @RequestMapping(value = "/addFile", method = RequestMethod.GET)
    @ResponseBody
    public String addFileAndFolder(HeraFile heraFile) {
        heraFile.setOwner(getOwner());
        heraFile.setHostGroupId(HeraGlobalEnvironment.defaultWorkerGroup);
        String id = heraFileService.insert(heraFile);
        return id;
    }

    @RequestMapping(value = "/find", method = RequestMethod.GET)
    @ResponseBody
    public HeraFile getHeraFile(HeraFile heraFile) {
        heraFile.setOwner(getOwner());
        HeraFile file = heraFileService.findById(heraFile.getId());
        return file;
    }

    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    @ResponseBody
    public String delete(HeraFile heraFile) {
        HeraFile file = heraFileService.findById(heraFile.getId());
        String response = "";
        heraFileService.delete(heraFile.getId());
        response = "删除成功";
        return response;
    }

    /**
     * 手动执行脚本
     *
     * @param heraFile
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @RequestMapping(value = "/debug", method = RequestMethod.POST)
    @ResponseBody
    public WebAsyncTask<String> debug(@RequestBody HeraFile heraFile) throws ExecutionException, InterruptedException {

        return new WebAsyncTask<>(3000, () -> {
            HeraFile file = heraFileService.findById(heraFile.getId());
            String name = file.getName();
            String runType = "1";
            file.setContent(heraFile.getContent());
            heraFileService.updateContent(heraFile);

            HeraDebugHistory history = HeraDebugHistory.builder()
                    .fileId(file.getId())
                    .script(heraFile.getContent())
                    .startTime(new Date())
                    .owner(file.getOwner())
                    .hostGroupId(file.getHostGroupId())
                    .build();
            String postfix = name.substring(name.lastIndexOf("."));
            if (".hive".equalsIgnoreCase(postfix)) {
                runType = "hive";
            } else if (".sh".equalsIgnoreCase(postfix)) {
                runType = "shell";
            }
            history.setRunType(runType);
            String newId = debugHistoryService.insert(history);
            workClient.executeJobFromWeb(ExecuteKind.DebugKind, newId);
            return file.getId();
        });
    }

    /**
     * 手动执行选中的代码
     *
     * @param heraFile
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @RequestMapping(value = "/debugSelectCode", method = RequestMethod.POST)
    @ResponseBody
    public WebAsyncTask<String> debugSelectCode(@RequestBody HeraFile heraFile) {

        String owner = getOwner();
        return new WebAsyncTask<>(3000, () -> {
            HeraFile file = heraFileService.findById(heraFile.getId());
            file.setContent(heraFile.getContent());
            String name = file.getName();
            String runType = "1";
            HeraDebugHistory history = HeraDebugHistory.builder()
                    .fileId(file.getId())
                    .script(heraFile.getContent())
                    .startTime(new Date())
                    .owner(owner)
                    .hostGroupId(file.getHostGroupId())
                    .build();
            String postfix = name.substring(name.lastIndexOf("."));
            if (".hive".equalsIgnoreCase(postfix)) {
                runType = "hive";
            } else if (".sh".equalsIgnoreCase(postfix)) {
                runType = "shell";
            }
            history.setRunType(runType);
            String newId = debugHistoryService.insert(history);
            workClient.executeJobFromWeb(ExecuteKind.DebugKind, newId);
            return file.getId();
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
    public List<HeraDebugHistory> findDebugHistory(String fileId) {
        List<HeraDebugHistory> list = debugHistoryService.findByFileId(fileId);
        return list;
    }

    /**
     * 取消执行脚本
     *
     * @param id
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @RequestMapping(value = "/cancelJob", method = RequestMethod.GET)
    @ResponseBody
    public WebAsyncTask<String> cancelJob(String id) throws ExecutionException, InterruptedException {
        ExecuteKind kind = ExecuteKind.DebugKind;
        return new WebAsyncTask<>(3000, () ->
                workClient.cancelJobFromWeb(kind, id));

    }


    @RequestMapping(value = "/getLog", method = RequestMethod.GET)
    @ResponseBody
    public HeraDebugHistory getJobLog(Integer id) {
        return debugHistoryService.findLogById(id);
    }

    @RequestMapping(value = "saveScript", method = RequestMethod.POST)
    @ResponseBody
    public RestfulResponse saveScript(@RequestBody HeraFile heraFile) {
        int result = heraFileService.updateContent(heraFile);
        return RestfulResponse.builder().success(true).msg("保存成功").results(result).build();
    }


}
