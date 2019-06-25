package com.dfire.core.emr;

import com.dfire.config.HeraGlobalEnv;

/**
 * desc:
 *
 * @author scx
 * @create 2019/04/01
 */
public class WrapEmr implements Emr, EmrJob {

    private volatile static AbstractEmr emr;

    @Override
    public void addJob() {
        checkEmr();
        emr.addJob();
    }

    @Override
    public void removeJob() {
        emr.removeJob();
    }

    @Override
    public String getLogin(String user) {
        checkEmr();
        return emr.getLogin(user);
    }

    @Override
    public String getLogin(String user, String ip) {
        checkEmr();
        return emr.getLogin(user, ip);
    }

    @Override
    public String getFixLogin(String host) {
        checkEmr();
        return emr.getFixLogin(host);
    }

    @Override
    public String getIp() {
        checkEmr();
        return emr.getIp();
    }

    @Override
    public boolean isAlive() {
        checkEmr();
        return emr.isAlive();
    }

    private void checkEmr() {
        if (emr == null) {
            synchronized (WrapEmr.class) {
                if (emr == null) {
                    switch (HeraGlobalEnv.getEmrCluster()) {
                        case AmazonEmr.NAME:
                            emr = new AmazonEmr();
                            break;
                        case AliYunEmr.NAME:
                            emr = new AliYunEmr();
                            break;
                        default:
                            throw new RuntimeException("未识别的emr集群类型:" + HeraGlobalEnv.getEmrCluster());
                    }
                }
            }
        }
    }
}