package com.homeapp.planner.member;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class UserDeletedListener {

    private static final Logger log = LoggerFactory.getLogger(UserDeletedListener.class);

    private final HouseholdMemberRepository memberRepository;

    public UserDeletedListener(HouseholdMemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @KafkaListener(
        topics = "user.deleted",
        properties = "spring.json.value.default.type=com.homeapp.planner.member.UserDeletedEvent")
    public void onUserDeleted(@Payload UserDeletedEvent event,
                              @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                              @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Odebrano user.deleted userId={} partition={} offset={}",
            event.userId(), partition, offset);

        // Ten sam event może przyjść drugi raz (np. po ponowieniu). Wtedy domownika już nie ma i nic nie robimy.
        if (!memberRepository.existsById(event.userId())) {
            log.warn("Nie znam domownika userId={}, nie ma czego usuwać", event.userId());
            return;
        }

        // Zadania i wykluczenia tej osoby usunie sama baza (ON DELETE CASCADE w migracjach).
        memberRepository.deleteById(event.userId());
        log.info("Usunięto domownika userId={} razem z jego zadaniami", event.userId());
    }
}
