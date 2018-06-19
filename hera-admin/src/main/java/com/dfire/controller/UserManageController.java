package com.dfire.controller;

import com.alibaba.fastjson.JSONObject;
import com.dfire.common.entity.HeraUser;
import com.dfire.common.service.HeraUserService;
import com.dfire.common.vo.RestfulResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:10 2018/6/14
 * @desc
 */
@Controller
@RequestMapping("/userManage")
public class UserManageController {

    @Autowired
    private HeraUserService heraUserService;


    @RequestMapping(value = "/initUser", method = RequestMethod.POST)
    @ResponseBody
    public List<HeraUser> initUser() {
        List<HeraUser> list = heraUserService.getAll();
        return list;
    }

    @RequestMapping(value = "/editUser", method = RequestMethod.POST)
    @ResponseBody
    public RestfulResponse editUser(@RequestBody HeraUser user) {
        int result = heraUserService.update(user);
        RestfulResponse restfulResponse =  RestfulResponse
                .builder()
                .build();
        if (result > 0) {
            restfulResponse.setMsg("更新成功");
            restfulResponse.setCode(String.valueOf(result));
            restfulResponse.setSuccess(true);
        }
        return restfulResponse;
    }

    /**
     * operateType: 1,执行删除操作，2，执行审核通过操作，3，执行审核拒绝操作
     *
     * @param param
     * @return
     */

    @RequestMapping(value = "/operateUser", method = RequestMethod.POST)
    @ResponseBody
    public RestfulResponse operateUser(@RequestBody String param) {
        JSONObject jsonObject = JSONObject.parseObject(param);
        String id = (String) jsonObject.get("id");
        String operateType = (String) jsonObject.get("operateType");

        RestfulResponse response = RestfulResponse.builder().build();
        int result;

        OperateTypeEnum operateTypeEnum = OperateTypeEnum.parse(operateType);
        if(operateTypeEnum == OperateTypeEnum.Delete) {
            result = heraUserService.delete(id);
            if(result > 0) {
                response.setMsg("删除成功");
                response.setCode(String.valueOf(result));
                response.setSuccess(true);
            }
        } else if(operateTypeEnum == OperateTypeEnum.Approve) {
            result = heraUserService.updateEffective(id, "1");
            if(result > 0) {
                response.setMsg("审核通过");
                response.setCode(String.valueOf(result));
                response.setSuccess(true);
            }
        } else if(operateTypeEnum == OperateTypeEnum.Refuse) {
            result = heraUserService.updateEffective(id, "0");
            if(result > 0) {
                response.setMsg("审核拒绝");
                response.setCode(String.valueOf(result));
                response.setSuccess(false);
            }
        }
        return response;
    }

    public enum OperateTypeEnum {
        Delete("1"), Approve("2"), Refuse("3");
        private String operateType;
        @Override
        public String toString() {
            return operateType;
        }
         OperateTypeEnum(String type) {
            this.operateType = type;
        }

        public static OperateTypeEnum parse(String operateType) {
            Optional<OperateTypeEnum> option = Arrays.asList(OperateTypeEnum.values())
                    .stream()
                    .filter(operate -> operate.toString().equals(operateType))
                    .findAny();
            return  option.get();
        }
    }
}
