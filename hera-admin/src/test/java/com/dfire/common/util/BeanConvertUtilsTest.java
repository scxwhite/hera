package com.dfire.common.util;

import com.dfire.common.entity.HeraJob;
import com.dfire.common.entity.vo.HeraJobVo;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by xiaosuda on 2018/6/13.
 */
public class BeanConvertUtilsTest {

    @Test
    public void convertToHeraJob() {
        HeraJobVo heraJobVo = new HeraJobVo();
        heraJobVo.setId("1");
        HeraJob heraJob = BeanConvertUtils.convertToHeraJob(heraJobVo);

        System.out.println(heraJob.getResources());
    }
}