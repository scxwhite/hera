package com.dfire.controller;

import com.dfire.common.entity.HeraDebugHistory;
import com.dfire.common.entity.HeraFile;
import com.dfire.common.entity.vo.HeraFileTreeNodeVo;
import com.dfire.common.service.HeraDebugHistoryService;
import com.dfire.common.service.HeraFileService;
import com.dfire.core.message.Protocol.ExecuteKind;
import com.dfire.core.netty.worker.WorkClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.WebAsyncTask;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

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
        System.out.println(heraFile.getId());
        heraFile.setOwner("biadmin");
        heraFileService.insert(heraFile);
        return "sucess";
    }

    @RequestMapping(value = "/find", method = RequestMethod.GET)
    @ResponseBody
    public HeraFile getHeraFile(HeraFile heraFile) {
        System.out.println(heraFile.getId());
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

    @GetMapping("/debug")
    public WebAsyncTask<String> debug(String id, String script) throws ExecutionException, InterruptedException {

        WebAsyncTask<String> webAsyncTask = new WebAsyncTask<>(3000, new Callable<String>() {
            @Override
            public String call() throws Exception {
                HeraFile file = heraFileService.findById(id);
                HeraDebugHistory history = HeraDebugHistory.builder()
                        .fileId(id)
                        .script(script)
                        .owner("pjx")
                        .build();
                if(file.getType().equals("1")) {
                    history.setRunType("shell");
                } else if(file.getType().equals("2")) {
                    history.setRunType("hive");
                }
                String newId = debugHistoryService.insert(history);
                System.out.println(history.getId());
                workClient.executeJobFromWeb(ExecuteKind.DebugKind, newId);

                return file.getId();
            }
        });
        return webAsyncTask;
    }


    @RequestMapping(value = "findDebugHistory", method = RequestMethod.GET)
    @ResponseBody
    public List<HeraDebugHistory> findDebugHistory(String fileId) {
        List<HeraDebugHistory> list = debugHistoryService.findByFileId(fileId);
        return list;
    }

}
