package com.zhang.java;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class CommunityApplication {

    /**
     * ioc容器实例化当前bean时，执行该方法
     * 解决netty启动冲突问题，因为redis依赖netty，es也依赖netty
     */
    @PostConstruct
    public void init() {
        // see Netty4Utils.setAvailableProcessors()
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

    public static void main(String[] args) {
        SpringApplication.run(CommunityApplication.class, args);
    }

}
