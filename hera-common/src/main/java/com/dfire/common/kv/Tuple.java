package com.dfire.common.kv;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 16:27 2018/1/16
 * @desc 原始数据格式变为列表，方便构建Job目录树和Job DAG
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tuple<S, T> {

    private S source;

    private T target;


}
