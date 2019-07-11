package com.dfire.controller;

import com.dfire.common.entity.HeraHostGroup;
import com.dfire.common.entity.model.JsonResponse;
import com.dfire.common.entity.model.TableResponse;
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
    public TableResponse getAll() {

        List<HeraHostGroup> groupList = heraHostGroupService.getAll();

        if (groupList == null) {
            return new TableResponse(-1, "查询失败");
        }
        return new TableResponse(groupList.size(), 0, groupList);

    }


    @RequestMapping(value = "add", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse add(HeraHostGroup heraHostGroup) {
        boolean res = heraHostGroupService.insert(heraHostGroup) > 0;
        return new JsonResponse(res, res ? "新增成功" : "新增失败");
    }

    @RequestMapping(value = "update", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse update(HeraHostGroup heraHostGroup) {
        boolean update = heraHostGroupService.update(heraHostGroup) > 0;
        return new JsonResponse(update, update ? "更新成功" : "更新失败");
    }

    @RequestMapping(value = "del", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse del(Integer id) {
        int res = heraHostGroupService.delete(id);
        return new JsonResponse(res > 0, res > 0 ? "删除成功" : "删除失败");
    }
}
