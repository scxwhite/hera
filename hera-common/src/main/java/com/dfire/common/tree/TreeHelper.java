package com.dfire.common.tree;

import com.dfire.common.entity.HeraFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 17:04 2018/3/30
 * @desc 构建目录树
 */
public class TreeHelper {

    private TreeNode root;
    private List<TreeNode> tempNodeList;
    private boolean isValidTree = true;

    public TreeHelper() {
    }

    public TreeHelper(List<TreeNode> treeNodeList) {
        tempNodeList = treeNodeList;
        generateTree();
    }

    /**
     * @desc generate a tree from the given treeNode or entity list
     */
    public void generateTree() {
        HashMap nodeMap = putNodeIntoMap();
        putChildIntoParent(nodeMap);
    }

    /**
     * @return
     * @desc put all the treeNodes into a hash table by its id as the key
     */
    private HashMap putNodeIntoMap() {
        HashMap<String, TreeNode> nodeMap = new HashMap();
        tempNodeList.stream().forEach(node -> {
            String id = node.getId();
            String key = String.valueOf(id);
            nodeMap.put(key, node);
        });
        return nodeMap;
    }

    /**
     * @param nodeMap a HashMap that contains all the treenodes by its id as the key
     * @desc set the parent nodes point to the child nodes
     */
    private void putChildIntoParent(HashMap<String, TreeNode> nodeMap) {
        for (Map.Entry<String, TreeNode> entry : nodeMap.entrySet()) {
            TreeNode tmpNode = entry.getValue();
            String parentId = tmpNode.getParentId();
            if (nodeMap.containsKey(parentId)) {
                TreeNode parentNode = nodeMap.get(parentId);
                if (parentNode == null) {
                    this.isValidTree = false;
                    return;
                } else {
                    parentNode.addChildNode(tmpNode);
                }
            }
        }
    }

    public void initTempNodeList() {
        this.tempNodeList = (this.tempNodeList == null ? this.tempNodeList = new ArrayList<>() : this.tempNodeList);
    }

    public void addTreeNode(TreeNode treeNode) {
        initTempNodeList();
        this.tempNodeList.add(treeNode);
    }

    /**
     *
     * @param treeNode
     * @return
     * @desc insert a tree node to the tree generated already
     */
    public boolean insertTreeNode(TreeNode treeNode) {
        boolean insertFlag = root.insertJuniorNode(treeNode);
        return insertFlag;
    }

    /**
     *
     * @param list
     * @return
     * @desc 将HeraFile转换为Node
     */
    public  List<TreeNode> convert(List<HeraFile> list, Convert<HeraFile, TreeNode> convert) {
        List<TreeNode> tempNodeList = new ArrayList<TreeNode>();
        list.forEach(file -> tempNodeList.add(convert.transform(file)));
        return tempNodeList;
    }

    @FunctionalInterface
    public interface Convert<HeraFile, TreeNode>   {
         TreeNode transform(HeraFile file);
    }


    public boolean isValidTree() {
        return this.isValidTree;
    }

    public TreeNode getRoot() {
        return root;
    }

    public void setRoot(TreeNode root) {
        this.root = root;
    }

    public List<TreeNode> getTempNodeList() {
        return tempNodeList;
    }

    public void setTempNodeList(List<TreeNode> tempNodeList) {
        this.tempNodeList = tempNodeList;
    }

}
