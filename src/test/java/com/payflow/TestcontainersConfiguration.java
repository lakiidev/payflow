package com.payflow;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @RestartScope
    PostgreSQLContainer primaryContainer() {
        return new PostgreSQLContainer(DockerImageName.parse("postgres:18-alpine"))
                .withDatabaseName("payflow");
    }

    @Bean
    DynamicPropertyRegistrar datasourceProperties(PostgreSQLContainer primaryContainer) {
        return registry -> {
            registry.add("spring.datasource.write.url", primaryContainer::getJdbcUrl);
            registry.add("spring.datasource.write.username", primaryContainer::getUsername);
            registry.add("spring.datasource.write.password", primaryContainer::getPassword);
            registry.add("spring.datasource.read.url", primaryContainer::getJdbcUrl);
            registry.add("spring.datasource.read.username", primaryContainer::getUsername);
            registry.add("spring.datasource.read.password", primaryContainer::getPassword);
        };
    }

    @Bean
    @ServiceConnection
    @RestartScope
    KafkaContainer kafkaContainer() {
        return new KafkaContainer(DockerImageName.parse("apache/kafka:3.8.1"));
    }
    @Bean
    @ServiceConnection
    @RestartScope
    RedisContainer redisContainer() {
        return new RedisContainer(DockerImageName.parse("redis:8.6-alpine"));
    }
}