package com.dfire.core.spi;

import java.lang.annotation.*;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 21:10 2017/12/29
 * @desc
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SPI {

    /**
     * config中的键值
     */
    String key() default "";

    /**
     * 默认扩展实现
     */
    String dftValue() default "";
}
