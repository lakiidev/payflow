package com.payflow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(
        properties = {
                "spring.autoconfigure.exclude=org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration"
        },
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@Import(TestcontainersConfiguration.class)
class PayflowApplicationTests {

    @Test
    void contextLoads() {
    }
}