package com.dfire.common.processor;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午11:37 2018/5/2
 * @desc
 */
public class DownProcessor implements Processor {

    @Override
    public String getId() {
        return "download";
    }

    @Override
    public String getConfig() {
        return "";
    }

    @Override
    public void parse(String config) {

    }
}
