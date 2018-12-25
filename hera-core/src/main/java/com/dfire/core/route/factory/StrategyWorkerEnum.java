package com.dfire.core.route.factory;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:20 2018/10/11
 * @desc
 */
public enum StrategyWorkerEnum {

    FIRST("first"), RANDOM("random");

    private String strategy;


    StrategyWorkerEnum(String first) {
        this.strategy = first;
    }


    @Override
    public String toString() {
        return strategy;
    }

    public static StrategyWorkerEnum parse(String routeStrategy) {
        Optional<StrategyWorkerEnum> optional = Arrays.asList(StrategyWorkerEnum.values())
                .stream()
                .filter(rs -> routeStrategy.equalsIgnoreCase(rs.strategy))
                .findAny();
        return optional.get();
    }


}
