package com.dfire.core.event.handler;

import com.dfire.common.util.StringUtil;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by xiaosuda on 2018/7/12.
 */
public class JobHandlerTest {

    @Test
    public void getActionId() {

        boolean b = StringUtil.actionIdToJobId("201807060000000001", "1");
        System.out.println(b);

    }
}