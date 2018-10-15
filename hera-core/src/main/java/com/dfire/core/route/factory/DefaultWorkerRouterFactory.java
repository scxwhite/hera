package com.dfire.core.route.factory;

import com.dfire.core.route.RouteStrategyEnum;
import com.dfire.core.route.WorkerRouter;
import com.dfire.core.route.strategy.WorkerRouterFirst;
import com.dfire.core.route.strategy.WorkerRouterRandom;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:19 2018/10/11
 * @desc
 */
public class DefaultWorkerRouterFactory implements WorkerRouterFactory {

    @Override
    public WorkerRouter newWorkerRouter(String routeStrategy) {

        RouteStrategyEnum strategy = RouteStrategyEnum.parse(routeStrategy);

        switch (strategy) {
            case FIRST:
                return new WorkerRouterFirst();
            case RANDOM:
                return new WorkerRouterRandom();
            default:
                new WorkerRouterFirst();
        }

        return null;
    }
}
