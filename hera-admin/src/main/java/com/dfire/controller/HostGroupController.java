package com.dfire.controller;

import com.dfire.common.entity.HeraHostGroup;
import com.dfire.common.entity.model.JsonResponse;
import com.dfire.common.entity.model.TableResponse;
import com.dfire.common.service.HeraHostGroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
@Api("机器组相关接口")
public class HostGroupController {

    private final HeraHostGroupService heraHostGroupService;



    public HostGroupController(HeraHostGroupService heraHostGroupService) {
        this.heraHostGroupService = heraHostGroupService;
    }






    @RequestMapping(value = "list", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation("获取所有机器组列表")
    public TableResponse getAll() {

        List<HeraHostGroup> groupList = heraHostGroupService.getAll();

        if (groupList == null) {
            return new TableResponse(-1, "查询失败");
        }
        return new TableResponse(groupList.size(), 0, groupList);

    }


    @RequestMapping(value = "add", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation("添加机器组")
    public JsonResponse add(@ApiParam(value = "机器组", required = true) HeraHostGroup heraHostGroup) {
        boolean res = heraHostGroupService.insert(heraHostGroup) > 0;
        return new JsonResponse(res, res ? "新增成功" : "新增失败");
    }

    @RequestMapping(value = "update", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation("更新机器组")
    public JsonResponse update(@ApiParam(value = "机器组", required = true) HeraHostGroup heraHostGroup) {
        boolean update = heraHostGroupService.update(heraHostGroup) > 0;
        return new JsonResponse(update, update ? "更新成功" : "更新失败");
    }

    @RequestMapping(value = "del", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation("删除机器组")
    public JsonResponse del(@ApiParam(value = "机器组id", required = true) Integer id) {
        int res = heraHostGroupService.delete(id);
        return new JsonResponse(res > 0, res > 0 ? "删除成功" : "删除失败");
    }
}
