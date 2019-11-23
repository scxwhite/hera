package com.dfire.common.service;

import com.dfire.common.entity.HeraRecord;
import com.dfire.common.entity.model.TablePageForm;
import com.dfire.common.entity.vo.PageHelper;
import com.dfire.common.util.Pair;

import java.util.List;
import java.util.Map;

/**
 * desc:
 *
 * @author scx
 * @create 2019/07/16
 */
public interface HeraRecordService {

    boolean add(HeraRecord record);

    Pair<Integer, List<HeraRecord>> selectByPage(TablePageForm pageForm);

    Pair<Integer, List<HeraRecord>> selectByPage(TablePageForm pageForm, Integer gid);

    Map<String, Object> findPageByLogId(PageHelper pageHelper);
}