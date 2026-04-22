package com.payflow.infrastructure.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.write")
    public DataSourceProperties writeDataSourceProperties() {
        DataSourceProperties props = new DataSourceProperties();
        System.out.println("### WRITE URL: " + props.getUrl());
        return props;
    }

    @Bean
    @ConfigurationProperties("spring.datasource.write.hikari")
    public HikariDataSource writeDataSource() {
        HikariDataSource ds =  writeDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
        ds.setPoolName("PrimaryPool");
        return ds;
    }

    @Bean
    @ConfigurationProperties("spring.datasource.read")
    public DataSourceProperties readDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.read.hikari")
    public HikariDataSource readDataSource() {
        HikariDataSource ds =  readDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
        ds.setPoolName("SecondaryPool");
        return ds;
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariDataSource write = writeDataSource();
        HikariDataSource read = readDataSource();
        var routing = new RoutingDataSource();
        routing.setTargetDataSources(Map.of("write", write, "read", read));
        routing.setDefaultTargetDataSource(write);
        return routing;
    }
}
