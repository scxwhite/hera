package com.dfire.common.util;

import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 18:51 2018/3/26
 * @desc
 */
public class JobUtils {
    /**
     * @param evenMap
     * @return
     * @desc 获取系统环境的hadoop命令
     */
    public static String getHadoopCmd(Map<String, String> evenMap) {
        StringBuilder cmd = new StringBuilder(64);
        String hadoopHome = evenMap.get("HADOOP_HOME");
        if (hadoopHome != null) {
            cmd.append(hadoopHome).append("/bin/");
        }
        cmd.append("hadoop");
        return cmd.toString();
    }
}
