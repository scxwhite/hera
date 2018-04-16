package com.dfire.core.netty.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author xiaosuda
 * @date 2018/4/13
 */
public class AtomicIncrease {

    private static AtomicInteger rid=new AtomicInteger();

    public static int getAndIncrement(){

        return rid.getAndIncrement();
    }
}
