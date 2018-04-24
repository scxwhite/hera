package com.dfire.common.processor;

import java.io.Serializable;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 17:40 2018/1/11
 * @desc 可以提供Job运行的前置处理或者后置处理,处理日志运行日志
 */
public interface Processor extends Serializable {

    String getId();

    String getConfig();

    void parse(String config);

}
