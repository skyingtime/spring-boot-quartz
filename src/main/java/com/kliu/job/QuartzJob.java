package com.kliu.job;

import com.kliu.service.QuartzService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by yangliu on 28/08/2017.
 */
public class QuartzJob implements Job {
    @Autowired
    QuartzService quartzService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        quartzService.hello();
    }
}
