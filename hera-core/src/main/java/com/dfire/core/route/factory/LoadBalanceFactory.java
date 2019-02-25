package com.dfire.core.route.factory;

import com.dfire.core.config.HeraGlobalEnvironment;
import com.dfire.core.route.strategy.LoadBalance;
import com.dfire.core.route.strategy.impl.RandomLoadBalance;
import com.dfire.core.route.strategy.impl.RoundRobinLoadBalance;


/**
 * 负载均衡实例工厂
 *
 * @author xiaosuda
 */
public class LoadBalanceFactory {

    public static LoadBalance getLoadBalance() {

        if (RoundRobinLoadBalance.NAME.equals(HeraGlobalEnvironment.getLoadBalance())) {
            return new RoundRobinLoadBalance();
        }

        if (RandomLoadBalance.NAME.equals(HeraGlobalEnvironment.getLoadBalance())) {
            return new RandomLoadBalance();
        }

        return new RoundRobinLoadBalance();
    }

}
