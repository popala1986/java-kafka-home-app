package com.homeapp.planner.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;

class KafkaErrorConfigTest {

    @Test
    void sendsFailedEventToDeadLetterTopicOfThisService() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("user.updated", 2, 15L, "key", "value");

        TopicPartition target = KafkaErrorConfig.deadLetterTopic(record, new IllegalStateException("boom"));

        assertThat(target.topic()).isEqualTo("user.updated.planner.DLT");
        assertThat(target.partition()).isEqualTo(-1);
    }
}
