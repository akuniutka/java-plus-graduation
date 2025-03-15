package ru.practicum.ewm.aggregator.repository;

public interface EventScoreRepository {

    float getById(long id);

    void save(long id, float score);
}
