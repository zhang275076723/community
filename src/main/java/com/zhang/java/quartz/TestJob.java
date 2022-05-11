package com.zhang.java.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @Date 2022/5/11 15:32
 * @Author zsy
 * @Description
 */
public class TestJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println(Thread.currentThread().getName() + ": execute a quartz job.");
    }
}
