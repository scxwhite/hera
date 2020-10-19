package com.dfire.common.service;

import com.dfire.common.entity.HeraRerun;
import com.dfire.common.entity.model.TablePageForm;
import com.dfire.common.entity.vo.HeraRerunVo;
import com.dfire.common.util.Pair;

import java.util.List;

/**
 * @author xiaosuda
 * @date 2018/12/14
 */
public interface HeraRerunService {

    boolean add(HeraRerunVo heraRerun);

    HeraRerun findById(Integer id);

    HeraRerunVo findVoById(Integer id);

    boolean updateById(HeraRerun heraRerun);

    boolean updateById(HeraRerunVo heraRerunVo);

    boolean deleteById(Integer id);

    Pair<Integer,List<HeraRerunVo>> findByPage(TablePageForm pageForm,Integer status);

    List<HeraRerunVo> findByEnd(Integer isEnd);

    HeraRerunVo findRecordByTime(Long millis, Integer jobId, int isEnd);

    HeraRerunVo findByIdAndEnd(Integer jobId, int i);

    Integer findCntByJob(Integer jobId, int isEnd);

}
