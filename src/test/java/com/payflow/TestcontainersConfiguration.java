package com.payflow;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Value("${spring.datasource.write.username}")
    private String username;

    @Value("${spring.datasource.write.password}")
    private String password;

    @Bean
    @RestartScope
    @ServiceConnection
    PostgreSQLContainer primaryContainer() {
        return new PostgreSQLContainer(DockerImageName.parse("postgres:18-alpine"))
                .withDatabaseName("payflow")
                .withUsername(username)
                .withPassword(password);
    }

    @Bean
    @ServiceConnection
    @RestartScope
    KafkaContainer kafkaContainer() {
        return new KafkaContainer(DockerImageName.parse("apache/kafka:3.8.1"));
    }
}
