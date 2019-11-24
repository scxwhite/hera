//package com.dfire.controller;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//
///**
// * @Author: <a href="mailto:">jiaxue.pjx@alibaba-inc.com</a>
// * @Description:
// * @Date: Created in 下午10:11 2019/10/4
// */
//@Controller
//@Slf4j
//public class RootController {
//
//    final static String LOCAL_PATH = "http://localhost:4444/";
//
//    @GetMapping("/")
//    public String root(Model model, boolean isDebug) {
//
//            model.addAttribute("index_css", LOCAL_PATH + "css/index.css");
//            model.addAttribute("index_js", LOCAL_PATH + "js/index.js");
//            return "index";
//    }
//}
