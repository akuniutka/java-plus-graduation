package ru.practicum.ewm.aggregator.repository;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class UserScoreRepositoryImpl implements UserScoreRepository {

    private static final Map<Long, Float> EMPTY_MAP = Map.of();
    private final Map<Long, Map<Long, Float>> scores = new HashMap<>();

    @Override
    public void save(final long userId, final long eventId, final float score) {
        scores.computeIfAbsent(userId, key -> new HashMap<>()).put(eventId, score);
    }

    @Override
    public Set<Long> findEventIdByUserId(final long userId) {
        return new HashSet<>(scores.getOrDefault(userId, EMPTY_MAP).keySet());
    }

    @Override
    public float getByUserIdAndEventId(final long userId, final long eventId) {
        return scores.getOrDefault(userId, EMPTY_MAP).getOrDefault(eventId, 0.0f);
    }
}
