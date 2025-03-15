package ru.practicum.ewm.aggregator.repository;

public interface MinScoreSumRepository {

    void save(long eventAId, long eventBId, float score);

    float getByEventIds(long eventAId, long eventBId);
}
