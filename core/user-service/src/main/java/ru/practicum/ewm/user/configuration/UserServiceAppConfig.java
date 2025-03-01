package ru.practicum.ewm.user.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class UserServiceAppConfig {

    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }
}
