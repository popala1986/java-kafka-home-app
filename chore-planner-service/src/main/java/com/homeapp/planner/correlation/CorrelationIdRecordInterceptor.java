package com.homeapp.planner.correlation;

import java.nio.charset.StandardCharsets;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;
import org.springframework.kafka.listener.RecordInterceptor;
import org.springframework.stereotype.Component;

// Lustro filtra HTTP, tylko dla Kafki: zanim @KafkaListener dostanie event, wpisujemy jego correlationId do MDC.
// Spring Boot sam podpina ten bean pod wszystkie listenery w serwisie.
@Component
public class CorrelationIdRecordInterceptor implements RecordInterceptor<Object, Object> {

    @Override
    public ConsumerRecord<Object, Object> intercept(ConsumerRecord<Object, Object> record,
                                                    Consumer<Object, Object> consumer) {
        Header header = record.headers().lastHeader(CorrelationId.KAFKA_HEADER);
        // Stare eventy (sprzed tego kroku) nie mają nagłówka, więc dostają nowy ID.
        String fromHeader = header == null ? null : new String(header.value(), StandardCharsets.UTF_8);
        MDC.put(CorrelationId.MDC_KEY, CorrelationId.orNew(fromHeader));
        return record;
    }

    @Override
    public void afterRecord(ConsumerRecord<Object, Object> record, Consumer<Object, Object> consumer) {
        MDC.remove(CorrelationId.MDC_KEY);
    }
}
