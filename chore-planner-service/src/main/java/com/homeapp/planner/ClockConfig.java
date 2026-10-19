package com.homeapp.planner;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    // "Dzisiaj" liczymy w polskiej strefie czasowej, żeby wiek zmieniał się o północy w Polsce.
    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("Europe/Warsaw"));
    }
}
