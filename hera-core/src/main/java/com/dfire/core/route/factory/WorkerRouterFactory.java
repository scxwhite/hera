package com.dfire.core.route.factory;

import com.dfire.core.route.WorkerRouter;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午3:53 2018/10/11
 * @desc
 */
public interface WorkerRouterFactory {

    /**
     * 机器选择路由策略
     *
     * @param routeStrategy
     * @return
     */

    WorkerRouter newWorkerRouter(String routeStrategy);
}
