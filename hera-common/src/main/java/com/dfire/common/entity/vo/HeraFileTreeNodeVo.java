package com.dfire.common.entity.vo;

import com.dfire.common.tree.HeraFileTreeNode;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:49 2018/4/11
 * @desc
 */
@Data
public class HeraFileTreeNodeVo {

    String id = "";
    String name = "";
    List<HeraFileTreeNodeVo> children = new ArrayList<>();

    public  HeraFileTreeNodeVo() {
        this.children = new ArrayList<>();
    }

    public HeraFileTreeNodeVo(HeraFileTreeNode heraFileTreeNode) {
        this.id = heraFileTreeNode.getHeraFileVo().getId();
        this.name = heraFileTreeNode.getHeraFileVo().getName();
    }

}
