package ru.practicum.ewm.aggregator.repository;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class EventScoreRepositoryImpl implements EventScoreRepository {

    private final Map<Long, Float> scores = new HashMap<>();

    @Override
    public float getById(final long id) {
        return scores.getOrDefault(id, 0.0f);
    }

    @Override
    public void save(final long id, final float score) {
        scores.put(id, score);
    }
}
