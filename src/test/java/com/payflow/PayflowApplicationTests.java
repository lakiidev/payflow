package com.payflow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        properties = {"spring.kafka.admin.fail-fast=false"},
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class PayflowApplicationTests {

    @Test
    void contextLoads() {
    }
}