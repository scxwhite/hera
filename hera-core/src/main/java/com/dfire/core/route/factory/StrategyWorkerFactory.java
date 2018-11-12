package com.dfire.core.route.factory;

import com.dfire.core.route.strategy.IStrategyWorker;
import com.dfire.core.route.strategy.impl.StrategyByFirstImpl;
import com.dfire.core.route.strategy.impl.StrategyByRandomImpl;



/**
 * 获取具体实现类
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午3:53 2018/10/11
 * @desc
 */
public class StrategyWorkerFactory {
    /**
     * // TODO 暂时写死了配置
     * @param strategyWorkerEnum
     * @return
     */
    public static IStrategyWorker getStrategyWorker(StrategyWorkerEnum strategyWorkerEnum){
        switch (strategyWorkerEnum){
            case FIRST:
                return new StrategyByFirstImpl();
            case RANDOM:
                return new StrategyByRandomImpl();
                default:
        }
        return null;
    }

}
