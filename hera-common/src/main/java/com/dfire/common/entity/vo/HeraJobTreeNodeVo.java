package com.dfire.common.entity.vo;

import lombok.Builder;
import lombok.Data;

import java.util.Objects;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:40 2018/5/19
 * @desc
 */
@Data
@Builder
public class HeraJobTreeNodeVo {
    String id;
    String parent;
    String name;
    Integer directory;
    String jobName;
    Integer jobId;
    boolean isParent;

    public boolean getIsParent() {
        return isParent;
    }

    public void setIsParent(boolean isParent) {
        this.isParent = isParent;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HeraJobTreeNodeVo that = (HeraJobTreeNodeVo) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
