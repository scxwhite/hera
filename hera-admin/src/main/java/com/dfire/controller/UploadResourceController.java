package com.dfire.controller;

import com.dfire.common.entity.model.JsonResponse;
import com.dfire.common.enums.LogTypeEnum;
import com.dfire.common.enums.RecordTypeEnum;
import com.dfire.common.util.HierarchyProperties;
import com.dfire.config.HeraGlobalEnv;
import com.dfire.core.job.JobContext;
import com.dfire.core.job.ProcessJob;
import com.dfire.core.job.UploadEmrFileJob;
import com.dfire.core.job.UploadLocalFileJob;
import com.dfire.logs.ErrorLog;
import com.dfire.logs.HeraLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import springfox.documentation.annotations.ApiIgnore;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午1:22 2018/7/21
 * @desc
 */
@Controller
@RequestMapping("/uploadResource")
@Api("资源上传接口")
public class UploadResourceController extends BaseHeraController {


    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    @ApiOperation("上传接口")
    public JsonResponse uploadResource(@ApiIgnore MultipartHttpServletRequest request) {
        Map<String, MultipartFile> fileMap = request.getFileMap();
        String fileName = null;
        String newFilePath;
        File file;
        JsonResponse response = new JsonResponse();
        response.setSuccess(true);
        StringBuilder resMsg = new StringBuilder();
        try {
            for (String key : fileMap.keySet()) {
                MultipartFile multipartFile = fileMap.get(key);
                fileName = multipartFile.getOriginalFilename();
                int lastPoint = fileName.lastIndexOf(".");
                fileName = fileName.substring(0, lastPoint) + "_" + System.nanoTime() + fileName.substring(lastPoint);
                newFilePath = HeraGlobalEnv.getWorkDir() + File.separator + fileName;
                file = new File(newFilePath);
                multipartFile.transferTo(file);
                JobContext jobContext = JobContext.builder().build();
                jobContext.setProperties(new HierarchyProperties(new HashMap<>(16)));
                jobContext.setWorkDir(HeraGlobalEnv.getWorkDir());
                ProcessJob uploadJob;
                int exitCode;
                //如果是emr集群 先 scp 到emr固定机器上
                if (HeraGlobalEnv.isEmrJob()) {
                    //默认都是hadoop用户
                    uploadJob = new UploadEmrFileJob(jobContext, file.getAbsolutePath(), fileName, HeraGlobalEnv.emrFixedHost);
                    exitCode = uploadJob.run();
                } else {
                    uploadJob = new UploadLocalFileJob(jobContext, file.getAbsolutePath(), HeraGlobalEnv.getHdfsUploadPath());
                    exitCode = uploadJob.run();
                }
                HeraLog.info("controller upload file command {}", uploadJob.getCommandList().toString());
                if (exitCode == 0) {
                    addRecord(LogTypeEnum.UPLOAD, 1, fileName, RecordTypeEnum.UPLOAD, getSsoName(), getOwnerId());
                    resMsg.append(fileName).append("[上传成功]: ").append(HeraGlobalEnv.getHdfsUploadPath()).append(fileName).append("<br>");
                } else {
                    response.setSuccess(false);
                    addRecord(LogTypeEnum.UPLOAD, 1, fileName + "上传失败", RecordTypeEnum.UPLOAD, getSsoName(), getOwnerId());
                    resMsg.append(fileName).append("[上传失败]: ").append(HeraGlobalEnv.getHdfsUploadPath()).append(fileName).append("<br>");
                }
                //删除临时文件
                file.delete();
            }
        } catch (Exception e) {
            ErrorLog.error("上传文件异常:" + fileName, e);
            return new JsonResponse(false, "上传文件异常，请联系管理员");
        }
        response.setMessage(resMsg.toString());
        return response;
    }
}
