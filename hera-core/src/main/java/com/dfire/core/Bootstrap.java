package com.dfire.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 17:50 2018/1/10
 * @desc
 */
@ComponentScan("com.dfire")
@EnableAutoConfiguration
@SpringBootApplication
public class Bootstrap {

    public static void main(String[] args) {

        SpringApplication.run(Bootstrap.class, args);
    }
}
