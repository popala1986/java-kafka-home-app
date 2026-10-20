package com.homeapp.planner.member;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.homeapp.planner.chore.ChoreService;

@Component
public class UserRegisteredListener {

    private static final Logger log = LoggerFactory.getLogger(UserRegisteredListener.class);

    private final HouseholdMemberRepository memberRepository;
    private final ChoreService choreService;

    public UserRegisteredListener(HouseholdMemberRepository memberRepository, ChoreService choreService) {
        this.memberRepository = memberRepository;
        this.choreService = choreService;
    }

    @KafkaListener(topics = "user.registered")
    public void onUserRegistered(@Payload UserRegisteredEvent event,
                                 @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                 @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Odebrano user.registered userId={} partition={} offset={}",
            event.userId(), partition, offset);

        if (event.householdId() == null) {
            // Eventy z Kroku 4 (przed rodzinami) nie mają householdId: pomijamy je.
            log.warn("Pomijam event bez householdId (stary format) userId={}", event.userId());
            return;
        }

        if (memberRepository.existsById(event.userId())) {
            // Kafka gwarantuje "co najmniej raz", więc ten sam event może przyjść ponownie.
            log.info("Domownik userId={} już istnieje, pomijam duplikat", event.userId());
            return;
        }

        memberRepository.save(HouseholdMember.from(event));
        log.info("Zapisano domownika {} w rodzinie {}", event.firstName(), event.householdId());

        // Pierwszy domownik nowej rodziny (zwykle admin) = rodzina dostaje startową listę obowiązków.
        choreService.addDefaultChoresIfMissing(event.householdId());
    }
}
