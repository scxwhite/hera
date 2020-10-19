package com.dfire.common.enums;

/**
 * desc:
 *
 * @author scx
 * @create 2020/04/14
 */
public enum CycleEnum {
    /**
     * 自依赖，依赖于当前任务的上一周期，如果是今天第一个，则依赖昨天最后一个周期
     */
    SELF_LAST("自依赖，依赖于当前任务的上一周期"),


    /**
     * 等待上游任务的最后一个周期
     */
    UP_LAST("依赖上游任务的最后一个周期"),

    /**
     * 等待下游任务的最后一个周期
     */
    DOWN_LAST("依赖下游任务的最后一个周期"),

    /**
     * 无
     */
    NONE("无");



    String desc;

    CycleEnum(String desc) {
        this.desc = desc;
    }


    public String getDesc() {
        return desc;
    }

    public static CycleEnum parse(String desc) {

        CycleEnum[] values = CycleEnum.values();

        for (CycleEnum value : values) {
            if(value.desc.equals(desc) || value.name().equals(desc)) {
                return value;
            }
        }
        return CycleEnum.NONE;

    }


    public static boolean isSelfDep(String name) {
        return CycleEnum.SELF_LAST.name().equals(name);
    }

}
