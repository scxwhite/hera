package com.dfire.core.emr;

import com.dfire.common.exception.HeraException;

import java.util.List;

/**
 * desc:
 *
 * @author scx
 * @create 2020/02/18
 */
public class FixedEmr extends AbstractEmr {

    public final static String NAME = "fixed";

    @Override
    protected boolean checkAlive(String cacheClusterId) {
        return true;
    }

    @Override
    protected void init() {

    }

    @Override
    protected void terminateCluster(String clusterId) {

    }

    @Override
    protected void shutdown() {

    }

    @Override
    protected String sendCreateRequest(String owner) {
        return "fixed-" + System.currentTimeMillis();
    }

    @Override
    protected String getAliveId(String owner) {
        return "fixed-" + System.currentTimeMillis();
    }

    @Override
    protected boolean isCompletion(String clusterId) throws HeraException {
        return true;
    }

    @Override
    protected List<String> getMasterIp(String clusterId) {
        return null;
    }
}
