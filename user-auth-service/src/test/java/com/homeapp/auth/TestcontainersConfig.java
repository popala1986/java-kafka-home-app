package com.homeapp.auth;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

// Testy nie korzystają już z Postgresa i Kafki z docker compose. Testcontainers stawia na czas testów
// własne, czyste kontenery (te same obrazy co w docker-compose.yml) i usuwa je po testach.
// @ServiceConnection: Spring Boot sam bierze z kontenera adres, port, login i hasło
// i podstawia je zamiast spring.datasource.* i spring.kafka.bootstrap-servers z application.yaml.
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfig {

    @Bean
    @ServiceConnection
    public PostgreSQLContainer postgres() {
        return new PostgreSQLContainer("postgres:17-alpine");
    }

    @Bean
    @ServiceConnection
    public KafkaContainer kafka() {
        return new KafkaContainer("apache/kafka:3.9.1");
    }
}
