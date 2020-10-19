package com.dfire.common.service.impl;

import com.dfire.common.entity.HeraRecord;
import com.dfire.common.entity.model.TablePageForm;
import com.dfire.common.entity.vo.PageHelper;
import com.dfire.common.mapper.HeraRecordMapper;
import com.dfire.common.service.HeraRecordService;
import com.dfire.common.util.ActionUtil;
import com.dfire.common.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * desc:
 *
 * @author scx
 * @create 2019/07/16
 */
@Service
public class HeraRecordServiceImpl implements HeraRecordService {


    @Autowired
    private HeraRecordMapper recordMapper;

    @Override
    public boolean add(HeraRecord record) {
        if (record.getContent() == null) {
            record.setContent("");
        }

        record.setGmtModified(ActionUtil.getCurrentMillis());
        record.setGmtCreate(ActionUtil.getCurrentMillis());
        return recordMapper.insert(record) > 1;
    }

    @Override
    public Pair<Integer, List<HeraRecord>> selectByPage(TablePageForm pageForm) {
        return Pair.of(recordMapper.allCount(), recordMapper.selectByPage(pageForm.getStartPos(), pageForm.getLimit()));
    }

    @Override
    public Pair<Integer, List<HeraRecord>> selectByPage(TablePageForm pageForm, Integer gid) {
        return Pair.of(recordMapper.countByGid(gid), recordMapper.selectByGid(pageForm.getStartPos(), pageForm.getLimit(), gid));
    }

    @Override
    public Map<String, Object> findPageByLogId(PageHelper pageHelper) {
        Map<String, Object> res = new HashMap<>(2);
        res.put("total", recordMapper.selectCountById(pageHelper.getJobId()));
        res.put("rows", recordMapper.findPageByLogId(pageHelper.getOffset(), pageHelper.getPageSize(), pageHelper.getJobId()));
        return res;
    }
}
