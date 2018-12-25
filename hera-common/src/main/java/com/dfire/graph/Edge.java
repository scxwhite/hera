package com.dfire.graph;

import lombok.Data;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午6:43 2018/8/15
 * @desc
 */
@Data
public class Edge {

    private GraphNode nodeA;
    private GraphNode nodeB;

    public Edge(GraphNode nodeA, GraphNode nodeB) {
        this.nodeA = nodeA;
        this.nodeB = nodeB;
    }
}
