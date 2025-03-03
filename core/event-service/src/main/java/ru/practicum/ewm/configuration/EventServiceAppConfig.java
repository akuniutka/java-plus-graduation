package ru.practicum.ewm.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class EventServiceAppConfig {

    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }
}
