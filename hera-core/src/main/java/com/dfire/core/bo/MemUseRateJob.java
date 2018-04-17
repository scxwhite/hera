package com.dfire.core.bo;

import com.dfire.core.config.HeraGlobalEnvironment;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xiaosuda
 * @date 2018/4/13
 */
@Data
@Slf4j
public class MemUseRateJob {

    private float rate;

    private static Pattern pattern = Pattern.compile("\\d+");

    private final String MEM_TOTAL = "MemTotal";

    private final String MEM_AVAILABLE = "MemAvailable";

    private final String MEM_FREE = "MemFree";

    private final String BUFFERS = "Buffers";

    private final String CACHED = "Cached";

    private final String MEM_INFO_PATH = "/proc/meminfo";

    private float memAvailable = 0.0f;

    private float memTotal = 0.0f;

    public MemUseRateJob(float rate) {
        this.rate = rate;
    }

    /**
     * 在 /proc/meminfo 文件有系统内存的实时信息
     */
    public void readMemUsed() {
        if (!HeraGlobalEnvironment.isLinuxSystem()) {
            memTotal = 4096f;
            rate = 0.6f;
            return ;
        }
        File file = new File(MEM_INFO_PATH);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            log.info("文件不存在：{}", MEM_INFO_PATH);
            e.printStackTrace();
        }
        String line;
        Float memFree = null, buffers = null, cached = null;
        try {
            while ((line = reader.readLine()) != null) {
                if (line.contains(MEM_TOTAL)) {
                    memTotal = matchVal(line);
                } else {
                    //如果linux内核3.4 直接读 MemAvailable
                    if (line.contains(MEM_AVAILABLE)) {
                        memAvailable = matchVal(line);
                        break;
                    } else {
                        if (line.contains(MEM_FREE)) {
                            memFree = matchVal(line);
                        } else if (line.contains(BUFFERS)) {
                            buffers = matchVal(line);
                        } else if (line.contains(CACHED)) {
                            cached = matchVal(line);
                        }

                        if (memFree != null && buffers != null && cached != null) {
                            memAvailable = memFree + buffers + cached;
                            break;
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(reader);
        }
        rate = (memTotal - memAvailable) / memTotal;
    }

    public float matchVal(String target) {
        float x = 0.0f;
        Matcher matcher = pattern.matcher(target);
        if (matcher.find()) {
            x = Float.parseFloat(matcher.group()) / 1024;
        }
        return x;
    }

}
