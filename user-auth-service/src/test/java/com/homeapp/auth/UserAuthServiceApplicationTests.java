package com.homeapp.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

// Baza i Kafka z Testcontainers (TestcontainersConfig), a nie z docker compose.
@SpringBootTest
@Import(TestcontainersConfig.class)
class UserAuthServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
