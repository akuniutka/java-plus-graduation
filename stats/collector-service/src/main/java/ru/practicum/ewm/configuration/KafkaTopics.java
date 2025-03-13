package ru.practicum.ewm.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("kafka.topics")
public record KafkaTopics(String actions) {

}
