package com.homeapp.planner.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;

import com.homeapp.planner.TestcontainersConfig;
import com.homeapp.planner.chore.ChoreRepository;

// Test integracyjny plannera: event user.registered w prawdziwej Kafce → domownik i startowe obowiązki w bazie.
@SpringBootTest
@Import(TestcontainersConfig.class)
class UserRegisteredIntegrationTest {

    @Autowired
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @Autowired
    private HouseholdMemberRepository memberRepository;

    @Autowired
    private ChoreRepository choreRepository;

    @Test
    void firstMemberOfNewHouseholdIsSavedAndHouseholdGetsFiftyChores() {
        UUID userId = UUID.randomUUID();
        UUID householdId = UUID.randomUUID();
        UserRegisteredEvent event = new UserRegisteredEvent(UUID.randomUUID(), userId, householdId, "ADMIN",
            "Paweł", LocalDate.of(1986, 5, 20), Instant.now());

        kafkaTemplate.send("user.registered", userId.toString(), event);

        // Listener czyta od "earliest", więc nie musimy czekać na partycje jak w notification-service.
        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            assertThat(memberRepository.findById(userId))
                .hasValueSatisfying(member -> {
                    assertThat(member.getFirstName()).isEqualTo("Paweł");
                    assertThat(member.getHouseholdId()).isEqualTo(householdId);
                    assertThat(member.getRole()).isEqualTo("ADMIN");
                });
            assertThat(choreRepository.findAllByHouseholdIdOrderByMinAgeAscNameAsc(householdId)).hasSize(50);
        });
    }

    @Test
    void duplicateEventIsIgnoredAndSecondMemberDoesNotCopyChoresAgain() {
        UUID householdId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID kidId = UUID.randomUUID();
        UserRegisteredEvent admin = new UserRegisteredEvent(UUID.randomUUID(), adminId, householdId, "ADMIN",
            "Paweł", LocalDate.of(1986, 5, 20), Instant.now());
        UserRegisteredEvent kid = new UserRegisteredEvent(UUID.randomUUID(), kidId, householdId, "USER",
            "Ola", LocalDate.of(2016, 4, 30), Instant.now());

        // Wszystkie trzy wiadomości z tym samym kluczem trafiają do jednej partycji, więc są obsłużone po kolei.
        // Gdy Ola jest już w bazie, wiemy, że dwie wcześniejsze wiadomości też zostały obsłużone.
        String key = householdId.toString();
        kafkaTemplate.send("user.registered", key, admin);
        kafkaTemplate.send("user.registered", key, admin);
        kafkaTemplate.send("user.registered", key, kid);

        await().atMost(Duration.ofSeconds(20)).until(() -> memberRepository.existsById(kidId));

        assertThat(memberRepository.findAllByHouseholdIdOrderByBirthDateAsc(householdId))
            .extracting(HouseholdMember::getFirstName)
            .containsExactly("Paweł", "Ola");
        assertThat(choreRepository.findAllByHouseholdIdOrderByMinAgeAscNameAsc(householdId)).hasSize(50);
    }
}
