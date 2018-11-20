package com.dfire.core.tool;

import com.dfire.protocol.RpcWorkInfo.OSInfo;
import com.dfire.protocol.RpcWorkInfo.ProcessMonitor;
import com.dfire.protocol.RpcWorkInfo.WorkInfo;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xiaosuda
 * @date 2018/11/12
 */
public class OsProcessJob extends RunShell {


    private WorkInfo workInfo;

    public OsProcessJob() {
        super("top -b -n 1");
    }

    @Override
    public Integer run() {

        Integer exitCode = -1;
        try {
            exitCode = super.run();
            if (exitCode == 0) {
                String result = super.getResult();
                if (result != null) {
                    String[] lines = result.split("\n");
                    List<ProcessMonitor> processMonitors = new ArrayList<>();
                    float user = 0.0f, system = 0.0f, cpu = 0.0f,
                            swap = 0.0f, swapTotal = 0.0f, swapUsed = 0.0f, swapCached = 0.0f, swapFree = 0.0f,
                            mem = 0.0f, memTotal = 0.0f, memFree = 0.0f, memBuffers = 0.0f;
                    for (String line : lines) {
                        String[] words = line.trim().split("\\s+");
                        if (words.length > 0) {
                            String first = words[0];
                            if (StringUtils.isBlank(first)) {
                                continue;
                            }
                            if ("Cpu(s):".equals(first)) {
                                try {
                                    user = Float.parseFloat(words[1].replace("%us,", ""));
                                    system = Float.parseFloat(words[2].replace("%sy,", ""));
                                    cpu = Float.parseFloat(words[4].replace("%id,", ""));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else if ("Mem:".equals(first)) {
                                memTotal = parseKb(words[1]);
                                memFree = parseKb(words[5]);
                                memBuffers = parseKb(words[7]);
                            } else if ("Swap:".equals(first)) {
                                swapTotal = parseKb(words[1]);
                                swapUsed = parseKb(words[3]);
                                swapFree = parseKb(words[5]);
                                swapCached = parseKb(words[7]);

                            } else if (StringUtils.isNumeric(first)) {
                                try {
                                    processMonitors.add(ProcessMonitor.newBuilder()
                                            .setPid(words[0])
                                            .setUser(words[1])
                                            .setViri(words[4])
                                            .setRes(words[5])
                                            .setCpu(words[8])
                                            .setMem(words[9])
                                            .setTime(words[10])
                                            .setCommand(words[11])
                                            .build());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    }

                    mem = 1.0f - (memTotal / (memFree + memBuffers + swapCached));
                    swap = 1.0f - (swapTotal / (swapFree + swapCached));
                    workInfo = WorkInfo.newBuilder()
                            .setOSInfo(OSInfo.newBuilder()
                                    .setUser(user)
                                    .setSystem(system)
                                    .setCpu(cpu)
                                    .setSwap(swap)
                                    .setMem(mem)
                                    .build())
                            .addAllProcessMonitor(processMonitors).build();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return exitCode;
    }


    private float parseKb(String str) {
        float res;
        try {
            res = Float.parseFloat(str.replace("k", ""));
        } catch (Exception e) {
            res = 0.0f;
        }
        return res;
    }

    public WorkInfo getRes() {
        if (workInfo == null) {
            return WorkInfo.newBuilder().build();
        }
        return workInfo;
    }

}
