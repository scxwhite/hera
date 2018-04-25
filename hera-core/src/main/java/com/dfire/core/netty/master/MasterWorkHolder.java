package com.dfire.core.netty.master;

import com.dfire.core.message.HeartBeatInfo;
import com.dfire.core.netty.Channel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:13 2018/1/12
 * @desc
 */

@Data
public class MasterWorkHolder {


    private  Channel channel;

    private Map<String, Boolean> running = new ConcurrentHashMap<String, Boolean>();

    private Map<String, Boolean> manningRunning = new ConcurrentHashMap<String, Boolean>();

    private Map<String, Boolean> debugRunning = new ConcurrentHashMap<String, Boolean>();

    public HeartBeatInfo heartBeatInfo;
}
