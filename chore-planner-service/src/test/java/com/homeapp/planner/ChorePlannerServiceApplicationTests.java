package com.homeapp.planner;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

// Od Kroku 11a Kafka w testach to pusty kontener, więc listenery mogą działać normalnie.
// Ta sama konfiguracja co w UserRegisteredIntegrationTest = Spring uruchamia aplikację (i kontenery) tylko raz dla obu.
@SpringBootTest
@Import(TestcontainersConfig.class)
class ChorePlannerServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
