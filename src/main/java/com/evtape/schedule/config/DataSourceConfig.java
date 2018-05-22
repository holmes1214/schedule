/*
 * Copyright (c) 2016. 版权所有,归北京易精灵科技有限公司.
 */

package com.evtape.schedule.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * DataSourceConfiguration
 *
 * @author Eric
 * @date 16/4/28
 */
@Configuration
public class DataSourceConfig {
    /**
     *
     * 定义数据源 ,参数来自 application.yml 中前缀为druid
     *
     * @return
     */
    @Bean
    @ConfigurationProperties(prefix = "druid")
    public DataSource druidDataSource() {
        return new DruidDataSource();
    }

}
