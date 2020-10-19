package com.dfire.core.tool;

import org.junit.Test;

import java.io.IOException;

/**
 * Created by xiaosuda on 2018/8/6.
 */
public class RunShellTest {

    @Test
    public void runShell() throws IOException {

        RunShell shell = new RunShell("sh x.sh");
        shell.setDirectory("/Users/wyr/Desktop");
        shell.run();

        System.out.println(shell.getResult());

    }
}