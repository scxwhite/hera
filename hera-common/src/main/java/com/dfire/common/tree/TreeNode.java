package com.dfire.common.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
public class TreeNode {

    private String id;
    private String parentId;
    private String nodeName;
    private Object object;
    private TreeNode parentNode;
    private List<TreeNode> childList;

    public TreeNode() {
        initChildList();
    }

    public TreeNode(TreeNode parentNode) {
        this.getParentNode();
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

    public void addChildNode(TreeNode treeNode) {
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
    public List<TreeNode> getElders() {
        List<TreeNode> elderList = new ArrayList<>();
        TreeNode parentNode = this.getParentNode();
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
    public List<TreeNode> getJuniors() {
        List<TreeNode> juniorList = new ArrayList<>();
        List<TreeNode> childList = this.getChildList();
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
    public List<TreeNode> getChildList() {
        return childList;
    }

    /**
     * @desc 删除节点和它下面的晚辈
     */
    public void deleteNode() {
        TreeNode parentNode = this.getParentNode();
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
        List<TreeNode> childListTmp = this.getChildList();
        this.setChildList(childListTmp.stream().filter(child -> !child.getId().equals(childId)).collect(Collectors.toList()));
    }

    /**
     * @param treeNode
     * @return
     * @desc 动态的插入一个新的节点到当前树中
     */
    public boolean insertJuniorNode(TreeNode treeNode) {
        String juniorParentId = treeNode.getParentId();
        if (this.parentId == juniorParentId) {
            addChildNode(treeNode);
            return true;
        } else {
            List<TreeNode> childList = this.getChildList();
            int childNumber = childList.size();
            boolean insertFlag;
            for (int i = 0; i < childNumber; i++) {
                TreeNode childNode = childList.get(i);
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
    public TreeNode findTreeNodeById(String id) {
        if (this.id == id) {
            return this;
        }
        if (childList.isEmpty() || childList == null) {
            return null;
        } else {
            int childNumber = childList.size();
            for (int i = 0; i < childNumber; i++) {
                TreeNode child = childList.get(i);
                TreeNode resultNode = child.findTreeNodeById(id);
                if (resultNode != null) {
                    return resultNode;
                }
            }
        }
        return null;
    }

    public void traverse() {
        if(StringUtils.isBlank(getId())) {
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

    public void print(int content) {
        System.out.println(String.valueOf(content));
    }


}
