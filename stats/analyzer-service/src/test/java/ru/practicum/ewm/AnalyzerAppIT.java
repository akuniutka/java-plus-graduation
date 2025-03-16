package ru.practicum.ewm;

import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
class AnalyzerAppIT {

    @Container
    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.2"));

    @DynamicPropertySource
    static void overrideProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Test
    void whenLoadContext_ThenNoError() {
    }

    @TestConfiguration
    static class Config {

        @Bean
        NewTopic actionsTopic(@Value("${kafka.topics.actions}") final String topicName) {
            return TopicBuilder.name(topicName)
                    .build();
        }

        @Bean
        NewTopic similarityTopic(@Value("${kafka.topics.similarity}") final String topicName) {
            return TopicBuilder.name(topicName)
                    .build();
        }
    }
}