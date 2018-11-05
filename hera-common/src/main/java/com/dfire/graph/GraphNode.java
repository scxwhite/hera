package com.dfire.graph;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午6:43 2018/8/15
 * @desc
 */
@Data
public class GraphNode<T> implements Serializable {

    /**
     * 顶点名称
     */
    private T nodeName;
    /**
     * 顶点说明
     */
    private Object remark;

    public GraphNode() {
    }

    public GraphNode(T nodeName, Object remark) {
        this.nodeName = nodeName;
        this.remark = remark;
    }

    @Override
    public boolean equals(Object graphNode) {
        if (!(graphNode instanceof GraphNode)) {
            return false;
        }
        GraphNode other = (GraphNode) graphNode;

        if (!other.getNodeName().toString().trim().equals(this.getNodeName().toString().trim())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return this.getNodeName().hashCode();
    }
}
