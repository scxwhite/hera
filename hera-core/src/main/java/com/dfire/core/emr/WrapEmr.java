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
    public void addJob(String owner) {
        checkEmr();
        emr.addJob(checkOwner(owner));
    }

    @Override
    public void removeJob(String owner) {
        checkEmr();
        emr.removeJob(checkOwner(owner));
    }


    @Override
    public String getLogin(String user, String owner) {
        checkEmr();
        return emr.getLogin(user, checkOwner(owner));
    }

    @Override
    public String getFixLogin(String host) {
        checkEmr();
        return emr.getFixLogin(host);
    }

    @Override
    public String getIp(String owner) {
        checkEmr();
        return emr.getIp(checkOwner(owner));
    }

    private String checkOwner(String owner) {
        if (!HeraGlobalEnv.getEmrGroups().contains(owner)) {
            return "other";
        }
        return owner;
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
                        case FixedEmr.NAME:
                            emr = new FixedEmr();
                            break;
                        default:
                            throw new RuntimeException("未识别的emr集群类型:" + HeraGlobalEnv.getEmrCluster());
                    }
                }
            }
        }
    }
}
