package com.dfire.controller;

import com.alibaba.fastjson.JSONObject;
import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.HeraRecord;
import com.dfire.common.entity.HeraUser;
import com.dfire.common.entity.model.JsonResponse;
import com.dfire.common.entity.model.TablePageForm;
import com.dfire.common.entity.model.TableResponse;
import com.dfire.common.entity.vo.HeraRecordVo;
import com.dfire.common.entity.vo.PageHelper;
import com.dfire.common.enums.LogTypeEnum;
import com.dfire.common.enums.RecordTypeEnum;
import com.dfire.common.service.HeraJobService;
import com.dfire.common.service.HeraUserService;
import com.dfire.common.util.ActionUtil;
import com.dfire.common.util.Pair;
import com.dfire.config.HeraGlobalEnv;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

/**
 * desc:
 *
 * @author scx
 * @create 2019/07/17
 */
@Controller
@Api(value = "历史记录查看接口")
@RequestMapping("/record")
public class RecordController extends BaseHeraController {


    @Autowired
    private HeraUserService heraUserService;

    @Autowired
    private HeraJobService heraJobService;

    @RequestMapping
    public String record() {
        return "/jobManage/record.index";
    }


    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation("所有历史查看")
    public TableResponse listRecord(@ApiParam(value = "分页", required = true) TablePageForm pageForm) {
        String ownerName;
        boolean isAdmin = HeraGlobalEnv.getAdmin().equals(ownerName = getOwner());

        Pair<Integer, List<HeraRecord>> pair;
        Map<Integer, String> cacheOwner = new HashMap<>();
        if (isAdmin) {
            pair = recordService.selectByPage(pageForm);
        } else {
            pair = recordService.selectByPage(pageForm, Integer.parseInt(getOwnerId()));
        }
        List<HeraRecordVo> voList = pair.snd().stream().map(record -> {
            HeraRecordVo recordVo = new HeraRecordVo();
            BeanUtils.copyProperties(record, recordVo);
            recordVo.setCreateTime(ActionUtil.getDefaultFormatterDate(new Date(record.getGmtCreate())));
            recordVo.setType(RecordTypeEnum.parseById(record.getType()).getType());
            if (isAdmin) {
                if (!cacheOwner.containsKey(record.getGid())) {
                    cacheOwner.put(record.getGid(), Optional.of(heraUserService.findById(record.getGid())).map(HeraUser::getName).orElse("none"));
                }
                recordVo.setGName(cacheOwner.get(record.getGid()));
            } else {
                recordVo.setGName(ownerName);
            }
            return recordVo;
        }).collect(Collectors.toList());
        return new TableResponse(pair.fst(), 0, voList);
    }


    @RequestMapping(value = "/find", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation("根据类型查询历史记录")
    @SuppressWarnings("unchecked")
    public JsonResponse getRecord(@ApiParam(value = "分页", required = true) PageHelper pageHelper
            ) {
        Map<Integer, String> cacheOwner = new HashMap<>(2);
        Map<String, Object> res = recordService.findPageByLogId(pageHelper);
        String rows = "rows";
        List<HeraRecordVo> vos = ((List<HeraRecord>) res.get(rows)).stream().map(record -> {
            HeraRecordVo recordVo = new HeraRecordVo();
            BeanUtils.copyProperties(record, recordVo);
            recordVo.setType(RecordTypeEnum.parseById(record.getType()).getType());
            recordVo.setCreateTime(ActionUtil.getDefaultFormatterDate(new Date(record.getGmtCreate())));
            if (!cacheOwner.containsKey(record.getGid())) {
                cacheOwner.put(record.getGid(), Optional.ofNullable(heraUserService.findById(record.getGid())).map(HeraUser::getName).orElse("无"));
            }
            recordVo.setGName(cacheOwner.get(record.getGid()));
            return recordVo;
        }).collect(Collectors.toList());
        res.put(rows, vos);
        return new JsonResponse(true, res);

    }


    @RequestMapping("/now")
    @ResponseBody
    @ApiOperation("查询记录的最新状态")
    public JsonResponse findNow(@ApiParam(value = "记录id", required = true) Integer logId
            , @ApiParam(value = "记录类型:job,group,debug,upload,user", required = true) String logType
            , @ApiParam(value = "操作类型:RecordTypeEnum", required = true) String type) {
        LogTypeEnum typeEnum = LogTypeEnum.parseByName(logType);
        JSONObject resData = new JSONObject();
        String content = "";
        String runType = "Shell";
        switch (typeEnum) {
            case JOB:
                HeraJob heraJob = heraJobService.findById(logId);
                runType = heraJob.getRunType();
                switch (RecordTypeEnum.parseByName(type)) {
                    case SCRIPT:
                    case DELETE:
                        content = heraJob.getScript();
                        break;
                    case RUN_TYPE:
                        content = heraJob.getRunType();
                        break;
                    case CRON:
                        content = heraJob.getCronExpression();
                        break;
                    case CONFIG:
                        content = heraJob.getConfigs();
                        break;
                    case AREA:
                        content = heraJob.getAreaId();
                        break;
                    case SWITCH:
                        content = String.valueOf(heraJob.getAuto());
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
        resData.put("content", content);
        resData.put("runType", runType);

        return new JsonResponse(true, resData);
    }

}
