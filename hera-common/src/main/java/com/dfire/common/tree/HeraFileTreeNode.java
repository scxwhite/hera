package com.dfire.common.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.dfire.common.entity.vo.HeraFileVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:45 2018/1/16
 * @desc 树形结构节点封装
 */

@Builder
@Data
@AllArgsConstructor
public class HeraFileTreeNode {
    private String id;
    private String parentId;
    private HeraFileTreeNode parentNode;
    private List<HeraFileTreeNode> childList;
    private HeraFileVo heraFileVo;

    public HeraFileTreeNode() {
        initChildList();
    }

    public HeraFileTreeNode(HeraFileVo heraFileVo) {
        this.id = heraFileVo.getId();
        this.heraFileVo = heraFileVo;
        initChildList();
    }

    public void initChildList() {
        if (childList == null) {
            childList = new ArrayList<>();
        }
    }

    public boolean isLeaf() {
        if (childList == null) {
            return true;
        } else {
            if (childList.isEmpty()) {
                return true;
            } else {
                return false;
            }
        }
    }

    public void addChildNode(HeraFileTreeNode treeNode) {
        initChildList();
        childList.add(treeNode);
    }

    public boolean isValidTree() {
        return true;
    }

    /**
     * @return
     * @desc 返回当前节点的所有父亲节点集合
     */
    public List<HeraFileTreeNode> getElders() {
        List<HeraFileTreeNode> elderList = new ArrayList<>();
        HeraFileTreeNode parentNode = this.getParentNode();
        if (parentNode == null) {
            return elderList;
        } else {
            elderList.add(parentNode);
            elderList.addAll(parentNode.getElders());
            return elderList;
        }
    }

    /**
     * @return
     * @desc 返回当前节点的所有孩子节点集合
     */
    public List<HeraFileTreeNode> getJuniors() {
        List<HeraFileTreeNode> juniorList = new ArrayList<>();
        List<HeraFileTreeNode> childList = this.getChildList();
        if (childList == null) {
            return juniorList;
        } else {
            childList.stream().forEach(child -> {
                childList.add(child);
                childList.addAll(child.getJuniors());
            });
            return juniorList;
        }
    }

    /**
     * @return
     * @desc 返回当前节点的孩子集合
     */
    public List<HeraFileTreeNode> getChildList() {
        return childList;
    }

    /**
     * @desc 删除节点和它下面的晚辈
     */
    public void deleteNode() {
        HeraFileTreeNode parentNode = this.getParentNode();
        String id = this.getId();
        if (parentNode != null) {
            parentNode.deleteChildNode(id);
        }
    }

    /**
     * @param childId
     * @desc 删除当前节点的某个子节点
     */
    public void deleteChildNode(String childId) {
        List<HeraFileTreeNode> childListTmp = this.getChildList();
        this.setChildList(childListTmp.stream().filter(child -> !child.getId().equals(childId)).collect(Collectors.toList()));
    }

    /**
     * @param treeNode
     * @return
     * @desc 动态的插入一个新的节点到当前树中
     */
    public boolean insertJuniorNode(HeraFileTreeNode treeNode) {
        String juniorParentId = treeNode.getParentId();
        if (this.parentId == juniorParentId) {
            addChildNode(treeNode);
            return true;
        } else {
            List<HeraFileTreeNode> childList = this.getChildList();
            int childNumber = childList.size();
            boolean insertFlag;
            for (int i = 0; i < childNumber; i++) {
                HeraFileTreeNode childNode = childList.get(i);
                insertFlag = childNode.insertJuniorNode(treeNode);
                if (insertFlag == true) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param id
     * @return
     * @desc 找到一颗树中某个节点
     */
    public HeraFileTreeNode findTreeNodeById(String id) {
        if (this.id == id) {
            return this;
        }
        if (childList.isEmpty() || childList == null) {
            return null;
        } else {
            int childNumber = childList.size();
            for (int i = 0; i < childNumber; i++) {
                HeraFileTreeNode child = childList.get(i);
                HeraFileTreeNode resultNode = child.findTreeNodeById(id);
                if (resultNode != null) {
                    return resultNode;
                }
            }
        }
        return null;
    }

    public void traverse() {
        if(StringUtils.isBlank(heraFileVo.getId())) {
            return;
        }
        print(id);
        if(childList == null || childList.isEmpty()) {
            return;
        }
        int childNumber = childList.size();
        childList.stream().forEach(child -> traverse());
    }


    public void print(String content) {
        System.out.println(content);
    }


}
