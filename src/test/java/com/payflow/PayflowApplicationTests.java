package com.payflow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        properties = {
                "spring.autoconfigure.exclude=org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration"
        },
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
class PayflowApplicationTests {

    @Test
    void contextLoads() {
    }
}