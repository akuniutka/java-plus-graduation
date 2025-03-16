package ru.practicum.ewm.aggregator.service;

import ru.practicum.ewm.aggregator.model.UserScore;

public interface AggregatorService {

    void aggregate(UserScore score);
}
