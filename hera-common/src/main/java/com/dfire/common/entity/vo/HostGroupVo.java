package com.dfire.common.entity.vo;

import lombok.Builder;
import lombok.Data;

/**
 *
 * @author xiaosuda
 * @date 2018/4/20
 */
@Data
public class HostGroupVo {

    private Integer id;
    private String name;
    private boolean effective;
    private String gmtCreate;
    private String gmtModified;
    private String description;
}
