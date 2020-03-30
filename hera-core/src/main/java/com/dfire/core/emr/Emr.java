package com.dfire.core.emr;

/**
 * desc:
 *
 * @author scx
 * @create 2019/04/01
 */
public interface Emr {

    /**
     * 获得登录命令
     *
     * @return 登录命令
     */
    String getLogin(String user,String owner);

    /**
     * 获得固定集群的登录方式
     *
     * @return
     */
    String getFixLogin(String host);

    /**
     * 随机获得一台IP
     *
     * @return true/false
     */
    String getIp(String owner);

}
