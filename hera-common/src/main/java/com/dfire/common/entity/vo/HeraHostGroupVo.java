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

    private int nextPos;

}
