package ru.practicum.ewm.aggregator.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.aggregator.mapper.UserActionMapper;
import ru.practicum.ewm.aggregator.model.UserAction;
import ru.practicum.ewm.aggregator.service.AggregatorService;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserActionController {

    private final AggregatorService service;
    private final UserActionMapper mapper;

    @KafkaListener(topics = "${kafka.topics.actions}")
    public void add(final UserActionAvro actionAvro) {
        log.info("Received user action: userId = {}, eventId = {}, actionType = {}",
                actionAvro.getUserId(), actionAvro.getEventId(), actionAvro.getActionType());
        log.debug("User action = {}", actionAvro);
        final UserAction action = mapper.mapToUserAction(actionAvro);
        service.aggregate(action);
        log.info("Processed user action: userId = {}, eventId = {}, actionType = {}",
                actionAvro.getUserId(), actionAvro.getEventId(), actionAvro.getActionType());
    }
}
