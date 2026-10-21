package com.homeapp.notification;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

// Listenery wyłączone: test ma tylko sprawdzić, że aplikacja startuje (baza, Flyway, beany).
@SpringBootTest(properties = "spring.kafka.listener.auto-startup=false")
@Import(TestcontainersConfig.class)
class NotificationServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
