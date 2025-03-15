package ru.practicum.ewm.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.aggregator.model.Event;
import ru.practicum.ewm.aggregator.model.UserAction;
import ru.practicum.ewm.aggregator.repository.EventRepository;
import ru.practicum.ewm.aggregator.repository.MinScoreSumRepository;
import ru.practicum.ewm.configuration.KafkaTopics;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AggregatorServiceImpl implements AggregatorService {

    private static final Integer PARTITION_NOT_SET = null;

    private final KafkaTopics kafkaTopics;
    private final KafkaTemplate<Long, EventSimilarityAvro> kafkaTemplate;
    private final EventRepository eventRepository;
    private final MinScoreSumRepository minScoreSumRepository;

    @Override
    public void aggregate(final UserAction action) {
        final Event event = eventRepository.getOrCreate(action.eventId());
        final float oldScore = event.getUserScore(action.userId());
        final float newScore = action.actionType().getScore();
        if (newScore <= oldScore) {
            log.debug("Max user's score not changed: userId = {}, eventId = {}, max score = {}, new score = {}",
                    action.userId(), action.eventId(), oldScore, newScore);
            return;
        }
        event.setUserScore(action.userId(), newScore);
        final List<Event> eventsToProcess = eventRepository.findByUserId(action.userId()).stream()
                .filter(e -> e.getId() != event.getId())
                .toList();

        eventsToProcess.forEach(e -> {
            final float pairedUserScore = e.getUserScore(action.userId());
            if (pairedUserScore > oldScore) {
                float minScoreSumChange = Float.min(pairedUserScore, newScore) - oldScore;
                float minScoreSum = minScoreSumRepository.getByEventIds(e.getId(), event.getId()) + minScoreSumChange;
                minScoreSumRepository.save(e.getId(), event.getId(), minScoreSum);
            }
        });
        eventsToProcess.stream()
                .map(e -> calculateSimilarity(e, event, action.timestamp()))
                .forEach(this::sendToKafka);
    }

    private EventSimilarityAvro calculateSimilarity(final Event eventA, final Event eventB, final Instant timestamp) {
        final float minScoreSum = minScoreSumRepository.getByEventIds(eventA.getId(), eventB.getId());
        final float similarityScore = (float) (minScoreSum / Math.sqrt(eventA.getScore() * eventB.getScore()));
        return EventSimilarityAvro.newBuilder()
                .setEventA(Long.min(eventA.getId(), eventB.getId()))
                .setEventB(Long.max(eventA.getId(), eventB.getId()))
                .setScore(similarityScore)
                .setTimestamp(timestamp)
                .build();
    }

    private void sendToKafka(final EventSimilarityAvro similarity) {
        final ProducerRecord<Long, EventSimilarityAvro> record = new ProducerRecord<>(
                kafkaTopics.similarity(),
                PARTITION_NOT_SET,
                similarity.getTimestamp().toEpochMilli(),
                similarity.getEventA(),
                similarity
        );
        kafkaTemplate.send(record);
        log.info("Sent event similarity score to Kafka: eventA = {}, eventB = {}, score = {}",
                similarity.getEventA(), similarity.getEventB(), similarity.getScore());
        log.debug("Sent event similarity = {}", similarity);
    }
}
