package com.dfire.controller;

import com.dfire.common.entity.HeraJob;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.dfire.common.service.HeraJobService;


/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 2:24 2018/1/11
 * @desc
 */
@Controller
@RequestMapping(value="/jobs")
public class HeraJobController {

    @Autowired
    private HeraJobService heraJobService;

    @RequestMapping(value="/", method= RequestMethod.GET)
    @ResponseBody
    public HeraJob getJob() {
        // 处理"/books/"的GET请求，用来获取图书列表
        // 还可以通过@RequestParam传递参数来进行查询条件或者翻页信息的传递
        HeraJob heraJob = heraJobService.findById(675);
        return heraJob;
    }

    @ApiOperation(value="获取书籍列表", notes="")
    @RequestMapping(value={"/hh"}, method=RequestMethod.GET)
    public HeraJob getJob2() {
        HeraJob heraJob = heraJobService.findById(182);
        return heraJob;
    }
}
