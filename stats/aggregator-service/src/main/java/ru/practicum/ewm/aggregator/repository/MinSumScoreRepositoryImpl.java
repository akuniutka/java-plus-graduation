package ru.practicum.ewm.aggregator.repository;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MinSumScoreRepositoryImpl implements MinScoreSumRepository {

    private static final Map<Long, Float> EMPTY_MAP = Map.of();

    private final Map<Long, Map<Long, Float>> scores = new HashMap<>();

    @Override
    public void save(final long eventAId, final long eventBId, final float score) {
        final long eventIdA_ = Long.min(eventAId, eventBId);
        final long eventIdB_ = Long.max(eventAId, eventBId);
        scores.computeIfAbsent(eventIdA_, key -> new HashMap<>()).put(eventIdB_, score);
    }

    @Override
    public float getByEventIds(final long eventAId, final long eventBId) {
        final long eventIdA_ = Long.min(eventAId, eventBId);
        final long eventIdB_ = Long.max(eventAId, eventBId);
        return scores.getOrDefault(eventIdA_, EMPTY_MAP).getOrDefault(eventIdB_, 0.0f);
    }
}
