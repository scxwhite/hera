package com.dfire.core.schedule;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScheduleInfoLog {

	public static void info(String msg){
		log.info(msg);
	}
	
	public static void error(String msg){
		log.error(msg);
	}
	
	public static void error(String msg,Exception e){
		log.error(msg,e);
	}
	
}
