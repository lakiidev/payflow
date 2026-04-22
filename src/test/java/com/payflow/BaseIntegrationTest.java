package com.payflow;

import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@AutoConfigureRestTestClient
public abstract class BaseIntegrationTest {

    static final PostgreSQLContainer postgres;

    static {
        postgres = new PostgreSQLContainer(DockerImageName.parse("timescale/timescaledb:2.17.2-pg18")
                .asCompatibleSubstituteFor("postgres")
        ).withDatabaseName("payflow");
        postgres.start();
    }


    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.write.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.write.username", postgres::getUsername);
        registry.add("spring.datasource.write.password", postgres::getPassword);
        registry.add("spring.datasource.read.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.read.username", postgres::getUsername);
        registry.add("spring.datasource.read.password", postgres::getPassword);
    }
}