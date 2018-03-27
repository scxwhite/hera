package com.dfire.common.hierarchy;

import com.dfire.common.kv.Tuple;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 15:53 2018/1/16
 * @desc 构建任务的DAG图
 */
public class HeraJobGraph {

    private static MutableValueGraph<String, String> jobGraph;
    private static MutableValueGraph<String, String> directoryGraph;


    private static MutableValueGraph<String, String> buildJobGraph(List<Tuple<String, String>> tupleList) {

        jobGraph = ValueGraphBuilder.directed()
                .nodeOrder(ElementOrder.<String>natural())
                .allowsSelfLoops(true)
                .build();
        tupleList.forEach(new Consumer<Tuple<String, String>>() {
            @Override
            public void accept(Tuple<String, String> tuple) {
                jobGraph.putEdgeValue(tuple.getSource(), tuple.getTarget(), "config");
            }
        });
        return jobGraph;
    }

    private static MutableValueGraph<String, String> buildDirectoryGraph(List<Tuple<String, String>> tupleList) {

        directoryGraph = ValueGraphBuilder.directed()
                .nodeOrder(ElementOrder.<String>natural())
                .allowsSelfLoops(false)
                .build();
        tupleList.forEach(new Consumer<Tuple<String, String>>() {
            @Override
            public void accept(Tuple<String, String> tuple) {
                directoryGraph.putEdgeValue(tuple.getSource(), tuple.getTarget(), "msg");
            }
        });
        return jobGraph;
    }

}
