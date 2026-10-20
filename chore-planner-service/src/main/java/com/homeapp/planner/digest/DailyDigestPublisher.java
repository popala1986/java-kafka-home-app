package com.homeapp.planner.digest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.homeapp.planner.config.KafkaTopicConfig;

@Component
public class DailyDigestPublisher {

    private static final Logger log = LoggerFactory.getLogger(DailyDigestPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public DailyDigestPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // Klucz = userId, tak jak w auth: eventy jednej osoby trafiają do jednej partycji.
    public void publish(DailyDigestEvent event) {
        String key = event.userId().toString();

        kafkaTemplate.send(KafkaTopicConfig.DAILY_DIGEST_TOPIC, key, event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Nie udało się wysłać chore.daily.digest dla userId={}", key, ex);
                } else {
                    log.info("Wysłano chore.daily.digest userId={} zadań={} partition={} offset={}",
                        key,
                        event.tasks().size(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                }
            });
    }
}
