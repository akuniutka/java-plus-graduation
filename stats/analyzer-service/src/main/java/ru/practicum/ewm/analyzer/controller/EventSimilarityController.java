package ru.practicum.ewm.analyzer.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.analyzer.mapper.EventSimilarityMapper;
import ru.practicum.ewm.analyzer.model.SimilarityScore;
import ru.practicum.ewm.analyzer.service.SimilarityScoreService;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventSimilarityController {

    private final SimilarityScoreService service;
    private final EventSimilarityMapper mapper;

    @KafkaListener(topics = "${kafka.topics.similarity}", groupId = "stats.analyzers.similarity",
    properties = "value.deserializer=ru.practicum.ewm.serialization.EventSimilarityDeserializer")
    public void update(final EventSimilarityAvro similarity) {
        log.info("Received event similarity info: eventA = {}, eventB = {}, score = {}",
                similarity.getEventA(), similarity.getEventB(), similarity.getScore());
        log.debug("Event similarity message = {}", similarity);
        final SimilarityScore score = mapper.mapToSimilarityScore(similarity);
        service.updateSimilarityScore(score);
        log.info("Processed event similarity info: eventA = {}, eventB = {}, score = {}",
                similarity.getEventA(), similarity.getEventB(), similarity.getScore());
    }
}
