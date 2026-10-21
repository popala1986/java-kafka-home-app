package com.homeapp.notification.digest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.homeapp.notification.TestcontainersConfig;
import com.homeapp.notification.contact.Contact;
import com.homeapp.notification.contact.ContactRepository;
import com.homeapp.notification.notification.Channel;
import com.homeapp.notification.notification.Notification;
import com.homeapp.notification.notification.NotificationRepository;
import com.homeapp.notification.notification.NotificationStatus;

// Test integracyjny: cała droga od wiadomości w Kafce do historii w bazie.
// Prawdziwa Kafka i prawdziwy Postgres (Testcontainers), prawdziwe listenery i serwisy.
// Udajemy tylko serwer pocztowy (JavaMailSender), żeby test nie potrzebował Mailpita.
// Healthcheck poczty (Actuator) umie sprawdzać tylko prawdziwy serwer, więc w tym teście go wyłączamy.
@SpringBootTest(properties = "management.health.mail.enabled=false")
@Import(TestcontainersConfig.class)
class DailyDigestIntegrationTest {

    @Autowired
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private KafkaListenerEndpointRegistry listenerRegistry;

    @MockitoBean
    private JavaMailSender mailSender;

    // Listener podsumowań czyta od "latest", czyli tylko to, co przyjdzie po dostaniu partycji.
    // Gdybyśmy wysłali event wcześniej, przegapiłby go. Dlatego czekamy, aż każdy listener ma przydzieloną partycję.
    @BeforeEach
    void waitUntilListenersAreReady() {
        await().atMost(Duration.ofSeconds(30)).until(() ->
            listenerRegistry.getListenerContainers().stream()
                .allMatch(container -> !container.getAssignedPartitions().isEmpty()));
    }

    @Test
    void digestFromKafkaIsSentBySmsAndEmailAndSavedInHistory() {
        UUID userId = UUID.randomUUID();
        UUID householdId = UUID.randomUUID();
        contactRepository.save(
            new Contact(userId, householdId, "Ola", "ola@example.com", "+48600100300", Instant.now()));

        DailyDigestEvent event = new DailyDigestEvent(UUID.randomUUID(), userId, householdId, "Ola",
            LocalDate.of(2026, 10, 27),
            List.of(new DigestTask("Nakarm zwierzaka", 2), new DigestTask("Pościel łóżko", 1)),
            3, Instant.now());

        // Tak samo jak planner: topik, klucz = userId, event jako JSON.
        kafkaTemplate.send("chore.daily.digest", userId.toString(), event);

        // Listener działa w innym wątku, więc wynik nie pojawi się od razu.
        // Awaitility sprawdza warunek co chwilę, aż się spełni (albo minie 15 s i test padnie).
        await().atMost(Duration.ofSeconds(15)).untilAsserted(() ->
            assertThat(notificationRepository.findAllByHouseholdIdOrderByCreatedAtDesc(householdId))
                .extracting(Notification::getChannel, Notification::getStatus)
                .containsExactlyInAnyOrder(
                    tuple(Channel.SMS, NotificationStatus.SENT),
                    tuple(Channel.EMAIL, NotificationStatus.SENT)));

        ArgumentCaptor<SimpleMailMessage> mail = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(mail.capture());
        assertThat(mail.getValue().getTo()).containsExactly("ola@example.com");
        assertThat(mail.getValue().getSubject()).isEqualTo("Twoje zadania: wtorek 27.10 (3 pkt)");
    }
}
