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
            user.getEmail(),
            user.getFirstName(),
            user.getBirthDate(),
            user.getPhoneNumber(),
            Instant.now());

        String key = user.getId().toString();

        kafkaTemplate.send(KafkaTopicConfig.USER_REGISTERED_TOPIC, key, event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Nie udało się wysłać eventu user.registered dla userId={}", key, ex);
                } else {
                    log.info("Wysłano user.registered userId={} partition={} offset={}",
                        key,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                }
            });
    }
}
