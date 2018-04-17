package com.dfire.controller;

import com.dfire.common.constant.JobRunType;
import com.dfire.common.entity.HeraDebugHistory;
import com.dfire.common.entity.HeraFile;
import com.dfire.common.entity.vo.HeraFileTreeNodeVo;
import com.dfire.common.mapper.HeraFileMapper;
import com.dfire.common.service.HeraDebugHistoryService;
import com.dfire.common.service.HeraFileService;
import com.dfire.core.netty.worker.WorkClient;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:34 2018/1/13
 * @desc 开发中心页面控制器
 */
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

        List<HeraFile> fileVoList = heraFileService.getAllUserHeraFiles("biadmin");
        List<HeraFileTreeNodeVo> list = fileVoList.stream().map(file -> {
            HeraFileTreeNodeVo vo = HeraFileTreeNodeVo.builder().id(file.getId()).name(file.getName()).build();
            if(file.getParent() == null ||StringUtils.isBlank(file.getParent())) {
                vo.setParent(null);
            } else {
                vo.setParent(file.getParent());
            }
            if(file.getType().equals("1") ) {
                vo.setIsParent(true);
            }else if(file.getType().equals("2")) {
                vo.setIsParent(false);
            }
            return vo;
        }).collect(Collectors.toList());
         return list;
    }

    @RequestMapping(value = "/addFile", method = RequestMethod.GET)
    @ResponseBody
    public String addFileAndFolder(HeraFile heraFile) {
        System.out.println(heraFile.getId());
        heraFile.setOwner("biadmin");
        heraFileService.addHeraFile(heraFile);
        return "sucess";
    }

    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    @ResponseBody
    public String delete(HeraFile heraFile) {
        HeraFile file = heraFileService.getHeraFile(heraFile.getId());
        String response = "";
        if(file.getType().equals("1")) {
            response =  "文件夹不能删除";
        } else if(file.getType().equals("2")) {
            heraFileService.deleteHeraFile(heraFile.getId());
            response = "删除成功";
        }
        return response;
    }

    @RequestMapping(value = "/debug", method = RequestMethod.GET)
    @ResponseBody
    public String debug(String fileId, String mode, String script, String hostGroupId) {
        HeraFile file = heraFileService.getHeraFile(fileId);


        HeraDebugHistory history = HeraDebugHistory.builder()
                .fileId(fileId)
                .script(script)
                .hostGroupId(hostGroupId)
                .jobRunType(JobRunType.parser(mode))
                .build();
        debugHistoryService.addHeraDebugHistory(history);
        System.out.println(history.getId());





        return "";
    }




}
