package com.homeapp.auth.correlation;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

class CorrelationIdProducerInterceptorTest {

    private final CorrelationIdProducerInterceptor interceptor = new CorrelationIdProducerInterceptor();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void copiesCorrelationIdFromMdcToKafkaHeader() {
        MDC.put("correlationId", "test-ola-1");
        ProducerRecord<Object, Object> record = new ProducerRecord<>("user.updated", "key", "value");

        ProducerRecord<Object, Object> sent = interceptor.onSend(record);

        byte[] header = sent.headers().lastHeader("correlationId").value();
        assertThat(new String(header, StandardCharsets.UTF_8)).isEqualTo("test-ola-1");
    }
}
