package com.dfire.common.entity.vo;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

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
    private String hostGroupId;

    @Override
    public String toString() {
        return "HeraFileVo{" +
                "id='" + id + '\'' +
                ", parent='" + parent + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
