package com.payflow.infrastructure.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Testcontainers
class DataSourceRoutingIntegrationTest {

    @Container
    static PostgreSQLContainer primary = new PostgreSQLContainer("postgres:18-alpine");

    @Container
    static PostgreSQLContainer replica = new PostgreSQLContainer("postgres:18-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.write.url", primary::getJdbcUrl);
        registry.add("spring.datasource.write.username", primary::getUsername);
        registry.add("spring.datasource.write.password", primary::getPassword);
        registry.add("spring.datasource.read.url", replica::getJdbcUrl);
        registry.add("spring.datasource.read.username", replica::getUsername);
        registry.add("spring.datasource.read.password", replica::getPassword);
    }

    @Autowired
    @Qualifier("writeDataSource")
    private HikariDataSource writeDataSource;

    @Autowired
    @Qualifier("readDataSource")
    private HikariDataSource readDataSource;

    @Test
    void readOnlyTransactionShouldRouteToReadPool() {
        assertThat(readDataSource.getPoolName()).isEqualTo("SecondaryPool");
    }

    @Test
    void writeTransactionShouldRouteToWritePool() {
        assertThat(writeDataSource.getPoolName()).isEqualTo("PrimaryPool");
    }
}
