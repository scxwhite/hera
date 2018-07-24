package com.dfire.controller;

import com.dfire.common.util.HierarchyProperties;
import com.dfire.common.vo.RestfulResponse;
import com.dfire.core.job.JobContext;
import com.dfire.core.job.UploadLocalFileJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午1:22 2018/7/21
 * @desc
 */
@Controller
@RequestMapping("/uploadResource")
@Slf4j
public class UploadResourceController {

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public RestfulResponse uploadResource(MultipartHttpServletRequest request, @RequestParam("id") Integer id) {
        Map<String, MultipartFile> fileMap = request.getFileMap();
        String fileName;
        String newFileName = "";
        File file = null;
        RestfulResponse restfulResponse = RestfulResponse.builder().build();
        try {
            try {
                for (String key : fileMap.keySet()) {
                    MultipartFile multipartFile = fileMap.get(key);
                    fileName = multipartFile.getOriginalFilename();
                    String prefix = StringUtils.substringBefore(fileName, ".");
                    String suffix = StringUtils.substringAfter(fileName, ".");
                    newFileName = "/opt/logs/spring-boot/" + prefix + "-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + "." + suffix;
                    file = new File(newFileName);
                    multipartFile.transferTo(file);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            JobContext jobContext = new JobContext();
            jobContext.setProperties(new HierarchyProperties(new HashMap<>()));
            UploadLocalFileJob uploadJob = new UploadLocalFileJob(jobContext, file.getAbsolutePath(), "/hera/hdfs-upload-dir");

            int exitCode = uploadJob.run();
            log.info("controller upload file command {}",uploadJob.getCommandList().toString());
            if (exitCode == 0) {
                restfulResponse.setSuccess(true);
                restfulResponse.setMsg(newFileName);
                return restfulResponse;
            } else {
                restfulResponse.setSuccess(false);
                restfulResponse.setMsg("upload file error");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info("upload file error");

        }
        return restfulResponse;
    }
}
