package com.homeapp.auth.correlation;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.MDC;

// Kafka woła onSend przy każdym kafkaTemplate.send(...), jeszcze w wątku, który wysyła.
// Dopisujemy wtedy do wiadomości nagłówek correlationId z MDC. Podpięte w yaml: interceptor.classes.
public class CorrelationIdProducerInterceptor implements ProducerInterceptor<Object, Object> {

    @Override
    public ProducerRecord<Object, Object> onSend(ProducerRecord<Object, Object> record) {
        // Jeśli nagłówek już jest (np. event odkładany do DLT ma nagłówki oryginału), nie dublujemy go.
        if (record.headers().lastHeader(CorrelationId.KAFKA_HEADER) == null) {
            // Brak ID w MDC = wysyłka nie z zapytania HTTP, tylko np. z automatu @Scheduled. Wtedy nowy ID.
            String correlationId = CorrelationId.orNew(MDC.get(CorrelationId.MDC_KEY));
            record.headers().add(CorrelationId.KAFKA_HEADER, correlationId.getBytes(StandardCharsets.UTF_8));
        }
        return record;
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
    }

    @Override
    public void close() {
    }

    @Override
    public void configure(Map<String, ?> configs) {
    }
}
