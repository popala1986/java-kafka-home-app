package com.homeapp.notification.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;

class KafkaErrorConfigTest {

    @Test
    void sendsFailedEventToDeadLetterTopicOfThisService() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("chore.daily.digest", 2, 15L, "key", "value");

        TopicPartition target = KafkaErrorConfig.deadLetterTopic(record, new IllegalStateException("boom"));

        assertThat(target.topic()).isEqualTo("chore.daily.digest.notification.DLT");
        assertThat(target.partition()).isEqualTo(-1);
    }
}
