package com.dfire.core.util;

import com.dfire.config.HeraGlobalEnvironment;

/**
 *
 * @author xiaosuda
 * @date 2018/4/16
 */
public class CommandUtils {

    public static final String CHANGE_AUTHORITY = "chmod -R 777 ";
    public static final String RUN_SH_COMMAND = " sh ";

    /**
     * 修改文件权限命令
     * @param filePath  文件路径
     * @return  民营
     */
    public static String changeFileAuthority(String filePath){
        return CHANGE_AUTHORITY + filePath;
    }

    /**
     * 关闭终端不影响提交的程序
     * @param prefix        切换的用户命令
     * @param shellFilePath 脚本路径
     * @return  命令
     */
    public static String getRunShCommand(String prefix,String shellFilePath){
        if(HeraGlobalEnvironment.isLinuxSystem()){
            return  "setsid " + prefix + RUN_SH_COMMAND + shellFilePath;
        }
        return  prefix + RUN_SH_COMMAND + shellFilePath;
    }
}
