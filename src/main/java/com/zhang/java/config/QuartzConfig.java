package com.zhang.java.config;

import com.zhang.java.quartz.TestJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

/**
 * @Date 2022/5/11 15:33
 * @Author zsy
 * @Description Quartz配置类
 * Quartz在第一次访问时读取配置文件，保存到数据库，之后Quartz调度器Scheduler通过访问数据库调度任务，不再访问配置文件
 */
@Configuration
public class QuartzConfig {
//    /**
//     * 配置JobDetail
//     *
//     * @return
//     */
//    @Bean
//    public JobDetailFactoryBean testJobDetail() {
//        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
//        factoryBean.setJobClass(TestJob.class);
//        factoryBean.setName("testJob");
//        factoryBean.setGroup("testJobGroup");
//        //持久化
//        factoryBean.setDurability(true);
//        //可恢复
//        factoryBean.setRequestsRecovery(true);
//        return factoryBean;
//    }
//
//    /**
//     * 配置Trigger(SimpleTriggerFactoryBean, CronTriggerFactoryBean)
//     *
//     * @param testJobDetail 和testJobDetail()方法名保持一致
//     * @return
//     */
//    @Bean
//    public SimpleTriggerFactoryBean alphaTrigger(JobDetail testJobDetail) {
//        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
//        factoryBean.setJobDetail(testJobDetail);
//        factoryBean.setName("testTrigger");
//        factoryBean.setGroup("testTriggerGroup");
//        //多长时间执行一次任务，默认单位毫秒
//        factoryBean.setRepeatInterval(3000);
//        //存储job的状态
//        factoryBean.setJobDataMap(new JobDataMap());
//        return factoryBean;
//    }


}
