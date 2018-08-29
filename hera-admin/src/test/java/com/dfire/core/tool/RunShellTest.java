package com.dfire.core.tool;

import com.dfire.core.netty.worker.WorkContext;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by xiaosuda on 2018/8/6.
 */
public class RunShellTest {

    @Test
    public void runShell() throws IOException {

        RunShell shell = new RunShell("uptime");

        shell.run();


        System.out.println(shell.getResult());

    }
}