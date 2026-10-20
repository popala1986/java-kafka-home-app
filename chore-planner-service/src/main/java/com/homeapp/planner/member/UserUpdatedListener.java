package com.homeapp.planner.member;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class UserUpdatedListener {

    private static final Logger log = LoggerFactory.getLogger(UserUpdatedListener.class);

    private final HouseholdMemberRepository memberRepository;

    public UserUpdatedListener(HouseholdMemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // W application.yaml domyślny typ to UserRegisteredEvent. Ten listener czyta inny topik,
    // więc mówi deserializerowi wprost, na jaką klasę zamienić JSON.
    @KafkaListener(
        topics = "user.updated",
        properties = "spring.json.value.default.type=com.homeapp.planner.member.UserUpdatedEvent")
    public void onUserUpdated(@Payload UserUpdatedEvent event,
                              @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                              @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Odebrano user.updated userId={} partition={} offset={}",
            event.userId(), partition, offset);

        Optional<HouseholdMember> found = memberRepository.findById(event.userId());
        if (found.isEmpty()) {
            log.warn("Nie znam domownika userId={}, pomijam zmianę", event.userId());
            return;
        }

        HouseholdMember member = found.get();
        member.updateProfile(event.firstName(), event.birthDate());
        memberRepository.save(member);
        log.info("Zaktualizowano domownika {} ur. {}", event.firstName(), event.birthDate());
    }
}
