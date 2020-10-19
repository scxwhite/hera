package com.dfire.core.tool;

import com.dfire.config.HeraGlobalEnv;
import com.dfire.logs.ErrorLog;
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

    private float zero = 0.0f;


    private final String command;

    {
        switch (HeraGlobalEnv.getSystemEnum()) {
            case LINUX:
                command = "ps aux | awk '{if (NR>1) {print $0}}'";
                break;
            case MAC:
                command = "top -s 0 -n 30 -o cpu -O mem -l 2  -stats pid,user,cpu,time,mem,command,cpu_me";
                break;
            default:
                String oSName = System.getProperties().getProperty("os.name");
                command = "echo 未知的操作系统类型" + oSName;
                ErrorLog.error("未知的操作系统类型{}", oSName);
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
        switch (HeraGlobalEnv.getSystemEnum()) {
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
            ErrorLog.error("读取mac进行信息失败", e);
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
                    processMonitors = new ArrayList<>();

                    for (String line : lines) {
                        int len = line.length();
                        StringBuilder word = new StringBuilder();
                        char ch;
                        int index = -1;
                        int wordIndex = 0;
                        boolean workSplit = true;
                        String user = null, pid = null, cpu = null, mem = null, time = null, command;
                        while (++index < len) {
                            ch = line.charAt(index);
                            if (ch == ' ') {
                                if (wordIndex < 11 && workSplit) {
                                    workSplit = false;
                                    wordIndex++;
                                    if (wordIndex == 1) {
                                        user = word.toString();
                                    } else if (wordIndex == 2) {
                                        pid = word.toString();
                                    } else if (wordIndex == 3) {
                                        cpu = word.toString();
                                    } else if (wordIndex == 4) {
                                        mem = word.toString();
                                    } else if (wordIndex == 10) {
                                        time = word.toString();
                                    }
                                    word = new StringBuilder();
                                } else if (wordIndex >= 11) {
                                    word.append(' ');
                                }
                            } else {
                                workSplit = true;
                                word.append(ch);
                            }
                        }
                        command = word.toString();

                        processMonitors.add(ProcessMonitor.newBuilder()
                                .setUser(user)
                                .setPid(pid)
                                .setCpu(cpu)
                                .setMem(mem)
                                .setTime(time)
                                .setRes("")
                                .setCommand(command)
                                .build());

                    }


                    processMonitors.sort((o1, o2) -> {
                        int comp;
                        if ((comp = o1.getMem().compareTo(o2.getMem())) == 0) {
                            return -o1.getCpu().compareTo(o2.getCpu());
                        }
                        return -comp;
                    });
                    float user, system, cpu,
                            swap = zero, swapTotal, swapCached, swapFree,
                            mem = zero, memTotal, memFree, memBuffers;
                    // 设置cpu信息
                    super.setCommand("vmstat 1 1 | awk '{if (NR >2) print  $13,$14,$15}'");
                    super.run();
                    String[] cpuVal = super.getResult().split(" ");
                    user = parseFloat(cpuVal[0]);
                    system = parseFloat(cpuVal[1]);
                    cpu = parseFloat(cpuVal[2]);

                    //设置内存信息
                    super.setCommand("vmstat -s | grep -E 'total memory|used memory|free memory|buffer memory|swap cache|free swap|total swap' | awk '{print $1}'");
                    super.run();
                    String[] memInfo = super.getResult().split("\n");
                    memTotal = parseKb(memInfo[0]);
                    memFree = parseKb(memInfo[2]);
                    memBuffers = parseKb(memInfo[3]);
                    swapCached = parseKb(memInfo[4]);
                    swapTotal = parseKb(memInfo[5]);
                    swapFree = parseKb(memInfo[6]);

                    if (memTotal != zero) {
                        mem = 1.0f - ((memFree + memBuffers + swapCached) / memTotal);
                    }
                    if (swapTotal != zero) {
                        swap = 1.0f - ((swapFree) / swapTotal);
                    }
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
            ErrorLog.error("读取linux进程信息失败", e);
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
