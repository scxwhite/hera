package com.dfire;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 11:59 2018/1/1
 * @desc 启动类
 */
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.dfire")
@MapperScan(basePackages = "com.dfire.*.mapper")
@SpringBootApplication
@EnableScheduling
@ServletComponentScan(value = "com.dfire.config")
public class AdminBootstrap {

	public static void main(String[] args) {
		SpringApplication.run(AdminBootstrap.class, args);
	}
}
