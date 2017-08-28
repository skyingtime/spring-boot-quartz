package com.kliu.config;

import com.kliu.job.QuartzJob;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Properties;

/**
 * Created by yangliu on 28/08/2017.
 */
@Configuration
public class QuartzConfig {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${scheduler.cronexpression}")
    private String schedulerCronExpression;

    @Value("${scheduler.autoStartUp}")
    private boolean isAutoStartUp;

    @Value("${scheduler.driverDelegateClass}")
    private String driverDelegateClassValue;

    private static final String DRIVER_DELEGATE_CLASS = "org.quartz.jobStore.driverDelegateClass";
    private static final String CONFIG_FILE_PATH = "/quartz.properties";
    private static final String JOB_DETAIL_NAME = "quartz-job";
    private static final String TRIGGER_DETAIL_NAME = "quartz-scheduler-trigger";

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() throws IOException {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setOverwriteExistingJobs(true);
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);
        factory.setAutoStartup(isAutoStartUp);
        //custom job factory of spring with DI support for @Autowired
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        factory.setJobFactory(jobFactory);

        factory.setQuartzProperties(quartzProperties());
        factory.setTriggers(myQuartzJobTrigger());

        return factory;
    }

    @Bean
    public Properties quartzProperties() throws IOException {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource(CONFIG_FILE_PATH));
        Properties properties = new Properties();
        properties.setProperty(DRIVER_DELEGATE_CLASS, driverDelegateClassValue);
        propertiesFactoryBean.setProperties(properties);
        propertiesFactoryBean.afterPropertiesSet();
        return propertiesFactoryBean.getObject();
    }

    private JobDetail myQuartzJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(QuartzJob.class);
        factoryBean.setDurability(true);
        factoryBean.setApplicationContext(applicationContext);
        factoryBean.setName(JOB_DETAIL_NAME);
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }

    private Trigger myQuartzJobTrigger() {
        CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
        factoryBean.setJobDetail(myQuartzJobDetail());
        factoryBean.setName(TRIGGER_DETAIL_NAME);
        factoryBean.setStartTime(new Date());
        factoryBean.setCronExpression(schedulerCronExpression);
        factoryBean.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
        try {
            factoryBean.afterPropertiesSet();
            return factoryBean.getObject();
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
