package com.homeapp.notification.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

// Co robić, gdy obsługa eventu się wywali: spróbuj jeszcze 2 razy co sekundę,
// a jak dalej nie wychodzi, odłóż event do "martwego" topiku (DLT) i czytaj dalej.
@Configuration
public class KafkaErrorConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaErrorConfig.class);

    // Każdy serwis ma swoje DLT. Ten sam event może się wywalić tylko w jednym z nich.
    static final String DLT_SUFFIX = ".notification.DLT";

    // Spring owija prawdziwy błąd w kilka warstw ("Listener failed"). Do logu bierzemy ten najgłębszy, konkretny.
    private static String reason(Exception exception) {
        return NestedExceptionUtils.getMostSpecificCause(exception).getMessage();
    }

    // Nazwa DLT, np. chore.daily.digest -> chore.daily.digest.notification.DLT.
    // Partycja -1 = niech Kafka sama wybierze (DLT ma tylko jedną partycję).
    static TopicPartition deadLetterTopic(ConsumerRecord<?, ?> record, Exception exception) {
        String deadLetterTopic = record.topic() + DLT_SUFFIX;
        log.error("Odkładam event z {} (partycja {}, offset {}) do {}. Powód: {}",
            record.topic(), record.partition(), record.offset(), deadLetterTopic, reason(exception));
        return new TopicPartition(deadLetterTopic, -1);
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(ProducerFactory<Object, Object> producerFactory) {
        // Zepsutego JSON-a nie da się zamienić na obiekt, więc do DLT idą jego surowe bajty.
        // Bajty wysyła osobny KafkaTemplate, a poprawne obiekty (event, którego nie udało się obsłużyć) idą jako JSON.
        KafkaTemplate<Object, Object> bytesTemplate = new KafkaTemplate<>(producerFactory,
            Map.of(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class));
        KafkaTemplate<Object, Object> jsonTemplate = new KafkaTemplate<>(producerFactory);

        // LinkedHashMap pilnuje kolejności: najpierw sprawdzamy byte[], na końcu "cała reszta" (Object).
        Map<Class<?>, KafkaOperations<?, ?>> templates = new LinkedHashMap<>();
        templates.put(byte[].class, bytesTemplate);
        templates.put(Object.class, jsonTemplate);

        DeadLetterPublishingRecoverer recoverer =
            new DeadLetterPublishingRecoverer(templates, KafkaErrorConfig::deadLetterTopic);

        // FixedBackOff(1000, 2): czekaj 1 s i ponów maksymalnie 2 razy. Razem 3 próby.
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2L));
        errorHandler.setRetryListeners((record, exception, attempt) ->
            log.warn("Próba {} nieudana dla {} offset {}: {}",
                attempt, record.topic(), record.offset(), reason(exception)));
        return errorHandler;
    }

    // Auto-tworzenie topików jest w Kafce wyłączone, więc DLT zakładamy sami.
    @Bean
    public KafkaAdmin.NewTopics deadLetterTopics() {
        return new KafkaAdmin.NewTopics(
            deadLetter("user.registered"),
            deadLetter("user.updated"),
            deadLetter("chore.daily.digest"));
    }

    private static NewTopic deadLetter(String topic) {
        return TopicBuilder.name(topic + DLT_SUFFIX)
            .partitions(1)
            .replicas(1)
            .build();
    }
}
