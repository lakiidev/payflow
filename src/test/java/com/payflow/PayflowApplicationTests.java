package com.payflow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(
        properties = {"spring.kafka.admin.fail-fast=false"},
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@Import(TestcontainersConfiguration.class)
class PayflowApplicationTests {

    @Test
    void contextLoads() {
    }
}