package com.dfire.controller;

import com.dfire.common.entity.HeraHostGroup;
import com.dfire.common.entity.model.JsonResponse;
import com.dfire.common.service.HeraHostGroupService;
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
    public JsonResponse saveOrUpdate(HeraHostGroup heraHostGroup) {
        heraHostGroupService.insert(heraHostGroup);
        return new JsonResponse();
    }

    @RequestMapping(value = "del", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse del(Integer id) {
        int res = heraHostGroupService.delete(id);
        return new JsonResponse(res > 0, res > 0 ? "删除成功" : "删除失败");
    }
}
