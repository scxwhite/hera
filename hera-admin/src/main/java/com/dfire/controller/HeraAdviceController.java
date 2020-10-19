package com.dfire.controller;

import com.dfire.common.entity.HeraAdvice;
import com.dfire.common.entity.model.JsonResponse;
import com.dfire.common.service.HeraAdviceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author xiaosuda
 * @date 2018/12/5
 */
@Api("建议/留言接口")
@Controller
@RequestMapping("/adviceController")
public class HeraAdviceController extends BaseHeraController {


    @Autowired
    private HeraAdviceService heraAdviceService;


    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView toPage() {
        ModelAndView mv = new ModelAndView("/bugReport");
        mv.addObject("allMsg", heraAdviceService.getAll());
        return mv;
    }


    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation("添加留言接口")
    public JsonResponse addAdvice(@ApiParam(value = "建议对象", required = true) HeraAdvice heraAdvice) {
        if (heraAdvice.getMsg().contains("<") || heraAdvice.getMsg().contains(">")) {
            return new JsonResponse(false, "不允许输入特殊符号");
        }
        boolean res = heraAdviceService.addAdvice(heraAdvice);
        return new JsonResponse(res, res ? "添加成功" : "添加失败");
    }
}
