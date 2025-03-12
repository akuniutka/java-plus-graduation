package ru.practicum.ewm.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

@TestConfiguration
public class ClockConfig {

    private static final String TEST_CLOCK_TIME = "2000-01-01T00:00:01Z";
    private static final String TEST_TIMEZONE = "Z";

    @Bean
    @Primary
    public Clock clock() {
        return Clock.fixed(Instant.parse(TEST_CLOCK_TIME), ZoneId.of(TEST_TIMEZONE));
    }
}
