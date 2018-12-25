package com.dfire.graph;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午6:44 2018/8/15
 * @desc
 */
@Data
public class DirectionGraph<T> implements Serializable {

    /**
     * 默认图的点的个数
     */
    private Integer maxNodeNum = 60000;

    /**
     * srcEdge 表示图的边
     */
    private ArrayList<Integer>[] srcEdge;
    /**
     * tarEdge 表示图的边
     */
    private ArrayList<Integer>[] tarEdge;
    /**
     * 根据node节点的nodeName找下标
     */
    private Map<T, Integer> nodeMap;
    /**
     * 根据下标查找node节点
     */
    private Map<Integer, GraphNode<T>> indexMap;


    private int index;

    public DirectionGraph() {
        index = 0;
        srcEdge = new ArrayList[maxNodeNum];
        tarEdge = new ArrayList[maxNodeNum];
        nodeMap = new HashMap<>();
        indexMap = new HashMap<>();
    }

    public boolean addNode(GraphNode<T> graphNode) {
        if (nodeMap.get(graphNode.getNodeName()) != null) {
            return false;
        }
        nodeMap.put( graphNode.getNodeName(), index);
        indexMap.put(index++, graphNode);
        return true;
    }

    public boolean addEdge(GraphNode<T> graphNodeOne, GraphNode<T> graphNodeTwo) {
        Integer srcIndex = getNodeIndex(graphNodeOne);
        Integer tarIndex = getNodeIndex(graphNodeTwo);
        if (srcIndex == null) {
            return false;
        }
        if (srcEdge[srcIndex] == null) {
            srcEdge[srcIndex] = new ArrayList<>();
        }
        if (!srcEdge[srcIndex].contains(tarIndex)) {
            srcEdge[srcIndex].add(tarIndex);
        }
        if (tarEdge[tarIndex] == null) {
            tarEdge[tarIndex] = new ArrayList<>();
        }
        if (!tarEdge[tarIndex].contains(srcIndex)) {
            tarEdge[tarIndex].add(srcIndex);
        }
        return true;
    }

    public Integer getNodeIndex(GraphNode<T> node) {
        return nodeMap.get(node.getNodeName());
    }
}
