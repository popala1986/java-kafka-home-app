package com.homeapp.planner;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

// Listener wyłączony: test ma tylko sprawdzić, że aplikacja startuje,
// a nie czytać prawdziwe wiadomości z Kafki i przesuwać offsetów grupy.
@SpringBootTest(properties = "spring.kafka.listener.auto-startup=false")
@Import(TestcontainersConfig.class)
class ChorePlannerServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
