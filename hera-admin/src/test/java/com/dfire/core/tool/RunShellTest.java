package com.dfire.core.tool;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by xiaosuda on 2018/8/6.
 */
public class RunShellTest {

    @Test
    public void runShell() throws IOException {

        RunShell shell = new RunShell("ls /");

        shell.run();

        System.out.println(shell.getResult());

    }
}