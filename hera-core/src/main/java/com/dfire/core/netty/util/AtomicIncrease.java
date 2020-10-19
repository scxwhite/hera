package com.dfire.core.netty.util;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午8:00 2018/4/16
 * @desc 生成全局唯一id, 标志所有的netty层request与之对应的response,
 * 当request.id == response.id,标志一次请求结束，可解除对request的response的监听ResponseListener,标志一次请求结束
 */
public class AtomicIncrease {

    private static AtomicInteger rid = new AtomicInteger();

    public static int getAndIncrement() {

        return rid.getAndIncrement();
    }
}
