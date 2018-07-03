package com.dfire.controller;

import com.dfire.common.entity.HeraHostGroup;
import com.dfire.common.service.HeraHostGroupService;
import com.dfire.common.vo.RestfulResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author xiaosuda
 * @date 2018/4/20
 */
@Controller
@RequestMapping("/hostGroup/")
public class HostGroupController {

    @Autowired
    private HeraHostGroupService heraHostGroupService;

    @RequestMapping(value = "list", method = RequestMethod.GET)
    @ResponseBody
    public List<HeraHostGroup> getAll() {
        return heraHostGroupService.getAll();
    }


    @RequestMapping(value = "saveOrUpdate", method = RequestMethod.POST)
    @ResponseBody
    public RestfulResponse saveOrUpdate(HeraHostGroup heraHostGroup) {
        heraHostGroupService.insert(heraHostGroup);
        return RestfulResponse.builder().build();
    }

    @RequestMapping(value = "del", method = RequestMethod.POST)
    @ResponseBody
    public RestfulResponse del(Integer id) {
        int res = heraHostGroupService.delete(id);
        return new RestfulResponse(res > 0, res > 0 ? "删除成功" : "删除失败");
    }
}
