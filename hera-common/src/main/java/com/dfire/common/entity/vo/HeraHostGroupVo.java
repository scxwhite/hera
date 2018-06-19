package com.dfire.common.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:33 2018/1/12
 * @desc
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HeraHostGroupVo {

    private String id;

    private String name;

    private String description;

    private List<String> hosts;

    //当前主节点所在的位置
    private volatile int currentPosition;

    public String selectHost() {
        if(hosts == null) {
            return null;
        }
        int size = hosts.size();
        if(size == 1) {
            return hosts.get(0);
        }
        if(currentPosition >= size) {
            currentPosition = 0;
        }
        String host = hosts.get(currentPosition);
        currentPosition ++;
        return host;
    }

}
