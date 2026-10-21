package com.homeapp.auth.event;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.homeapp.auth.config.KafkaTopicConfig;
import com.homeapp.auth.user.User;

@Component
public class UserEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(UserEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public UserEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishUserRegistered(User user) {
        UserRegisteredEvent event = new UserRegisteredEvent(
            UUID.randomUUID(),
            user.getId(),
            user.getHousehold().getId(),
            user.getRole().name(),
            user.getEmail(),
            user.getFirstName(),
            user.getBirthDate(),
            user.getPhoneNumber(),
            Instant.now());

        send(KafkaTopicConfig.USER_REGISTERED_TOPIC, user, event);
    }

    public void publishUserUpdated(User user) {
        UserUpdatedEvent event = new UserUpdatedEvent(
            UUID.randomUUID(),
            user.getId(),
            user.getHousehold().getId(),
            user.getFirstName(),
            user.getEmail(),
            user.getBirthDate(),
            user.getPhoneNumber(),
            Instant.now());

        send(KafkaTopicConfig.USER_UPDATED_TOPIC, user, event);
    }

    // Klucz = userId: wszystkie eventy jednej osoby lądują w tej samej partycji, więc przychodzą po kolei.
    private void send(String topic, User user, Object event) {
        String key = user.getId().toString();

        kafkaTemplate.send(topic, key, event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Nie udało się wysłać eventu {} dla userId={}", topic, key, ex);
                } else {
                    log.info("Wysłano {} userId={} partition={} offset={}",
                        topic,
                        key,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                }
            });
    }
}
