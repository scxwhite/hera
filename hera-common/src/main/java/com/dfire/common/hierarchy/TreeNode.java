package com.dfire.common.hierarchy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:45 2018/1/16
 * @desc 树形结构节点封装
 */

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TreeNode {

    private String parentId;

    private String ChildId;

}
