package ru.practicum.ewm.aggregator.repository;

import java.util.Set;

public interface UserScoreRepository {

    void save(long userId, long eventId, float score);

    Set<Long> findEventIdByUserId(long userId);

    float getByUserIdAndEventId(long userId, long eventId);
}
