package com.homeapp.notification.correlation;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdRecordInterceptorTest {

    private final CorrelationIdRecordInterceptor interceptor = new CorrelationIdRecordInterceptor();

    @Test
    void putsCorrelationIdFromKafkaHeaderIntoMdcAndClearsItAfterwards() {
        ConsumerRecord<Object, Object> record = new ConsumerRecord<>("chore.daily.digest", 0, 0L, "key", "value");
        record.headers().add("correlationId", "test-ola-1".getBytes(StandardCharsets.UTF_8));

        interceptor.intercept(record, null);
        assertThat(MDC.get("correlationId")).isEqualTo("test-ola-1");

        interceptor.afterRecord(record, null);
        assertThat(MDC.get("correlationId")).isNull();
    }
}
