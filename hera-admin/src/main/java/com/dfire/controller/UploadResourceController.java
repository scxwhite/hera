package com.dfire.controller;

import com.dfire.common.entity.model.JsonResponse;
import com.dfire.common.util.HierarchyProperties;
import com.dfire.config.HeraGlobalEnvironment;
import com.dfire.core.job.JobContext;
import com.dfire.core.job.UploadLocalFileJob;
import com.dfire.logs.HeraLog;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
public class UploadResourceController {


    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public JsonResponse uploadResource(MultipartHttpServletRequest request) {
        if (HeraGlobalEnvironment.isEmrJob()) {
            return new JsonResponse(false, "EMR 环境下无法上传资源。");
        }
        Map<String, MultipartFile> fileMap = request.getFileMap();
        String fileName;
        String newFilePath;
        String newFileName = "";
        File file = null;
        JsonResponse jsonResponse = new JsonResponse();
        try {
            try {
                for (String key : fileMap.keySet()) {
                    MultipartFile multipartFile = fileMap.get(key);
                    fileName = multipartFile.getOriginalFilename();
                    String prefix = StringUtils.substringBefore(fileName, ".");
                    String suffix = StringUtils.substringAfter(fileName, ".");
                    newFileName = prefix + "-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + "." + suffix;
                    newFilePath = HeraGlobalEnvironment.getWorkDir() + File.separator + newFileName;
                    file = new File(newFilePath);
                    multipartFile.transferTo(file);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            JobContext jobContext = JobContext.builder().build();
            jobContext.setProperties(new HierarchyProperties(new HashMap<>(16)));
            jobContext.setWorkDir(HeraGlobalEnvironment.getWorkDir());
            UploadLocalFileJob uploadJob = new UploadLocalFileJob(jobContext, file.getAbsolutePath(), HeraGlobalEnvironment.getHdfsUploadPath());
            HeraLog.info("controller upload file command {}", uploadJob.getCommandList().toString());
            int exitCode = uploadJob.run();
            if (exitCode == 0) {
                jsonResponse.setSuccess(true);
                jsonResponse.setMessage(HeraGlobalEnvironment.getHdfsUploadPath() + newFileName);
                return jsonResponse;
            } else {
                jsonResponse.setSuccess(false);
                jsonResponse.setMessage("upload file error");
            }
        } catch (Exception e) {
            e.printStackTrace();
            HeraLog.info("upload file error", e);

        }
        return jsonResponse;
    }
}
