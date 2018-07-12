package com.dfire.controller;

import com.dfire.common.entity.HeraDebugHistory;
import com.dfire.common.entity.HeraFile;
import com.dfire.common.entity.vo.HeraDebugHistoryVo;
import com.dfire.common.entity.vo.HeraFileTreeNodeVo;
import com.dfire.common.service.HeraDebugHistoryService;
import com.dfire.common.service.HeraFileService;
import com.dfire.common.util.BeanConvertUtils;
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
import java.util.stream.Collectors;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:34 2018/1/13
 * @desc 开发中心页面控制器
 */
@Slf4j
@Controller
@RequestMapping("/developCenter")
public class DevelopCenterController {

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
        List<HeraFileTreeNodeVo> list = heraFileService.buildFileTree("biadmin");
        return list;
    }

    @RequestMapping(value = "/addFile", method = RequestMethod.GET)
    @ResponseBody
    public String addFileAndFolder(HeraFile heraFile) {
        heraFile.setOwner("biadmin");
        heraFileService.insert(heraFile);
        return "success";
    }

    @RequestMapping(value = "/find", method = RequestMethod.GET)
    @ResponseBody
    public HeraFile getHeraFile(HeraFile heraFile) {
        heraFile.setOwner("biadmin");
        HeraFile file = heraFileService.findById(heraFile.getId());
        return file;
    }

    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    @ResponseBody
    public String delete(HeraFile heraFile) {
        HeraFile file = heraFileService.findById(heraFile.getId());
        String response = "";
        if (file.getType().equals("1")) {
            response = "文件夹不能删除";
        } else if (file.getType().equals("2")) {
            heraFileService.delete(heraFile.getId());
            response = "删除成功";
        }
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

            HeraDebugHistory history = HeraDebugHistory.builder()
                    .fileId(heraFile.getId())
                    .script(heraFile.getContent())
                    .startTime(new Date())
                    .owner("pjx")
                    .build();
            if (file.getType().equals("1")) {
                history.setRunType("hive");
            } else if (file.getType().equals("2")) {
                history.setRunType("shell");
            }
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
    public List<HeraDebugHistoryVo> findDebugHistory(String fileId) {
        List<HeraDebugHistory> list = debugHistoryService.findByFileId(fileId);
        List<HeraDebugHistoryVo> result = list.stream().map(heraDebugHistory -> {
            HeraDebugHistoryVo historyVo = BeanConvertUtils.convert(heraDebugHistory);
            return historyVo;
        }).collect(Collectors.toList());
        return result;
    }

    /**
     * 取消执行脚本
     *
     * @param id
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @RequestMapping(value = "/cancelJob", method = RequestMethod.POST)
    @ResponseBody
    public WebAsyncTask<String> cancelJob(String id) throws ExecutionException, InterruptedException {
        ExecuteKind kind = ExecuteKind.DebugKind;
        return new WebAsyncTask<>(3000, () ->
                workClient.cancelJobFromWeb(kind, id));

    }

}
