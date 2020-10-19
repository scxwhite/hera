package com.dfire.config;

import com.dfire.common.enums.RunAuthType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * @author scx
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RunAuth {

    /**
     * 需要赋权的类型
     *
     * @return RunAuthType
     */
    RunAuthType authType() default RunAuthType.JOB;

    /**
     * id 的下标 -1表示第一个参数是vo
     *
     * @return int
     */
    int idIndex() default 0;

    /**
     * 赋权类型的下标，会覆盖authType
     *
     * @return
     */
    int typeIndex() default -1;
}
