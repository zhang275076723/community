package com.zhang.java.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Date 2022/5/11 11:46
 * @Author zsy
 * @Description spring线程池配置类
 */
@Configuration
@EnableScheduling
@EnableAsync
public class ThreadPoolConfig {

}
