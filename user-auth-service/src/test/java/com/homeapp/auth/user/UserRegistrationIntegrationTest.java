package com.homeapp.auth.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.test.web.servlet.MockMvc;

import com.homeapp.auth.TestcontainersConfig;

// Test integracyjny auth: prawdziwe zapytanie HTTP (MockMvc przez wszystkie filtry i Security),
// prawdziwy zapis w Postgresie i prawdziwy event w Kafce. Nic nie jest udawane.
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
class UserRegistrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConsumerFactory<String, String> consumerFactory;

    @Test
    void registrationSavesAdminAndPublishesUserRegisteredWithCorrelationId() throws Exception {
        // Losowy email: baza w kontenerze jest wspólna dla testów, a email musi być unikalny.
        String email = "pawel-" + UUID.randomUUID() + "@example.com";

        mockMvc.perform(post("/api/auth/register")
                .header("X-Correlation-Id", "it-register-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "householdName": "Opalowie",
                      "email": "%s",
                      "password": "Haslo123!",
                      "firstName": "Paweł",
                      "birthDate": "1986-05-20",
                      "phoneNumber": "+48600100200"
                    }
                    """.formatted(email)))
            .andExpect(status().isCreated());

        User saved = userRepository.findByEmail(email).orElseThrow();
        assertThat(saved.getRole()).isEqualTo(Role.ADMIN);

        // Czytamy topik user.registered tak, jak zrobiłby to planner, tylko własną, testową grupą od początku.
        Properties overrides = new Properties();
        overrides.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        try (Consumer<String, String> consumer =
                 consumerFactory.createConsumer("auth-it-" + UUID.randomUUID(), null, null, overrides)) {
            consumer.subscribe(List.of("user.registered"));
            List<ConsumerRecord<String, String>> received = new ArrayList<>();

            // pollInSameThread: consumer Kafki nie lubi, gdy używa go kilka wątków, więc Awaitility pyta w wątku testu.
            await().pollInSameThread().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
                consumer.poll(Duration.ofMillis(500)).forEach(received::add);
                assertThat(received).anyMatch(record -> saved.getId().toString().equals(record.key()));
            });

            ConsumerRecord<String, String> record = received.stream()
                .filter(r -> saved.getId().toString().equals(r.key()))
                .findFirst()
                .orElseThrow();
            assertThat(record.value())
                .contains("\"email\":\"" + email + "\"")
                .contains("\"role\":\"ADMIN\"")
                .contains("\"phoneNumber\":\"+48600100200\"");
            // Correlation ID z nagłówka HTTP dojechał aż do nagłówka wiadomości w Kafce (Krok 10b).
            byte[] correlationId = record.headers().lastHeader("correlationId").value();
            assertThat(new String(correlationId, StandardCharsets.UTF_8)).isEqualTo("it-register-1");
        }
    }
}
