package com.dfire.core.route;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:20 2018/10/11
 * @desc
 */
public enum RouteStrategyEnum {

    FIRST("first"), RANDOM("random");

    private String routeStrategy;


    RouteStrategyEnum(String first) {
        this.routeStrategy = first;

    }

    @Override
    public String toString() {
        return routeStrategy;
    }

    public static RouteStrategyEnum parse(String routeStrategy) {
        Optional<RouteStrategyEnum> optional = Arrays.asList(RouteStrategyEnum.values())
                .stream()
                .filter(rs -> routeStrategy.equalsIgnoreCase(rs.routeStrategy))
                .findAny();
        return optional.get();
    }


}
