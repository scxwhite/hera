package com.dfire.controller;

import com.dfire.common.entity.HeraAdvice;
import com.dfire.common.entity.model.JsonResponse;
import com.dfire.common.service.HeraAdviceService;
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
@Controller
@RequestMapping("/adviceController")
public class HeraAdviceController extends BaseHeraController {


    @Autowired
    private HeraAdviceService heraAdviceService;


    @RequestMapping()
    public ModelAndView toPage() {
        ModelAndView mv = new ModelAndView("/bugReport");
        mv.addObject("allMsg", heraAdviceService.getAll());
        return mv;
    }


    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse addAdvice(HeraAdvice heraAdvice) {
        boolean res = heraAdviceService.addAdvice(heraAdvice);
        return new JsonResponse(res, res ? "添加成功" : "添加失败");
    }
}
