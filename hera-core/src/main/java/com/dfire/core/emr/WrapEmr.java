package com.dfire.core.emr;

import com.dfire.config.HeraGlobalEnvironment;

/**
 * desc:
 *
 * @author scx
 * @create 2019/04/01
 */
public class WrapEmr implements Emr {

    private static Emr emr;

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
    public String getIp() {
        checkEmr();
        return emr.getIp();
    }

    private void checkEmr() {
        if (emr == null) {
            synchronized (WrapEmr.class) {
                if (emr == null) {
                    switch (HeraGlobalEnvironment.getEmrCluster()) {
                        case AmazonJob.NAME:
                            emr = new AmazonJob();
                            break;
                        case AliyunEmr.NAME:
                            emr = new AliyunEmr();
                            break;
                        default:
                            throw new RuntimeException("未识别的emr集群类型:" + HeraGlobalEnvironment.getEmrCluster());
                    }
                }
            }
        }
    }
}