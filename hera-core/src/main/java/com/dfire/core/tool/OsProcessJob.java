package com.dfire.core.tool;

import com.dfire.core.config.HeraGlobalEnvironment;
import com.dfire.logs.HeraLog;
import com.dfire.protocol.RpcWorkInfo;
import com.dfire.protocol.RpcWorkInfo.OSInfo;
import com.dfire.protocol.RpcWorkInfo.ProcessMonitor;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xiaosuda
 * @date 2018/11/12
 */
public class OsProcessJob extends RunShell {


    private OSInfo osInfo;

    private List<ProcessMonitor> processMonitors;


    private final String KB = "K";
    private final String MB = "M";
    private final String GB = "G";
    private final String numRegex = "[^.&&\\D]";

    private final String command;

    {
        switch (HeraGlobalEnvironment.getSystemEnum()) {
            case LINUX:
                command = "top -b -a -n 1";
                break;
            case MAC:
                command = "top -s 0 -n 30 -o cpu -O mem -l 2  -stats pid,user,cpu,time,mem,command,cpu_me";
                break;
            default:
                String oSName = System.getProperties().getProperty("os.name");
                command = "echo 未知的操作系统类型" + oSName;
                HeraLog.error("未知的操作系统类型{}", oSName);
        }
    }

    public OsProcessJob() {
        super();
    }

    @Override
    public Integer run() {
        if (command == null) {
            throw new NullPointerException("指令为空，无法执行Shell");
        }
        super.setCommand(command);
        switch (HeraGlobalEnvironment.getSystemEnum()) {
            case LINUX:
                return runLinux();
            case MAC:
                return runMac();
            default:
                return -1;
        }
    }

    private Integer runMac() {
        Integer exitCode = -1;
        try {
            exitCode = super.run();
            if (exitCode == 0) {
                String result = super.getResult();
                result = result.substring(result.lastIndexOf("Processes"));
                String[] lines = result.split("\n");
                float user = 0.0f, sys = 0.0f, cpu = 0.0f;
                float memUsed = 0.0f, memWired = 0.0f, memUnUsed = 0.0f;
                String regex = "\\s+";
                String[] words;
                processMonitors = new ArrayList<>();
                for (String line : lines) {
                    words = line.split(regex);
                    if (StringUtils.isBlank(words[0])) {
                        continue;
                    }
                    if ("CPU".equals(words[0])) {
                        user = parseFloat(words[2]);
                        sys = parseFloat(words[4]);
                        cpu = parseFloat(words[6]);
                    } else if ("PhysMem:".equals(words[0])) {
                        memUsed = parseFloat(words[1]);
                        memWired = parseFloat(words[3]);
                        memUnUsed = parseFloat(words[5]);
                    } else if (StringUtils.isNumeric(words[0])) {
                        processMonitors.add(RpcWorkInfo.ProcessMonitor.newBuilder()
                                .setPid(words[0])
                                .setUser(words[1])
                                .setCpu(words[2])
                                .setTime(words[3])
                                .setMem(String.valueOf(getMemPercent(words[4], memUnUsed + memUsed)))
                                .setRes(words[4])
                                .setCommand(words[5])
                                .build());
                    }

                    osInfo = OSInfo.newBuilder()
                            .setUser(user)
                            .setSystem(sys)
                            .setCpu(cpu)
                            .setSwap(100f)
                            .setMem(memUsed / (memUnUsed + memUsed) * 100f)
                            .build();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return exitCode;
    }

    private Float getMemPercent(String used, float total) {
        return getMb(used) / total * 100;
    }

    private Float getMb(String used) {
        float res = 0.0f;
        if (used.contains(KB)) {
            res = parseFloat(used) / 1024;
        } else if (used.contains(MB)) {
            res = parseFloat(used);
        } else if (used.contains(GB)) {
            res = parseFloat(used) * 1024;
        }

        return res;
    }

    /**
     * used 数字必须连续
     *
     * @param used
     * @return
     */

    private float parseFloat(String used) {
        try {
            return Float.parseFloat(used.replaceAll(numRegex, ""));
        } catch (Exception e) {
            return 0f;
        }
    }


    private Integer runLinux() {
        Integer exitCode = -1;
        try {
            exitCode = super.run();
            if (exitCode == 0) {
                String result = super.getResult();
                if (result != null) {
                    String[] lines = result.split("\n");
                    float user = 0.0f, system = 0.0f, cpu = 0.0f,
                            swap = 0.0f, swapTotal = 0.0f, swapUsed = 0.0f, swapCached = 0.0f, swapFree = 0.0f,
                            mem = 0.0f, memTotal = 0.0f, memFree = 0.0f, memBuffers = 0.0f;
                    processMonitors = new ArrayList<>();
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
                                    if (processMonitors.size() > 30) {
                                        continue;
                                    }
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

                    mem = 1.0f - ((memFree + memBuffers + swapCached) / memTotal);
                    swap = 1.0f - ((swapFree) / swapTotal);


                    osInfo = OSInfo.newBuilder()
                            .setUser(user)
                            .setSystem(system)
                            .setCpu(cpu)
                            .setSwap(swap * 100f)
                            .setMem(mem * 100f)
                            .build();
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

    public List<ProcessMonitor> getProcessMonitors() {
        return processMonitors;
    }

    public OSInfo getOsInfo() {
        return osInfo;
    }
}
