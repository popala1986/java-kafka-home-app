package com.homeapp.planner.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

// Planner od teraz też wysyła eventy, więc sam zakłada swój topik (tak jak auth w Kroku 4).
@Configuration
public class KafkaTopicConfig {

    public static final String DAILY_DIGEST_TOPIC = "chore.daily.digest";

    @Bean
    public NewTopic dailyDigestTopic() {
        return TopicBuilder.name(DAILY_DIGEST_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }
}
