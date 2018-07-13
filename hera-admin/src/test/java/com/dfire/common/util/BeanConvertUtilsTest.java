package com.dfire.common.util;

import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.vo.HeraJobVo;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xiaosuda on 2018/6/13.
 */
public class BeanConvertUtilsTest {

    @Test
    public void convertToHeraJob() {
        HeraJobVo heraJobVo = new HeraJobVo();
        HeraJob heraJob = BeanConvertUtils.convertToHeraJob(heraJobVo);

        System.out.println(heraJob.getResources());
    }

    @Test
    public void stringToMap() {
        String x = "a=b";
        Map<String,String> res = new HashMap<>();

        BeanConvertUtils.stringToMap(x, res);
        Assert.assertTrue(res.size() == 1);
    }
}