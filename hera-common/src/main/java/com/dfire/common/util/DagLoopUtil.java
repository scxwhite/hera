package com.dfire.common.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author xiaosuda
 * @date 2018/7/10
 */
public class DagLoopUtil {


    private List<Integer>[] edges;

    private int in[];

    private static final Integer DEFAULT_SIZE = 10000;

    private Integer edgeSize;


    private boolean hasLoop;
    private boolean hasCheck;

    public DagLoopUtil(Integer edgeSize) {
        this.edgeSize = edgeSize + 1;
        init();
    }

    public DagLoopUtil() {
        this(DEFAULT_SIZE);
    }

    public void addEdge(Integer child, Integer parent) {
        if (edges[parent] == null) {
            edges[parent] = new ArrayList<>();
        }
        if (!edges[parent].contains(child)) {
            edges[parent].add(child);
            in[child]++;
        }
    }


    public boolean isLoop() {
        return topSort();
    }

    private boolean topSort() {
        if (hasCheck) {
            return hasLoop;
        }
        LinkedList<Integer> zeroQueue = new LinkedList<>();

        for (int i = 0; i < edgeSize; i++) {
            if (in[i] == 0) {
                zeroQueue.push(i);
            }
        }
        int sum = 0;

        while (zeroQueue.size() > 0) {
            Integer node = zeroQueue.poll();
            sum++;
            List<Integer> list = edges[node];
            if (list != null) {
                for (Integer o : list) {
                    if (--in[o] == 0) {
                        zeroQueue.push(o);
                    }
                }
                list.clear();
            }
        }

        hasCheck = true;
        return hasLoop = sum != edgeSize;
    }

    public void init() {
        edges = new ArrayList[edgeSize];
        in = new int[edgeSize];
        hasCheck = false;
        hasLoop = true;
    }


    /**
     * 列出整个任务链路图
     * @return
     */
    public String getLoop() {
        //TODO （最高根据topSort的结果 获得成环的那些节点(in[o] > 0的点)  直接返回即可）
        topSort();
        StringBuilder sb = new StringBuilder();
        boolean[] vis = new boolean[edgeSize];
        for (int i = 0; i < edgeSize; i++) {
            if (edges[i] != null && edges[i].size() > 0) {
                buildLoop(sb, i, vis);
                break;
            }
        }
        return sb.substring(0, sb.length() - 1);
    }


    private void buildLoop(StringBuilder sb, Integer index, boolean vis[]) {
        List<Integer> edge = edges[index];
        if (!vis[index] && edge != null && edge.size() > 0) {
            vis[index] = true;
            edge.forEach(x -> {
                sb.append(x).append("->").append(edges[x]).append(",");
                buildLoop(sb, x, vis);
            });
        }
    }
}


