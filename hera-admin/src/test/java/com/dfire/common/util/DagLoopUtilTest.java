package com.dfire.common.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by xiaosuda on 2018/7/10.
 */
public class DagLoopUtilTest {

    @Test
    public void isLoop() {

        DagLoopUtil dagLoopUtil = new DagLoopUtil(10);
        dagLoopUtil.addEdge(1, 2);
        dagLoopUtil.addEdge(2, 3);
        dagLoopUtil.addEdge(3, 1);
        assertTrue(dagLoopUtil.isLoop());
        System.out.println(dagLoopUtil.getLoop());
        dagLoopUtil.init();
        dagLoopUtil.addEdge(1, 2);
        dagLoopUtil.addEdge(2, 1);
        assertTrue(dagLoopUtil.isLoop());

        System.out.println(dagLoopUtil.getLoop());

        dagLoopUtil.init();
        dagLoopUtil.addEdge(2, 4);
        dagLoopUtil.addEdge(3, 4);
        dagLoopUtil.addEdge(1, 2);
        dagLoopUtil.addEdge(1, 3);
        dagLoopUtil.addEdge(4, 1);

        assertTrue(dagLoopUtil.isLoop());
        System.out.println(dagLoopUtil.getLoop());

    }

}