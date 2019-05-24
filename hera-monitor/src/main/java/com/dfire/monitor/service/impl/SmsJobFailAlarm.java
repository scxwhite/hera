package com.dfire.monitor.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.dfire.common.constants.Constants;
import com.dfire.common.constants.TimeFormatConstant;
import com.dfire.common.entity.HeraAction;
import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.HeraJobMonitor;
import com.dfire.common.entity.HeraUser;
import com.dfire.common.service.HeraJobActionService;
import com.dfire.common.service.HeraJobMonitorService;
import com.dfire.common.service.HeraJobService;
import com.dfire.common.service.HeraUserService;
import com.dfire.common.util.ActionUtil;
import com.dfire.common.util.HeraDateTool;
import com.dfire.config.HeraGlobalEnvironment;
import com.dfire.event.HeraJobFailedEvent;
import com.dfire.logs.ErrorLog;
import com.dfire.logs.ScheduleLog;
import com.dfire.monitor.config.Alarm;
import com.dfire.monitor.service.JobFailAlarm;
import jdk.nashorn.internal.runtime.GlobalConstants;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.*;

/**
 *
 * 短信告警 使用者自己实现
 * @author lujunjie
 * @date 2019/5/24
 */
@Alarm("smsJobFailAlarm")
public class SmsJobFailAlarm implements JobFailAlarm {
    @Autowired
    @Qualifier("heraJobMemoryService")
    private HeraJobService heraJobService;

    @Autowired
    private HeraJobActionService heraJobActionService;

    @Autowired
    private HeraJobMonitorService heraJobMonitorService;

    @Autowired
    private HeraUserService heraUserService;

    public void sendDankeSms(DankeSmsRequest req) {
        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(HeraGlobalEnvironment.getSmsDankeUrl());

        try {
            String content = JSON.toJSONString(req);
            System.out.println(content);

            RequestEntity se = new StringRequestEntity(content, "application/json", "UTF-8");
            method.setRequestEntity(se);

            int code = client.executeMethod(method);
            if (code != 200) {
                ErrorLog.error("send sms error: " + code);
            }
            String responseBodyAsString = method.getResponseBodyAsString(2000);
            System.out.println("sms the response body is " + responseBodyAsString);
        }
        catch (UnsupportedCharsetException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void alarm(HeraJobFailedEvent failedEvent) {
        String actionId = failedEvent.getActionId();
        Integer jobId = ActionUtil.getJobId(actionId);
        if (jobId == null) {
            return;
        }
        HeraJob heraJob = heraJobService.findById(jobId);
        if (heraJob.getAuto() != 1) {
            return;
        }
        HeraAction heraAction = heraJobActionService.findById(actionId);
        Set<String> mobiles = new HashSet<>();
        try {
            // job所有人及关注人员都接收短信
            if (Constants.PUB_ENV.equals(HeraGlobalEnvironment.getEnv())) {
                HeraUser user = heraUserService.findByName(heraJob.getOwner());
                mobiles.add(user.getPhone().trim());
            }

            HeraJobMonitor monitor = heraJobMonitorService.findByJobId(heraJob.getId());
            if (monitor != null) {
                String ids = monitor.getUserIds();
                String[] id = ids.split(Constants.COMMA);
                for (String anId : id) {
                    if (StringUtils.isBlank(anId)) {
                        continue;
                    }
                    HeraUser user = heraUserService.findById(Integer.parseInt(anId));
                    if (user != null && user.getPhone() != null) {
                        mobiles.add(user.getPhone().trim());
                    }
                }
            }

            DankeSmsRequest req = new DankeSmsRequest();
            req.setTemplateCode(HeraGlobalEnvironment.getSmsDankeTemplateCode());
            req.addParam("notice_type", "告警");
            req.addParam("task_name", heraJob.getName() + "(" + jobId + ")");
            req.addParam("task_desc", heraJob.getDescription());
            req.addParam("task_owner", heraJob.getOwner());
            req.addParam("notice_time",  HeraDateTool.DateToString(heraAction.getStatisticEndTime(), TimeFormatConstant.YYYY_MM_DD_HH_MM_SS));
            req.addParam("task_state", heraAction.getStatus());
            req.addParam("phone", StringUtils.join(mobiles, Constants.COMMA));

            this.sendDankeSms(req);
        } catch (Exception e) {
            e.printStackTrace();
            ErrorLog.error("发送短信失败");
        }
    }

    public class DankeSmsRequest{
        private ArrayList<Map<String, String>> params = new ArrayList<>();
        private String templateCode;

        public void addParam(String key, String value){
            Map<String, String> kv = new HashMap<>();
            kv.put("key", key);
            kv.put("value", value);

            params.add(kv);
        }

        public void setParams(ArrayList<Map<String, String>> params){
            this.params = params;
        }
        public ArrayList<Map<String, String>> getParams(){ return params; }

        @JSONField(name = "template_code")
        public String getTemplateCode() {
            return templateCode;
        }
        public void setTemplateCode(String templateCode) {
            this.templateCode = templateCode;
        }
    }

    public static void main(String[] args) {
        HeraDateTool.DateToString(null, TimeFormatConstant.YYYY_MM_DD_HH_MM_SS);

        Map<String, String> kv = new HashMap<>();
        kv.put("key", null);
        kv.put("value", null);

        System.out.println(JSON.toJSONString(kv));
    }
}
