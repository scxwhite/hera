package com.dfire.common.entity.vo;

import lombok.Builder;
import lombok.Data;

import java.util.Date;



/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:51 2018/4/17
 * @desc 日志
 */
@Data
@Builder
public class HeraFileVo {

    private String id;
    private String parent;
    private Date createDate;
    private Date modifiedDate;
    private String name;
    private boolean folder;
    private String content;
    private String owner;
    private boolean admin;
    private int hostGroupId;

    @Override
    public String toString() {
        return "HeraFileVo{" +
                "id='" + id + '\'' +
                ", parent='" + parent + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
