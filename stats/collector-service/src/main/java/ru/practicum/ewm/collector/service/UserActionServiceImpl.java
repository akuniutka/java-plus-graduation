package ru.practicum.ewm.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.configuration.KafkaTopics;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserActionServiceImpl implements UserActionService {

    private final KafkaTopics kafkaTopics;
    private final KafkaTemplate<Void, UserActionAvro> kafkaTemplate;

    @Override
    public void send(final UserActionAvro userActionAvro) {
        kafkaTemplate.send(kafkaTopics.actions(), userActionAvro);
        log.info("Sent user action to Kafka: topic = {}, userId = {}, eventId = {}, actionType = {}",
                kafkaTopics.actions(), userActionAvro.getUserId(), userActionAvro.getEventId(),
                userActionAvro.getActionType());
        log.debug("Sent action = {}", userActionAvro);
    }
}
