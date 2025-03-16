package ru.practicum.ewm.analyzer.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.analyzer.mapper.UserActionMapper;
import ru.practicum.ewm.analyzer.model.UserScore;
import ru.practicum.ewm.analyzer.service.UserScoreService;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserActionController {

    private final UserScoreService service;
    private final UserActionMapper mapper;

    @KafkaListener(topics = "${kafka.topics.actions}", groupId = "stats.analyzers.actions",
            properties = "value.deserializer=ru.practicum.ewm.serialization.UserActionDeserializer")
    public void aggregate(final UserActionAvro action) {
        log.info("Received user action: userId = {}, eventId = {}, actionType = {}",
                action.getUserId(), action.getEventId(), action.getActionType());
        log.debug("User action = {}", action);
        final UserScore score = mapper.mapToUserScore(action);
        service.updateUserScore(score);
        log.info("Processed user action: userId = {}, eventId = {}, actionType = {}",
                action.getUserId(), action.getEventId(), action.getActionType());
    }
}
