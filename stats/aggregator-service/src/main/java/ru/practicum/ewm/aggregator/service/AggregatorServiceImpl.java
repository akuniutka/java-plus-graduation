package ru.practicum.ewm.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.aggregator.model.UserAction;
import ru.practicum.ewm.aggregator.repository.EventScoreRepository;
import ru.practicum.ewm.aggregator.repository.MinScoreSumRepository;
import ru.practicum.ewm.aggregator.repository.UserScoreRepository;
import ru.practicum.ewm.configuration.KafkaTopics;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AggregatorServiceImpl implements AggregatorService {

    private final KafkaTopics kafkaTopics;
    private final KafkaTemplate<Void, EventSimilarityAvro> kafkaTemplate;
    private final UserScoreRepository userScoreRepository;
    private final EventScoreRepository eventScoreRepository;
    private final MinScoreSumRepository minScoreSumRepository;

    @Override
    public void aggregate(final UserAction action) {
        final float maxScore = getMaxScore(action.userId(), action.eventId());
        final float newScore = action.actionType().getScore();
        if (newScore <= maxScore) {
            log.debug("Max user's score not changed: userId = {}, eventId = {}, max score = {}, new score = {}",
                    action.userId(), action.eventId(), maxScore, newScore);
            return;
        }
        updateMaxScore(action.userId(), action.eventId(), newScore);
        final float eventTotalScore = updateEventTotalScore(action.eventId(), newScore - maxScore);
        final Set<Long> eventsToProcess = userScoreRepository.findEventIdByUserId(action.userId()).stream()
                .filter(anotherEventId -> anotherEventId != action.eventId())
                .collect(Collectors.toSet());
        final CachedData cached = CachedData.from(action, maxScore, Math.sqrt(eventTotalScore));

        eventsToProcess.forEach(event -> updateMinScoreSum(event, cached));
        eventsToProcess.stream()
                .map(event -> calculateSimilarity(event, cached))
                .forEach(this::sendToKafka);
    }

    private float getMaxScore(final long userId, final long eventId) {
        return userScoreRepository.getByUserIdAndEventId(userId, eventId);
    }

    private void updateMaxScore(final long userId, final long eventId, final float newScore) {
        userScoreRepository.save(userId, eventId, newScore);
    }

    private float updateEventTotalScore(final long eventId, final float delta) {
        final float score = eventScoreRepository.getById(eventId) + delta;
        eventScoreRepository.save(eventId, score);
        return score;
    }

    private void updateMinScoreSum(final long event, final CachedData cache) {
        final float userScore = userScoreRepository.getByUserIdAndEventId(cache.user, event);
        if (userScore <= cache.oldScore) {
            return;
        }
        float delta = Float.min(userScore, cache.newScore) - cache.oldScore;
        final float minScoreSum = minScoreSumRepository.getByEventIds(event, cache.event) + delta;
        minScoreSumRepository.save(event, cache.event, minScoreSum);
    }

    private EventSimilarityAvro calculateSimilarity(final long event, final CachedData cache) {
        final float minScoreSum = minScoreSumRepository.getByEventIds(event, cache.event);
        final float eventTotalScore = eventScoreRepository.getById(event);
        final double rootedEventTotalScore = Math.sqrt(eventTotalScore);
        final float similarityScore = (float) (minScoreSum / rootedEventTotalScore / cache.rootedEventTotalScore);
        return EventSimilarityAvro.newBuilder()
                .setEventA(Long.min(event, cache.event))
                .setEventB(Long.max(event, cache.event))
                .setScore(similarityScore)
                .setTimestamp(cache.timestamp)
                .build();
    }

    private void sendToKafka(final EventSimilarityAvro similarity) {
        kafkaTemplate.send(kafkaTopics.similarity(), similarity);
        log.info("Sent event similarity score to Kafka: eventA = {}, eventB = {}, score = {}",
                similarity.getEventA(), similarity.getEventB(), similarity.getScore());
        log.debug("Sent event similarity = {}", similarity);
    }

    private record CachedData(

            long user,
            long event,
            float oldScore,
            float newScore,
            double rootedEventTotalScore,
            Instant timestamp
    ) {

        static CachedData from(final UserAction action, final float oldScore,
                final double rootedEventTotalScore
        ) {
            return new CachedData(
                    action.userId(),
                    action.eventId(),
                    oldScore,
                    action.actionType().getScore(),
                    rootedEventTotalScore,
                    action.timestamp()
            );
        }
    }
}
